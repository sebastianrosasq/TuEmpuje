# Plan de construcción en 16 sprints

TuEmpuje se desarrollará mediante 16 iteraciones con un objetivo, una entrega y criterios de aceptación verificables

La aplicación Android concentrará la publicación y búsqueda de oportunidades, las postulaciones y la selección de candidatos

La página web de presentación, la inteligencia artificial y los pagos se incorporarán después del recorrido principal

## Alcance y calendario

El plazo académico estimado es de siete a diez días y el primer avance se presentará al día siguiente de iniciar la construcción

Los sprints son unidades cortas de planificación para este proyecto, no equivalen necesariamente a un día ni se presentan como una aplicación completa de Scrum

La meta del curso es completar el recorrido básico hasta el sprint 10 y preparar su presentación en el sprint 13, si el tiempo y los accesos lo permiten

Los sprints restantes describen la evolución del producto, por lo que su inclusión en este plan no implica que puedan completarse dentro del plazo académico

Si el avance es menor al previsto, se priorizarán una app ejecutable, los casos principales y las pruebas de permisos, indicando con claridad los datos simulados y las funciones pendientes

**Estado:** la app ya cuenta con navegación, cuenta, perfil privado, ajustes y guardados, las oportunidades, postulaciones y selección se conectaron a Firebase y el recorrido entre dos cuentas se verificó en emuladores locales, las reglas y el índice se desplegaron al proyecto de desarrollo

La revisión del sistema visual incorporó componentes compartidos, estados de error y vacío, transiciones y adaptación de la navegación con texto ampliado, sigue pendiente la revisión manual con TalkBack y en otros dispositivos, el estado detallado del recorrido se encuentra en [Diseño y navegación](10-diseno-y-navegacion.md)

La revisión más amplia de accesibilidad y dispositivos corresponde al sprint 12, el trabajo en las funciones restantes continúa y no se considera cerrado un sprint hasta verificar todos sus criterios

## Sprints

| Sprint | Objetivo y entrega | Criterio de aceptación | Dependencias |
|---|---|---|---|
| 01 · Base Android | Crear el proyecto Kotlin con Compose, configuración de compilación y tema inicial de TuEmpuje | La aplicación compila y abre una pantalla inicial en un emulador o dispositivo | Entorno Android configurado y repositorio disponible |
| 02 · Lenguaje visual | Crear colores, tipografía, componentes básicos y superficies con profundidad inspiradas en vidrio | Los componentes son legibles en fondos claros y oscuros, y las acciones tienen etiquetas y áreas táctiles adecuadas | Sprint 01 |
| 03 · Navegación y primer avance | Conectar Explorar, Publicar, Actividad y Perfil, con detalle y formulario de oportunidad usando datos ficticios | Cada destino abre su pantalla, Atrás vuelve al contexto anterior y la demostración identifica los datos ficticios | Sprints 01 y 02 |
| 04 · Acceso y perfiles | Configurar Firebase Authentication y perfiles básicos para publicar o postular con una misma cuenta | Dos cuentas de prueba pueden iniciar sesión y cada una recupera su perfil sin acceder al de otra para editarlo | Sprint 01 y acceso a un proyecto Firebase de desarrollo |
| 05 · Datos y permisos | Definir oportunidades y postulaciones en Firestore, reglas iniciales de acceso y datos ficticios | Las operaciones permitidas y denegadas se comprueban con cuentas de prueba y reglas, sin credenciales privadas en el repositorio | Sprint 04 y Firebase configurado |
| 06 · Publicación | Implementar el formulario, las validaciones y el guardado de oportunidades en Android | Una publicación válida se guarda asociada a su responsable y una incompleta muestra errores sin publicarse | Sprints 03 a 05 |
| 07 · Descubrimiento | Conectar el listado, detalle y filtros de oportunidades a los datos compartidos | Una oferta creada por una cuenta aparece con sus condiciones en otra, con estados de carga, lista vacía y error | Sprints 05 y 06 |
| 08 · Postulación | Permitir a una cuenta postular y consultar sus solicitudes | Se guarda la postulación vinculada a la oferta y al usuario, se impide un duplicado activo y se informa el resultado confirmado | Sprints 05 y 07 |
| 09 · Candidatos | Permitir a quien publica consultar los candidatos de sus oportunidades | La persona propietaria ve sus postulantes y otra cuenta no autorizada recibe denegación | Sprints 05, 06 y 08 |
| 10 · Selección y seguimiento | Registrar la selección y mostrar el estado a ambas partes | La selección respeta las vacantes incluso ante solicitudes simultáneas y se muestra solo después de su confirmación | Sprint 09 y operación atómica con autorización |
| 11 · Guardados locales | Incorporar Room para conservar oportunidades guardadas y definir la estrategia de actualización | Una oportunidad guardada sigue visible tras cerrar la app y los datos desactualizados no se presentan como una confirmación del servidor | Sprints 07 y 10 |
| 12 · Calidad y accesibilidad | Revisar navegación, errores, contraste, legibilidad, permisos, conexión y rendimiento en dispositivos disponibles | El recorrido principal supera las pruebas acordadas o sus fallos quedan documentados, con revisión de tamaño de texto y accesibilidad | Sprints 03 a 10 |
| 13 · Entrega académica | Preparar instalación, documentación de ejecución, demostración y registro de resultados | La app se abre siguiendo instrucciones reproducibles y la presentación distingue funciones reales, simuladas y pendientes | Sprints 01 a 10 según avance, con resultados del sprint 12 |
| 14 · Cierre y confianza | Añadir el estado de finalización y preparar reportes, moderación y soporte para un piloto posterior | El estado del trabajo se distingue del pago y los permisos y casos de reporte implementados se verifican, sin declarar listo el piloto hasta completar sus controles | Sprint 10 y revisión de requisitos de operación |
| 15 · Asistencia con Gemini | Integrar la propuesta de borrador editable para publicar una oportunidad, con controles de acceso y límites | La persona revisa antes de publicar, los campos desconocidos quedan pendientes y un fallo conserva la opción de completar el formulario manualmente | Sprints 06 y 12, credenciales y configuración aprobada |
| 16 · Pagos en pruebas | Elegir un proveedor compatible y probar el ciclo de pago en un entorno de pruebas, después de definir condiciones y modelo de cobro | Se registran resultados de pago aprobado, pendiente y rechazado, así como eventos repetidos y devolución cuando el proveedor lo permita, sin afirmar que existe custodia o pago real | Sprints 10 y 14, elegibilidad, cuenta de pruebas y documentación del proveedor |

## Primer avance

La primera demostración buscará completar la base ejecutable del sprint 01 y avanzar en los sprints 02 y 03

La evidencia mínima será abrir la aplicación, mostrar su identidad y recorrer las pantallas que estén construidas, explicando qué datos son ficticios y qué botones aún no tienen una operación conectada

Si el entorno o las dependencias impiden ejecutar la app a tiempo, se registrará el bloqueo y se presentarán únicamente los avances que se puedan comprobar

## Navegación y comportamiento

Los destinos principales previstos son **Explorar**, **Publicar**, **Actividad** y **Perfil**

El detalle de una oportunidad, el formulario y la revisión de candidatos serán pantallas secundarias con un regreso coherente al contexto anterior

Los botones deberán comunicar su resultado y mostrar los estados pertinentes de carga, error y confirmación, sin simular que una escritura se completó si el servidor no la confirmó

Las condiciones de pago, requisitos y tareas tendrán prioridad visual antes de postular, mientras que la selección y otras operaciones sensibles deberán validar identidad, permisos y estado de las vacantes en el sistema de datos o la lógica protegida

## Dirección visual

La interfaz utilizará los colores de TuEmpuje y una interpretación propia de superficies translúcidas y profundidad, reservada para controles y navegación cuando mejore la jerarquía visual

Las tarjetas y formularios conservarán fondos legibles, contraste suficiente y una alternativa sólida para dispositivos o configuraciones donde la transparencia dificulte la lectura

El material Liquid Glass es propio de las plataformas de Apple y no está disponible como componente nativo de Android

En Compose, `Modifier.blur` desenfoca el contenido del propio componente y no crea por sí solo un desenfoque de lo que está detrás, además su soporte depende de la versión de Android, por lo que una implementación de desenfoque de fondo requiere evaluación y pruebas adicionales

## Fuera de la entrega prioritaria

La página de presentación se desarrollará cuando el recorrido principal lo permita y dirigirá a la descarga una vez exista un enlace disponible

Un modelo propio de inteligencia artificial se investigará por separado cuando haya datos adecuados y criterios de evaluación, sin sustituir automáticamente a Gemini

La activación de pagos reales y un lanzamiento público requieren verificaciones adicionales de seguridad, privacidad, soporte, elegibilidad y condiciones de operación

## Referencias

- [Patrones de navegación en Android](https://developer.android.com/design/ui/mobile/guides/layout-and-content/layout-and-nav-patterns)
- [Principios de navegación de Android](https://developer.android.com/guide/navigation/principles)
- [Materiales y Liquid Glass en Apple](https://developer.apple.com/design/human-interface-guidelines/materials)
- [Desenfoque en Jetpack Compose](https://developer.android.com/develop/ui/compose/graphics/blur/overview)
- [Accesibilidad en Jetpack Compose](https://developer.android.com/codelabs/jetpack-compose-accessibility)
