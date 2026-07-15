document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".edit-button[aria-controls]").forEach((button) => {
        const targetId = button.getAttribute("aria-controls");
        const target = document.getElementById(targetId);
        const expanded = target ? !target.hidden : false;
        button.setAttribute("aria-expanded", String(expanded));

        button.addEventListener("click", () => {
            if (!target) {
                return;
            }
            const willOpen = target.hidden;
            target.hidden = !willOpen;
            button.setAttribute("aria-expanded", String(willOpen));
            if (willOpen) {
                const firstField = target.querySelector("input:not([type='hidden']), textarea, select, button");
                firstField?.focus();
            }
        });
    });

    function csrfHeaders(form) {
        const token = form.querySelector('input[name="_csrf"]')?.value || document.querySelector('meta[name="_csrf"]')?.content;
        const header = document.querySelector('meta[name="_csrf_header"]')?.content || "X-CSRF-TOKEN";
        return token ? { [header]: token } : {};
    }

    function setResult(form, message, state) {
        const localResult = form.querySelector('[role="status"]');
        const pageResult = document.getElementById("upload-result");
        const target = localResult || pageResult;
        if (target) {
            target.textContent = message;
            target.className = state ? `form-status ${state}` : "form-status";
        }
    }

    document.querySelectorAll("form.product-form").forEach((form) => {
        form.addEventListener("submit", function (event) {
            const submitButton = form.querySelector('button[type="submit"]');
            if (submitButton?.disabled) {
                event.preventDefault();
                return;
            }

            const fileInput = form.querySelector(".product-image-input");
            const imageUrlInput = form.querySelector('input[name="imageUrl"]');
            const file = fileInput?.files?.[0];

            if (!file) {
                if (submitButton) {
                    submitButton.disabled = true;
                    submitButton.textContent = "Saving...";
                }
                setResult(form, "Saving product...", "pending");
                return;
            }

            event.preventDefault();
            if (submitButton) {
                submitButton.disabled = true;
                submitButton.textContent = "Uploading...";
            }
            setResult(form, "Uploading image...", "pending");

            const formData = new FormData();
            formData.append("image", file);

            fetch("/admin/products/images", {
                method: "POST",
                headers: csrfHeaders(form),
                body: formData
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error("Image upload failed.");
                    }
                    return response.json();
                })
                .then(data => {
                    if (!data.imageUrl) {
                        throw new Error("Image upload failed.");
                    }
                    imageUrlInput.value = data.imageUrl;
                    setResult(form, "Image uploaded. Saving product...", "success");
                    form.submit();
                })
                .catch(error => {
                    setResult(form, `${error.message} Use a JPEG or PNG under the size and dimension limits.`, "error");
                    if (submitButton) {
                        submitButton.disabled = false;
                        submitButton.textContent = submitButton.classList.contains("add-products-button") ? "Add Product" : "Save Changes";
                    }
                });
        });
    });
});
