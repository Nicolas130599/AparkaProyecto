
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Aparka | Panel administrador</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>

<%@ include file="header.jspf" %>

<main class="contenedor dashboard-admin">

    <div class="dashboard-encabezado animar-entrada">
        <div class="dashboard-encabezado-texto">
            <h1 class="titulo-seccion">Panel de administración</h1>
            <p class="descripcion">
                Resumen general de ocupación y disponibilidad
                del estacionamiento.
            </p>
        </div>

        <a href="${pageContext.request.contextPath}/admin/reporte-excel"
           class="btn btn-secundario dashboard-exportar">
            &#128202; Exportar a Excel
        </a>
    </div>

    <section class="dashboard-kpis" aria-label="Indicadores generales">

        <article class="dashboard-kpi">
            <span class="dashboard-kpi-etiqueta">Zonas registradas</span>
            <strong class="dashboard-kpi-valor">${totalZonas}</strong>
            <span class="dashboard-kpi-detalle">Zonas del estacionamiento</span>
        </article>

        <article class="dashboard-kpi">
            <span class="dashboard-kpi-etiqueta">Capacidad total</span>
            <strong class="dashboard-kpi-valor">${capacidadTotal}</strong>
            <span class="dashboard-kpi-detalle">Espacios registrados</span>
        </article>

        <article class="dashboard-kpi">
            <span class="dashboard-kpi-etiqueta">Espacios ocupados</span>
            <strong class="dashboard-kpi-valor">${espaciosOcupados}</strong>
            <span class="dashboard-kpi-detalle">Ocupación actual</span>
        </article>

        <article class="dashboard-kpi">
            <span class="dashboard-kpi-etiqueta">Espacios disponibles</span>
            <strong class="dashboard-kpi-valor">${espaciosDisponibles}</strong>
            <span class="dashboard-kpi-detalle">Espacios libres actualmente</span>
        </article>

    </section>

    <section class="dashboard-panel animar-entrada">
        <div class="dashboard-panel-cabecera">
            <div>
                <h2>Ocupación general</h2>
                <p>Estado actual del estacionamiento</p>
            </div>

            <strong class="dashboard-porcentaje">
                <fmt:formatNumber value="${porcentajeOcupacion}"
                                  maxFractionDigits="1"/>%
            </strong>
        </div>

        <div class="dashboard-progreso"
             role="progressbar"
             aria-label="Ocupación general"
             aria-valuemin="0"
             aria-valuemax="100"
             aria-valuenow="<fmt:formatNumber value='${porcentajeOcupacion}' maxFractionDigits='0'/>">
            <div class="dashboard-progreso-relleno"
                 style="width: <fmt:formatNumber value='${porcentajeOcupacion}' maxFractionDigits='0'/>%;">
            </div>
        </div>

        <p class="dashboard-nota">
            ${espaciosOcupados} de ${capacidadTotal} espacios ocupados.
            Este indicador refleja la ocupación actual,
            independientemente de los filtros históricos.
        </p>
    </section>

    <section class="dashboard-panel animar-entrada">

        <div class="dashboard-panel-cabecera">
            <div>
                <h2>Zonas de mayor flujo vehicular</h2>
                <p>Ranking según el porcentaje de ocupación actual.</p>
            </div>
            <a class="dashboard-enlace"
               href="${pageContext.request.contextPath}/admin/zonas">
                Ver gestión de zonas &rarr;
            </a>
        </div>

        <div class="tabla-wrapper">
            <table class="tabla-aparka dashboard-ranking">
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
                            <td><strong>${zona.nombre}</strong></td>
                            <td>${zona.ubicacion}</td>
                            <td>
                                <div class="barra-ocupacion">
                                    <div class="barra-relleno badge-${zona.nivelFlujo}"
                                         style="width: <fmt:formatNumber value='${zona.porcentajeOcupacion}' maxFractionDigits='0'/>%;">
                                    </div>
                                </div>
                                <span>
                                    ${zona.capacidadOcupada} /
                                    ${zona.capacidadTotal}
                                </span>
                            </td>
                            <td>
                                <span class="badge badge-${zona.nivelFlujo}">
                                    ${zona.nivelFlujo}
                                </span>
                            </td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty ranking}">
                        <tr>
                            <td colspan="5" class="dashboard-vacio">
                                No hay zonas registradas para mostrar.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </section>

</main>

<script src="${pageContext.request.contextPath}/js/animaciones.js"></script>
</body>
</html>
