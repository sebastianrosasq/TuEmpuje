<p align="center">
  <img src="docs/assets/tuempuje-icono.svg" alt="Símbolo de TuEmpuje: dos piezas forman una escalera" width="150">
</p>

# TuEmpuje

**Oportunidades de trabajo, primeras experiencias y colaboración profesional**

Arequipa, Perú · Proyecto de programación móvil

---

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

La primera versión reunirá en Android la publicación, consulta, postulación y selección de candidatos

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

El proyecto se encuentra en **desarrollo inicial**

El repositorio incluye una primera versión Android con navegación y datos ficticios en memoria

Las publicaciones y postulaciones del prototipo son de demostración y se pierden al cerrar el proceso, Firebase, la web, Gemini y los pagos aún no están configurados

La interfaz utiliza Material 3 Expressive con una versión preliminar de la biblioteca, por lo que sus dependencias deberán revisarse antes de una publicación real

## Ejecutar el prototipo Android

Abrir la carpeta `android/` en Android Studio con JDK 17 y el SDK de Android 36 instalados, sincronizar Gradle y ejecutar la aplicación en un emulador o dispositivo con Android 8 o superior

En Windows también se puede compilar desde `android/` con `./gradlew.bat assembleDebug`, el APK generado se encontrará en `android/app/build/outputs/apk/debug/`

## Documentación

| Documento | Contenido |
|---|---|
| [Documento de proyecto](docs/03-documento-de-proyecto.md) | Problema, justificación, objetivos y alcance |
| [Plan maestro](docs/00-plan-maestro.md) | Flujo general, arquitectura y etapas |
| [Arquitectura](docs/08-arquitectura.md) | Componentes, datos y decisiones técnicas |
| [Requisitos](docs/01-requisitos.md) | Reglas de negocio, permisos y criterios de aceptación |
| [Plan de trabajo y pruebas](docs/02-plan-y-pruebas.md) | Prioridades, entregables y escenarios de prueba |
| [Plan de 16 sprints](docs/09-sprints.md) | Objetivos, dependencias y criterios de aceptación |
| [Diseño y navegación](docs/10-diseno-y-navegacion.md) | Dirección visual, recorridos e interacciones |
| [Temas del curso](docs/05-temas-del-curso.md) | Aplicación de Kotlin, Compose, Room y Firebase |
| [Plan de inteligencia artificial](docs/06-plan-inteligencia-artificial.md) | Asistencia con Gemini y evaluación de un modelo propio |
| [Diseño de interfaces](docs/04-diseno.md) | Criterios de diseño y pantallas principales |
| [Guía de desarrollo](docs/07-desarrollo.md) | Organización del repositorio y forma de trabajo |

## Estructura actual

```text
TuEmpuje/
├── android/                Proyecto Android en Kotlin y Compose
├── docs/
│   ├── assets/
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

Los directorios de la web y los servicios se incorporarán cuando comience su implementación

## Criterios de trabajo

- Mantener cambios relacionados en commits con mensajes breves y descriptivos
- Probar cada recorrido antes de darlo por terminado
- Identificar los datos de prueba y las funciones pendientes
- Mantener contraseñas, claves y datos personales fuera del repositorio
- Actualizar la documentación cuando cambien el alcance o la arquitectura
