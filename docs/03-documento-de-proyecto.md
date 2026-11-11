# TuEmpuje: documento de proyecto

**Versión:** 0.2 · **Fecha:** 11 de noviembre de 2026

**Curso:** Programación móvil · **Modalidad:** proyecto grupal

**Área de aplicación inicial:** Arequipa, Perú · **Estado:** análisis y diseño

## 1. Presentación

TuEmpuje es una propuesta de plataforma para conectar a jóvenes y otras personas que buscan oportunidades de trabajo con profesionales y negocios que necesitan apoyo. La aplicación Android reunirá la publicación, consulta, postulación y selección. Una página web informativa futura presentará el proyecto y enlazará a la aplicación cuando esté disponible.

La primera versión se centrará en una situación concreta: una persona o un negocio publica una oportunidad con tareas y condiciones definidas, un candidato postula y quien publicó puede revisar su perfil y seleccionarlo. La idea incluye trabajos por turno, pequeños proyectos y ayudantías para profesionales.

Por ejemplo, un fotógrafo podría publicar una ayudantía para una sesión. El aviso indicaría qué hará la persona, cuánto durará el trabajo, quién proporciona el equipo y cuánto se pagará. Un estudiante interesado podría postular con su perfil. Los nombres, empresas y montos utilizados en prototipos serán ficticios y se presentarán como ejemplos.

## 2. Planteamiento del problema

Para una persona joven, demostrar que puede realizar un trabajo puede ser difícil cuando todavía no cuenta con experiencia laboral. Al mismo tiempo, algunos profesionales y negocios necesitan apoyo para actividades de duración limitada y deben explicar qué tareas ofrecen, bajo qué condiciones y qué conocimientos esperan.

Estas necesidades pueden encontrarse en portales laborales, redes y contactos directos. TuEmpuje propone reunir el proceso en un mismo flujo: publicar condiciones claras, recibir postulaciones, seleccionar y hacer seguimiento. La propuesta no elimina por sí sola la falta de empleo ni garantiza contrataciones; su utilidad dependerá de que existan oportunidades reales y de que ambas partes confíen en el servicio.

### Pregunta del proyecto

¿Cómo desarrollar una aplicación Android que facilite la publicación y gestión de oportunidades de trabajo entre jóvenes, profesionales y negocios?

### Antecedentes y alcance de la investigación

Existen plataformas que ya cubren partes de este proceso. [Workana](https://help.workana.com/hc/es/articles/360041829933--C%C3%B3mo-funcionan-los-proyectos-en-Workana) permite publicar proyectos y recibir propuestas; [UWork](https://www.u-work.org/index.html) presenta oportunidades por turnos vinculadas con la disponibilidad de estudiantes; y [Parker Dewey](https://www.parkerdewey.com/faq) conecta a empleadores con talento al inicio de su carrera mediante proyectos pagados.

El aporte que TuEmpuje buscará validar es la combinación de primeras experiencias, ayudantías publicadas por profesionales y oportunidades de negocios en un contexto local. No se afirma que esta combinación sea inédita ni que ya exista demanda suficiente en Arequipa.

## 3. Justificación

El proyecto tiene dos propósitos. En el curso, permite aplicar programación móvil, diseño de interfaces, persistencia de datos, autenticación, servicios compartidos y pruebas en un producto con varios actores. Fuera del curso, serviría como base para investigar si profesionales y negocios están dispuestos a publicar oportunidades pagadas y si los jóvenes encuentran valor en ellas.

La versión académica demostrará un proceso técnico con datos de prueba. Un piloto con personas y pagos reales requerirá, además, revisar la modalidad de contratación, la protección de datos, la moderación, la atención de incidentes y las condiciones del proveedor de pagos.

## 4. Objetivos

### Objetivo general

Desarrollar un prototipo funcional de TuEmpuje en Android que permita a jóvenes y otras personas encontrar y postular a oportunidades y a profesionales y negocios publicarlas y gestionar postulaciones desde la misma aplicación.

### Objetivos específicos e indicadores de cumplimiento

| Objetivo específico | Cómo se verificará en la entrega académica | Referencia |
|---|---|---|
| 1. Definir los actores, sus necesidades y las reglas de publicación y postulación. | Documento de requisitos con roles, permisos, reglas y criterios de aceptación. | RN-01 a RN-17. |
| 2. Diseñar las pantallas principales de Android para ambos roles. | Prototipo navegable que muestre el flujo de publicación, consulta, postulación y selección. | Mapa de pantallas y criterios de diseño. |
| 3. Implementar cuentas y perfiles básicos para postulantes y clientes. | Dos cuentas de prueba con perfiles diferenciados y acceso a sus funciones correspondientes. | RF-01 y RF-02. |
| 4. Implementar la publicación y consulta de oportunidades sobre una fuente de datos compartida. | Una cuenta crea una oportunidad en Android y otra la consulta con los mismos datos. | RF-03, RF-04 y PA-03. |
| 5. Permitir la postulación y gestión de candidatos. | Una cuenta postula en Android y el cliente visualiza y selecciona al candidato en la misma app. | RF-06 a RF-09; PA-04 a PA-06. |
| 6. Evaluar el funcionamiento del recorrido y los permisos básicos. | Registro de pruebas con resultados, errores encontrados y correcciones o limitaciones pendientes. | RNF-03; PA-01 a PA-08 y PA-10. |
| 7. Planificar las funciones necesarias para una evolución comercial. | Documento de pendientes sobre moderación, operación, pagos y publicación, sin declarar que están implementados. | RF-14 a RF-21; PA-09 y PA-11. |

Estos indicadores miden la entrega académica. Para evaluar un piloto posterior se registrarían, entre otros datos, oportunidades publicadas, postulaciones recibidas, contrataciones completadas y clientes que vuelven a publicar. No se asignan cifras de impacto todavía: faltan usuarios reales y mediciones.

## 5. Usuarios y necesidades

| Usuario | Necesidad principal | Acciones previstas |
|---|---|---|
| Postulante | Encontrar una oportunidad compatible con sus conocimientos y disponibilidad. | Crear perfil, explorar, filtrar, postular y consultar el estado. |
| Profesional | Encontrar apoyo para una tarea o proyecto. | Publicar ayudantías, revisar candidatos y seleccionar. |
| Negocio | Cubrir una necesidad de personal o un proyecto concreto. | Publicar oportunidades, revisar postulaciones y gestionar sus ofertas. |
| Administrador, etapa posterior | Revisar contenido y atender incidencias. | Consultar reportes, moderar publicaciones y registrar decisiones. |

Una misma persona podría publicar y postular desde su cuenta. Las capacidades que se le permitan en cada actividad dependerán de sus habilidades, experiencia, condiciones de seguridad y credenciales cuando corresponda. El sistema no asumirá que ser estudiante implica carecer de conocimientos ni que ser profesional implica tener una habilitación verificada.

## 6. Alcance

### Primera entrega académica

- Registro y acceso de prueba para postulante y cliente.
- Perfil básico con datos necesarios para participar.
- Publicación de oportunidades desde la app Android.
- Consulta de oportunidades desde la app Android.
- Postulación y revisión de candidatos.
- Selección y visualización del estado.
- Uso de una misma fuente de datos para el recorrido principal.
- Validaciones de formularios, estados de carga y error, y permisos básicos.

Esta es la meta priorizada. El tiempo real disponible y las exigencias del curso pueden requerir ajustes; cualquier función simulada debe identificarse en la demostración.

### Desarrollo posterior

Mensajería, notificaciones, historial de experiencias, reseñas, administración de reportes, verificación profesional y encargos para particulares. Una página informativa podrá presentar la aplicación y enlazar a su descarga cuando esté disponible. Las colas se considerarán solo cuando el lugar permita que otra persona ceda su posición.

### Asistencia con inteligencia artificial

Se ha elegido Gemini para una primera función de asistencia: convertir la descripción escrita por un cliente en un borrador de oportunidad. El cliente revisará y completará los datos antes de publicar. Se evaluará su integración una vez funcione el recorrido principal; su inclusión en la entrega dependerá del tiempo y de la configuración disponible.

En paralelo, cuando sea posible, se preparará un conjunto de ejemplos para estudiar un modelo propio de clasificación de categorías. Ese trabajo no modifica automáticamente Gemini ni implica entrenar un modelo desde cero. El enfoque y los criterios se desarrollan en el [plan de inteligencia artificial](06-plan-inteligencia-artificial.md).

### Pagos

La pasarela forma parte de la visión del proyecto y se abordará después de implementar el flujo principal. Primero se evaluarán proveedores y se probará una integración en entorno de pruebas. Los pagos reales dependen de las condiciones del proveedor, el tipo de servicio y los requisitos de operación.

## 7. Funcionamiento propuesto

### Flujo del postulante

1. Se registra e ingresa a su cuenta.
2. Completa su perfil, habilidades y disponibilidad.
3. Consulta oportunidades y filtra según sus intereses.
4. Revisa tareas, lugar, horario, pago y requisitos.
5. Envía su postulación y consulta su estado.
6. Si es seleccionado, revisa las condiciones acordadas para el trabajo.

### Flujo del cliente

1. Se registra como profesional o negocio.
2. Crea una oportunidad con tareas, requisitos, fecha, lugar, pago y número de vacantes.
3. Publica el aviso y recibe postulaciones.
4. Revisa candidatos y selecciona sin superar las vacantes disponibles.
5. Consulta el estado de la contratación.

### Flujo administrativo

En una etapa posterior, el administrador revisará reportes y publicaciones que necesiten moderación. Las acciones administrativas deben estar restringidas en el servidor y registrar quién realizó cada cambio.

## 8. Requisitos principales

| Código | Requisito | Resultado esperado |
|---|---|---|
| RF-01 | Registro e inicio de sesión. | Cada persona accede a su propia cuenta. |
| RF-02 | Gestión de perfil. | El usuario puede guardar sus datos y habilidades. |
| RF-03 | Publicación desde Android. | El cliente crea una oportunidad con campos obligatorios completos. |
| RF-04 | Consulta desde Android. | El postulante ve las oportunidades guardadas por la plataforma. |
| RF-05 | Búsqueda y filtros. | Los resultados coinciden con los criterios seleccionados. |
| RF-06 | Postulación. | Se registra una postulación por candidato y oportunidad, sin duplicados activos. |
| RF-07 | Revisión de candidatos. | El cliente consulta únicamente las postulaciones de sus ofertas. |
| RF-08 | Selección. | El cliente selecciona candidatos dentro del límite de vacantes. |
| RF-09 | Consulta de estado. | El postulante visualiza si su postulación fue enviada o seleccionada. |
| RF-10 | Condiciones del trabajo. | Se registran participantes, tareas y condiciones acordadas. |

Los requisitos completos, sus prioridades y los permisos están en [Requisitos y criterios de aceptación](01-requisitos.md).

## 9. Arquitectura y herramientas propuestas

| Parte | Herramienta propuesta | Función |
|---|---|---|
| App móvil | Kotlin, Android Studio y Jetpack Compose. | Interfaz Android para publicar, explorar, postular y seleccionar. |
| Estado y navegación | MVVM, flujo unidireccional, ViewModel y Navigation Compose. | Organizar la interacción entre pantallas y el estado de la app. |
| Persistencia en el dispositivo | Room sobre SQLite. | Conservar oportunidades guardadas y datos previamente consultados según la estrategia local. |
| Página informativa, posterior | Tecnología por definir. | Presentar el proyecto y enlazar a la app cuando esté disponible. |
| Identidad | Firebase Authentication. | Registro e inicio de sesión. |
| Datos | Cloud Firestore. | Almacenamiento compartido de perfiles, ofertas y postulaciones. |
| Reglas y lógica protegida | Firebase Security Rules y funciones de servidor cuando sean necesarias. | Restringir accesos y validar operaciones sensibles. |
| API REST | Retrofit, si se habilita un servicio HTTP concreto. | Consumir un servicio real sin duplicar el acceso de la app a Firestore. |
| Asistencia de IA | Gemini. | Proponer un borrador de oportunidad que el cliente revisa y edita. |
| Ubicación, widget y tareas diferidas | Google Maps SDK, Jetpack Glance y WorkManager, según disponibilidad. | Mejoras opcionales para ubicar oportunidades, consultar guardados y ejecutar tareas diferibles. |
| Versiones | Git. | Mantener historial y controlar cambios del proyecto. |
| Diseño | Herramientas de prototipado. | Revisar pantallas y recorridos antes de construir las interfaces. |
| Pagos, después | Proveedor por evaluar. | Procesar transacciones cuando se cumplan los requisitos técnicos y operativos. |

Firebase cuenta con emuladores locales para las pruebas y un proyecto de desarrollo en la nube para identidad y perfiles con datos ficticios. Antes de un piloto real se revisarán las exigencias del curso, los costos y la configuración de producción. Android Developers recomienda separar la interfaz y la capa de datos, emplear repositorios y organizar el estado de la interfaz de forma clara. [Recomendaciones de arquitectura Android](https://developer.android.com/topic/architecture/recommendations).

La aplicación de cada tema visto durante el semestre, sus prioridades y formas de comprobarlo se detallan en [Temas del curso y aplicación en TuEmpuje](05-temas-del-curso.md).

La aplicación Android no debe decidir por sí sola si una persona tiene permisos de administración, si una vacante está disponible o si un pago se completó. Esas operaciones requieren validación en el sistema de datos o en la lógica de servidor.

## 10. Estructura del proyecto

```text
TuEmpuje/
├── docs/       Documentación, requisitos, plan y criterios de diseño
├── android/    Aplicación Android en Kotlin
├── web/        Página informativa prevista para una etapa posterior
├── backend/    Funciones de negocio que deban ejecutarse en servidor
├── infra/      Configuración de acceso, reglas e índices
└── tests/      Pruebas integradas y escenarios compartidos
```

Esta es una organización propuesta. En la fecha de este documento solo existen los archivos de documentación; las carpetas de aplicación se crearán al empezar el desarrollo.

## 11. Desarrollo y cronograma

El trabajo se organizará en iteraciones diarias. Cada una tendrá un resultado comprobable: definir, diseñar, construir, probar y registrar los pendientes de una parte concreta. Al terminar el día se revisará si lo planificado cabe en el tiempo restante.

| Etapa | Resultado previsto |
|---|---|
| Análisis y diseño | Documento de proyecto, requisitos, flujos y prototipo. |
| Base técnica | Proyecto Android, fuente de datos y acceso. |
| Oportunidades | Publicación y consulta en Android. |
| Postulaciones | Envío, revisión y selección desde Android. |
| Pruebas y entrega | Verificación del recorrido, permisos, errores y presentación. |
| Evolución | Moderación, comunicaciones, pagos y condiciones para un piloto real. |

El detalle por día está en [Plan de trabajo y pruebas](02-plan-y-pruebas.md). El equipo priorizará las funciones según el tiempo disponible y el avance de cada etapa.

## 12. Evaluación y pruebas

Se comprobará el recorrido con datos ficticios: crear una oferta válida desde una cuenta, observarla desde otra, enviar una postulación y revisar y seleccionar al candidato desde la cuenta publicadora en Android. También se probará que no se publiquen formularios incompletos, que una persona no postule dos veces y que un cliente no acceda a los candidatos de otra publicación.

Cada prueba debe registrar escenario, pasos, resultado esperado, resultado obtenido y estado. Para la interfaz se revisará legibilidad, navegación y tamaño de controles. Android recomienda áreas táctiles de al menos 48 × 48 dp. [Guía de accesibilidad Android](https://developer.android.com/guide/topics/ui/accessibility/views/apps-views?hl=en).

Para seguridad se tomarán como referencia [OWASP MASVS](https://mas.owasp.org/MASVS/) en la app y [OWASP ASVS](https://asvs.owasp.org/) en el servidor. Utilizarlas como guía no equivale a una certificación de seguridad.

## 13. Riesgos y decisiones abiertas

| Tema | Situación actual | Acción necesaria |
|---|---|---|
| Tiempo | Plazo estimado de 7 a 10 días. | Priorizar el recorrido principal y ajustar diariamente. |
| Exigencias del curso | No se han confirmado todas las restricciones técnicas. | Verificar herramientas, fecha y criterios de evaluación. |
| Demanda local | No se ha probado que los clientes usarán TuEmpuje. | Conversar con profesionales y negocios y observar contrataciones reales en un piloto posterior. |
| Datos personales | Todavía no existe una política ni un sistema implementado. | Diseñar permisos y revisar la normativa antes de usar datos reales. |
| Pago y contratación | No hay proveedor ni modalidad comercial aprobada. | Analizarlo al final con el proveedor y asesoría correspondiente. |
| Nombre | TuEmpuje está pendiente de verificación de marca. | Consultar Indecopi, dominio y tiendas antes de publicar. |

## 14. Fuentes consultadas

- [Android Developers: arquitectura recomendada](https://developer.android.com/topic/architecture/recommendations).
- [Android Developers: accesibilidad](https://developer.android.com/guide/topics/ui/accessibility/views/apps-views?hl=en).
- [Workana: funcionamiento de proyectos](https://help.workana.com/hc/es/articles/360041829933--C%C3%B3mo-funcionan-los-proyectos-en-Workana).
- [UWork: información de la plataforma](https://www.u-work.org/index.html).
- [Parker Dewey: proyectos y funcionamiento](https://www.parkerdewey.com/faq).
- [Autoridad Nacional de Protección de Datos Personales: reglamento](https://www.gob.pe/institucion/anpd/campa%C3%B1as/128319-nuevo-reglamento-de-proteccion-de-datos-personales).
