<p align="center">
  <img src="docs/assets/tuempuje-icono.svg" alt="Símbolo de TuEmpuje" width="150">
</p>

# TuEmpuje

**Oportunidades de trabajo, primeras experiencias y colaboración profesional**

Arequipa, Perú · Proyecto de programación móvil

---

<p align="center">
  <img src="docs/assets/empu.png" alt="Empu, mascota de TuEmpuje" width="150">
</p>

Empu acompaña algunos momentos de la aplicación y expresa sus estados con gestos y movimientos

## El proyecto

TuEmpuje conecta a personas que buscan oportunidades de trabajo con profesionales y negocios que necesitan apoyo para tareas, campañas o proyectos

La propuesta se enfoca en facilitar las primeras experiencias laborales mediante oportunidades con condiciones claras y un proceso de postulación accesible

Un profesional puede publicar una ayudantía, revisar perfiles y seleccionar a alguien para su proyecto

Una persona que busca trabajo puede encontrar oportunidades acordes con sus habilidades, postular y consultar el estado de su solicitud

## Cómo funciona

```text
Publicar una oportunidad  →  Encontrar y postular  →  Revisar y seleccionar
       Profesional                 Postulante              Profesional
```

Cada publicación debe indicar las tareas, los conocimientos requeridos, el lugar, la duración y el pago ofrecido

La aplicación reúne en Android la publicación, consulta, postulación y selección de candidatos

Una página web de presentación podrá explicar el proyecto y dirigir a las personas a la aplicación cuando exista un enlace de instalación

## Componentes previstos

| Componente | Función | Tecnología propuesta |
|---|---|---|
| Aplicación móvil | Publicar oportunidades, postular y gestionar candidatos | Kotlin, Jetpack Compose y Material 3 |
| Página web | Presentar el proyecto y dirigir a la aplicación | Tecnología por definir |
| Identidad y datos | Gestionar cuentas y compartir información | Firebase Authentication y Cloud Firestore |
| Persistencia local | Conservar oportunidades guardadas en el dispositivo | Room y SQLite |
| Asistencia | Ayudar a redactar borradores de oportunidades | Gemini |

La integración de pagos se evaluará después de completar el recorrido principal

Las tecnologías pueden ajustarse según los requisitos del curso y las necesidades de implementación

## Estado

El proyecto se encuentra en **desarrollo**

El repositorio incluye una aplicación Android en desarrollo con navegación, cuenta, perfil, ajustes y guardados locales con Room

En Ajustes se puede elegir entre el estilo original y cinco estilos oscuros: Prisma, Pulso, Petróleo, Tinta y Horizonte. La selección se conserva en el dispositivo

Los estados vacíos de exploración, postulaciones y mensajes incluyen ilustraciones adaptadas a fondos claros y oscuros, Empu conserva su papel como mascota de la aplicación

Las cuentas y los perfiles pueden probarse con los emuladores locales o con un proyecto Firebase de desarrollo en la nube, utilizando datos ficticios

Las oportunidades, postulaciones y selección de candidatos están conectadas a Firestore en el entorno de desarrollo, el recorrido se verificó entre cuentas en emuladores locales y mediante el SDK en Firebase de desarrollo

El acceso con Google y la recuperación de contraseña están integrados en el proyecto de desarrollo y se probaron en un teléfono físico

La finalización de trabajos y el registro de reportes están incorporados, el equipo cuenta con una herramienta de línea de comandos para revisar reportes, su uso en la nube requiere credenciales administrativas

La recuperación de contraseña envía un enlace al correo, la app valida el enlace y solicita la nueva contraseña, la asociación publicada corresponde al certificado de depuración de este entorno

La eliminación completa de cuentas se preparó para el emulador mediante una función protegida, su despliegue y la configuración de expiración de bloqueos están pendientes, por lo que todavía no está disponible en la nube

Al crear la cuenta se elige un perfil personal o empresarial, después se puede corregir el tipo desde el perfil conservando el historial

El registro solicita nombre, correo, documento, celular y nacionalidad, admite DNI, carné de extranjería y pasaporte con validación de formato, el celular conserva el formato peruano y los documentos no verifican identidad

La búsqueda puede filtrarse por categoría, ubicación, modalidad, experiencia, fecha de publicación y vacantes, también puede ordenarse por intereses o por fecha

Las cuentas personales publican como particulares y las empresas usan los datos de su perfil empresarial, los datos compartidos de un negocio pueden retirarse de una oportunidad

Las oportunidades nuevas indican si el trabajo es presencial, híbrido o remoto, las publicaciones anteriores mantienen su información sin asignarles una modalidad que no declararon

Las personas pueden completar sus habilidades y disponibilidad antes de postular, retirar una candidatura, editar o retirar una publicación sin seleccionados y consultar el historial

Las nuevas publicaciones indican si requieren experiencia previa, la búsqueda permite filtrar las oportunidades que no la requieren y el formulario ofrece plantillas editables

El empleador marca un trabajo como finalizado y la persona seleccionada confirma su experiencia, los trabajos confirmados aparecen en Actividad y pueden recibir una referencia que su titular decide compartir o reportar

Las confirmaciones y referencias registran lo declarado por las partes, no verifican identidad ni acreditan pagos, las publicaciones anteriores que no indican experiencia requerida permanecen sin clasificar

El perfil permite guardar un CV en PDF con los datos registrados, al postular se puede elegir si se adjunta un PDF de hasta 700 kB para que lo consulte quien publicó la oportunidad

El asistente de Empu utiliza Gemini en el entorno de desarrollo para proponer oportunidades y sugerencias de CV, al publicar permite elegir las condiciones, reutilizar los datos del negocio y revisar una vista previa editable antes de confirmar, la persona autoriza el envío de su descripción y el formulario manual sigue disponible

El creador de CV ofrece seis diseños, muestra la primera página antes de exportar y permite añadir una foto opcional, guardar un borrador general o por oportunidad y generar un PDF, todavía no interpreta el contenido de archivos existentes

Una cuenta personal puede postular y publicar solicitudes de apoyo, una cuenta de empresa puede publicar y consultar oportunidades, los perfiles anteriores deben seleccionar su tipo al ingresar

TuEmpuje está dirigido a personas de 18 años o más, antes de abrir la aplicación al público se debe completar y verificar el control de edad de acceso conforme a las condiciones del proveedor de IA

La bandeja reúne las conversaciones asociadas a postulaciones, los avisos de mensajes y cambios de estado pueden activarse en ajustes

Los avisos revisan la actividad mediante WorkManager aproximadamente cada hora cuando hay conexión, Android puede retrasarlos por batería y la primera revisión establece una referencia sin avisar, no son mensajes push inmediatos

El envío de CV está implementado para archivos de hasta 700 kB y falta comprobar el recorrido completo entre dos teléfonos, la web, los pagos y una interfaz de moderación siguen pendientes

El sistema visual comparte avisos, botones y estados vacíos, con transiciones y una navegación que se adapta al texto ampliado, las pruebas y limitaciones están registradas en [Diseño y navegación](docs/10-diseno-y-navegacion.md)

La interfaz utiliza Material 3 Expressive con una versión preliminar de la biblioteca, por lo que sus dependencias deberán revisarse antes de una publicación real

## Ejecutar el prototipo Android

La compilación predeterminada utiliza los emuladores locales de Firebase, para probar las cuentas hay que iniciarlos siguiendo [Identidad y reglas de datos](infra/README.md)

Abrir la carpeta `android/` en Android Studio con JDK 17 y el SDK de Android 36 instalados, sincronizar Gradle y ejecutar la aplicación en el emulador de Android con API 26 o superior

La dirección de desarrollo `10.0.2.2` conecta el emulador Android con los servicios del equipo anfitrión, por lo que esta configuración de cuenta no funciona directamente en un teléfono físico

En Windows también se puede compilar desde `android/` con `./gradlew.bat assembleDebug`, el APK generado se encontrará en `android/app/build/outputs/apk/debug/`

Para compilar contra Firebase de desarrollo, preparar la configuración local según [Identidad y reglas de datos](infra/README.md) y ejecutar desde `android/`:

```powershell
./gradlew.bat assembleDebug '-PtuempujeBackend=cloud'
```

Ambas compilaciones usan la misma ruta de salida del APK, por lo que la última reemplaza a la anterior, la conexión a la nube está disponible solo en versiones de depuración y no constituye una configuración de producción

## Documentación

La [documentación del proyecto](docs/README.md) reúne los requisitos, la arquitectura, las decisiones de diseño y el plan de trabajo

La configuración de servicios, las reglas de acceso y las pruebas de seguridad se describen en [infraestructura](infra/README.md)

## Estructura actual

```text
TuEmpuje/
├── android/                Proyecto Android en Kotlin y Compose
├── infra/                  Emuladores, reglas y funciones de Firebase
├── design/empu-rive/       Recurso editable de Empu
├── docs/                   Documentación y recursos visuales
├── .editorconfig
├── .gitattributes
├── .gitignore
└── README.md
```

La web y los servicios de producción se incorporarán cuando comience su implementación

## Criterios de trabajo

- Mantener cambios relacionados en commits con mensajes breves y descriptivos
- Probar cada recorrido antes de darlo por terminado
- Identificar los datos de prueba y las funciones pendientes
- Mantener contraseñas, claves y datos personales fuera del repositorio
- Actualizar la documentación cuando cambien el alcance o la arquitectura
