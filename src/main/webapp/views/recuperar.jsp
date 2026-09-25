<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Aparka | Recuperar contraseña</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body class="pagina-auth">
    <div class="auth-card animar-entrada">
        <h1 class="logo">🅿️ Aparka</h1>
        <p class="subtitulo">Recuperar contraseña</p>

        <c:if test="${not empty error}">
            <div class="alerta alerta-error">${error}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/recuperar" method="post" class="form-aparka">
            <label for="username">Usuario</label>
            <input type="text" id="username" name="username" required autofocus>

            <label for="email">Correo registrado</label>
            <input type="email" id="email" name="email" required>

            <label for="nuevaContrasena">Nueva contraseña</label>
            <input type="password" id="nuevaContrasena" name="nuevaContrasena" required minlength="6">

            <button type="submit" class="btn btn-primario">Actualizar contraseña</button>
        </form>

        <p class="enlace-secundario">
            <a href="${pageContext.request.contextPath}/login">Volver al login</a>
        </p>
    </div>
    <script src="${pageContext.request.contextPath}/js/animaciones.js"></script>
</body>
</html>
