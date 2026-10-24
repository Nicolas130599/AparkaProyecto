# 🅿️ APARKA — Sistema web de gestión de estacionamientos

**APARKA** es un proyecto académico desarrollado con **Java, Jakarta Servlets y JSP** para gestionar ingresos y salidas vehiculares, consultar disponibilidad y analizar el flujo de vehículos por zonas de estacionamiento.

![Java](https://img.shields.io/badge/Java-17%2B-orange) ![Jakarta](https://img.shields.io/badge/Jakarta-Servlets%20%26%20JSP-blue) ![MySQL](https://img.shields.io/badge/Base%20de%20datos-MySQL-4479A1) ![Maven](https://img.shields.io/badge/Build-Maven-C71A36) ![JUnit](https://img.shields.io/badge/JUnit-80%20pruebas%20aprobadas-25A162) ![Tomcat](https://img.shields.io/badge/Servidor-Tomcat%2011-F8DC75)

> **Estado verificado el 23/10/2026:** el Pull Request [#3](https://github.com/Nicolas130599/AparkaProyecto/pull/3) se fusionó a `main`; `mvn clean verify` finalizó con **BUILD SUCCESS**, **80 pruebas**, **0 fallos**, **0 errores** y **0 omitidas**. Las verificaciones manuales realizadas abarcaron inicio de sesión, navegación básica por roles, cierre de sesión y bloqueo del dashboard administrativo sin autorización. Adicionalmente, se verificó el despliegue local con la base de demostración `APARKA_BD_100%` y un registro de entrada que actualizó la ocupación del Dashboard. Esto no constituye una auditoría de seguridad ni una prueba integral de todos los módulos.

## ✨ Funcionalidades

| Rol | Funcionalidades disponibles en el código |
|---|---|
| **Usuario** | Inicio y cierre de sesión, registro y recuperación de contraseña, consulta de zonas y tarifas, reserva, registro de ingreso vehicular y administración de perfil/fotografía. |
| **Administrador** | Panel de indicadores, registro de entradas y salidas, historial de flujo vehicular, mapa de calor, gestión de zonas y cajeros, administración de perfil y exportación de reportes Excel. |

## 📸 Galería de capturas

Las imágenes corresponden a una ejecución local de APARKA con datos de demostración. Las pantallas ilustran las interfaces; no acreditan por sí solas todas las operaciones de extremo a extremo.

### Acceso al sistema

**Inicio de sesión** — formulario de autenticación para los roles de usuario y administrador.

![Inicio de sesión de APARKA](docs/screenshots/login.png)

### Panel de administrador

**Dashboard** — capacidad, ocupación, disponibilidad y ranking de zonas.

![Dashboard administrativo](docs/screenshots/dashboard.png)

| Registro de entradas | Registro de salidas |
|---|---|
| ![Registro de entradas](docs/screenshots/entradas_admin.png) | ![Registro de salidas](docs/screenshots/salidas_admin.png) |

**Flujo vehicular** — historial de ingresos y salidas con filtros por fecha, zona y tipo.

![Historial de flujo vehicular](docs/screenshots/flujo_vehicular.png)

**Zonas y flujo** — indicadores, mapa de calor, gestión de zonas y cajeros.

![Mapa de calor y gestión de zonas](docs/screenshots/mapa_calor.png)

| Gestión de zonas y resumen por nivel | Cajeros y módulos de pago |
|---|---|
| ![Gestión de zonas](docs/screenshots/gestion_zonas.png) | ![Cajeros y módulos de pago](docs/screenshots/cajeros.png) |

**Perfil de administrador** — información personal, fotografía y opciones de cambio de contraseña.

![Perfil de administrador](docs/screenshots/perfil_admin.png)

### Panel de usuario

**Zonas disponibles** — consulta de ocupación, capacidad, tarifas y acceso al registro vehicular.

![Zonas disponibles para el usuario](docs/screenshots/zonas_usuario.png)

**Registrar ingreso vehicular** — formulario de placa con una zona previamente seleccionada.

![Registro de ingreso vehicular de usuario](docs/screenshots/ingreso_usuario.png)

**Perfil de usuario** — información personal, fotografía y opciones de seguridad de la cuenta.

![Perfil de usuario](docs/screenshots/perfil_usuario.png)

## 🧰 Tecnologías

- **Java 17** como objetivo de compilación del proyecto; JDK compatible (validado localmente con JDK 24).
- **Jakarta Servlets, JSP/JSTL**, HTML, CSS y JavaScript.
- **MySQL** mediante JDBC y consultas parametrizadas con `PreparedStatement`.
- **Apache Maven**, empaquetado **WAR** y **Apache Tomcat 11**.
- **JUnit 5** y Maven Surefire para pruebas automatizadas.

### Librerías utilizadas

| Librería | Aplicación en APARKA |
|---|---|
| Google Guava | `Preconditions` en `ValidacionUtil` |
| Apache Commons Lang / Validator | `StringUtils` y `EmailValidator` en `ValidacionUtil` |
| Apache POI | `XSSFWorkbook` en `ReporteExcelServlet` |
| Logback / SLF4J | Configuración de registros y eventos de aplicación |
| jBCrypt | Hash y verificación de contraseñas en `PasswordUtils` |

Las dependencias se administran desde `pom.xml`.

## 🏗️ Arquitectura

```text
src/
├── main/
│   ├── java/com/aparka/
│   │   ├── controller/  # Servlets y solicitudes HTTP
│   │   ├── service/     # Servicios de aplicación
│   │   ├── dao/         # Acceso a datos
│   │   ├── model/       # Entidades y modelos
│   │   └── util/        # Validación, conexión y seguridad
│   └── webapp/
│       ├── views/       # Vistas JSP
│       ├── css/         # Estilos
│       └── js/          # Comportamiento del cliente
└── test/java/           # Pruebas JUnit
```

### Patrón MVC (Modelo–Vista–Controlador)

APARKA organiza la aplicación web por responsabilidades:

- **Modelo:** `com.aparka.model` incluye `Zona`, `Entrada` y `UsuarioSistema`, que representan información y, cuando corresponde, reglas de negocio.
- **Vista:** las páginas JSP y los recursos HTML/CSS/JavaScript presentan la información y los formularios.
- **Controlador:** los Servlets de `com.aparka.controller`, como `LoginServlet`, `RegistrarEntradaServlet` y `GestionZonaServlet`, reciben solicitudes HTTP y coordinan respuestas.

Esta separación facilita mantener la presentación independiente de los datos y del procesamiento de solicitudes.

### Patrón DAO (Data Access Object)

El paquete `com.aparka.dao` centraliza el acceso a MySQL. Por ejemplo, `ZonaDAO` implementa `listarTodas()`, `obtenerPorId()`, `insertar()`, `actualizar()` y `eliminar()` usando JDBC.

- Las consultas con parámetros utilizan `PreparedStatement`, por ejemplo `WHERE ZonaId = ?` con `ps.setInt(1, zonaId)`.
- `try-with-resources` cierra conexiones, sentencias y resultados.
- `mapear(ResultSet)` transforma las filas recuperadas en objetos `Zona`.
- `listarTodas()` calcula la ocupación real contando entradas sin salida (`TieneSalida = 0`), en lugar de depender de un contador manual almacenado en la tabla de zonas.

**Evidencia:** [`ZonaDAO.java`](src/main/java/com/aparka/dao/ZonaDAO.java).

### Principios SOLID: aplicación verificable

**Responsabilidad única (SRP)** es el principio SOLID que se puede ilustrar directamente con las clases revisadas:

| Clase | Responsabilidad principal |
|---|---|
| [`Zona.java`](src/main/java/com/aparka/model/Zona.java) | Representar una zona y calcular su porcentaje de ocupación y nivel de flujo. |
| [`ZonaService.java`](src/main/java/com/aparka/service/ZonaService.java) | Ofrecer operaciones de gestión de zonas y delegar la persistencia. |
| [`ZonaDAO.java`](src/main/java/com/aparka/dao/ZonaDAO.java) | Ejecutar consultas SQL y mapear los resultados a objetos del modelo. |

Por ejemplo, `ZonaService.crearZona(z)` delega en `zonaDAO.insertar(z)`: el servicio no contiene la instrucción SQL del `INSERT`. La distribución entre modelo, servicio y persistencia es **coherente con SRP** y mejora la organización y mantenibilidad.

**Alcance de la evidencia:** no se declara una implementación integral de los cinco principios SOLID. En particular, `ZonaService` instancia directamente `new ZonaDAO()`, por lo que este ejemplo no acredita inversión de dependencias (DIP). Otros principios deben demostrarse con clases específicas si el equipo los presenta en la sustentación.

### Pruebas unitarias y metodología TDD

El equipo identifica **TDD (Test-Driven Development)** como parte de su enfoque de desarrollo. En el repositorio se encuentran pruebas automatizadas con **JUnit 5**, que permiten comprobar comportamientos esperados y casos límite. La presencia de pruebas finales no acredita por sí sola el orden histórico **Red → Green → Refactor**.

**Ejemplo comprobado:** [`ZonaTest.java`](src/test/java/com/aparka/model/ZonaTest.java) valida los métodos `getPorcentajeOcupacion()` y `getNivelFlujo()` de [`Zona.java`](src/main/java/com/aparka/model/Zona.java):

| Escenario probado | Resultado esperado |
|---|---|
| 25 espacios ocupados de 50 | 50 % de ocupación |
| Capacidad total de 0 | 0 %, sin división por cero |
| 39 % de ocupación | `BAJO` |
| 40 % de ocupación | `MEDIO` |
| 79 % de ocupación | `MEDIO` |
| 80 % de ocupación | `ALTO` |
| 100 % de ocupación | `ALTO` |

Las pruebas usan `@Test`, `@ParameterizedTest`, `@CsvSource` y `assertEquals`; la clase `ZonaTest` aporta **10 ejecuciones** a la batería registrada. La clase de pruebas fue incorporada al historial en el commit [`12aaa16`](https://github.com/Nicolas130599/AparkaProyecto/commit/12aaa16), del **25/09/2026**. Ese commit demuestra su incorporación, pero no por sí mismo que la prueba antecediera a la implementación.

**Resultado global registrado:** 80 pruebas aprobadas, 0 fallos, 0 errores y 0 omitidas, al ejecutar `mvn clean verify` el 23/10/2026. Para sustentar el ciclo TDD estricto, el equipo deberá mostrar evidencia adicional de la secuencia prueba fallida → implementación mínima → refactorización, si la conserva.

## 🔐 Seguridad

Controles implementados en el código:

- **BCrypt:** almacenamiento de contraseñas como hashes y verificación mediante `PasswordUtils`.
- **CSRF:** validación de tokens en las rutas POST protegidas por `CsrfFilter`.
- **Validación de entradas:** reglas de formato y contenido en `ValidacionUtil`.
- **Autorización por rol:** `RoleFilter` restringe las rutas `/admin/*`.
- **Sesiones:** renovación del identificador de sesión tras autenticación exitosa.
- **Intentos de acceso:** limitación de intentos fallidos de inicio de sesión.
- **Base de datos:** consultas parametrizadas y operaciones transaccionales para controlar la capacidad de las zonas.
- **Secretos:** la conexión MySQL requiere variables de entorno, sin credenciales fijas en `ConexionBD`.

**Alcance:** los controles anteriores no equivalen a una auditoría de seguridad completa. Deben seguir revisándose los permisos de cada endpoint, la configuración de cookies/sesiones, el despliegue y la gestión de secretos.

> **Advertencia sobre el historial:** versiones anteriores del repositorio público contenían credenciales. Retirarlas del código actual no las elimina de los commits históricos. Es necesario **revocar o rotar** cualquier secreto expuesto, incluidas credenciales MySQL y SMTP. No reescribir el historial compartido sin coordinarlo con el equipo.

## 🧪 Pruebas automatizadas

Ejecutar desde la raíz del proyecto:

```bash
mvn clean verify
```

**Última ejecución verificada: 23 de octubre de 2026.**

| Clase de prueba | Pruebas | Fallos | Errores |
|---|---:|---:|---:|
| `EntradaDAOTest` | 3 | 0 | 0 |
| `MapaCalorTest` | 6 | 0 | 0 |
| `ResumenNivelTest` | 3 | 0 | 0 |
| `ZonaTest` | 10 | 0 | 0 |
| `FotoPerfilUtilTest` | 7 | 0 | 0 |
| `LoginAttemptLimiterTest` | 6 | 0 | 0 |
| `ValidacionUtilTest` | 45 | 0 | 0 |
| **Total** | **80** | **0** | **0** |

Resultado: **BUILD SUCCESS**, con **0 pruebas omitidas**. La compilación generó `target/AparkaProyecto.war`. Algunas pruebas requieren una base MySQL de pruebas aislada (`parking_test`); **no ejecutar pruebas que escriben datos contra la base de demostración compartida o una base de producción**.

## 🚀 Instalación local para el equipo

Esta guía permite reproducir el entorno de demostración validado el **23/10/2026**. Cada integrante ejecuta **su propia instancia de MySQL**: clonar GitHub no conecta automáticamente las computadoras a una base de datos remota.

### Requisitos

- JDK compatible con el objetivo Java 17 (se utilizó JDK 24 en la validación local).
- Apache Maven (se utilizó Maven 3.9.16).
- Apache Tomcat 11 (se utilizó Tomcat 11.0.24).
- MySQL Server compatible (se utilizó MySQL 9.5, puerto local `3307`) y, opcionalmente, MySQL Workbench.
- Git y un editor como Visual Studio Code.
- Configuración SMTP únicamente para funcionalidades que envían correos.

### 1. Clonar el repositorio

```powershell
git clone https://github.com/Nicolas130599/AparkaProyecto.git
cd AparkaProyecto
```

### 2. Importar la base de datos de demostración

El archivo **completo de estructura y datos** está incluido en el repositorio:

```text
database/APARKA_BD_100_PORCIENTO.sql
```

En **MySQL Workbench**:

1. Conectarse a la instancia local de MySQL con un usuario que pueda crear bases y tablas.
2. Ir a **Server → Data Import**.
3. Seleccionar **Import from Self-Contained File** y elegir `database/APARKA_BD_100_PORCIENTO.sql`.
4. Dejar vacío **Default Target Schema** si el script ya contiene `CREATE DATABASE` y `USE`.
5. Hacer clic en **Start Import** y verificar **Import Completed**.
6. Actualizar la lista de esquemas y comprobar que existe `APARKA_BD_100%`.

El archivo importado en la validación contenía **11 tablas** y **10 usuarios de demostración**. El script incluye sentencias que pueden **eliminar y recrear tablas**: importar solo en un entorno de prueba y respaldar cualquier base existente antes de ejecutarlo. No se requiere ejecutar `schema (1).sql` adicionalmente para este entorno de demostración.

Para comprobar la importación desde MySQL Workbench:

```sql
USE `APARKA_BD_100%`;
SHOW TABLES;
SELECT COUNT(*) AS total_usuarios FROM usuariosistema;
```

> El nombre real del esquema contiene `%`. En instrucciones SQL se escribe literalmente `APARKA_BD_100%`; en la URL JDBC de esta instalación se utiliza `%25`.

### 3. Crear el usuario MySQL de la aplicación

El archivo SQL **no crea automáticamente** el usuario de conexión `aparka_app` ni sus permisos. En MySQL Workbench, conectado como administrador, ejecutar lo siguiente **solo si el usuario aún no existe**:

```sql
CREATE USER 'aparka_app'@'localhost' IDENTIFIED BY 'CAMBIAR_POR_CONTRASENA_LOCAL_SEGURA';
GRANT SELECT, INSERT, UPDATE, DELETE
ON `APARKA_BD_100%`.* TO 'aparka_app'@'localhost';
SHOW GRANTS FOR 'aparka_app'@'localhost';
```

Reemplazar el texto de ejemplo por una contraseña segura **propia de cada equipo**. Si `aparka_app` ya existe, no repetir `CREATE USER`; comprobar sus permisos y añadir el `GRANT` si hace falta. No publicar contraseñas reales en el repositorio.

### 4. Configurar variables de entorno

La clase [`ConexionBD.java`](src/main/java/com/aparka/util/ConexionBD.java) exige tres variables de entorno, sin credenciales ni base predeterminadas:

| Variable | Descripción |
|---|---|
| `APARKA_DB_URL` | URL JDBC hacia la instancia y base local |
| `APARKA_DB_USER` | Usuario MySQL de la aplicación |
| `APARKA_DB_PASSWORD` | Contraseña de ese usuario MySQL |

En **PowerShell**, desde la carpeta del proyecto:

```powershell
$env:APARKA_DB_URL = 'jdbc:mysql://localhost:3307/APARKA_BD_100%25'
$env:APARKA_DB_USER = 'aparka_app'
$env:APARKA_DB_PASSWORD = Read-Host 'Contraseña MySQL de aparka_app'
```

**Ajustar el puerto**: utilizar `3306` si esa es la configuración local, en vez de `3307`. El valor de `APARKA_DB_PASSWORD` es la contraseña **MySQL de `aparka_app`**, no la contraseña del login web.

Las variables definidas con `$env:` permanecen en la **sesión actual de PowerShell**. Iniciar Tomcat desde esa misma sesión para que herede los valores.

### 5. Compilar y generar el archivo WAR

```powershell
mvn clean package -DskipTests
```

Si aparece `BUILD SUCCESS`, el resultado se encuentra en:

```text
target/AparkaProyecto.war
```

Se omiten las pruebas en esta compilación inicial porque `EntradaDAOTest` exige una base de pruebas separada denominada `parking_test`, con su configuración correspondiente. **No cambiar el test para que escriba sobre `APARKA_BD_100%`.** Ejecutar la batería completa con `mvn clean verify` cuando el entorno de pruebas aislado esté preparado.

### 6. Desplegar e iniciar Tomcat 11 (Windows)

En PowerShell, **manteniendo la misma sesión que contiene las variables MySQL**, ajustar la ruta de Tomcat según la instalación:

```powershell
$env:CATALINA_HOME = 'C:\apache-tomcat-11.0.24'
$env:CATALINA_BASE = $env:CATALINA_HOME

Copy-Item '.\target\AparkaProyecto.war' "$env:CATALINA_HOME\webapps\AparkaProyecto.war" -Force
& "$env:CATALINA_HOME\bin\catalina.bat" run
```

Esperar el mensaje `Server startup in [...] milliseconds`. Mantener abierta esa terminal mientras se utiliza la aplicación.

Abrir en el navegador:

**http://localhost:8080/AparkaProyecto/**

Si se despliega una versión nueva, detener Tomcat antes de reemplazar el WAR y verificar que se despliegue la versión actualizada. El archivo WAR **no se copia automáticamente** a Tomcat al compilar con Maven.

### 7. Acceso con cuentas de demostración

Estas credenciales pertenecen al **login web de APARKA**, no a MySQL:

| Rol | Usuario | Contraseña |
|---|---|---|
| Administrador | `admin` | `AparkaDemo2026!` |
| Usuario estándar | `demo` | `AparkaDemo2026!` |

Usar únicamente con la base de demostración importada. **No reutilizar estas credenciales en un sistema público o de producción.**

### 8. Verificaciones funcionales registradas

El 23/10/2026 se comprobó localmente:

- Importación de **11 tablas** y presencia de **10 cuentas** de demostración.
- Acceso a MySQL con `aparka_app` y permisos sobre `APARKA_BD_100%`.
- Compilación con **BUILD SUCCESS** y generación de `AparkaProyecto.war`.
- Inicio de Tomcat 11 y acceso al formulario de login.
- Autenticación con la cuenta de demostración `admin`.
- Carga del Dashboard, Entradas, Salidas, Flujo vehicular, Zonas y flujo, Gestión de zonas, Cajeros y Mi perfil.
- Registro de una entrada vehicular de prueba, reflejada en el Dashboard: **ocupados de 5 a 6**, **disponibles de 180 a 179** y **Zona Centro de 3/50 a 4/50**.

La visualización de un formulario **no implica** que todas sus acciones se hayan probado. En particular, la operación de salida, las modificaciones de zonas y cajeros y la exportación Excel requieren verificaciones funcionales adicionales.

### 9. Solución de problemas frecuentes

| Situación | Qué revisar |
|---|---|
| `Falta configurar la variable de entorno obligatoria` | Definir las tres variables `APARKA_DB_*` en la sesión desde la que se inicia Tomcat. |
| `Access denied for user 'aparka_app'` | Verificar la contraseña MySQL y los permisos sobre `APARKA_BD_100%`. |
| `Unknown database` | Importar el SQL y comprobar el nombre exacto del esquema y el puerto. |
| `CATALINA_HOME environment variable is not defined correctly` | Establecer `CATALINA_HOME` y `CATALINA_BASE` con la ruta real de Tomcat. |
| HTTP 404 en `/AparkaProyecto/` | Confirmar que el WAR esté en `webapps`, revisar los logs de Tomcat y el contexto desplegado. |
| Fallos en `EntradaDAOTest` | Configurar una base de pruebas aislada `parking_test`; nunca ejecutar esas pruebas contra la base compartida. |

Para correo SMTP, configurar `APARKA_EMAIL_PASSWORD` de forma segura cuando corresponda. El envío de correo debe verificarse por separado.

## 🌿 Control de versiones

- Repositorio: [Nicolas130599/AparkaProyecto](https://github.com/Nicolas130599/AparkaProyecto).
- Rama principal: `main`.
- Pull Request [#3 — Seguridad CSRF, validaciones y pruebas](https://github.com/Nicolas130599/AparkaProyecto/pull/3): **fusionado y cerrado el 23/10/2026**.
- Los cambios se verificaron en `main` con 80 pruebas JUnit satisfactorias.
- El SQL de demostración se agregó al repositorio en el commit `088ca8a` (`database/APARKA_BD_100_PORCIENTO.sql`).

## 📚 Documentación académica

Los diagramas UML, las evidencias y los anexos del APF3 pueden entregarse por separado. Deben reflejar las funcionalidades y el código realmente presentes en el repositorio.

---

**APARKA · Proyecto académico de Ingeniería de Sistemas.**
