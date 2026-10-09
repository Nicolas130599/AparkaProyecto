<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Aparka | Crear cuenta</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
    <style>
        .input-grupo-password {
            position: relative;
            display: flex;
            align-items: center;
        }
        .input-grupo-password input {
            width: 100%;
            padding-right: 40px;
        }
        .btn-toggle-password {
            position: absolute;
            right: 10px;
            background: none;
            border: none;
            cursor: pointer;
            font-size: 1.1rem;
        }
        /* Estilo para el mensaje de error de coincidencia de contraseñas */
        .error-password {
            color: #ff4d4d;
            font-size: 0.85rem;
            margin-top: 4px;
            display: none;
        }
    </style>
</head>
<body class="pagina-auth">
    <div class="auth-card animar-entrada">
        <h1 class="logo">🅿️ Aparka</h1>
        <p class="subtitulo">Crea tu cuenta de usuario</p>

        <c:if test="${not empty error}">
            <div class="alerta alerta-error">${error}</div>
        </c:if>

        <!-- Se añadió un ID y un evento 'onsubmit' para validar antes de enviar -->
        <form action="${pageContext.request.contextPath}/registro" method="post" class="form-aparka" id="formRegistro" onsubmit="return validarContrasenas(event)">
            <label for="nombres">Nombre completo</label>
            <input type="text" id="nombres" name="nombres" required autofocus>

            <label for="documento">Documento de identidad</label>
            <input type="text" id="documento" name="documento" maxlength="20" required>

            <label for="telefono">Teléfono</label>
            <input type="text" id="telefono" name="telefono" maxlength="20" required>

            <label for="username">Usuario</label>
            <input type="text" id="username" name="username" required>

            <label for="email">Correo electrónico</label>
            <input type="email" id="email" name="email" required>

            <label for="contrasena">Contraseña</label>
            <div class="input-grupo-password">
                <input type="password" id="contrasena" name="contrasena" required minlength="6">
                <button type="button" class="btn-toggle-password" onclick="togglePassword('contrasena', this)">👁️</button>
            </div>

            <label for="confirmaContrasena">Repita contraseña</label>
            <div class="input-grupo-password">
                <input type="password" id="confirmaContrasena" name="confirmaContrasena" required minlength="6">
                <button type="button" class="btn-toggle-password" onclick="togglePassword('confirmaContrasena', this)">👁️</button>
            </div>
            <!-- Mensaje dinámico en rojo -->
            <div id="mensajeErrorPassword" class="error-password">Las contraseñas no coinciden</div>

            <button type="submit" class="btn btn-primario" style="margin-top: 15px;">Registrarme</button>
        </form>

        <p class="enlace-secundario">
            ¿Ya tienes cuenta?
            <a href="${pageContext.request.contextPath}/login">Inicia sesión</a>
        </p>
    </div>

    <!-- Scripts para el ojito y la validación de contraseñas iguales -->
    <script>
        function togglePassword(idInput, btn) {
            const input = document.getElementById(idInput);
            if (input.type === "password") {
                input.type = "text";
                btn.textContent = "🙈";
            } else {
                input.type = "password";
                btn.textContent = "👁️";
            }
        }

        function validarContrasenas(event) {
            const pass = document.getElementById('contrasena').value;
            const confirma = document.getElementById('confirmaContrasena').value;
            const mensajeError = document.getElementById('mensajeErrorPassword');

            if (pass !== confirma) {
                // Previene que el formulario se envíe
                event.preventDefault();
                // Muestra el texto en rojo
                mensajeError.style.display = 'block';
                return false;
            }

            mensajeError.style.display = 'none';
            return true;
        }
    </script>
    <script src="${pageContext.request.contextPath}/js/animaciones.js"></script>
</body>
</html>
