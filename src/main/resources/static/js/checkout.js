document.addEventListener("DOMContentLoaded", function () {
    const continentSelect = document.getElementById("continentSelect");
    const proceedToPaymentBtn = document.getElementById("proceedToPayment");

    let taxRate, shippingCost, taxAmount, finalPrice; // declare variables to store numeric values

    continentSelect.addEventListener("change", function () {
        updateCheckoutInfo();
    });

    function updateCheckoutInfo() {
        const continent = continentSelect.value;

        fetch(`/checkout/update?continent=${encodeURIComponent(continent)}`)
            .then(response => response.json())
            .then(data => {
                if (data) {
                    // store numeric values directly in variables
                    taxRate = data.taxRate;
                    shippingCost = data.shippingCost;
                    taxAmount = data.taxAmount;
                    finalPrice = data.finalPrice;

                    // display values on the page
                    document.getElementById("taxRate").innerText = `Tax Rate: ${taxRate}%`;
                    document.getElementById("shippingCost").innerText = `Shipping Cost: $${shippingCost.toFixed(2)}`;
                    document.getElementById("taxAmount").innerText = `Tax Amount: $${taxAmount.toFixed(2)}`;
                    document.getElementById("finalPrice").innerText = `Final Price: $${finalPrice.toFixed(2)}`;
                } else {
                    console.error("Data is missing or incorrect");
                }
            })
            .catch(error => console.error("Error:", error));
    }

    proceedToPaymentBtn.addEventListener("click", function (event) {
        event.preventDefault();

        const continent = continentSelect.value;

        // check if any of the values are undefined or null
        if (taxRate == null || shippingCost == null || taxAmount == null || finalPrice == null) {
            console.error("One or more values are missing.");
            return;
        }

        // send the correct data to the backend
        fetch("/checkout/process", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ continent })
        })
            .then(response => response.json()) // JSON response
            .then(data => {
                if (!data.orderNumber) {
                    throw new Error("Order number missing from response");
                }

                // redirect with all parameters
                window.location.href = `/checkout/payment?orderNumber=${encodeURIComponent(data.orderNumber)}&continent=${encodeURIComponent(continent)}&taxRate=${taxRate}&shippingCost=${shippingCost}&taxAmount=${taxAmount}&finalPrice=${finalPrice}`;
            })
            .catch(error => console.error("Error:", error));
    });

    updateCheckoutInfo();
});
