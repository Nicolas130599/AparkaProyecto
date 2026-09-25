<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Aparka | Consulta de flujo vehicular</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<%@ include file="header.jspf" %>

<main class="contenedor">
    <h2 class="titulo-seccion animar-entrada">Consulta de flujo vehicular</h2>
    <p class="descripcion">Historial de ingresos y salidas, filtrable por fecha, zona y tipo.</p>

    <div class="tarjeta-formulario animar-entrada" style="margin-bottom:20px;">
        <form action="${pageContext.request.contextPath}/admin/flujo" method="get" class="form-aparka form-inline">
            <div>
                <label for="fechaInicio">Fecha inicio</label>
                <input type="date" id="fechaInicio" name="fechaInicio" value="${fechaInicio}">
            </div>
            <div>
                <label for="fechaFin">Fecha fin</label>
                <input type="date" id="fechaFin" name="fechaFin" value="${fechaFin}">
            </div>
            <div>
                <label for="zonaId">Zona</label>
                <select id="zonaId" name="zonaId">
                    <option value="">Todas</option>
                    <c:forEach var="zona" items="${zonas}">
                        <option value="${zona.zonaId}" ${zonaIdSeleccionada == zona.zonaId ? 'selected' : ''}>${zona.nombre}</option>
                    </c:forEach>
                </select>
            </div>
            <div>
                <label for="tipo">Tipo</label>
                <select id="tipo" name="tipo">
                    <option value="">Todos</option>
                    <option value="ENTRADA" ${tipoSeleccionado == 'ENTRADA' ? 'selected' : ''}>Entrada</option>
                    <option value="SALIDA" ${tipoSeleccionado == 'SALIDA' ? 'selected' : ''}>Salida</option>
                </select>
            </div>
            <button type="submit" class="btn btn-primario" style="width:auto; align-self:flex-end;">Buscar</button>
        </form>
    </div>

    <div class="tabla-wrapper animar-entrada">
        <table class="tabla-apparka">
            <thead>
            <tr>
                <th>Ticket</th>
                <th>Placa</th>
                <th>Tipo</th>
                <th>Fecha / Hora</th>
                <th>Puerta</th>
                <th>Zona</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="mov" items="${movimientos}">
                <tr>
                    <td>${mov.ticket}</td>
                    <td>${mov.placa}</td>
                    <td>
                        <span class="badge ${mov.tipo == 'Entrada' ? 'badge-BAJO' : 'badge-ALTO'}">${mov.tipo}</span>
                    </td>
                    <td><fmt:formatDate value="${mov.fechaHora}" pattern="dd/MM/yyyy HH:mm"/></td>
                    <td>${mov.puerta}</td>
                    <td>${mov.zona}</td>
                </tr>
            </c:forEach>
            <c:if test="${empty movimientos}">
                <tr><td colspan="6" style="text-align:center; color:#7c8a8e;">No hay movimientos en el rango seleccionado.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
</main>

<script src="${pageContext.request.contextPath}/js/animaciones.js"></script>
</body>
</html>
