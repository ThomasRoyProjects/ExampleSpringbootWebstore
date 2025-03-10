document.addEventListener("DOMContentLoaded", function () {
    console.log("DOM fully loaded, initializing checkout and cart restoration.");
    
    const cartButton = document.getElementById("cartButton");
    const cartSidebar = document.getElementById("cartSidebar");
    const closeCart = document.getElementById("closeCart");

    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute("content");
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

    cartButton.addEventListener("click", function () {
        cartSidebar.classList.add("active");
        loadCart();
    });

    closeCart.addEventListener("click", function () {
        cartSidebar.classList.remove("active");
    });

    window.addEventListener("click", function (e) {
        if (!cartSidebar.contains(e.target) && !cartButton.contains(e.target) && !e.target.classList.contains("add-to-cart")) {
            cartSidebar.classList.remove("active");
        }
    });

    function saveCartToLocalStorage(cartHtml) {
        if (cartHtml.trim() !== "") {  
            localStorage.setItem("cartContents", cartHtml);
            console.log("Cart saved to localStorage.");
        } else {
            console.warn("Cart is empty; not saving to localStorage.");
        }
    }

    function loadCartFromLocalStorage() {
        const savedCart = localStorage.getItem("cartContents");
        if (savedCart) {
            console.log("Loaded cart from localStorage.");
        } else {
            console.warn("No saved cart found in localStorage.");
        }
        return savedCart || "";
    }

    function restoreCart() {
        console.log("Restoring cart from localStorage...");
        const savedCart = loadCartFromLocalStorage();
        if (savedCart) {
            const cartItemsContainer = document.querySelector(".cart-items");
            if (cartItemsContainer) {
                cartItemsContainer.innerHTML = savedCart;
                console.log("Cart UI updated from localStorage.");
                attachCartEventListeners();
            } else {
                console.error("Cart items container not found during restore.");
            }
        }
    }

    function addToCart(productId) {
        console.log(`Adding product with ID ${productId} to the cart`);
        fetch("/cart/add", {
            method: "POST",
            headers: { 
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({ productId: productId, quantity: 1 })
        }).then(response => response.text())
          .then(html => {
            console.log("Response from server:", html);
            updateCartUI(html);
        }).catch(error => {
            console.error("Error adding to cart:", error);
        });
    }

    function removeFromCart(productId) {
        console.log(`Removing product with ID ${productId} from the cart`);
        fetch(`/cart/remove/${productId}`, {
            method: "POST",
            headers: { 
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            }
        }).then(response => response.text())
          .then(html => {
            console.log("Response from server:", html);
            updateCartUI(html);
        }).catch(error => {
            console.error("Error removing from cart:", error);
        });
    }

    function clearCart() {
        console.log("Clearing cart...");
        fetch("/cart/clear", {
            method: "POST",
            headers: { 
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            }
        }).then(response => response.text())
          .then(html => {
            console.log("Response from server:", html);
            updateCartUI(html);
        }).catch(error => {
            console.error("Error clearing cart:", error);
        });
    }

    function loadCart() {
        console.log("Loading cart...");
        fetch("/cart/items")
            .then(response => response.text())
            .then(html => {
                console.log("Response from server:", html);
                if (html.trim() !== "") {
                    updateCartUI(html);
                } else {
                    restoreCart(); 
                }
            })
            .catch(error => console.error("Error loading cart:", error));
    }

    function updateCartUI(html) {
        const cartItemsContainer = document.querySelector(".cart-items");
        const cartTotalContainer = document.querySelector(".cart-footer h3 span");

        if (cartItemsContainer) {
            if (html.trim() !== "") {  
                cartItemsContainer.innerHTML = html;
                forceDOMUpdate(cartItemsContainer);
                attachCartEventListeners();

                const totalPrice = document.querySelector(".cart-footer h3 span")?.textContent.trim();
                if (cartTotalContainer) {
                    cartTotalContainer.textContent = totalPrice || "$0.00";  
                }

                saveCartToLocalStorage(html);
            }
        } else {
            console.error("Cart items container not found!");
        }
    }

    function attachCartEventListeners() {
        console.log("Attaching event listeners to remove buttons.");
        document.querySelectorAll(".remove-item").forEach(button => {
            button.addEventListener("click", function () {
                let productId = this.getAttribute("data-id");
                console.log("Removing product with ID:", productId);
                removeFromCart(productId);
            });
        });

        document.getElementById("clearCart")?.addEventListener("click", function () {
            console.log("Clearing cart...");
            clearCart();
        });
    }

    function forceDOMUpdate(cartItemsContainer) {
        cartItemsContainer.style.display = 'none';
        cartItemsContainer.offsetHeight; 
        cartItemsContainer.style.display = '';
    }

    document.body.addEventListener("click", function (event) {
        if (event.target && event.target.classList.contains("add-to-cart")) {
            let productId = event.target.getAttribute("data-id");
            console.log("Adding product with ID:", productId);
            addToCart(productId);
        }
    });

    restoreCart();
});
