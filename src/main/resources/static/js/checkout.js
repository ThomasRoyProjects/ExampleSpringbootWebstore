document.addEventListener("DOMContentLoaded", function () {
    const continentSelect = document.getElementById("continentSelect");
    const proceedToPaymentBtn = document.getElementById("proceedToPayment");

    continentSelect.addEventListener("change", function () {
        updateCheckoutInfo();
        updateProceedToPaymentLink();
    });

    function updateCheckoutInfo() {
        const continent = continentSelect.value;

        fetch(`/checkout/update?continent=${continent}`)
            .then(response => response.json())
            .then(data => {
                if (data) {
                    document.getElementById("taxRate").innerText = `Tax Rate: ${data.taxRate}%`;
                    document.getElementById("shippingCost").innerText = `Shipping Cost: $${data.shippingCost.toFixed(2)}`;
                    document.getElementById("taxAmount").innerText = `Tax Amount: $${data.taxAmount.toFixed(2)}`;
                    document.getElementById("finalPrice").innerText = `Final Price: $${data.finalPrice.toFixed(2)}`;
                } else {
                    console.error("Data is missing or incorrect");
                }
            })
            .catch(error => console.error("Error:", error));
    }

    function updateProceedToPaymentLink() {
        const continent = continentSelect.value;
        const taxRate = document.getElementById("taxRate").innerText.split(": ")[1].replace("%", "");
        const shippingCost = document.getElementById("shippingCost").innerText.split("$")[1];
        const taxAmount = document.getElementById("taxAmount").innerText.split("$")[1];
        const finalPrice = document.getElementById("finalPrice").innerText.split("$")[1];

        const paymentUrl = `/payment?continent=${encodeURIComponent(continent)}&taxRate=${taxRate}&shippingCost=${shippingCost}&taxAmount=${taxAmount}&finalPrice=${finalPrice}`;

        proceedToPaymentBtn.setAttribute("href", paymentUrl);
    }

    updateCheckoutInfo();
    updateProceedToPaymentLink();
});
