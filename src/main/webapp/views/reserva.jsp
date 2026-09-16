<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Aparka | Registrar ingreso</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<%@ include file="header.jspf" %>

<main class="contenedor contenedor-estrecho">
    <div class="tarjeta-formulario animar-entrada">
        <h2 class="titulo-seccion">Registrar ingreso vehicular</h2>

        <form action="${pageContext.request.contextPath}/reserva" method="post" class="form-aparka">
            <label for="zonaId">Zona</label>
            <input type="hidden" id="zonaId" name="zonaId" value="${param.zonaId}">
            <input type="text" value="Zona seleccionada: #${param.zonaId}" disabled>

            <label for="placa">Placa del vehículo</label>
            <input type="text" id="placa" name="placa" placeholder="Ej. ABC-123" required>

            <button type="submit" class="btn btn-primario">Confirmar ingreso</button>
        </form>
    </div>
</main>

<script src="${pageContext.request.contextPath}/js/animaciones.js"></script>
</body>
</html>
