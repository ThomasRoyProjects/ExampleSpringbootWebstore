document.addEventListener("DOMContentLoaded", function () {
    const continentSelect = document.getElementById("continentSelect");
    const checkoutForm = document.getElementById("checkoutForm");
    const confirmButton = document.getElementById("confirmCheckout");
    const errorBox = document.getElementById("checkoutError");
    const csrfToken = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");
    let submitting = false;

    if (!continentSelect || !checkoutForm) {
        return;
    }

    function showError(message) {
        if (errorBox) {
            errorBox.textContent = message || "";
        }
    }

    function setPending(isPending) {
        if (!confirmButton) {
            return;
        }
        confirmButton.disabled = isPending;
        confirmButton.setAttribute("aria-busy", String(isPending));
        confirmButton.textContent = isPending ? "Confirming..." : "Confirm Order";
    }

    function updateCheckoutInfo() {
        showError("Updating totals...");
        fetch(`/checkout/update?continent=${encodeURIComponent(continentSelect.value)}`)
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => Promise.reject(new Error(data.error || "Invalid checkout region")));
                }
                return response.json();
            })
            .then(data => {
                document.getElementById("subtotal").textContent = `Subtotal: $${data.totalPrice}`;
                document.getElementById("taxRate").textContent = `Tax Rate: ${data.taxRate}%`;
                document.getElementById("shippingCost").textContent = `Shipping Cost: $${data.shippingCost}`;
                document.getElementById("taxAmount").textContent = `Tax Amount: $${data.taxAmount}`;
                document.getElementById("finalPrice").textContent = `Final Price: $${data.finalPrice}`;
                showError("");
            })
            .catch(error => showError(error.message));
    }

    continentSelect.addEventListener("change", updateCheckoutInfo);

    checkoutForm.addEventListener("submit", function (event) {
        event.preventDefault();
        if (submitting) {
            return;
        }
        if (!csrfToken || !csrfHeader) {
            showError("Security token is missing. Refresh and try again.");
            return;
        }

        submitting = true;
        setPending(true);
        showError("Confirming order...");

        fetch("/checkout/process", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({ continent: continentSelect.value })
        })
            .then(response => response.json().then(data => ({ ok: response.ok, data })))
            .then(result => {
                if (!result.ok) {
                    throw new Error(result.data.error || "Unable to confirm order");
                }
                window.location.href = result.data.receiptUrl;
            })
            .catch(error => {
                submitting = false;
                setPending(false);
                showError(error.message);
            });
    });

    updateCheckoutInfo();
});
