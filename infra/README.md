# Identidad y reglas de datos

Esta carpeta contiene la configuración de Firebase Authentication y Firestore para TuEmpuje, junto con las reglas y sus pruebas

La aplicación puede usar el proyecto ficticio `demo-tuempuje` mediante emuladores o el proyecto de desarrollo `tuempuje-dev-e1835b` en la nube, ambos entornos son para datos ficticios

## Requisitos

- Node.js 24.12 o superior y npm
- Java 21 o superior para los emuladores actuales de Firebase
- Los puertos locales 9099 y 8080 disponibles

Instalar dependencias desde esta carpeta:

```powershell
npm ci
```

Iniciar los emuladores de Authentication y Firestore:

```powershell
npm run emulators
```

El emulador Android puede acceder al equipo anfitrión mediante `10.0.2.2`, con Authentication en el puerto `9099` y Firestore en el `8080`

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

## Dependencias de desarrollo

Las herramientas de esta carpeta se utilizan para ejecutar emuladores y pruebas, no se incluyen en la aplicación Android

La auditoría de npm del 11 de noviembre de 2026 registró 14 avisos moderados en dependencias transitivas de estas herramientas, sin avisos altos ni críticos

El ajuste de `@grpc/grpc-js` en `overrides` corrige avisos altos de una dependencia de pruebas, las reglas se volvieron a comprobar después del cambio y debe revisarse el ajuste cuando Firebase actualice su dependencia

Los avisos restantes no tienen una corrección automática compatible en el árbol actual, por lo que no se aplica `npm audit fix --force` sin revisar sus cambios

## Datos y autorizaciones

| Ruta | Datos previstos | Acceso inicial |
|---|---|---|
| `users/{uid}` | Identidad, nombre, correo y datos privados de habilidades, experiencia y disponibilidad | Solo el titular puede leer y actualizar su perfil, el correo y el identificador son inmutables |
| `opportunities/{id}` | Responsable, nombre, categoría, tareas, requisitos, condiciones, vacantes y estado | Una cuenta con perfil crea su propia oportunidad publicada, las cuentas autenticadas consultan publicaciones |
| `opportunities/{id}/applications/{uid}` | Identificador y nombre del postulante, estado y fecha | El postulante crea una solicitud propia y puede leerla, el dueño de la oportunidad puede revisar y seleccionar sus postulaciones |

La consulta de oportunidades compartidas debe incluir `where('status', '==', 'published')`, porque las reglas de Firestore no filtran resultados por cuenta propia

Las postulaciones usan el identificador del candidato como identificador del documento para impedir duplicados, no se permite postular a una oportunidad propia, la consulta de postulaciones personales utiliza un índice de grupo sobre `applicantId`

La creación de oportunidades y postulaciones en Android se implementará en sus sprints correspondientes, la existencia de estas reglas no significa que esos recorridos ya estén conectados

Los perfiles son privados y no exponen el correo ni los datos de experiencia a otras cuentas, la postulación comparte únicamente el nombre del candidato confirmado contra su perfil

La selección se realiza mediante una transacción que actualiza la postulación y el número de vacantes cubiertas de forma conjunta, las reglas y las pruebas impiden superar el límite, la edición y eliminación de oportunidades y postulaciones siguen sin habilitarse

El contenido de pago de una oportunidad es informativo, no existe una integración de cobro ni se procesan datos bancarios

## Referencias

- [Pruebas de reglas con el emulador de Firestore](https://firebase.google.com/docs/firestore/security/test-rules-emulator)
- [Consultas y reglas de seguridad](https://firebase.google.com/docs/firestore/security/rules-query)
- [Configuración de Firebase Local Emulator Suite](https://firebase.google.com/docs/emulator-suite/install_and_configure)
