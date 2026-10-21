<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Aparka | Iniciar sesión</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body class="pagina-auth">
    <div class="auth-card animar-entrada">
        <h1 class="logo">🅿️ Aparka</h1>
        <p class="subtitulo">Gestión inteligente de estacionamientos</p>

        <c:if test="${not empty error}">
            <div class="alerta alerta-error">${error}</div>
        </c:if>
        <c:if test="${param.registrado == '1'}">
            <div class="alerta alerta-exito">Cuenta creada. Ahora inicia sesión.</div>
        </c:if>
        <c:if test="${param.passwordActualizada == '1'}">
            <div class="alerta alerta-exito">Contraseña actualizada. Ya puedes iniciar sesión.</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/login" method="post" class="form-aparka">
            <label for="username">Usuario</label>
            <input type="text" id="username" name="username" required autofocus>


<label for="contrasena">Contraseña</label>

<div class="login-password-wrapper">
    <input type="password"
           id="contrasena"
           name="contrasena"
           autocomplete="current-password"
           required>

    <button type="button"
            class="login-password-toggle"
            id="toggleContrasena"
            aria-label="Mostrar contraseña"
            aria-pressed="false"
            aria-controls="contrasena"
            title="Mostrar contraseña">
        <svg viewBox="0 0 24 24"
             width="20"
             height="20"
             fill="none"
             stroke="currentColor"
             stroke-width="1.8"
             stroke-linecap="round"
             stroke-linejoin="round"
             aria-hidden="true">
            <path d="M2 12s3.6-6 10-6 10 6 10 6-3.6 6-10 6S2 12 2 12z"/>
            <circle cx="12" cy="12" r="3"/>
        </svg>
    </button>
</div>


            <button type="submit" class="btn btn-primario">Ingresar</button>
        </form>

        <p class="enlace-secundario">
            ¿No tienes cuenta?
            <a href="${pageContext.request.contextPath}/registro">Regístrate aquí</a>
        </p>
        <p class="enlace-secundario">
            <a href="${pageContext.request.contextPath}/recuperar">¿Olvidaste tu contraseña?</a>
        </p>
    </div>
    <script src="${pageContext.request.contextPath}/js/animaciones.js"></script>

<script>
document.addEventListener("DOMContentLoaded", function () {
    const campo = document.getElementById("contrasena");
    const boton = document.getElementById("toggleContrasena");

    if (!campo || !boton) return;

    boton.addEventListener("click", function () {
        const mostrar = campo.type === "password";

        campo.type = mostrar ? "text" : "password";
        boton.setAttribute("aria-pressed", String(mostrar));
        boton.setAttribute(
            "aria-label",
            mostrar ? "Ocultar contraseña" : "Mostrar contraseña"
        );
        boton.title = mostrar ? "Ocultar contraseña" : "Mostrar contraseña";
    });
});
</script>
</body>
</html>
