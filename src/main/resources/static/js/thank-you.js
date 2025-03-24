document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);

    // extract params
    const orderNumber = urlParams.get("orderNumber") || "N/A";
    const continent = urlParams.get("continent") || "N/A";
    const taxRate = parseFloat(urlParams.get("taxRate")) || 0.0;
    const shippingCost = parseFloat(urlParams.get("shippingCost")) || 0.0;
    const taxAmount = parseFloat(urlParams.get("taxAmount")) || 0.0;
    const finalPrice = parseFloat(urlParams.get("finalPrice")) || 0.0;

    // populate the fields with the URL params
    document.getElementById("orderNumber").innerText = orderNumber;
    document.getElementById("continent").innerText = continent;
    document.getElementById("taxRate").innerText = taxRate.toFixed(2);
    document.getElementById("shippingCost").innerText = shippingCost.toFixed(2);
    document.getElementById("taxAmount").innerText = taxAmount.toFixed(2);
    document.getElementById("finalPrice").innerText = finalPrice.toFixed(2);
});
