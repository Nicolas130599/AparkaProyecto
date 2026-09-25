<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Aparka | Gestión de zonas</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<%@ include file="header.jspf" %>

<main class="contenedor">
    <h2 class="titulo-seccion animar-entrada">Gestión de zonas</h2>

    <div class="tarjeta-formulario animar-entrada">
        <h3>Nueva zona</h3>
        <form action="${pageContext.request.contextPath}/admin/zonas" method="post" class="form-aparka form-inline">
            <input type="hidden" name="accion" value="crear">
            <input type="text" name="nombre" placeholder="Nombre de zona" required>
            <input type="text" name="ubicacion" placeholder="Ubicación" required>
            <input type="number" name="capacidadTotal" placeholder="Capacidad total" required>
            <input type="number" step="0.1" name="tarifaHora" placeholder="Tarifa/hora" required>
            <button type="submit" class="btn btn-primario">Agregar zona</button>
        </form>
    </div>

    <div class="tabla-wrapper animar-entrada">
        <table class="tabla-aparka">
            <thead>
            <tr>
                <th>Zona</th><th>Ubicación</th><th>Capacidad</th><th>Tarifa</th><th>Flujo</th><th>Acciones</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="zona" items="${zonas}">
                <tr>
                    <td>${zona.nombre}</td>
                    <td>${zona.ubicacion}</td>
                    <td>${zona.capacidadOcupada} / ${zona.capacidadTotal}</td>
                    <td>S/ ${zona.tarifaHora}</td>
                    <td><span class="badge badge-${zona.nivelFlujo}">${zona.nivelFlujo}</span></td>
                    <td>
                        <form action="${pageContext.request.contextPath}/admin/zonas" method="post" class="form-linea">
                            <input type="hidden" name="accion" value="eliminar">
                            <input type="hidden" name="id" value="${zona.zonaId}">
                            <button type="submit" class="btn btn-peligro btn-chico">Eliminar</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</main>

<script src="${pageContext.request.contextPath}/js/animaciones.js"></script>
</body>
</html>
