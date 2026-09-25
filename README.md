# Aparka — Cómo aplicar este código a tu proyecto

> ⚠️ **Actualización importante**: el proyecto ahora usa la base de datos **`Parking`**
> aportada por el compañero de backend, con tablas reales (`UsuarioSistema`, `Entrada`,
> `Salida`, `CentroComercial`, etc.) en vez del esquema simplificado anterior
> (`usuario`, `zona`, `reserva`). Se agregó una tabla `Zona` porque el esquema original
> no traía capacidad ni nombre de zona — sin eso no se puede calcular el flujo vehicular,
> que es la función central de Aparka.
>
> **Credenciales de prueba actualizadas:**
> | Rol | Usuario | Contraseña |
> |---|---|---|
> | Admin | `admin` | `admin123` |
> | Usuario | `demo` | `demo123` |
>
> (Antes se usaba correo/contraseña; ahora es usuario/contraseña, porque `UsuarioSistema`
> usa `Username` en vez de `correo`.)

> ⚠️ **Tomcat 11**: esta versión del código ya está corregida para
> **Jakarta EE 10 / Servlet 6.0** (namespace `jakarta.servlet.*`), que es lo que exige
> Tomcat 10 y 11. También requiere **JDK 17 o superior**.

Este paquete implementa la **Alternativa 1 (Servlets + JSP)** completa: 7 pantallas,
2 perfiles (Usuario/Administrador), formularios funcionales y estilos con la paleta
azul petróleo + crema.

## 1. Copiar los archivos a tu proyecto existente
Tu estructura ya tenía `src/main/java/com/aparka/controller` y `webapp/css` y `webapp/views`.
Simplemente **copia el contenido de este paquete sobre tu proyecto**, respetando las rutas:

```
src/main/java/com/aparka/controller/   → 7 Servlets (.java)
src/main/java/com/aparka/model/        → Usuario, Zona, Reserva
src/main/java/com/aparka/dao/          → UsuarioDAO, ZonaDAO, ReservaDAO
src/main/java/com/aparka/service/      → ZonaService
src/main/java/com/aparka/util/         → ConexionBD, RoleFilter
src/main/webapp/views/                  → 7 archivos .jsp + header.jspf
src/main/webapp/css/estilos.css
src/main/webapp/js/animaciones.js
src/main/webapp/WEB-INF/web.xml
pom.xml
schema.sql
```

## 2. Configurar la base de datos
1. Instala MySQL si no lo tienes.
2. Ejecuta el script `schema.sql` (actualizado) — crea la base de datos `Parking`
   completa: `CentroComercial`, `Rol`, `UsuarioSistema`, `CajaMaster`, `PuertaMaster`,
   `Zona` (agregada), `Venta`, `Entrada`, `Salida`, más datos de ejemplo (2 usuarios,
   4 zonas, 3 ingresos activos).
3. Abre `src/main/java/com/aparka/util/ConexionBD.java` y actualiza:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/Parking...";
   private static final String USUARIO = "root";
   private static final String PASSWORD = "TU_PASSWORD_AQUI";
   ```
   (ajusta el puerto si usas uno distinto a 3306, como hicimos antes con el 3307)

## 3. Verificar el `pom.xml`
Ya incluye las dependencias necesarias: `javax.servlet-api`, `jstl`, `mysql-connector-java`.
Si tu IDE (Eclipse/IntelliJ/NetBeans) generó un `pom.xml` distinto, solo agrega esas
3 dependencias al tuyo.

## 4. Ejecutar el proyecto
- Verifica que tengas **JDK 17+** instalado (`java -version`). Tomcat 11 no arranca con JDK 11 o inferior.
- En **IntelliJ IDEA** o **Eclipse**: configura un servidor **Apache Tomcat 11**,
  agrega el proyecto como *Artifact/War exploded* y ejecuta.
- Por línea de comandos:
  ```bash
  mvn clean package
  # copia el .war generado en target/ a la carpeta webapps/ de tu Tomcat
  ```
- Abre `http://localhost:8080/AparkaProyecto/login`

## 5. Cuentas de prueba (del schema.sql)
| Rol   | Correo              | Contraseña |
|-------|---------------------|------------|
| Admin | admin@aparka.com   | admin123   |
| Usuario | demo@aparka.com  | demo123    |

## 6. Flujo de pantallas
```
login.jsp → (según rol) → zonas.jsp (usuario) / admin_dashboard.jsp (admin)
zonas.jsp → reserva.jsp → confirma ingreso → vuelve a zonas.jsp
admin_dashboard.jsp → admin/zonas (gestion_zonas.jsp, CRUD)
```

## 7. Solución de problemas comunes

| Error / síntoma | Causa probable | Solución |
|---|---|---|
| `NoClassDefFoundError: javax/servlet/...` | Estás usando el código viejo (javax) en Tomcat 11 | Usa este paquete actualizado (jakarta) |
| El servidor no arranca / "Unsupported class file major version" | JDK incompatible | Instala JDK 17+ y apunta tu IDE/JAVA_HOME a esa versión |
| `HTTP Status 404` al abrir `/login` | El contexto de la app no coincide | Revisa que la URL sea `http://localhost:8080/NOMBRE_DEL_WAR/login` (el nombre depende de cómo lo desplegaste) |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | Falta el conector MySQL en el classpath del servidor | Corre `mvn clean package` para que Maven empaquete la dependencia dentro del `.war` |
| Página en blanco o error 500 en las vistas | Falta la implementación de JSTL | Verifica que la dependencia `org.glassfish.web:jakarta.servlet.jsp.jstl` esté en el `pom.xml` (ya incluida aquí) |
| `Communications link failure` | La base de datos no está corriendo o mal configurada | Revisa que MySQL esté activo y que `ConexionBD.java` tenga la URL/usuario/contraseña correctos |

## 8. Siguientes pasos recomendados
- Cambiar el guardado de contraseñas en texto plano por **hash (BCrypt)**.
- Agregar `registrarSalidaVehiculo()` en un botón "Salir" dentro del historial del usuario.
- Si el proyecto crece, migrar a la **Alternativa 2 (Spring MVC)** propuesta en el
  documento de arquitectura — la capa `service`/`dao` ya está diseñada para facilitar ese cambio.
