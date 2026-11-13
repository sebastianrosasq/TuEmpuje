# Identidad y reglas de datos

Esta carpeta contiene la configuración de Firebase Authentication y Firestore para TuEmpuje, sus reglas, una función protegida y las pruebas locales

La aplicación puede usar el proyecto ficticio `demo-tuempuje` mediante emuladores o el proyecto de desarrollo `tuempuje-dev-e1835b` en la nube, ambos entornos son para datos ficticios

## Requisitos

- Node.js 24.12 o superior y npm
- Java 21 o superior para los emuladores actuales de Firebase
- Los puertos locales 9099, 8080 y 5001 disponibles

Instalar dependencias desde esta carpeta:

```powershell
npm ci
npm ci --prefix functions
```

Iniciar los emuladores de Authentication, Firestore y Functions:

```powershell
npm run emulators
```

El emulador Android puede acceder al equipo anfitrión mediante `10.0.2.2`, con Authentication en el puerto `9099`, Firestore en el `8080` y Functions en el `5001`

Ejecutar las pruebas de reglas, con el puerto 8080 libre:

```powershell
npm run test:rules
```

El comando inicia Firestore, ejecuta los casos y detiene el emulador al finalizar, las cuentas y los documentos son ficticios y se pierden al detener el emulador si no se configura una exportación

En Windows, si el Java predeterminado es anterior a la versión requerida y Android Studio incluye uno compatible, se puede establecer `JAVA_HOME` en la terminal antes de iniciar los emuladores

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

No es necesario iniciar sesión en Firebase para estas pruebas con el proyecto `demo-tuempuje`, no se ejecuta ningún despliegue a la nube

## Proyecto de desarrollo en la nube

El proyecto [TuEmpuje Desarrollo](https://console.firebase.google.com/project/tuempuje-dev-e1835b/overview) tiene habilitados Authentication con correo y contraseña y Google, además de una base Firestore con las reglas de este repositorio

La base de desarrollo se creó automáticamente en `nam5`, una región multirregional de Estados Unidos, la ubicación de producción se decidirá por separado y no se ha habilitado la facturación durante esta configuración

Para compilar Android con este entorno, una persona con acceso al proyecto debe descargar la configuración de la aplicación `pe.tuempuje.app` desde Firebase Console y guardarla en `android/app/google-services.json`

Después, desde `infra/` ejecutar `npm ci` y `npm run android:config`, el segundo comando genera `android/firebase.local.properties` y no reemplaza un archivo existente

Desde `android/` compilar con `./gradlew.bat assembleDebug '-PtuempujeBackend=cloud'`, este APK usa internet y puede probarse en un emulador o teléfono físico, la compilación sin esa opción sigue utilizando los emuladores locales

Los dos archivos locales de configuración están excluidos de Git, la configuración de Firebase contiene identificadores de aplicación y no debe confundirse con una clave privada o una contraseña, las cuentas del equipo deben recibir sus propios permisos en el proyecto

El acceso con Google necesita que la huella SHA-1 de la clave que firma el APK esté registrada en Firebase y que `google-services.json` incluya el cliente web OAuth generado por el proveedor, las claves de firma de otras máquinas o de una versión de lanzamiento deben registrarse por separado

Los cambios posteriores a las reglas deben comprobarse en el emulador y desplegarse de forma explícita al proyecto correspondiente, la configuración actual no habilita una versión de producción

Para preparar oportunidades y una postulación de ejemplo en el proyecto de desarrollo, ejecutar `node seed-demo.mjs` desde `infra/`, el comando identifica los avisos como ejemplos y guarda las cuentas locales en `.env.demo.local`, un archivo excluido de Git

## Dependencias de desarrollo

Las herramientas de esta carpeta se utilizan para ejecutar emuladores y pruebas, no se incluyen en la aplicación Android

La auditoría de npm del 11 de noviembre de 2026 registró 14 avisos moderados en dependencias transitivas de estas herramientas, sin avisos altos ni críticos

El ajuste de `@grpc/grpc-js` en `overrides` corrige avisos altos de una dependencia de pruebas, las reglas se volvieron a comprobar después del cambio y debe revisarse el ajuste cuando Firebase actualice su dependencia

Los avisos restantes no tienen una corrección automática compatible en el árbol actual, por lo que no se aplica `npm audit fix --force` sin revisar sus cambios

## Datos y autorizaciones

| Ruta | Datos previstos | Acceso inicial |
|---|---|---|
| `users/{uid}` | Nombre, correo, DNI, celular y datos privados de habilidades, experiencia y disponibilidad | Solo el titular puede leer y actualizar su perfil, el correo y el identificador son inmutables |
| `opportunities/{id}` | Responsable, nombre, categoría, tareas, requisitos, condiciones, vacantes y estado | Una cuenta con perfil crea su propia oportunidad publicada, las cuentas autenticadas consultan publicaciones |
| `opportunities/{id}/applications/{uid}` | Identificador, nombre, habilidades, experiencia, disponibilidad, distrito, estudios, portafolio, estado y fecha | El postulante crea una solicitud propia y puede leerla, el dueño de la oportunidad puede revisar y seleccionar sus postulaciones |
| `opportunities/{id}/applications/{uid}/messages/{messageId}` | Mensajes asociados a una postulación | Solo quien postula y quien publicó pueden leer, el envío se bloquea si la postulación o la oferta se retiran |

La consulta de oportunidades compartidas debe incluir `where('status', '==', 'published')`, porque las reglas de Firestore no filtran resultados por cuenta propia

Las postulaciones usan el identificador del candidato como identificador del documento para impedir duplicados, no se permite postular a una oportunidad propia, la consulta de postulaciones personales utiliza un índice de grupo sobre `applicantId`

Las oportunidades y postulaciones están conectadas a Firestore en Android, los cambios de reglas deben desplegarse antes de probar funciones nuevas en la nube

Los perfiles son privados y no exponen el DNI, el celular ni el correo a otras cuentas, la postulación comparte el nombre, habilidades, experiencia, disponibilidad, distrito, estudios y portafolio confirmados contra su perfil

El DNI y el celular se guardan en el perfil privado, el formato se valida sin comprobar identidad ante RENIEC

La selección y el retiro de una candidatura seleccionada actualizan la postulación y las vacantes de forma conjunta, una publicación sin personas seleccionadas puede editarse o retirarse y el historial sigue visible para sus participantes

El contenido de pago de una oportunidad es informativo, no existe una integración de cobro ni se procesan datos bancarios

## Recuperación de contraseña

El enlace de Firebase se valida con `verifyPasswordResetCode` antes de permitir el cambio, Android reconoce únicamente el dominio y la ruta de acciones configurados

La asociación de Android App Links publicada en Hosting corresponde al certificado de depuración local, para preparar otra firma generar el archivo con `node hosting/assetlinks.mjs HUELLA_SHA256` y registrar la huella en Firebase

Hosting reemplaza el contenido del sitio en cada despliegue, por eso hay que conservar cualquier contenido existente antes de publicar la asociación, el archivo generado no se incluye en Git y una versión de lanzamiento necesitará su propia firma y verificación

## Gemini en desarrollo

Firebase AI Logic utiliza Gemini Developer API en el plan Spark, el nivel gratuito tiene límites y Google puede utilizar el contenido enviado para mejorar sus productos

La integración en la app está pendiente, antes de implementarla debe definirse qué información se envía, informar a la persona y exigir que revise los resultados antes de usarlos

Antes de un despliegue público debe revisarse el cumplimiento de las restricciones de edad y uso de Gemini para toda la aplicación

App Check está activado para Firebase AI Logic, cada instalación de depuración necesita registrar su propio token en Firebase, el token es secreto y no debe añadirse al repositorio ni distribuirse con el APK

La carga de CV como archivo sigue pendiente, Firebase Storage necesita el plan Blaze y se evaluará otra opción antes de elegir dónde guardar documentos
## Eliminación de cuentas

`functions/deleteAccount` exige una sesión reciente y elimina el perfil, las oportunidades propias con sus postulaciones, las postulaciones a otras oportunidades y los reportes relacionados, también ajusta las vacantes cuando corresponde

Durante el proceso se crea un bloqueo privado para impedir nuevas escrituras y permitir reintentos, una vez eliminada la cuenta queda un identificador temporal con `expireAt` para bloquear credenciales ya emitidas

Antes del despliegue debe configurarse y verificarse una política TTL de Firestore para el campo `deletionJobs.expireAt`, los bloqueos de operaciones incompletas no tienen expiración y requieren seguimiento operativo, TTL no elimina documentos de forma inmediata

La función solo se ha probado con emuladores y no está desplegada, su despliegue requiere el plan Blaze de Firebase y revisar sus costos, no activar el botón de eliminación como funcional en la nube hasta completar esa configuración y verificar el recorrido completo

```powershell
npm ci --prefix functions
npm run test:deletion
```

## Referencias

- [Pruebas de reglas con el emulador de Firestore](https://firebase.google.com/docs/firestore/security/test-rules-emulator)
- [Consultas y reglas de seguridad](https://firebase.google.com/docs/firestore/security/rules-query)
- [Configuración de Firebase Local Emulator Suite](https://firebase.google.com/docs/emulator-suite/install_and_configure)
