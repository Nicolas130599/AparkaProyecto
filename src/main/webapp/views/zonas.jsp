<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Aparka | Zonas de estacionamiento</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<%@ include file="header.jspf" %>

<main class="contenedor">
    <h2 class="titulo-seccion animar-entrada">Zonas disponibles</h2>
    <p class="descripcion">Consulta el nivel de flujo vehicular antes de estacionar.</p>

    <c:if test="${param.reservado == '1'}">
        <div class="alerta alerta-exito">¡Ingreso registrado correctamente!</div>
    </c:if>

    <div class="grid-zonas">
        <c:forEach var="zona" items="${zonas}">
            <div class="card-zona animar-entrada badge-${zona.nivelFlujo}">
                <div class="card-zona-header">
                    <h3>${zona.nombre}</h3>
                    <span class="badge badge-${zona.nivelFlujo}">${zona.nivelFlujo}</span>
                </div>
                <p class="ubicacion">📍 ${zona.ubicacion}</p>
                <p>Ocupación: ${zona.capacidadOcupada} / ${zona.capacidadTotal}
                   (<fmt:formatNumber value="${zona.porcentajeOcupacion}" maxFractionDigits="0"/>%)</p>
                <p>Tarifa: S/ ${zona.tarifaHora} / hora</p>
                <a href="${pageContext.request.contextPath}/reserva?zonaId=${zona.zonaId}" class="btn btn-secundario">
                    Estacionar aquí
                </a>
            </div>
        </c:forEach>
    </div>
</main>

<script src="${pageContext.request.contextPath}/js/animaciones.js"></script>
</body>
</html>
