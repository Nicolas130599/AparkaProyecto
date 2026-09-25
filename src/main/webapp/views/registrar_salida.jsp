<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Aparka | Registrar salida</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<%@ include file="header.jspf" %>

<main class="contenedor contenedor-estrecho">
    <div class="tarjeta-formulario animar-entrada">
        <h2 class="titulo-seccion">Registrar salida vehicular</h2>
        <p class="descripcion">Busca por número de ticket o placa para cerrar el ingreso.</p>

        <c:if test="${param.registrado == '1'}">
            <div class="alerta alerta-exito">Salida registrada correctamente.</div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alerta alerta-error">${error}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/admin/salidas" method="post" class="form-aparka">
            <input type="hidden" name="accion" value="buscar">
            <label for="criterio">N° Ticket o Placa</label>
            <input type="text" id="criterio" name="criterio" placeholder="Ej. ABC-123 o 1024" required autofocus>
            <button type="submit" class="btn btn-primario">Buscar</button>
        </form>

        <c:if test="${not empty entradaEncontrada}">
            <hr style="margin:18px 0; border:none; border-top:1px solid #eee0c3;">
            <h3 style="margin-bottom:8px;">Vehículo encontrado</h3>
            <p><strong>Ticket:</strong> ${entradaEncontrada.parkingTransNo}</p>
            <p><strong>Placa:</strong> ${entradaEncontrada.placa}</p>
            <p><strong>Ingreso:</strong> <fmt:formatDate value="${entradaEncontrada.fechaHora}" pattern="dd/MM/yyyy HH:mm"/></p>

            <form action="${pageContext.request.contextPath}/admin/salidas" method="post" class="form-aparka" style="margin-top:14px;">
                <input type="hidden" name="accion" value="confirmar">
                <input type="hidden" name="ticket" value="${entradaEncontrada.parkingTransNo}">

                <label for="puertaSalidaId">Puerta de salida</label>
                <select id="puertaSalidaId" name="puertaSalidaId" required>
                    <c:forEach var="puerta" items="${puertas}">
                        <option value="${puerta.puertaId}">${puerta.descripcion}</option>
                    </c:forEach>
                </select>

                <button type="submit" class="btn btn-primario">Confirmar salida</button>
            </form>
        </c:if>
    </div>
</main>

<script src="${pageContext.request.contextPath}/js/animaciones.js"></script>
</body>
</html>
