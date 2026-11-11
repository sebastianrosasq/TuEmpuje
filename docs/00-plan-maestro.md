# Plan maestro de TuEmpuje

**Versión:** 0.2 de trabajo · **Fecha:** 11 de noviembre de 2026 · **Estado:** análisis y diseño · **Ámbito inicial:** Arequipa, Perú

Este documento reúne la base de producto, técnica y operativa. Complementos: [requisitos y criterios de aceptación](01-requisitos.md) y [plan diario y pruebas](02-plan-y-pruebas.md). Una propuesta o hipótesis identificada aquí no es una función ya construida, un hecho validado ni una promesa de una fecha de lanzamiento.

La formulación del problema, los objetivos y sus indicadores se desarrolla en el [documento de proyecto](03-documento-de-proyecto.md). Los [criterios de diseño](04-diseno.md) describen las pantallas y el recorrido prioritario. La [relación de temas del curso](05-temas-del-curso.md) explica dónde se usarían Room, MVVM, Retrofit y las demás herramientas estudiadas.

Se ha elegido Gemini como proveedor inicial de asistencia con IA. El [plan de inteligencia artificial](06-plan-inteligencia-artificial.md) describe la integración propuesta y el posible entrenamiento de un modelo propio en paralelo cuando existan tiempo y datos adecuados.

## 1. Resumen

**Qué es:** una plataforma de oportunidades que conecta personas, especialmente jóvenes que buscan primeras experiencias pagadas, con profesionales y negocios que necesitan apoyo en tareas, campañas o proyectos.

**Para quién:** jóvenes y otras personas que buscan trabajo; profesionales que desean contratar ayudantes; negocios que necesitan publicar oportunidades; administradores que revisan contenido y atienden incidencias.

**Por qué:** una persona puede tener habilidades sin experiencia laboral demostrable, mientras que quien ofrece trabajo puede necesitar ayuda de duración y alcance definidos. La demanda por la plataforma y la disposición a pagar siguen sin validar.

**Cómo:** una aplicación Android en Kotlin para publicar, consultar y gestionar oportunidades, respaldada por servicios compartidos que conserven información, reglas y permisos. Una página web de presentación podrá explicar el proyecto y dirigir a la descarga cuando esté disponible.

**Resultado académico deseado:** demostrar el recorrido completo de una oportunidad con datos de prueba: crear → consultar → postular → seleccionar → confirmar → finalizar → registrar experiencia.

**Resultado comercial posterior:** prestar el servicio con usuarios y pagos reales, después de implementar y verificar seguridad, condiciones legales, soporte, controles operativos y un proveedor adecuado.

## 2. Objetivo, valor y límites

### Objetivo general

Desarrollar un prototipo funcional de TuEmpuje para Android que permita a jóvenes y otras personas encontrar y postular a oportunidades y a profesionales y negocios publicarlas y gestionar postulaciones.

### Objetivos específicos

1. Identificar actores, necesidades y reglas para publicar, postular y seleccionar.
2. Diseñar interfaces móviles que comuniquen claramente las tareas y condiciones de cada oportunidad.
3. Implementar cuentas y perfiles básicos para postulantes y clientes, con acceso controlado según la función de cada usuario.
4. Implementar la publicación y consulta de oportunidades sobre una fuente de datos compartida entre usuarios de Android.
5. Permitir postular y revisar el estado de la postulación; permitir al cliente revisar y seleccionar candidatos para sus propias ofertas.
6. Probar el recorrido principal, la validación de datos y los permisos para documentar sus resultados y limitaciones.
7. Definir una ruta posterior para moderación, experiencia laboral y pagos, condicionada a tiempo disponible y validación operativa.

El objetivo del curso es construir y probar el sistema; demostrar impacto en la empleabilidad o rentabilidad exige un piloto posterior. Los indicadores y evidencias de cada objetivo se detallan en la sección 4 del documento de proyecto.

| Destinatario | Necesidad | Valor que se pretende comprobar |
|---|---|---|
| Joven o trabajador inicial | Oportunidades pagadas compatibles con sus habilidades y horarios. | Encontrar ofertas claras y construir experiencia demostrable. |
| Profesional | Ayuda concreta para un proyecto o evento. | Encontrar colaboradores disponibles y volver a contactarles. |
| Negocio | Cubrir tareas, turnos o proyectos. | Publicar una necesidad y revisar candidatos en un solo lugar. |
| Administración | Detectar anuncios problemáticos y atender incidencias. | Tener una cola de revisión y un registro de decisiones. |

**Propuesta de valor:** una oportunidad debe indicar qué se hará, quién contrata, qué conocimientos necesita, dónde y cuándo se realiza y cuánto se pagará. Para ayudantías debe quedar claro quién supervisa y qué funciones realizará cada persona.

**No validado:** usuarios activos, cantidad de oportunidades por día, contratación recurrente, disposición a pagar comisiones y rentabilidad. Las plataformas existentes muestran antecedentes de empleo y proyectos pagados, pero no prueban que TuEmpuje tendrá demanda local.

## 3. Modalidades y tipos de oportunidad

1. **Turno o campaña:** tareas durante una jornada o fechas definidas, por ejemplo atención en un evento.
2. **Proyecto por entregable:** resultado delimitado, por ejemplo edición de tres videos.
3. **Ayudantía de profesional:** tareas de apoyo con supervisión y responsabilidades descritas.
4. **Servicio profesional:** actividad de una persona con conocimientos y credenciales requeridas cuando corresponda.
5. **Encargo puntual:** futura ampliación para particulares; las colas solo se ofrecen donde el reemplazo esté permitido.

Una persona puede publicar y postular usando la misma cuenta. Estudiar una carrera no habilita automáticamente para todas sus actividades. La clasificación de una tarea depende de sus riesgos, competencias y requisitos aplicables.

### Ejemplo principal

Un fotógrafo publica: «Ayudante para una sesión de productos; ordenar equipo y fondos bajo indicaciones; cuatro horas; equipo proporcionado por el fotógrafo; pago y fecha indicados en la oferta». Una estudiante postula. El fotógrafo elige, acuerdan condiciones, realizan el trabajo y ambos pueden registrar su resultado. Si el cliente del fotógrafo existe, su relación con el fotógrafo es un acuerdo separado del acuerdo entre fotógrafo y ayudante.

## 4. Alcance por entregables

### A. Avance académico inicial

Meta propuesta: documentos, mapa de pantallas, arquitectura y primer recorrido visible ejecutable si el tiempo disponible lo permite. La demostración debe marcar claramente qué tiene datos simulados. No se promete que la investigación ni el diseño ya estén convertidos en código.

Entregables:

- Este plan, requisitos con identificadores y backlog priorizado.
- Boceto o interfaz Android inicial con listado y detalle de oportunidades.
- Boceto o interfaz Android para crear una oportunidad.
- Diagrama sencillo del flujo, modelo de información y roles.
- Registro del estado real: funcional, simulado o pendiente.

### B. Versión académica funcional

Flujo prioritario en Android: acceso y perfiles básicos; cliente publica una oportunidad; estudiante la consulta y postula; cliente ve postulaciones y selecciona; ambas partes pueden consultar estado. Usar una fuente de datos compartida y demostrarlo con cuentas de prueba.

Si queda tiempo: historial sencillo y notificaciones internas. La moderación y sus herramientas se definirán en una etapa posterior. Un inicio de sesión simulado o datos locales solo sirven para demostrar el recorrido y deben rotularse como tales.

### C. Versión previa a producción y piloto real

Autenticación y autorización reales para todas las superficies; protección de datos; moderación; trazabilidad; recuperación de cuenta; reportes y soporte; validación con profesionales y jóvenes; política de privacidad y condiciones; pruebas de seguridad y operación. La eventual aplicación de la normativa de colocación laboral y de protección de datos se revisa con las autoridades y asesoría competente.

### D. Etapa final de pagos

La integración de pagos se realizará **después de los flujos principales**. La primera integración será en entorno de prueba. Los pagos reales solo se habilitan después de validar elegibilidad del proveedor, la modalidad de contratación, las comisiones, las pruebas y los controles de operación. No se asumirá que la pasarela garantiza fondos, retención o reparto múltiple.

### Fuera del primer corte

Aplicación iOS, entrenamiento de un modelo propio, geolocalización continua, reconocimiento automático de credenciales, convenios formales con universidades, contratación masiva avanzada, múltiples países y garantías de pago. Son posibles evoluciones, no requisitos del primer incremento. Gemini podrá incorporarse después de que funcione el recorrido principal, si el tiempo y las credenciales lo permiten.

## 5. Recorrido general de la plataforma

```text
Profesional o negocio                      Joven o trabajador
        │                                         │
        ├─ Crea su perfil                         ├─ Crea su perfil
        ├─ Publica la oportunidad ───────────────►├─ Consulta y filtra
        │                                         ├─ Revisa condiciones
        │◄─────────────── postulación ────────────┤
        ├─ Revisa y selecciona                    │
        └─ Confirma condiciones ◄────────────────►┘
                         │
                   Trabajo en curso
                         │
                  Resultado registrado
                         │
         Experiencia y reseña cuando corresponda
```

**Flujos complementarios:** una publicación puede admitir varias vacantes; cada postulante seleccionado requiere su propia contratación. Un reporte detiene la publicación o abre una revisión según la situación y debe tener responsable. El estado de la contratación es independiente del estado de pago.

## 6. Mapa de pantallas

### Android: joven y profesional

1. Inicio y acceso.
2. Perfil y habilidades.
3. Inicio: oportunidades relevantes.
4. Búsqueda y filtros.
5. Detalle: tareas, persona contratante, pago, fecha y requisitos.
6. Postulación y confirmación.
7. Mis postulaciones y estados.
8. Trabajo o acuerdo seleccionado.
9. Perfil profesional e historial: posterior al recorrido básico.
10. Publicación y edición de oportunidades.
11. Lista y detalle de postulantes, selección y seguimiento.
12. Mensajes, notificaciones y reporte: según prioridad y disponibilidad.

### Web de presentación

Página informativa prevista para presentar la propuesta, explicar cómo funciona y dirigir a la descarga oficial de la app cuando esté disponible. No sustituye los recorridos de la aplicación.

### Administración

Las herramientas de revisión de ofertas, reportes, moderación y auditoría se definirán después del recorrido principal. Las acciones de administración se autorizan en el servidor, no solo en la interfaz.

## 7. Estructura de información

Entidades iniciales; los campos definitivos se ajustan al proveedor y diseño:

| Entidad | Contenido principal |
|---|---|
| Usuario | Identificador, datos de acceso y estado de cuenta. |
| Perfil | Nombre visible, habilidades, ubicación aproximada, disponibilidad y rol de participación. |
| Negocio o profesional | Nombre, titular o representante, datos de contacto y estado de verificación. |
| Oportunidad | Publicador, título, descripción, tareas, categoría, modalidad, lugar, horarios, remuneración, vacantes y estado. |
| Postulación | Oportunidad, postulante, mensaje, fecha y estado. |
| Contratación | Oportunidad, postulante seleccionado, contratante, condiciones aceptadas y estado propio. |
| Mensaje | Participantes, contenido, fecha y referencia al contexto. |
| Experiencia/reseña | Contratación finalizada, autor, destinatario, contenido y visibilidad. |
| Reporte | Motivo, usuario, contenido reportado, estado, responsable y resolución. |
| Pago | Referencia del proveedor, monto, moneda y estado; se incorpora al final. |
| Registro de auditoría | Acciones sensibles, responsable y fecha, sin incluir secretos ni datos innecesarios. |

**Reglas de integridad:** una oportunidad puede recibir muchas postulaciones; una cuenta no debe postular dos veces a la misma oferta activa; una contratación relaciona a un candidato y un contratante; no se seleccionan más personas que las vacantes; solo participantes acceden a su contratación y comunicaciones. Las operaciones simultáneas deben validarse de forma atómica en el servidor/base de datos.

## 8. Arquitectura propuesta

### Decisión provisional

| Componente | Propuesta | Motivo | Alternativa / límite |
|---|---|---|---|
| Android | Kotlin + Jetpack Compose, Material 3, Navigation Compose y ViewModel con flujo unidireccional de datos. | Aprovecha los temas del curso y separa interfaz, estado y datos. | Las interfaces se implementarán y probarán en Android. |
| Web de presentación | Landing responsive, tecnología por definir. | Explica TuEmpuje y dirige a la app cuando exista un enlace de descarga. | No incluye publicación ni gestión de postulaciones; no es prioritaria para el recorrido académico. |
| Autenticación y datos | Firebase Authentication + Cloud Firestore. | Servicio administrado y SDK disponibles para Android. | Si el curso exige servidor propio, evaluar Ktor + PostgreSQL. Configuración y costos se verifican antes de usar cuentas reales. |
| Datos locales Android | Room sobre SQLite para oportunidades guardadas y caché local controlada. | Permite aplicar persistencia estudiada y consultar datos disponibles con mala conexión. | Firestore también tiene caché propia; el repositorio debe definir cuál se utiliza para cada lectura y cómo se actualiza. |
| API HTTP Android | Retrofit para una API REST específica cuando se defina un servicio propio o externo. | Permite aplicar el consumo de API aprendido en el curso. | No se necesita Retrofit para acceder a Firestore mediante su SDK; no duplicar escrituras de oportunidades. |
| Asistencia con IA | Gemini para sugerir un borrador de oportunidad que revisará el cliente. | Ayuda a estructurar tareas y detectar campos faltantes. | Integración pendiente. Si falla, el formulario manual permanece disponible. |
| Ubicación | Selección manual de distrito; mapa con Google Maps SDK como mejora. | Facilita comprender la zona de un trabajo presencial. | Maps requiere configuración y facturación habilitada; no se necesita ubicación continua. |
| Widget y procesos diferidos | Jetpack Glance y WorkManager, si alcanza el tiempo. | Glance puede mostrar oportunidades guardadas; WorkManager permite tareas persistentes diferibles. | No garantiza ejecución a una hora exacta; no sustituye notificaciones remotas. |
| Lógica privilegiada | Cloud Functions o backend equivalente. | Centraliza selección, validaciones sensibles y futuras integraciones. | Requiere verificar límites, plan y costos; desarrollo local con emuladores cuando sea posible. |
| Reglas y almacenamiento | Reglas de seguridad de Firebase y almacenamiento de archivos según necesidad. | Control por usuario y recurso. | Las reglas no protegen automáticamente operaciones hechas con SDK de administración; esas rutas necesitan autorización explícita. |
| Notificaciones | Firebase Cloud Messaging, etapa posterior al recorrido básico. | Notificaciones móviles. | Requiere permisos y configuración de proyecto. |
| Control de versiones | Git y repositorio remoto. | Historial y revisión de cambios. | El repositorio contiene documentación; la implementación está pendiente. |
| Pagos | Proveedor por seleccionar; Mercado Pago es candidato para evaluar en Perú. | Ofrece documentación de marketplace y entorno de prueba. | Integración al final; disponibilidad, medios, comisiones y elegibilidad deben confirmarse para el caso concreto. |

La arquitectura se organiza en componentes según las necesidades del proyecto. Android Developers recomienda capas de interfaz y datos, repositorios y flujo de datos unidireccional; esas recomendaciones se adaptan al proyecto. [Documentación de arquitectura Android](https://developer.android.com/topic/architecture/recommendations).

```text
Android (Kotlin + Compose) ──► Identidad / datos / reglas ──► Base de datos
                                          │
                                          ├──► Lógica privilegiada / auditoría
                                          ├──► Gemini: asistencia propuesta
                                          └──► Proveedor de pagos: etapa final

Web de presentación ──► información y descarga de la app cuando esté disponible
```

En Android, la UI envía acciones al ViewModel; este consulta un repositorio. El repositorio usa Firestore para información compartida y Room para la vista local definida. Si se incorpora una API REST, Retrofit se integra como otra fuente del repositorio para una necesidad concreta. Las postulaciones y selecciones se confirman con el servidor; sin conexión no se muestran como aceptadas.

Ningún cliente tiene credenciales secretas ni decide por sí mismo que una persona es administradora, que una vacante puede llenarse o que un pago fue aprobado. El backend debe validar identidad, permisos, datos y estados.

## 9. Estructura de carpetas propuesta

```text
TuEmpuje/
├── README.md
├── docs/                    # análisis, arquitectura, requisitos y pruebas
├── android/                 # proyecto Android independiente
│   └── app/src/main/java/.../
│       ├── core/            # componentes comunes y utilidades
│       ├── data/            # modelos de datos, fuentes y repositorios
│       ├── domain/          # reglas de negocio cuando aporten claridad
│       └── features/        # acceso, oportunidades, postulaciones, perfil
├── web/                     # página de presentación futura
├── backend/                 # funciones o servicios de negocio
├── infra/                   # reglas de acceso, índices y configuración
└── tests/                   # pruebas integradas y escenarios compartidos
```

Es una estructura objetivo. Existe una base Android de demostración y un entorno local de Firebase para cuentas, perfiles y pruebas de reglas; todavía no se ha configurado un proyecto en la nube. La organización se ajustará a las necesidades reales, sin crear módulos vacíos.

## 10. Pagos: diseño a investigar al final

Separar cuatro conceptos: **contratación**, **pago del cliente**, **comisión** y **abono al trabajador**. Cada uno tiene su propio estado y registro. Un trabajo terminado no significa pago recibido.

Mercado Pago documenta en Perú su solución [Split de Pagos 1:1](https://www.mercadopago.com.pe/developers/es/docs/split-payments/split-1-1/overview), con requisitos de identificación y OAuth para el vendedor. Su [documentación de requisitos](https://www.mercadopago.com.pe/developers/es/docs/split-payments/split-1-1/prerequisites) indica que el modelo 1:N está limitado a vendedores de cartera asesorada. La [documentación del checkout](https://www.mercadopago.com.pe/developers/es/docs/split-payments/split-1-1/integration-configuration/integrate-marketplace) también describe límites de cuentas y escenarios de reembolso. Repartir un pago no equivale a custodiarlo hasta terminar el trabajo.

Trabajo necesario: seleccionar producto permitido; confirmar elegibilidad y medios reales; definir comprador/vendedor y obligaciones; construir el checkout de prueba; procesar notificaciones verificadas en servidor; manejar repetición de eventos, pagos pendientes, rechazos, reembolsos, contracargos y conciliación; probar escenarios; y solo luego evaluar producción. La regla de Google Play cambia según el tipo de transacción: debe revisarse la [política de pagos](https://support.google.com/googleplay/android-developer/answer/9858738?hl=en) para cada modalidad y suscripción.

## 11. Proceso de trabajo

El proyecto usará ciclos de trabajo diarios y ligeros, inspirados en la gestión iterativa y ajustados al alcance y plazo disponibles. La [Guía Scrum](https://scrumguides.org/scrum-guide.html) define backlog, objetivo del producto, revisión e incremento.

Cada día: escoger un objetivo comprobable → diseñar el flujo → implementar una parte vertical que atraviese las capas necesarias → probar y corregir → mostrar el resultado → registrar decisiones y siguiente prioridad. El backlog se actualiza al terminar cada jornada y el alcance se ajusta al tiempo disponible.

**Definición de listo para empezar:** objetivo, actor, datos requeridos, restricciones y ejemplo de aceptación entendidos; dependencias identificadas.

**Definición de terminado para el curso:** se ejecuta desde una instalación documentada, cumple los criterios del alcance elegido, se verifica en Android, maneja error básico, no expone secretos, no rompe el recorrido anterior y registra qué es real o simulado.

**Definición adicional para producción:** permisos revisados en servidor, protección de datos, pruebas de seguridad, registros de incidentes, recuperación y respaldo, soporte y condiciones de operación. Pasarela y liberación están sujetas a requisitos de terceros.

## 12. Seguridad, privacidad y operación

- Datos de prueba durante desarrollo; no cargar documentación personal ni información financiera real.
- Acceso por cuenta y control por recurso; los administradores requieren permisos especiales.
- Validar datos en cliente y servidor; el cliente no es una frontera de confianza.
- No guardar contraseñas manualmente ni datos de tarjetas. Mantener secretos fuera del repositorio.
- Ofrecer un canal de reporte, moderación, respuesta y trazabilidad para casos de fraude o abuso antes de abrir al público.
- Definir qué se almacena, quién lo consulta, por cuánto tiempo y cómo se elimina; revisar la [normativa peruana de datos personales](https://www.gob.pe/institucion/anpd/campa%C3%B1as/128319-nuevo-reglamento-de-proteccion-de-datos-personales).
- Revisar [OWASP MASVS](https://mas.owasp.org/MASVS/) para móvil y [OWASP ASVS](https://asvs.owasp.org/) para web y backend. Son referencias para establecer controles, no certificaciones automáticas.
- Google Play requiere un mecanismo para solicitar eliminación de cuenta dentro de la app y mediante un recurso web cuando la app permite crear cuentas, con excepciones de conservación debidamente informadas. [Google Play: eliminación de cuentas](https://support.google.com/googleplay/android-developer/answer/13327111?hl=en).

## 13. Publicación y mantenimiento

Antes de publicar: comprobar nombre y marca; verificar cuenta de desarrollador; identificar responsable y condiciones de servicio; generar versión firmada; definir privacidad, eliminación de cuenta y ficha de datos; probar accesibilidad y dispositivos; completar pruebas de seguridad; crear canal de soporte; revisar costos, copias y recuperación; desplegar gradualmente y monitorear errores.

Si se usa una cuenta personal de Google Play creada después del 13 de noviembre de 2023, Google exige una prueba cerrada con al menos 12 testers inscritos continuamente durante 14 días antes de solicitar acceso a producción. Esto podría superar el plazo del curso y no se aplica automáticamente a todas las cuentas. [Requisitos de prueba de Google Play](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en-GB).

Después de publicar: responder incidencias, revisar errores, mantener dependencias y reglas, comprobar respaldos, atender solicitudes de datos y medir ofertas publicadas, postulaciones, contrataciones y finalización de forma responsable.

## 14. Riesgos y decisiones pendientes

| Riesgo o incógnita | Impacto | Tratamiento |
|---|---|---|
| Plazo de 7–10 días | Alto | Priorizar un solo recorrido integrado en Android; autenticación y datos se abordan con servicios administrados si resultan viables. |
| Falta de trabajos reales | Alto | Validar con negocios y profesionales; el prototipo usa datos ficticios. |
| Complejidad de permisos y datos | Alto | Acceso por recurso, reglas y pruebas antes de cualquier piloto real. |
| Pasarela y retiros no compatibles | Alto | Dejar pagos al final y confirmar con proveedor; usar sandbox antes de producción. |
| Alcance web o tecnología exigida por el curso | Medio | Confirmar con el docente y documentar decisiones. |
| Marca TuEmpuje | Medio | Consultar Indecopi, dominios y tiendas; el nombre aún no está verificado. |
| Costos y acceso a cuentas externas | Medio | Revisar plan y límites, presupuesto y responsables; no crear servicios facturables sin control. |
| Fraude, no pago, acoso o trabajos peligrosos | Alto | Reportes, revisión humana, condiciones claras y canal de soporte antes de operación real. |

## 15. Bases consultadas

- [Guía de arquitectura Android](https://developer.android.com/topic/architecture)
- [Recomendaciones de arquitectura Android](https://developer.android.com/topic/architecture/recommendations)
- [Seguridad de reglas de Firestore](https://firebase.google.com/docs/firestore/security/get-started)
- [OWASP MASVS](https://mas.owasp.org/MASVS/) y [OWASP ASVS](https://asvs.owasp.org/)
- [Guía Scrum](https://scrumguides.org/scrum-guide.html)
- [Mercado Pago: pagos marketplace](https://www.mercadopago.com.pe/developers/es/docs/split-payments/split-1-1/overview)
- [Mercado Pago: webhooks y verificación](https://www.mercadopago.com.pe/developers/es/docs/links-and-debts/additional-content/your-integrations/notifications/webhooks)
- [Google Play: requisitos de prueba](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en-GB)
- [Sunafil: contrato laboral y locación](https://www.gob.pe/institucion/sunafil/noticias/594915-cual-es-la-diferencia-entre-un-contrato-de-trabajo-y-un-contrato-de-locacion-de-servicios)
