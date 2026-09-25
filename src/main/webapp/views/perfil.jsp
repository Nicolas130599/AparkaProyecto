<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Aparka | Mi perfil</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<%@ include file="header.jspf" %>

<main class="contenedor contenedor-estrecho">
    <div class="tarjeta-formulario animar-entrada">
        <h2 class="titulo-seccion">Mi perfil</h2>
        <p><strong>Nombre:</strong> ${sessionScope.usuario.nombres}</p>
        <p><strong>Usuario:</strong> ${sessionScope.usuario.username}</p>
        <p><strong>Correo:</strong> ${sessionScope.usuario.email}</p>
        <p><strong>Rol:</strong> ${sessionScope.usuario.nombreRol}</p>
    </div>
</main>

<script src="${pageContext.request.contextPath}/js/animaciones.js"></script>
</body>
</html>
