# Identidad y reglas de datos

Esta carpeta contiene la configuración local de Firebase Authentication y Firestore para TuEmpuje, junto con las reglas y sus pruebas

La aplicación se conecta al proyecto ficticio `demo-tuempuje` mediante los emuladores locales, sin desplegar servicios ni almacenar datos reales

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

## Dependencias de desarrollo

Las herramientas de esta carpeta se utilizan para ejecutar emuladores y pruebas, no se incluyen en la aplicación Android

La auditoría de npm del 11 de noviembre de 2026 registró 14 avisos moderados en dependencias transitivas de estas herramientas, sin avisos altos ni críticos

El ajuste de `@grpc/grpc-js` en `overrides` corrige avisos altos de una dependencia de pruebas, las reglas se volvieron a comprobar después del cambio y debe revisarse el ajuste cuando Firebase actualice su dependencia

Los avisos restantes no tienen una corrección automática compatible en el árbol actual, por lo que no se aplica `npm audit fix --force` sin revisar sus cambios

## Datos y autorizaciones

| Ruta | Datos previstos | Acceso inicial |
|---|---|---|
| `users/{uid}` | `uid`, `displayName` y `email` | Solo el titular puede leer y editar su nombre, el correo y el identificador son inmutables |
| `opportunities/{id}` | Responsable, título, tareas, ubicación, pago, duración, vacantes, estado y fecha | Una cuenta con perfil crea su propia oportunidad publicada, las cuentas autenticadas consultan publicaciones |
| `opportunities/{id}/applications/{uid}` | Identificador del postulante, estado y fecha | El postulante crea una solicitud propia y puede leerla, el dueño de la oportunidad puede revisar sus postulaciones |

La consulta de oportunidades compartidas debe incluir `where('status', '==', 'published')`, porque las reglas de Firestore no filtran resultados por cuenta propia

Las postulaciones usan el identificador del candidato como identificador del documento para impedir duplicados, no se permite postular a una oportunidad propia

La creación de oportunidades y postulaciones en Android se implementará en sus sprints correspondientes, la existencia de estas reglas no significa que esos recorridos ya estén conectados

Los perfiles son privados y no exponen el correo a otras cuentas, antes de mostrar información de candidatos deberá definirse una vista pública separada con datos mínimos y reglas propias

La edición o eliminación de oportunidades y postulaciones, la selección de candidatos y los cambios de vacantes permanecen bloqueados hasta implementar y probar una operación autorizada que resuelva la concurrencia de forma atómica

El contenido de pago de una oportunidad es informativo, no existe una integración de cobro ni se procesan datos bancarios

## Referencias

- [Pruebas de reglas con el emulador de Firestore](https://firebase.google.com/docs/firestore/security/test-rules-emulator)
- [Consultas y reglas de seguridad](https://firebase.google.com/docs/firestore/security/rules-query)
- [Configuración de Firebase Local Emulator Suite](https://firebase.google.com/docs/emulator-suite/install_and_configure)
