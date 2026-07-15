document.addEventListener("DOMContentLoaded", function () {
    const cartButton = document.getElementById("cartButton");
    const cartSidebar = document.getElementById("cartSidebar");
    const closeCart = document.getElementById("closeCart");
    const getCartItemsContainer = () => document.getElementById("cartItems");
    const cartStatus = document.getElementById("cartStatus");
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");
    let returnFocusTo = null;
    const pendingProducts = new Set();

    function csrfHeaders() {
        return {
            "Content-Type": "application/json",
            [csrfHeader]: csrfToken
        };
    }

    function announce(message, type) {
        if (!cartStatus) {
            return;
        }
        cartStatus.textContent = message || "";
        cartStatus.className = type ? `cart-status ${type}` : "cart-status";
    }

    function setDrawer(open) {
        if (!cartButton || !cartSidebar) {
            return;
        }
        cartSidebar.classList.toggle("active", open);
        cartSidebar.setAttribute("aria-hidden", String(!open));
        cartButton.setAttribute("aria-expanded", String(open));
        document.body.classList.toggle("cart-open", open);

        if (open) {
            returnFocusTo = document.activeElement instanceof HTMLElement ? document.activeElement : cartButton;
            loadCart();
            window.requestAnimationFrame(() => closeCart?.focus());
        } else if (returnFocusTo && typeof returnFocusTo.focus === "function") {
            returnFocusTo.focus();
            returnFocusTo = null;
        }
    }

    function hasCsrfToken() {
        if (!csrfHeader || !csrfToken) {
            announce("Security token is missing. Refresh and try again.", "error");
            return false;
        }
        return true;
    }

    function setButtonPending(button, pending, text) {
        if (!button) {
            return;
        }
        if (pending) {
            button.dataset.originalText = button.textContent;
            button.disabled = true;
            button.setAttribute("aria-busy", "true");
            button.textContent = text;
        } else {
            button.disabled = button.matches(".add-to-cart") && button.dataset.outOfStock === "true";
            button.removeAttribute("aria-busy");
            if (button.dataset.originalText) {
                button.textContent = button.dataset.originalText;
                delete button.dataset.originalText;
            }
        }
    }

    function handleCartResponse(response) {
        return response.text().then(html => {
            if (!response.ok) {
                throw new Error(html.replace(/<[^>]*>/g, "").trim() || "Cart update failed.");
            }
            return html;
        });
    }

    function updateCartUI(html, message) {
        const cartItemsContainer = getCartItemsContainer();
        if (cartItemsContainer && html) {
            const template = document.createElement("template");
            template.innerHTML = html.trim();
            const renderedFragment = template.content.querySelector("#cartItems");
            const renderedTotal = (renderedFragment || template.content).querySelector("#cartTotal")?.textContent.replace("Total: ", "");
            if (renderedFragment) {
                cartItemsContainer.replaceWith(renderedFragment);
            } else {
                cartItemsContainer.innerHTML = html;
            }
            const cartTotalContainer = document.getElementById("cartFooterTotal");
            if (cartTotalContainer && renderedTotal) {
                cartTotalContainer.textContent = renderedTotal;
            }
        }
        if (message) {
            announce(message, "success");
        }
    }

    function loadCart() {
        if (!getCartItemsContainer()) {
            return Promise.resolve();
        }
        announce("Loading cart...", "pending");
        return fetch("/cart/items", { headers: { "X-Requested-With": "XMLHttpRequest" } })
            .then(handleCartResponse)
            .then(html => updateCartUI(html, "Cart updated."))
            .catch(error => announce(error.message, "error"));
    }

    function mutateCart(url, options, successMessage) {
        if (!hasCsrfToken()) {
            return Promise.resolve();
        }
        announce("Updating cart...", "pending");
        return fetch(url, options)
            .then(handleCartResponse)
            .then(html => updateCartUI(html, successMessage))
            .catch(error => announce(error.message, "error"));
    }

    function addToCart(button) {
        const productId = button.getAttribute("data-id");
        if (!productId || pendingProducts.has(productId)) {
            return;
        }
        pendingProducts.add(productId);
        setButtonPending(button, true, "Adding...");
        mutateCart("/cart/add", {
            method: "POST",
            headers: csrfHeaders(),
            body: JSON.stringify({ productId: Number(productId), quantity: 1 })
        }, "Added to cart.").finally(() => {
            pendingProducts.delete(productId);
            setButtonPending(button, false);
            setDrawer(true);
        });
    }

    function removeFromCart(button) {
        const productId = button.getAttribute("data-id");
        if (!productId) {
            return;
        }
        setButtonPending(button, true, "Removing...");
        mutateCart(`/cart/remove/${encodeURIComponent(productId)}`, {
            method: "POST",
            headers: csrfHeaders()
        }, "Removed from cart.").finally(() => setButtonPending(button, false));
    }

    function clearCart(button) {
        setButtonPending(button, true, "Clearing...");
        mutateCart("/cart/clear", {
            method: "POST",
            headers: csrfHeaders()
        }, "Cart cleared.").finally(() => setButtonPending(button, false));
    }

    cartButton?.addEventListener("click", function () {
        setDrawer(!cartSidebar?.classList.contains("active"));
    });

    closeCart?.addEventListener("click", function () {
        setDrawer(false);
    });

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape" && cartSidebar?.classList.contains("active")) {
            setDrawer(false);
        }
    });

    document.addEventListener("click", function (event) {
        const target = event.target;
        if (!(target instanceof Element)) {
            return;
        }
        const addButton = target.closest(".add-to-cart");
        if (addButton) {
            addToCart(addButton);
            return;
        }
        const removeButton = target.closest(".remove-item");
        if (removeButton) {
            removeFromCart(removeButton);
            return;
        }
        const clearButton = target.closest("#clearCart");
        if (clearButton) {
            clearCart(clearButton);
            return;
        }
        if (cartSidebar?.classList.contains("active") && cartButton && !cartSidebar.contains(target) && !cartButton.contains(target)) {
            setDrawer(false);
        }
    });

    if (cartSidebar) {
        cartSidebar.setAttribute("aria-hidden", "true");
    }
    if (cartButton) {
        cartButton.setAttribute("aria-expanded", "false");
    }
});
