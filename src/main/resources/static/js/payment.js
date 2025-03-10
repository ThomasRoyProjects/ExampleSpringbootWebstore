document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);

    const continent = urlParams.get("continent");
    const taxRate = urlParams.get("taxRate");
    const shippingCost = urlParams.get("shippingCost");
    const taxAmount = urlParams.get("taxAmount");
    const finalPrice = urlParams.get("finalPrice");

    document.getElementById("continentDisplay").innerText = `Continent: ${continent}`;
    document.getElementById("taxRateDisplay").innerText = `Tax Rate: ${taxRate}%`;
    document.getElementById("shippingCostDisplay").innerText = `Shipping Cost: $${shippingCost}`;
    document.getElementById("taxAmountDisplay").innerText = `Tax Amount: $${taxAmount}`;
    document.getElementById("finalPriceDisplay").innerText = `Final Price: $${finalPrice}`;

    document.getElementById("paymentForm").addEventListener("submit", function (event) {
        event.preventDefault(); 

        alert("Thank you for your order!");
        window.location.href = "/products"; 
    });
});
