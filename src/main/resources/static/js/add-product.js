document.querySelector("form").addEventListener("submit", function(event) {
    event.preventDefault(); 

    let form = this;
    let fileInput = document.getElementById("image");
    let file = fileInput.files[0];

    if (file) {
        let formData = new FormData();
        formData.append("image", file);

     
        fetch("/admin/uploadImage", {
            method: "POST",
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (data.imageUrl) {
                document.getElementById("imageUrl").value = data.imageUrl;
                form.submit();
            } else {
                alert("Error uploading image: " + data.error);
            }
        })
        .catch(error => {
            alert("Failed to upload image.");
            console.error(error);
        });
    } else {
        form.submit();
    }
});
