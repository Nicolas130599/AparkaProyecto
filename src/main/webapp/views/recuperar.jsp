<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Aparka | Recuperar contraseña</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/estilos.css">
</head>

<body class="pagina-auth">

    <div class="auth-card animar-entrada">

        <h1 class="logo">🅿️ Aparka</h1>

        <p class="subtitulo">Recuperar contraseña</p>

        <c:if test="${not empty error}">
            <div class="alerta alerta-error">
                <c:out value="${error}" />
            </div>
        </c:if>

        <c:if test="${not empty mensaje}">
            <div class="alerta alerta-exito">
                <c:out value="${mensaje}" />
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/recuperar"
              method="post"
              class="form-aparka">

            <input type="hidden"
                   name="csrfToken"
                   value="<c:out value='${sessionScope.csrfToken}' />">

            <label for="identificador">
                Usuario o correo electrónico
            </label>

            <input type="text"
                   id="identificador"
                   name="identificador"
                   maxlength="150"
                   autocomplete="username"
                   placeholder="Ingresa tu usuario o correo"
                   required
                   autofocus>

            <button type="submit"
                    class="btn btn-primario">
                Enviar enlace de recuperación
            </button>

        </form>

        <p class="enlace-secundario">
            <a href="${pageContext.request.contextPath}/login">
                Volver al login
            </a>
        </p>

    </div>

    <script src="${pageContext.request.contextPath}/js/animaciones.js"></script>

</body>
</html>
