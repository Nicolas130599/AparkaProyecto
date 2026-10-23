
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Aparka | Registrar ingreso</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>

<%@ include file="header.jspf" %>

<main class="contenedor contenedor-estrecho">
    <div class="tarjeta-formulario animar-entrada">

        <h2 class="titulo-seccion">
            Registrar ingreso vehicular
        </h2>

        <%-- Mensaje de validación o zona llena --%>
        <c:if test="${not empty error}">
            <div class="mensaje-error" role="alert">
                <c:out value="${error}" />
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/reserva"
              method="post"
              class="form-aparka">

            <%-- Protección CSRF --%>
            <input type="hidden"
                   name="csrfToken"
                   value="<c:out value='${sessionScope.csrfToken}'/>">

            <%-- Zona seleccionada --%>
            <label for="zonaSeleccionada">Zona</label>

            <input type="hidden"
                   id="zonaId"
                   name="zonaId"
                   value="<c:out value='${not empty zonaIdIngresada ? zonaIdIngresada : param.zonaId}'/>">

            <input type="text"
                   id="zonaSeleccionada"
                   value="Zona seleccionada: #<c:out value='${not empty zonaIdIngresada ? zonaIdIngresada : param.zonaId}'/>"
                   disabled>

            <%-- Placa vehicular --%>
            <label for="placa">Placa del vehículo</label>

            <input type="text"
                   id="placa"
                   name="placa"
                   placeholder="Ej. ABC-123"
                   minlength="5"
                   maxlength="7"
                   maxlength="7"
                   value="<c:out value='${not empty placaIngresada ? placaIngresada : param.placa}'/>"
                   required>

            <button type="submit" class="btn btn-primario">
                Confirmar ingreso
            </button>

        </form>
    </div>
</main>

<script src="${pageContext.request.contextPath}/js/animaciones.js"></script>

</body>
</html>
