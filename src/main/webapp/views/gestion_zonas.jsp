<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Aparka | Zonas y flujo</title>
    <link rel="stylesheet" href="${ctx}/css/estilos.css">
</head>
<body>
<%@ include file="header.jspf" %>

<main class="contenedor contenedor-ancho">

    <!-- ============ Cabecera + filtro ============ -->
    <div class="gz-cabecera animar-entrada">
        <div>
            <h2 class="gz-titulo">Gestión de zonas</h2>
            <p class="gz-subtitulo">Monitoreo del flujo vehicular y administración de zonas por nivel del estacionamiento.</p>
        </div>
        <form action="${ctx}/admin/zonas" method="get" class="gz-filtro">
            <input type="hidden" name="nivel" value="<c:out value='${nivelActual}'/>">
            <div><label for="fechaInicio">Desde</label>
                <input type="date" id="fechaInicio" name="fechaInicio" value="<c:out value='${fechaInicio}'/>"></div>
            <div><label for="fechaFin">Hasta</label>
                <input type="date" id="fechaFin" name="fechaFin" value="<c:out value='${fechaFin}'/>"></div>
            <button type="submit" class="btn btn-primario btn-chico">Actualizar</button>
        </form>
    </div>

    <c:if test="${param.ok == '1'}"><div class="alerta alerta-exito">Los cambios se guardaron correctamente.</div></c:if>
    <c:if test="${param.error == '1'}"><div class="alerta alerta-error">Revisa los datos: nombre o identificador válido, y capacidad y tarifa numéricas y no negativas.</div></c:if>
    <c:if test="${param.error == '2'}"><div class="alerta alerta-error">Ya existe un cajero con ese identificador.</div></c:if>
    <c:if test="${param.error == '3'}"><div class="alerta alerta-error">No se puede eliminar: el cajero tiene ventas registradas.</div></c:if>
    <c:if test="${not cajasDisponibles}"><div class="alerta alerta-error">Los cajeros no están disponibles en este momento. Revisa la conexión con la base de datos (el esquema se actualiza solo al iniciar la aplicación).</div></c:if>

    <!-- ============ KPIs ============ -->
    <div class="gz-kpis animar-entrada">
        <div class="gz-kpi"><span class="gz-kpi-etiqueta">Zonas registradas</span>
            <strong class="gz-kpi-valor">${zonas.size()}</strong>
            <span class="gz-kpi-detalle">${totalCapacidad} espacios en total</span></div>
        <div class="gz-kpi"><span class="gz-kpi-etiqueta">Ocupación actual</span>
            <strong class="gz-kpi-valor">${porcentajeGlobal}%</strong>
            <span class="gz-kpi-detalle">${totalOcupados} de ${totalCapacidad} espacios</span></div>
        <div class="gz-kpi"><span class="gz-kpi-etiqueta">Zona con más flujo</span>
            <strong class="gz-kpi-valor gz-kpi-texto"><c:choose><c:when test="${mapa.vacio}">—</c:when><c:otherwise><c:out value="${mapa.zonaTop}"/></c:otherwise></c:choose></strong>
            <span class="gz-kpi-detalle"><c:if test="${not mapa.vacio}">${mapa.totalZonaTop} ingresos en el rango</c:if></span></div>
        <div class="gz-kpi"><span class="gz-kpi-etiqueta">Hora pico</span>
            <strong class="gz-kpi-valor"><c:choose><c:when test="${mapa.vacio}">—</c:when>
                <c:otherwise><fmt:formatNumber value="${mapa.horaPico}" minIntegerDigits="2"/>:00</c:otherwise></c:choose></strong>
            <span class="gz-kpi-detalle"><c:if test="${not mapa.vacio}">${mapa.totalHoraPico} ingresos en esa hora</c:if></span></div>
        <div class="gz-kpi"><span class="gz-kpi-etiqueta">Pagos en el rango</span>
            <strong class="gz-kpi-valor"><c:choose><c:when test="${cajasDisponibles}">${totalPagos}</c:when><c:otherwise>—</c:otherwise></c:choose></strong>
            <span class="gz-kpi-detalle"><c:if test="${cajasDisponibles}">${cajas.size()} cajeros registrados</c:if></span></div>
    </div>

    <!-- ============ Plano + mapa de calor ============ -->
    <section class="gz-panel animar-entrada">
        <div class="gz-panel-cab">
            <div class="gz-toggle" role="tablist" aria-label="Capa del plano">
                <a role="tab" href="${ctx}/admin/zonas?capa=zonas&nivel=${nivelActual}&fechaInicio=${fechaInicio}&fechaFin=${fechaFin}"
                   class="gz-toggle-op ${capa == 'zonas' ? 'gz-toggle-activo' : ''}">Zonas</a>
                <a role="tab" href="${ctx}/admin/zonas?capa=cajeros&nivel=${nivelActual}&fechaInicio=${fechaInicio}&fechaFin=${fechaFin}"
                   class="gz-toggle-op ${capa == 'cajeros' ? 'gz-toggle-activo' : ''}">Cajeros</a>
            </div>
            <a class="gz-enlace" href="${ctx}/admin/mapa-calor?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}">Ver cuadrícula completa por hora &rarr;</a>
        </div>

        <nav class="gz-tabs" aria-label="Niveles del estacionamiento">
            <c:forEach var="nv" items="${niveles}">
                <a href="${ctx}/admin/zonas?capa=${capa}&nivel=${nv}&fechaInicio=${fechaInicio}&fechaFin=${fechaFin}"
                   class="gz-tab ${nv == nivelActual ? 'gz-tab-activo' : ''}">
                    ${nv}<c:if test="${not empty elementosPorNivel[nv]}"> (${elementosPorNivel[nv]})</c:if>
                </a>
            </c:forEach>
        </nav>

        <div class="gz-plano">
            <img src="${ctx}/img/planos/${nivelActual}.png" alt="Plano del nivel ${nivelActual}" loading="lazy">
            <c:if test="${capa == 'zonas'}">
                <c:forEach var="z" items="${zonasDelNivel}">
                    <c:set var="fz" value="${mapa.porZona[z.nombre]}" />
                    <div class="gz-pin" style="left:${z.planoX}%;top:${z.planoY}%">
                        <span class="gz-calor gz-calor-n${empty fz ? 0 : fz.nivelTotal}"></span>
                        <span class="gz-punto"></span>
                        <span class="gz-etiqueta"><c:out value="${z.nombre}"/> &middot;
                            <fmt:formatNumber value="${z.porcentajeOcupacion}" maxFractionDigits="0"/>%</span>
                    </div>
                </c:forEach>
                <c:if test="${empty zonasDelNivel}">
                    <div class="gz-plano-vacio">Aún no hay zonas ubicadas en este plano.<br>Usa <strong>Editar</strong> en una zona y haz clic sobre el plano para ubicarla.</div>
                </c:if>
            </c:if>
            <c:if test="${capa == 'cajeros'}">
                <c:forEach var="cj" items="${cajasDelNivel}">
                    <div class="gz-pin" style="left:${cj.planoX}%;top:${cj.planoY}%">
                        <span class="gz-calor gz-calor-n${cj.nivelPagos}"></span>
                        <span class="gz-caja"></span>
                        <span class="gz-etiqueta"><c:out value="${cj.cajaId}"/> &middot; ${cj.pagos} pagos</span>
                    </div>
                </c:forEach>
                <c:if test="${empty cajasDelNivel}">
                    <div class="gz-plano-vacio">Aún no hay cajeros ubicados en este plano.<br>Crea uno con <strong>Nuevo cajero</strong> y haz clic sobre el plano para ubicarlo.</div>
                </c:if>
            </c:if>
        </div>

        <div class="gz-leyenda">
            <span><c:choose><c:when test="${capa == 'zonas'}">Flujo en el rango:</c:when><c:otherwise>Pagos en el rango:</c:otherwise></c:choose></span><span>menor</span>
            <i class="gz-sw gz-calor-n1"></i><i class="gz-sw gz-calor-n2"></i><i class="gz-sw gz-calor-n3"></i><i class="gz-sw gz-calor-n4"></i><i class="gz-sw gz-calor-n5"></i>
            <span>mayor</span><span class="gz-leyenda-nota"><c:choose><c:when test="${capa == 'zonas'}">El porcentaje de cada zona es su ocupación actual.</c:when><c:otherwise>Cada cajero muestra los pagos registrados en el rango de fechas.</c:otherwise></c:choose></span>
        </div>
    </section>

    <!-- ============ Tabla de zonas ============ -->
    <section class="gz-panel animar-entrada">
        <div class="gz-panel-cab">
            <h3>Zonas</h3>
            <button type="button" class="btn btn-primario btn-chico" id="btnNuevaZona">Nueva zona</button>
        </div>
        <div class="tabla-wrapper gz-tabla">
            <table class="tabla-aparka">
                <thead>
                <tr><th>Zona</th><th>Nivel</th><th>Ocupación</th><th>Estado</th><th>Flujo por hora (00–23)</th><th>Tarifa</th><th>Acciones</th></tr>
                </thead>
                <tbody>
                <c:forEach var="z" items="${zonas}">
                    <c:set var="fz" value="${mapa.porZona[z.nombre]}" />
                    <tr>
                        <td><strong><c:out value="${z.nombre}"/></strong><br><span class="gz-ubicacion"><c:out value="${z.ubicacion}"/></span></td>
                        <td><c:choose><c:when test="${not empty z.nivel}"><span class="gz-nivel">${z.nivel}</span></c:when><c:otherwise><span class="gz-ubicacion">Sin ubicar</span></c:otherwise></c:choose></td>
                        <td class="gz-celda-ocup">
                            <fmt:formatNumber value="${z.porcentajeOcupacion}" maxFractionDigits="0" var="pct"/>
                            <div class="gz-barra"><span class="gz-barra-${z.nivelFlujo}" style="width:${z.porcentajeOcupacion > 100 ? 100 : pct}%"></span></div>
                            <small>${z.capacidadOcupada} / ${z.capacidadTotal} (${pct}%)</small>
                        </td>
                        <td><span class="gz-estado gz-estado-${z.nivelFlujo}">${z.nivelFlujo}</span></td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty fz}">
                                    <div class="gz-tira">
                                        <c:forEach var="h" begin="0" end="23"><i class="calor-${fz.niveles[h]}" title="${h}:00 h · ${fz.horas[h]} ingresos"></i></c:forEach>
                                    </div>
                                </c:when>
                                <c:otherwise><span class="gz-ubicacion">Sin datos</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>S/ <fmt:formatNumber value="${z.tarifaHora}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td class="gz-acciones">
                            <button type="button" class="btn btn-secundario btn-chico gz-editar"
                                    data-id="${z.zonaId}"
                                    data-nombre="<c:out value='${z.nombre}'/>"
                                    data-ubicacion="<c:out value='${z.ubicacion}'/>"
                                    data-capacidad="${z.capacidadTotal}"
                                    data-tarifa="${z.tarifaHora}"
                                    data-nivel="${z.nivel}"
                                    data-x="${z.planoX}" data-y="${z.planoY}">Editar</button>
                            <form action="${ctx}/admin/zonas" method="post" class="form-linea"
                                onsubmit="return confirm('¿Eliminar esta zona? Esta acción no se puede deshacer.');">

                                <input type="hidden" name="csrfToken"
                                    value="<c:out value='${sessionScope.csrfToken}'/>">

                                <input type="hidden" name="accion" value="eliminar">
                                <input type="hidden" name="id" value="${z.zonaId}">
                                <button type="submit" class="btn btn-peligro btn-chico">Eliminar</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </section>

    <!-- ============ Resumen por nivel ============ -->
    <section class="gz-panel animar-entrada">
        <div class="gz-panel-cab"><h3>Resumen por nivel</h3></div>
        <div class="tabla-wrapper gz-tabla">
            <table class="tabla-aparka">
                <thead><tr><th>Nivel</th><th>Zonas</th><th>Visitas (ingresos)</th><th>Cajeros</th><th>Pagos</th><th>Monto</th></tr></thead>
                <tbody>
                <c:forEach var="r" items="${resumenNiveles}">
                    <tr>
                        <td><span class="gz-nivel">${r.nivel}</span></td>
                        <td>${r.zonas}</td><td><strong>${r.visitas}</strong></td>
                        <td>${r.cajeros}</td><td><strong>${r.pagos}</strong></td>
                        <td>S/ <fmt:formatNumber value="${r.monto}" minFractionDigits="2" maxFractionDigits="2"/></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty resumenNiveles}">
                    <tr><td colspan="6" class="gz-ubicacion">Ubica zonas o cajeros en los planos para ver el resumen por nivel.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
        <p class="gz-nota">Las visitas se cuentan por los ingresos a las zonas de cada nivel y los pagos por los cajeros de cada nivel.
            Son medidas distintas: una persona puede estacionar en un nivel y pagar en un cajero de otro.</p>
    </section>

    <!-- ============ Cajeros ============ -->
    <section class="gz-panel animar-entrada">
        <div class="gz-panel-cab">
            <h3>Cajeros y módulos de pago</h3>
            <button type="button" class="btn btn-primario btn-chico" id="btnNuevaCaja">Nuevo cajero</button>
        </div>
        <div class="tabla-wrapper gz-tabla">
            <table class="tabla-aparka">
                <thead><tr><th>Cajero</th><th>Tipo</th><th>Nivel</th><th>Pagos</th><th>Monto</th><th>Ticket promedio</th><th>Acciones</th></tr></thead>
                <tbody>
                <c:forEach var="cj" items="${cajas}">
                    <tr>
                        <td><strong><c:out value="${cj.cajaId}"/></strong><br><span class="gz-ubicacion"><c:out value="${cj.ubicacion}"/></span></td>
                        <td><c:out value="${cj.tipoCaja}"/></td>
                        <td><c:choose><c:when test="${not empty cj.nivel}"><span class="gz-nivel">${cj.nivel}</span></c:when><c:otherwise><span class="gz-ubicacion">Sin ubicar</span></c:otherwise></c:choose></td>
                        <td><strong>${cj.pagos}</strong></td>
                        <td>S/ <fmt:formatNumber value="${cj.monto}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td>S/ <fmt:formatNumber value="${cj.ticketPromedio}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td class="gz-acciones">
                            <button type="button" class="btn btn-secundario btn-chico gz-editar-caja"
                                    data-id="<c:out value='${cj.cajaId}'/>" data-tipo="<c:out value='${cj.tipoCaja}'/>"
                                    data-ubicacion="<c:out value='${cj.ubicacion}'/>" data-nivel="${cj.nivel}"
                                    data-x="${cj.planoX}" data-y="${cj.planoY}">Editar</button>
                            <form action="${ctx}/admin/cajas" method="post" class="form-linea"
                                onsubmit="return confirm('¿Eliminar este cajero? Solo es posible si no tiene ventas registradas.');">

                                <input type="hidden" name="csrfToken"
                                    value="<c:out value='${sessionScope.csrfToken}'/>">

                                <input type="hidden" name="accion" value="eliminar">
                                <input type="hidden" name="id" value="<c:out value='${cj.cajaId}'/>">

                                <button type="submit" class="btn btn-peligro btn-chico">Eliminar</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty cajas}">
                    <tr><td colspan="7" class="gz-ubicacion">Aún no hay cajeros registrados.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</main>

<!-- ============ Diálogo crear / editar zona ============ -->
<dialog id="dlgZona" class="gz-dialogo">
        <form action="${ctx}/admin/zonas" method="post" class="form-aparka" id="formZona">
            <h3 id="dlgTitulo">Nueva zona</h3>
            <input type="hidden" name="csrfToken"
                value="<c:out value='${sessionScope.csrfToken}'/>">
            <input type="hidden" name="accion" id="fAccion" value="crear">
        <input type="hidden" name="id" id="fId">
        <input type="hidden" name="planoX" id="fX">
        <input type="hidden" name="planoY" id="fY">
        <div class="gz-form-grid">
            <div><label for="fNombre">Nombre de la zona</label><input type="text" id="fNombre" name="nombre" maxlength="100" required></div>
            <div><label for="fUbicacion">Ubicación (referencia)</label><input type="text" id="fUbicacion" name="ubicacion" maxlength="150"></div>
            <div><label for="fCapacidad">Capacidad total</label><input type="number" id="fCapacidad" name="capacidadTotal" min="0" required></div>
            <div><label for="fTarifa">Tarifa por hora (S/)</label><input type="number" id="fTarifa" name="tarifaHora" step="0.1" min="0" required></div>
            <div><label for="fNivel">Plano / nivel</label>
                <select id="fNivel" name="nivel">
                    <option value="">Sin ubicar</option>
                    <c:forEach var="nv" items="${niveles}"><option value="${nv}">${nv}</option></c:forEach>
                </select></div>
        </div>
        <div id="pickerBloque" hidden>
            <p class="gz-ayuda">Haz clic sobre el plano para ubicar la zona.</p>
            <div class="gz-picker" id="picker">
                <img id="pickerImg" alt="Plano para ubicar la zona" data-base="${ctx}/img/planos/">
                <span class="gz-marcador" id="pickerMarcador" hidden></span>
            </div>
        </div>
        <div class="gz-dialogo-acciones">
            <button type="button" class="btn btn-neutro btn-chico" id="btnCancelarZona">Cancelar</button>
            <button type="submit" class="btn btn-primario btn-chico">Guardar</button>
        </div>
    </form>
</dialog>

<!-- ============ Diálogo crear / editar cajero ============ -->
<dialog id="dlgCaja" class="gz-dialogo">
    <form action="${ctx}/admin/cajas" method="post" class="form-aparka">
        <h3 id="cajaTitulo">Nuevo cajero</h3>
        <input type="hidden" name="csrfToken"
            value="<c:out value='${sessionScope.csrfToken}'/>">
        <input type="hidden" name="accion" id="cAccion" value="crear">
        <input type="hidden" name="planoX" id="cX">
        <input type="hidden" name="planoY" id="cY">
        <div class="gz-form-grid">
            <div><label for="cId">Identificador</label><input type="text" id="cId" name="cajaId" maxlength="30" pattern="[A-Za-z0-9_\-]{1,30}" placeholder="Ej. CAJ-P1-01" required></div>
            <div><label for="cTipo">Tipo</label><input type="text" id="cTipo" name="tipoCaja" maxlength="50" list="tiposCaja" placeholder="Ej. Cajero automático">
                <datalist id="tiposCaja"><option value="Cajero automático"></option><option value="Módulo de pago"></option><option value="Caja atendida"></option></datalist></div>
            <div><label for="cUbicacion">Ubicación (referencia)</label><input type="text" id="cUbicacion" name="ubicacion" maxlength="100"></div>
            <div><label for="cNivel">Plano / nivel</label>
                <select id="cNivel" name="nivel"><option value="">Sin ubicar</option>
                    <c:forEach var="nv" items="${niveles}"><option value="${nv}">${nv}</option></c:forEach></select></div>
        </div>
        <div id="cPickerBloque" hidden>
            <p class="gz-ayuda">Haz clic sobre el plano para ubicar el cajero.</p>
            <div class="gz-picker" id="cPicker">
                <img id="cPickerImg" alt="Plano para ubicar el cajero" data-base="${ctx}/img/planos/">
                <span class="gz-marcador" id="cMarcador" hidden></span>
            </div>
        </div>
        <div class="gz-dialogo-acciones">
            <button type="button" class="btn btn-neutro btn-chico" id="btnCancelarCaja">Cancelar</button>
            <button type="submit" class="btn btn-primario btn-chico">Guardar</button>
        </div>
    </form>
</dialog>
<script src="${ctx}/js/animaciones.js"></script>
<script src="${ctx}/js/gestion_zonas.js"></script>
</body>
</html>
