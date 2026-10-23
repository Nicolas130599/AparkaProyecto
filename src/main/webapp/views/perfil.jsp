
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="u" value="${sessionScope.usuario}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Aparka | Mi perfil</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/estilos.css">
</head>

<body>

<%@ include file="header.jspf" %>

<main class="contenedor contenedor-medio">

    <h2 class="titulo-seccion animar-entrada">Mi perfil</h2>

    <p class="descripcion animar-entrada">
        Administra tu información personal, tu foto y la seguridad de tu cuenta.
    </p>

    <c:if test="${not empty mensajeTexto}">
        <div class="alerta ${mensajeTipo == 'exito' ? 'alerta-exito' : 'alerta-error'}">
            <c:out value="${mensajeTexto}"/>
        </div>
    </c:if>

    <div class="perfil-layout">

        <!-- ================= COLUMNA IZQUIERDA: FOTO E IDENTIDAD ================= -->

        <aside class="tarjeta-formulario perfil-lateral animar-entrada">

            <div class="perfil-avatar-wrap">

                <c:choose>

                    <c:when test="${not empty u.fotoPerfil}">
                        <img id="avatarGrande"
                             class="perfil-avatar"
                             alt="Foto de perfil"
                             src="${pageContext.request.contextPath}/perfil/foto">
                    </c:when>

                    <c:otherwise>
                        <div id="avatarGrande"
                             class="perfil-avatar perfil-avatar-iniciales">
                            <c:out value="${u.iniciales}"/>
                        </div>
                    </c:otherwise>

                </c:choose>

            </div>

            <h3 class="perfil-nombre">
                <c:out value="${u.nombres}"/>
            </h3>

            <span class="rol-badge">
                <c:out value="${u.nombreRol}"/>
            </span>

            <p class="perfil-usuario">
                @<c:out value="${u.username}"/>
            </p>

            <!-- ================= SUBIR O CAMBIAR FOTO ================= -->

            <form id="formFoto"
                  action="${pageContext.request.contextPath}/perfil/foto"
                  method="post"
                  enctype="multipart/form-data"
                  class="perfil-foto-form">

                <%-- Protección CSRF: subir o cambiar fotografía --%>
                <input type="hidden"
                       name="csrfToken"
                       value="<c:out value='${sessionScope.csrfToken}'/>">

                <input type="hidden"
                       name="accion"
                       value="subir">

                <input type="file"
                       id="inputFoto"
                       name="foto"
                       accept="image/jpeg,image/png"
                       hidden>

                <label for="inputFoto"
                       id="btnElegirFoto"
                       class="btn btn-secundario btn-chico">

                    ${empty u.fotoPerfil ? 'Subir foto' : 'Cambiar foto'}

                </label>

                <div id="accionesFoto"
                     class="perfil-acciones-foto"
                     hidden>

                    <button type="submit"
                            class="btn btn-primario btn-chico">
                        Guardar foto
                    </button>

                    <button type="button"
                            id="btnCancelarFoto"
                            class="btn btn-chico btn-neutro">
                        Cancelar
                    </button>

                </div>

                <p id="errorFoto"
                   class="error-campo"
                   hidden></p>

            </form>

            <!-- ================= ELIMINAR FOTO ================= -->

            <c:if test="${not empty u.fotoPerfil}">

                <form action="${pageContext.request.contextPath}/perfil/foto"
                      method="post"
                      onsubmit="return confirm('¿Quitar tu foto de perfil?');">

                    <%-- Protección CSRF: eliminar fotografía --%>
                    <input type="hidden"
                           name="csrfToken"
                           value="<c:out value='${sessionScope.csrfToken}'/>">

                    <input type="hidden"
                           name="accion"
                           value="eliminar">

                    <button type="submit"
                            class="enlace-quitar">
                        Quitar foto
                    </button>

                </form>

            </c:if>

            <p class="perfil-ayuda">
                Para un perfil profesional usa una foto de frente,
                con fondo neutro y buena iluminación.
                JPG o PNG, máximo 2&nbsp;MB.
                Se recorta automáticamente en formato cuadrado.
            </p>

        </aside>

        <!-- ================= COLUMNA DERECHA: DATOS Y SEGURIDAD ================= -->

        <div class="perfil-principal">

            <!-- ================= INFORMACIÓN PERSONAL ================= -->

            <section class="tarjeta-formulario animar-entrada">

                <h3 class="perfil-seccion-titulo">
                    Información personal
                </h3>

                <form action="${pageContext.request.contextPath}/perfil"
                      method="post"
                      class="form-aparka"
                      id="formDatos">

                    <%-- Protección CSRF: actualización de datos personales --%>
                    <input type="hidden"
                           name="csrfToken"
                           value="<c:out value='${sessionScope.csrfToken}'/>">

                    <input type="hidden"
                           name="accion"
                           value="datos">

                    <label for="nombres">
                        Nombre completo
                    </label>

                    <input type="text"
                           id="nombres"
                           name="nombres"
                           maxlength="150"
                           required
                           value="<c:out value='${u.nombres}'/>">

                    <label for="email">
                        Correo electrónico
                    </label>

                    <input type="email"
                           id="email"
                           name="email"
                           maxlength="100"
                           required
                           value="<c:out value='${u.email}'/>"
                           data-original="<c:out value='${u.email}'/>">

                    <div id="bloqueConfirmarCorreo"
                         class="bloque-confirmar"
                         hidden>

                        <label for="passwordActualDatos">
                            Confirma tu contraseña actual para cambiar el correo
                        </label>

                        <div class="input-grupo-password">

                            <input type="password"
                                   id="passwordActualDatos"
                                   name="passwordActual"
                                   autocomplete="current-password">

                            <button type="button"
                                    class="btn-toggle-password"
                                    data-toggle="passwordActualDatos"
                                    aria-label="Mostrar u ocultar contraseña">
                                👁️
                            </button>

                        </div>

                        <p class="perfil-ayuda">
                            Es una medida de seguridad: este correo se usa
                            para recuperar tu cuenta.
                        </p>

                    </div>

                    <label for="telefono">
                        Teléfono
                    </label>

                    <input type="tel"
                           id="telefono"
                           name="telefono"
                           maxlength="20"
                           required
                           value="<c:out value='${u.telefono}'/>">

                    <div class="fila-solo-lectura">

                        <div>

                            <label for="usernameLectura">
                                Usuario
                            </label>

                            <input type="text"
                                   id="usernameLectura"
                                   value="<c:out value='${u.username}'/>"
                                   disabled>

                        </div>

                        <div>

                            <label for="documentoLectura">
                                Documento de identidad
                            </label>

                            <input type="text"
                                   id="documentoLectura"
                                   value="<c:out value='${u.documento}'/>"
                                   disabled>

                        </div>

                    </div>

                    <p class="perfil-ayuda">
                        El usuario y el documento no se pueden modificar.
                        Si hay un error, contacta a soporte.
                    </p>

                    <button type="submit"
                            class="btn btn-primario">
                        Guardar cambios
                    </button>

                </form>

            </section>

            <!-- ================= SEGURIDAD ================= -->

            <section class="tarjeta-formulario animar-entrada">

                <h3 class="perfil-seccion-titulo">
                    Seguridad
                </h3>

                <form action="${pageContext.request.contextPath}/perfil"
                      method="post"
                      class="form-aparka"
                      id="formPassword">

                    <%-- Protección CSRF: cambio de contraseña --%>
                    <input type="hidden"
                           name="csrfToken"
                           value="<c:out value='${sessionScope.csrfToken}'/>">

                    <input type="hidden"
                           name="accion"
                           value="password">

                    <label for="passwordActual">
                        Contraseña actual
                    </label>

                    <div class="input-grupo-password">

                        <input type="password"
                               id="passwordActual"
                               name="passwordActual"
                               required
                               autocomplete="current-password">

                        <button type="button"
                                class="btn-toggle-password"
                                data-toggle="passwordActual"
                                aria-label="Mostrar u ocultar contraseña">
                            👁️
                        </button>

                    </div>

                    <label for="passwordNueva">
                        Nueva contraseña
                    </label>

                    <div class="input-grupo-password">

                        <input type="password"
                               id="passwordNueva"
                               name="passwordNueva"
                               required
                               minlength="6"
                               autocomplete="new-password">

                        <button type="button"
                                class="btn-toggle-password"
                                data-toggle="passwordNueva"
                                aria-label="Mostrar u ocultar contraseña">
                            👁️
                        </button>

                    </div>

                    <label for="passwordConfirma">
                        Repite la nueva contraseña
                    </label>

                    <div class="input-grupo-password">

                        <input type="password"
                               id="passwordConfirma"
                               name="passwordConfirma"
                               required
                               minlength="6"
                               autocomplete="new-password">

                        <button type="button"
                                class="btn-toggle-password"
                                data-toggle="passwordConfirma"
                                aria-label="Mostrar u ocultar contraseña">
                            👁️
                        </button>

                    </div>

                    <p id="errorPassword"
                       class="error-campo"
                       hidden>
                        Las contraseñas no coinciden.
                    </p>

                    <button type="submit"
                            class="btn btn-primario">
                        Actualizar contraseña
                    </button>

                </form>

            </section>

        </div>

    </div>

</main>

<script src="${pageContext.request.contextPath}/js/animaciones.js"></script>
<script src="${pageContext.request.contextPath}/js/perfil.js"></script>

</body>
</html>
