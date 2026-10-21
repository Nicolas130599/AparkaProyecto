<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Aparka | Mapa de calor</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<%@ include file="header.jspf" %>

<main class="contenedor">
    <h2 class="titulo-seccion animar-entrada">Mapa de calor del flujo vehicular</h2>
    <p class="descripcion">Ingresos por zona y hora del día. Mientras más intenso el color, mayor es el flujo.</p>

    <div class="tarjeta-formulario animar-entrada" style="margin-bottom:20px;">
        <form action="${pageContext.request.contextPath}/admin/mapa-calor" method="get" class="form-aparka form-inline">
            <div>
                <label for="fechaInicio">Fecha inicio</label>
                <input type="date" id="fechaInicio" name="fechaInicio" value="<c:out value='${fechaInicio}'/>">
            </div>
            <div>
                <label for="fechaFin">Fecha fin</label>
                <input type="date" id="fechaFin" name="fechaFin" value="<c:out value='${fechaFin}'/>">
            </div>
            <button type="submit" class="btn btn-primario" style="width:auto; align-self:flex-end;">Actualizar</button>
        </form>
    </div>

    <!-- Resumen -->
    <div class="resumen-calor animar-entrada">
        <div class="tarjeta-formulario resumen-item">
            <span class="resumen-etiqueta">Zona con más flujo</span>
            <c:choose>
                <c:when test="${mapa.vacio}"><strong class="resumen-valor">—</strong></c:when>
                <c:otherwise>
                    <strong class="resumen-valor"><c:out value="${mapa.zonaTop}"/></strong>
                    <span class="resumen-detalle">${mapa.totalZonaTop} ingresos</span>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="tarjeta-formulario resumen-item">
            <span class="resumen-etiqueta">Hora pico</span>
            <c:choose>
                <c:when test="${mapa.vacio}"><strong class="resumen-valor">—</strong></c:when>
                <c:otherwise>
                    <strong class="resumen-valor">
                        <fmt:formatNumber value="${mapa.horaPico}" minIntegerDigits="2"/>:00 –
                        <fmt:formatNumber value="${mapa.horaPico}" minIntegerDigits="2"/>:59
                    </strong>
                    <span class="resumen-detalle">${mapa.totalHoraPico} ingresos en esa hora</span>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="tarjeta-formulario resumen-item">
            <span class="resumen-etiqueta">Ingresos en el rango</span>
            <strong class="resumen-valor">${mapa.totalGeneral}</strong>
        </div>
    </div>

    <c:if test="${mapa.vacio}">
        <div class="alerta alerta-error">No hay ingresos registrados en el rango seleccionado.</div>
    </c:if>

    <!-- Cuadrícula -->
    <div class="tabla-wrapper animar-entrada">
        <table class="mapa-calor">
            <thead>
            <tr>
                <th class="mc-zona">Zona</th>
                <c:forEach var="h" begin="0" end="23">
                    <th><fmt:formatNumber value="${h}" minIntegerDigits="2"/></th>
                </c:forEach>
                <th class="mc-total">Total</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="fila" items="${mapa.filas}">
                <tr>
                    <th class="mc-zona" scope="row"><c:out value="${fila.zona}"/></th>
                    <c:forEach var="h" begin="0" end="23">
                        <td class="calor calor-${fila.niveles[h]}"
                            title="${h}:00 h · ${fila.horas[h]} ingresos">${fila.horas[h] > 0 ? fila.horas[h] : ''}</td>
                    </c:forEach>
                    <td class="mc-total">${fila.total}</td>
                </tr>
            </c:forEach>
            </tbody>
            <tfoot>
            <tr>
                <th class="mc-zona">Todas</th>
                <c:forEach var="h" begin="0" end="23">
                    <td class="mc-total">${mapa.totalesPorHora[h] > 0 ? mapa.totalesPorHora[h] : ''}</td>
                </c:forEach>
                <td class="mc-total">${mapa.totalGeneral}</td>
            </tr>
            </tfoot>
        </table>
    </div>

    <div class="calor-leyenda">
        <span>Menos</span>
        <i class="calor-0"></i><i class="calor-1"></i><i class="calor-2"></i><i class="calor-3"></i><i class="calor-4"></i><i class="calor-5"></i>
        <span>Más</span>
    </div>
</main>

<script src="${pageContext.request.contextPath}/js/animaciones.js"></script>
</body>
</html>
