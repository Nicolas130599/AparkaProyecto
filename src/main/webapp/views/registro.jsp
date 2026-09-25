<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Aparka | Crear cuenta</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body class="pagina-auth">
    <div class="auth-card animar-entrada">
        <h1 class="logo">🅿️ Aparka</h1>
        <p class="subtitulo">Crea tu cuenta de usuario</p>

        <c:if test="${not empty error}">
            <div class="alerta alerta-error">${error}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/registro" method="post" class="form-aparka">
            <label for="nombres">Nombre completo</label>
            <input type="text" id="nombres" name="nombres" required autofocus>

            <label for="username">Usuario</label>
            <input type="text" id="username" name="username" required>

            <label for="email">Correo</label>
            <input type="email" id="email" name="email" required>

            <label for="contrasena">Contraseña</label>
            <input type="password" id="contrasena" name="contrasena" required minlength="6">

            <button type="submit" class="btn btn-primario">Registrarme</button>
        </form>

        <p class="enlace-secundario">
            ¿Ya tienes cuenta?
            <a href="${pageContext.request.contextPath}/login">Inicia sesión</a>
        </p>
    </div>
    <script src="${pageContext.request.contextPath}/js/animaciones.js"></script>
</body>
</html>
