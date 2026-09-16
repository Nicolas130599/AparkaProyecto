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

        <form action="${pageContext.request.contextPath}/login" method="post" class="form-aparka">
            <label for="correo">Correo</label>
            <input type="email" id="correo" name="correo" required autofocus>

            <label for="contrasena">Contraseña</label>
            <input type="password" id="contrasena" name="contrasena" required>

            <button type="submit" class="btn btn-primario">Ingresar</button>
        </form>

        <p class="enlace-secundario">
            ¿No tienes cuenta?
            <a href="${pageContext.request.contextPath}/registro">Regístrate aquí</a>
        </p>
    </div>
    <script src="${pageContext.request.contextPath}/js/animaciones.js"></script>
</body>
</html>
