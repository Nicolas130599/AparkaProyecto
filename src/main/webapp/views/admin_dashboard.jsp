<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Aparka | Panel administrador</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<%@ include file="header.jspf" %>

<main class="contenedor">
    <h2 class="titulo-seccion animar-entrada">Zonas de mayor flujo vehicular</h2>
    <p class="descripcion">Ranking en vivo según el % de ocupación registrado.</p>

    <a href="${pageContext.request.contextPath}/admin/reporte-excel" class="btn btn-secundario" style="display:inline-block; margin-bottom:16px; text-decoration:none;">
        📊 Exportar a Excel
    </a>

    <div class="tabla-wrapper animar-entrada">
        <table class="tabla-aparka">
            <thead>
            <tr>
                <th>#</th>
                <th>Zona</th>
                <th>Ubicación</th>
                <th>Ocupación</th>
                <th>Flujo</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="zona" items="${ranking}" varStatus="i">
                <tr>
                    <td>${i.index + 1}</td>
                    <td>${zona.nombre}</td>
                    <td>${zona.ubicacion}</td>
                    <td>
                        <div class="barra-ocupacion">
                            <div class="barra-relleno badge-${zona.nivelFlujo}"
                                 style="width: <fmt:formatNumber value='${zona.porcentajeOcupacion}' maxFractionDigits='0'/>%;"></div>
                        </div>
                        ${zona.capacidadOcupada} / ${zona.capacidadTotal}
                    </td>
                    <td><span class="badge badge-${zona.nivelFlujo}">${zona.nivelFlujo}</span></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</main>

<script src="${pageContext.request.contextPath}/js/animaciones.js"></script>
</body>
</html>
