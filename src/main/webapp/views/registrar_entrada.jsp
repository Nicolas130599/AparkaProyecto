<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Aparka | Registrar entrada</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<%@ include file="header.jspf" %>

<main class="contenedor contenedor-estrecho">
    <div class="tarjeta-formulario animar-entrada">
        <h2 class="titulo-seccion">Registrar ingreso vehicular</h2>
        <p class="descripcion">Registra el ingreso de cualquier vehículo a una zona (uso de mostrador/puerta).</p>

        <c:if test="${param.registrado == '1'}">
            <div class="alerta alerta-exito">Ingreso registrado correctamente — N° Ticket: <strong>${param.ticket}</strong></div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alerta alerta-error">${error}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/admin/entradas" method="post" class="form-aparka">
            <div class="form-inline">
                <div>
                    <label for="fecha">Fecha</label>
                    <input type="text" id="fecha" value="<fmt:formatDate value='<%= new java.util.Date() %>' pattern='dd/MM/yyyy'/>" disabled>
                </div>
                <div>
                    <label for="hora">Hora</label>
                    <input type="text" id="hora" value="<fmt:formatDate value='<%= new java.util.Date() %>' pattern='HH:mm'/>" disabled>
                </div>
            </div>

            <label for="ticket">N° Ticket</label>
            <input type="text" id="ticket" value="Se generará automáticamente" disabled>

            <label for="placa">Placa del vehículo</label>
            <input type="text" id="placa" name="placa" placeholder="Ej. ABC-123" required autofocus>

            <label for="puertaId">Puerta de acceso</label>
            <select id="puertaId" name="puertaId" required>
                <c:forEach var="puerta" items="${puertas}">
                    <option value="${puerta.puertaId}">${puerta.descripcion}</option>
                </c:forEach>
            </select>

            <label for="zonaId">Zona destino</label>
            <select id="zonaId" name="zonaId" required>
                <c:forEach var="zona" items="${zonas}">
                    <option value="${zona.zonaId}">${zona.nombre} (${zona.capacidadOcupada}/${zona.capacidadTotal})</option>
                </c:forEach>
            </select>

            <label for="tipoClienteId">Tipo de cliente</label>
            <select id="tipoClienteId" name="tipoClienteId" required>
                <option value="1">General</option>
                <option value="2">Frecuente</option>
                <option value="3">Persona con discapacidad</option>
                <option value="4">Cliente VIP</option>
            </select>

            <button type="submit" class="btn btn-primario">Registrar ingreso</button>
        </form>
    </div>
</main>

<script src="${pageContext.request.contextPath}/js/animaciones.js"></script>
</body>
</html>
