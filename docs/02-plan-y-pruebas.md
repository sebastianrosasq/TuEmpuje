# TuEmpuje: plan de trabajo y pruebas

**Fecha de planificación:** 11 de noviembre de 2026 · **Modalidad:** proyecto grupal · **Horizonte estimado:** de siete a diez días · **Primer avance académico:** 12 de noviembre de 2026

## 1. Orden de construcción

El [plan de 16 sprints](09-sprints.md) reúne los objetivos, entregables, dependencias y criterios de aceptación de cada iteración

El calendario se ajustará según el tiempo y el avance, sin eliminar controles de seguridad para habilitar usuarios reales

La web se plantea como una página de presentación posterior y la integración de pagos se reserva para después del recorrido académico principal

## 2. Prioridades si falta tiempo

**Imprescindible:** Android en Kotlin ejecutable, una fuente común de datos, publicación, listado, postulación, revisión y selección desde la app; estados y datos de demostración claramente indicados.

**Siguiente:** identidad real con permisos, acuerdo, finalización y reportes; las herramientas de administración se definirán después.

**Después del recorrido principal:** el asistente con Gemini, el entrenamiento y evaluación de un modelo propio, reseñas, pagos, validación profesional y funciones de colas. La mensajería por postulación y los avisos periódicos ya cuentan con una primera implementación, sin entrega push inmediata.

**Para demostrar los temas del curso:** usar Kotlin, Compose, manejo de estado, navegación, Material 3, Git, MVVM/UDF, Firebase, Room para guardados y WorkManager para las revisiones periódicas. Retrofit requiere una API REST real definida; las demás integraciones dependen de su necesidad y disponibilidad. Consultar [Temas del curso y aplicación en TuEmpuje](05-temas-del-curso.md).

Si la autenticación real no se termina para la presentación, se puede mostrar una demostración local con cuentas de prueba o datos ficticios, rotulada como prototipo. Eso impide abrirla a usuarios reales y debe indicarse expresamente. Sin autenticación y autorización reales no se puede afirmar que la plataforma es segura.

## 3. Backlog inicial y dependencias

| ID | Entregable | Depende de | Estado inicial |
|---|---|---|---|
| B-01 | Revisar restricciones del curso y preparar entorno. | Ninguno. | Pendiente |
| B-02 | Diseñar navegación y pantallas iniciales. | Requisitos iniciales. | Prototipo verificado en emulador |
| B-03 | Crear proyecto Android. | B-01. | Base compilada y abierta en emulador |
| B-04 | Configurar proveedor de identidad y datos de prueba. | B-01, cuenta y acceso disponibles. | Emuladores y proyecto de desarrollo en la nube configurados |
| B-05 | Registrar y mantener perfiles. | B-03, B-04. | Registro y perfil verificados en nube de desarrollo, edición y acceso comprobados localmente |
| B-06 | Publicar oportunidades desde Android. | B-04, B-05. | Pendiente |
| B-07 | Ver oportunidades en Android. | B-03, B-06. | Pendiente |
| B-08 | Postular y ver postulantes. | B-05, B-06, B-07. | Pendiente |
| B-09 | Seleccionar y consultar estado. | B-08. | Pendiente |
| B-10 | Registrar condiciones y cierre de trabajo. | B-09. | Pendiente |
| B-11 | Reportes, moderación y permisos de administrador. | B-04, B-06. | Pendiente |
| B-12 | Revisiones, historial y mensajes. | B-09, B-10. | Pendiente |
| B-13 | Pruebas integradas y preparación de entrega. | B-06 a B-09 como mínimo. | Pendiente |
| B-14 | Identificar proveedor y evaluar pasarela. | Flujo principal estable y modalidad definida. | Posterior |
| B-15 | Procesar pagos de prueba y evaluar salida real. | B-14 y cuentas aprobadas por proveedor. | Posterior |
| B-16 | Integrar Gemini para sugerir un borrador editable. | B-06, configuración de IA y controles de acceso. | Después del flujo principal, sujeto al plazo |
| B-17 | Preparar datos ficticios y evaluar un clasificador propio. | Categorías definidas, ejemplos revisados y tiempo disponible. | Investigación posterior o paralela, sin bloquear el producto |

## 4. Primera demostración

La presentación puede mostrar, de acuerdo con lo que se haya construido realmente:

1. Problema y públicos de TuEmpuje.
2. Diagrama del recorrido completo.
3. Formulario Android de publicación.
4. Pantalla Android de búsqueda y detalle de oportunidad.
5. Explicación del dato compartido entre cuentas o aclaración de que todavía es ficticio.
6. Requisitos, arquitectura, prioridades y calendario restante.

**Regla:** no afirmar que las cuentas comparten datos si aún no existe una fuente común; no mostrar transacciones ficticias como pagos reales; no presentar una pantalla como funcional si es una maqueta.

## 5. Pruebas de aceptación

| ID | Escenario | Pasos y resultado esperado | Estado |
|---|---|---|---|
| PA-01 | Publicación correcta. | Cliente autenticado ingresa campos válidos en Android → oferta guardada con su identificador. | Pendiente |
| PA-02 | Publicación incompleta. | Dejar remuneración o tareas vacías → mensaje claro y no se crea oferta. | Pendiente |
| PA-03 | Información compartida. | Crear oferta con una cuenta y consultarla desde otra en Android → título y condiciones coinciden. | Pendiente |
| PA-04 | Postulación y duplicado. | Candidato postula dos veces → solo una postulación activa y confirmación correcta. | Pendiente |
| PA-05 | Privacidad entre clientes. | Otro cliente intenta consultar postulaciones ajenas → acceso denegado en backend. | Verificado en reglas locales; pendiente en la nube |
| PA-06 | Límite de vacantes. | Dos selecciones simultáneas para una vacante → como máximo una contratación confirmada. | Pendiente |
| PA-07 | Resultado y estado. | Participantes completan trabajo → estado correcto; pago sigue independiente. | Pendiente |
| PA-08 | Moderación. | Usuario normal intenta acción de administrador → denegada; administrador autorizado registra motivo. | Pendiente |
| PA-09 | Pasarela, etapa posterior. | En sandbox: aprobado, pendiente, rechazado, devolución, webhook repetido e inválido → estados coherentes, sin doble procesamiento. | Posterior |
| PA-10 | Conexión interrumpida. | Desconectar red al enviar → mensaje claro; no afirmar éxito sin confirmación. | Pendiente |
| PA-11 | Eliminación de cuenta. | Iniciar solicitud por app y web → procedimiento y retención aplicable visibles. | Pendiente |
| PA-12 | Borrador de Gemini incompleto. | Describir un trabajo sin indicar sueldo ni lugar → mostrar esos campos como pendientes y no publicar automáticamente. | Pendiente |
| PA-13 | Fallo del asistente. | Simular error o tiempo de espera → conservar el texto y permitir completar el formulario manualmente. | Pendiente |
| PA-14 | Clasificador futuro. | Comparar el modelo con reglas simples y ejemplos reservados para prueba final; registrar resultados por categoría. | Posterior |

Además: pruebas unitarias de validación y reglas; integración de repositorios y permisos; prueba manual en emulador y al menos un equipo físico si está disponible; revisión de accesibilidad, errores y datos personales; pruebas de restauración y operación antes del piloto real. Priorizar pruebas significativas de permisos, postulaciones y transiciones.

### Resultados verificados

- Compilación Android y 17 pruebas unitarias: correctas
- 22 pruebas de reglas de Firestore en emulador: correctas, incluidos perfiles privados, rol de publicación, postulaciones y mensajes
- En el emulador Android se comprobó el primer ingreso, la opción de omitir intereses, el acceso con dos cuentas de ejemplo y la bandeja de cada participante
- En la interfaz Android, la cuenta de empleador publicó una oportunidad de ejemplo, la cuenta de postulante la encontró y postuló, el empleador la seleccionó y la cuenta de postulante mostró el estado seleccionado
- La conversación de ejemplo se abrió desde ambas cuentas y mostró los mensajes registrados en la nube de desarrollo
- Se concedió el permiso de notificaciones y se comprobó un aviso genérico tras añadir un mensaje de ejemplo y reprogramar la revisión, esto no mide el tiempo de entrega bajo restricciones de batería
- Se revisó el tema oscuro y el tamaño de texto ampliado en el emulador

El recorrido de publicación a selección se comprobó cambiando de cuenta en un emulador, la prueba en dos teléfonos, el acceso mediante Google, el gesto predictivo cancelado y la navegación con TalkBack necesitan verificación adicional

## 6. Criterios de salida

### Para entregar al curso

- El recorrido seleccionado funciona de inicio a fin o se muestra de forma transparente qué queda simulado.
- Hay instrucciones reproducibles para abrir Android.
- No hay secretos ni datos reales en el repositorio.
- Las pruebas ejecutadas y las limitaciones se documentan.
- La presentación distingue idea, alcance construido y trabajo futuro.

### Para un piloto real

- Autenticación, autorización y manejo de reportes comprobados.
- Términos, privacidad, eliminación y responsable de soporte definidos.
- Decisión revisada sobre modalidades de contratación y colocación laboral.
- Datos de producción separados de pruebas; backups, recuperación y monitoreo probados.
- Si se habilitan pagos: aprobación del proveedor, pruebas de punta a punta, conciliación, reembolsos y condiciones conocidas.
- Validación con clientes y trabajadores de prueba y canal de atención operativo.

### Para publicación amplia

- Cumplir requisitos vigentes de Google Play y su proceso de pruebas cuando aplique; no asumir que el curso y la publicación comercial tienen la misma fecha.
- Revisar marca, ficha, clasificación, protección de datos y cumplimiento de políticas.
- Preparar despliegue gradual, seguimiento de fallos y capacidad de retirar o corregir una versión.

## 7. Validación de producto

Objetivos exploratorios, no estimación representativa: hablar con profesionales y negocios que contratarían ayudantes y con jóvenes; recopilar tareas reales, horarios, pago, perfil exigido y cómo contratan hoy; conseguir oportunidades concretas y observar si se publican, postulan y completan. Medir contratación, pago, tiempo de selección y repetición antes de estimar rentabilidad.

## 8. Fuentes principales

- [Guía Scrum](https://scrumguides.org/scrum-guide.html)
- [Arquitectura Android](https://developer.android.com/topic/architecture/recommendations)
- [OWASP MASVS](https://mas.owasp.org/MASVS/) y [ASVS](https://asvs.owasp.org/)
- [Mercado Pago: cuentas de prueba](https://www.mercadopago.com.pe/developers/es/docs/checkout-api-orders/resources/test-accounts?scope=prod)
- [Mercado Pago: webhooks](https://www.mercadopago.com.pe/developers/es/docs/links-and-debts/additional-content/your-integrations/notifications/webhooks)
- [Google Play: requisitos de prueba](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en-GB)
