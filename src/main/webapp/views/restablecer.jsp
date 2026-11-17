<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Aparka | Restablecer contraseña</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/estilos.css">
</head>

<body class="pagina-auth">

    <div class="auth-card animar-entrada">

        <h1 class="logo">🅿️ Aparka</h1>

        <p class="subtitulo">Establecer nueva contraseña</p>

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

        <c:if test="${not empty token and empty mensaje}">

            <form action="${pageContext.request.contextPath}/restablecer"
                  method="post"
                  class="form-aparka">

                <input type="hidden"
                       name="token"
                       value="<c:out value='${token}' />">

                <input type="hidden"
                       name="csrfToken"
                       value="<c:out value='${csrfToken}' />">

                <label for="nuevaPassword">
                    Nueva contraseña
                </label>

                <input type="password"
                       id="nuevaPassword"
                       name="nuevaPassword"
                       minlength="8"
                       maxlength="128"
                       autocomplete="new-password"
                       required>

                <label for="confirmarPassword">
                    Confirmar contraseña
                </label>

                <input type="password"
                       id="confirmarPassword"
                       name="confirmarPassword"
                       minlength="8"
                       maxlength="128"
                       autocomplete="new-password"
                       required>

                <button type="submit"
                        class="btn btn-primario">
                    Guardar nueva contraseña
                </button>

            </form>

        </c:if>

        <p class="enlace-secundario">
            <a href="${pageContext.request.contextPath}/login">
                Volver al login
            </a>
        </p>

    </div>

    <script src="${pageContext.request.contextPath}/js/animaciones.js"></script>

</body>
</html>
