<p align="center">
  <img src="docs/assets/tuempuje-icono.svg" alt="Símbolo de TuEmpuje: dos piezas forman una escalera" width="150">
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

Las cuentas y los perfiles pueden probarse con los emuladores locales o con un proyecto Firebase de desarrollo en la nube, utilizando datos ficticios

Las oportunidades, postulaciones y selección de candidatos están conectadas a Firestore en el entorno de desarrollo, el recorrido se verificó entre cuentas en emuladores locales y mediante el SDK en Firebase de desarrollo

El acceso con Google está integrado y configurado en el proyecto de desarrollo, queda por probar el inicio de sesión completo con una cuenta de Google en un teléfono compatible

La finalización de trabajos y el registro de reportes están incorporados y sus reglas se desplegaron al proyecto de desarrollo, todavía falta una herramienta de moderación

La recuperación de contraseña envía un enlace al correo, la app puede validar el enlace y solicitar la nueva contraseña, la asociación publicada corresponde al certificado de depuración de este entorno y falta comprobar el recorrido en un dispositivo físico

La eliminación completa de cuentas se preparó para el emulador mediante una función protegida, su despliegue y la configuración de expiración de bloqueos están pendientes, por lo que todavía no está disponible en la nube

Al entrar se elige entre buscar trabajo y ofrecer oportunidades, después se inicia sesión o se crea una cuenta para continuar, el objetivo puede cambiarse desde el perfil

El registro solicita nombre, correo, DNI, celular y nacionalidad, la versión actual valida DNI y número móvil peruanos sin verificar identidad ante RENIEC

La búsqueda puede filtrarse por categoría, ubicación, modalidad remota y vacantes, solo el objetivo de ofrecer trabajo permite publicar

Las personas pueden completar sus habilidades y disponibilidad antes de postular, retirar una candidatura, editar o retirar una publicación sin seleccionados y consultar el historial

Gemini Developer API está habilitada en el proyecto de desarrollo, la asistencia para preparar CV y redactar oportunidades se implementará y verificará en una etapa posterior

La bandeja reúne las conversaciones asociadas a postulaciones, los avisos de mensajes y cambios de estado pueden activarse en ajustes

Los avisos revisan la actividad mediante WorkManager aproximadamente cada hora cuando hay conexión, Android puede retrasarlos por batería y la primera revisión establece una referencia sin avisar, no son mensajes push inmediatos

La carga y entrega de archivos CV, la web, los pagos y la moderación están pendientes, el proyecto de desarrollo se usa únicamente con datos de prueba

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

| Documento | Contenido |
|---|---|
| [Documento de proyecto](docs/03-documento-de-proyecto.md) | Problema, justificación, objetivos y alcance |
| [Plan maestro](docs/00-plan-maestro.md) | Flujo general, arquitectura y etapas |
| [Arquitectura](docs/08-arquitectura.md) | Componentes, datos y decisiones técnicas |
| [Identidad y reglas](infra/README.md) | Emuladores, modelo de datos y pruebas de acceso |
| [Requisitos](docs/01-requisitos.md) | Reglas de negocio, permisos y criterios de aceptación |
| [Plan de trabajo y pruebas](docs/02-plan-y-pruebas.md) | Prioridades, entregables y escenarios de prueba |
| [Plan de 16 sprints](docs/09-sprints.md) | Objetivos, dependencias y criterios de aceptación |
| [Referentes de empleo](docs/13-referentes.md) | Flujos consultados y decisiones de adaptación |
| [Diseño y navegación](docs/10-diseno-y-navegacion.md) | Dirección visual, recorridos e interacciones |
| [Asistencia y ubicación](docs/12-asistencia-y-ubicacion.md) | Funciones futuras de IA y alternativas para mapas |
| [Temas del curso](docs/05-temas-del-curso.md) | Aplicación de Kotlin, Compose, Room y Firebase |
| [Plan de inteligencia artificial](docs/06-plan-inteligencia-artificial.md) | Asistencia con Gemini y evaluación de un modelo propio |
| [Diseño de interfaces](docs/04-diseno.md) | Criterios de diseño y pantallas principales |
| [Guía de desarrollo](docs/07-desarrollo.md) | Organización del repositorio y forma de trabajo |

## Estructura actual

```text
TuEmpuje/
├── android/                Proyecto Android en Kotlin y Compose
├── infra/                  Emuladores, reglas y funciones de Firebase
├── design/empu-rive/       Recurso editable de Empu
├── docs/
│   ├── assets/
│   │   ├── empu.png
│   │   ├── prototipo-inicio.png
│   │   ├── prototipo-oscuro.png
│   │   ├── tuempuje-icono.svg
│   │   └── tuempuje-simbolo.svg
│   ├── 00-plan-maestro.md
│   ├── 01-requisitos.md
│   ├── 02-plan-y-pruebas.md
│   ├── 03-documento-de-proyecto.md
│   ├── 04-diseno.md
│   ├── 05-temas-del-curso.md
│   ├── 06-plan-inteligencia-artificial.md
│   ├── 07-desarrollo.md
│   ├── 08-arquitectura.md
│   ├── 09-sprints.md
│   ├── 10-diseno-y-navegacion.md
│   └── README.md
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
