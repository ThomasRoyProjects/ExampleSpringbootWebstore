document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);

    const continent = urlParams.get("continent") || "N/A";
    const taxRate = parseFloat(urlParams.get("taxRate")) || 0.0;
    const shippingCost = parseFloat(urlParams.get("shippingCost")) || 0.0;
    const taxAmount = parseFloat(urlParams.get("taxAmount")) || 0.0;
    const finalPrice = parseFloat(urlParams.get("finalPrice")) || 0.0;

    // display the extracted values on the page
    document.getElementById("continentDisplay").innerText = `Continent: ${continent}`;
    document.getElementById("taxRateDisplay").innerText = `Tax Rate: ${taxRate}%`;
    document.getElementById("shippingCostDisplay").innerText = `Shipping Cost: $${shippingCost.toFixed(2)}`;
    document.getElementById("taxAmountDisplay").innerText = `Tax Amount: $${taxAmount.toFixed(2)}`;
    document.getElementById("finalPriceDisplay").innerText = `Final Price: $${finalPrice.toFixed(2)}`;

    // get CSRF token from meta tags
    const csrfToken = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    if (!csrfToken || !csrfHeader || csrfHeader.trim() === "" || csrfToken.trim() === "") {
        console.error("CSRF token or header name is missing/invalid!");
        return;
    }

    // handle form submission
    const paymentForm = document.getElementById("paymentForm");
    if (paymentForm) {
        paymentForm.addEventListener("submit", function (event) {
            event.preventDefault();

            const cardNumber = document.getElementById("cardNumber").value.trim();
            if (!cardNumber) {
                alert("Please enter a card number.");
                return;
            }

            const headers = new Headers();
            headers.append("Content-Type", "application/json");
            headers.append(csrfHeader, csrfToken);

            const requestBody = {
                cartItems: JSON.parse(localStorage.getItem("cart")) || [],
                continent: continent,
                taxRate: taxRate,
                shippingCost: shippingCost,
                taxAmount: taxAmount,
                finalPrice: finalPrice,
                cardNumber: cardNumber 
            };

            if (!Array.isArray(requestBody.cartItems)) {
                requestBody.cartItems = [];
            }

            console.log("Request Body:", requestBody);
            fetch("/checkout/process", {
                method: "POST",
                headers: headers,
                body: JSON.stringify(requestBody)
            }).then(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new Error("Order processing failed. Please try again.");
                }
            }).then(data => {
                if (data.orderNumber) {
                    const orderNumber = data.orderNumber;

                    const redirectUrl = `/thank-you?orderNumber=${encodeURIComponent(orderNumber)}
                        &continent=${encodeURIComponent(continent)}
                        &taxRate=${encodeURIComponent(taxRate.toString())}
                        &shippingCost=${encodeURIComponent(shippingCost.toString())}
                        &taxAmount=${encodeURIComponent(taxAmount.toString())}
                        &finalPrice=${encodeURIComponent(finalPrice.toString())}`;

                    window.location.href = redirectUrl;
                } else {
                    throw new Error("Order number missing from response");
                }
            }).catch(error => {
                console.error("Error:", error);
                alert("An error occurred: " + error.message);
            });
        });
    } else {
        console.error("Payment form not found.");
    }
});
