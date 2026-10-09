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
        <c:if test="${not empty mensaje}">
            <div class="alerta alerta-exito" style="color: #2e7d32; background: #e8f5e9; padding: 10px; border-radius: 4px; margin-bottom: 15px;">${mensaje}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/recuperar" method="post" class="form-aparka">
            <label for="identificador">Usuario o Correo electrónico</label>
            <input type="text" id="identificador" name="identificador" required autofocus placeholder="Ej. Wleon o correo@gmail.com">

            <button type="submit" class="btn btn-primario" style="margin-top: 15px;">Enviar nueva contraseña</button>
        </form>

        <p class="enlace-secundario">
            <a href="${pageContext.request.contextPath}/login">Volver al login</a>
        </p>
    </div>
    <script src="${pageContext.request.contextPath}/js/animaciones.js"></script>
</body>
</html>
