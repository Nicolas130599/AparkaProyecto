// Comportamiento de la pantalla "Mi perfil": vista previa de la foto,
// confirmación de contraseña al cambiar el correo y mostrar/ocultar contraseñas.
document.addEventListener("DOMContentLoaded", () => {
    const MAX_BYTES = 2 * 1024 * 1024;
    const TIPOS = ["image/jpeg", "image/png"];

    // ---------- Vista previa de la foto ----------
    const input = document.getElementById("inputFoto");
    const acciones = document.getElementById("accionesFoto");
    const btnElegir = document.getElementById("btnElegirFoto");
    const btnCancelar = document.getElementById("btnCancelarFoto");
    const errorFoto = document.getElementById("errorFoto");
    let avatar = document.getElementById("avatarGrande");
    let avatarOriginal = avatar ? avatar.cloneNode(true) : null;
    let urlPrevia = null;

    function restaurarAvatar() {
        if (urlPrevia) { URL.revokeObjectURL(urlPrevia); urlPrevia = null; }
        if (avatarOriginal && avatar) {
            const copia = avatarOriginal.cloneNode(true);
            avatar.replaceWith(copia);
            avatar = copia;
        }
        input.value = "";
        acciones.hidden = true;
        btnElegir.hidden = false;
    }

    function mostrarErrorFoto(texto) {
        errorFoto.textContent = texto;
        errorFoto.hidden = !texto;
    }

    if (input) {
        input.addEventListener("change", () => {
            mostrarErrorFoto("");
            const archivo = input.files[0];
            if (!archivo) return;

            if (!TIPOS.includes(archivo.type)) {
                mostrarErrorFoto("Formato no permitido. Usa una imagen JPG o PNG.");
                restaurarAvatar();
                return;
            }
            if (archivo.size > MAX_BYTES) {
                mostrarErrorFoto("La imagen supera el máximo de 2 MB.");
                restaurarAvatar();
                return;
            }

            if (urlPrevia) URL.revokeObjectURL(urlPrevia);
            urlPrevia = URL.createObjectURL(archivo);
            const img = document.createElement("img");
            img.id = "avatarGrande";
            img.className = "perfil-avatar";
            img.alt = "Vista previa de la nueva foto";
            img.src = urlPrevia;
            avatar.replaceWith(img);
            avatar = img;

            acciones.hidden = false;
            btnElegir.hidden = true;
        });
        btnCancelar.addEventListener("click", () => { mostrarErrorFoto(""); restaurarAvatar(); });
    }

    // ---------- Cambiar correo => pedir contraseña actual ----------
    const email = document.getElementById("email");
    const bloque = document.getElementById("bloqueConfirmarCorreo");
    const passDatos = document.getElementById("passwordActualDatos");
    if (email && bloque) {
        email.addEventListener("input", () => {
            const cambio = email.value.trim().toLowerCase() !== (email.dataset.original || "").trim().toLowerCase();
            bloque.hidden = !cambio;
            passDatos.required = cambio;
            if (!cambio) passDatos.value = "";
        });
    }

    // ---------- Mostrar / ocultar contraseñas ----------
    document.querySelectorAll("[data-toggle]").forEach((btn) => {
        btn.addEventListener("click", () => {
            const campo = document.getElementById(btn.dataset.toggle);
            const oculto = campo.type === "password";
            campo.type = oculto ? "text" : "password";
            btn.textContent = oculto ? "🙈" : "👁️";
        });
    });

    // ---------- Coincidencia de la nueva contraseña ----------
    const formPassword = document.getElementById("formPassword");
    if (formPassword) {
        formPassword.addEventListener("submit", (e) => {
            const coincide = document.getElementById("passwordNueva").value ===
                             document.getElementById("passwordConfirma").value;
            document.getElementById("errorPassword").hidden = coincide;
            if (!coincide) e.preventDefault();
        });
    }
});
