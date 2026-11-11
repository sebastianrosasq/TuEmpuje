# TuEmpuje: plan de trabajo y pruebas

**Fecha de planificación:** 11 de noviembre de 2026 · **Modalidad:** proyecto grupal · **Horizonte estimado:** de siete a diez días · **Primer avance académico:** 12 de noviembre de 2026

## 1. Orden de construcción

El objetivo de cada día es entregar una parte visible que se conecte con lo anterior. Si un día consume más tiempo, se desplazan los elementos siguientes; no se eliminan controles de seguridad para habilitar usuarios reales.

| Sprint | Resultado | Trabajo principal | Evidencia |
|---|---|---|---|
| 0 | Bases y recorrido definidos. | Plan, requisitos, mapa de pantallas, decisiones técnicas, backlog, diseño del flujo y prototipo inicial si el tiempo alcanza. | Documentos y explicación del recorrido; distinguir boceto y función real. |
| 1 | Avance visible. | Proyecto Android y web iniciales; navegación; pantallas de oportunidades y publicación; datos de prueba. Conectar una fuente compartida si es viable. | Abrir ambas superficies y demostrar lo que ya funciona, explicando límites. |
| 2 | Acceso y perfiles. | Configurar identidad y datos; registro, acceso, permisos básicos y perfiles de cliente/postulante. | Dos cuentas de prueba con perfiles diferenciados; errores comprensibles. |
| 3 | Publicación real. | Crear/listar oportunidad desde la web; validar campos y propiedad; persistir datos. | Crear oferta y verla luego de actualizar la web. |
| 4 | Descubrimiento Android. | Listado, detalle, filtros y conexión a la misma fuente de datos. | La oferta creada en web aparece en Android. |
| 5 | Postulación. | Crear postulación desde Android; impedir duplicados; listar en web. | Un candidato de prueba postula y el cliente ve su registro. |
| 6 | Selección y seguimiento. | Selección, límite de vacantes, estado consultable y acuerdo básico. | Cliente selecciona y el postulante ve el estado. |
| 7 | Cierre del recorrido. | Finalización, historial básico si es viable y manejo de errores. | Demostración completa de publicación a trabajo finalizado. |
| 8 | Calidad y panel. | Corregir fallos; autorización; estados vacíos; panel de administración mínimo o cola de reportes según tiempo. | Pruebas de permisos y recorrido en emulador y navegador. |
| 9–10, si están disponibles | Entrega y refuerzo. | Documentación de instalación y arquitectura; pruebas de dispositivos; preparación de presentación y video; mejoras priorizadas. | Versión reproducible y relación clara de pendientes. |
| Posterior al flujo principal | Pasarela y producción. | Revisar modelo de cobro, proveedor, sandbox, webhooks y operación; después seguridad, regulación y habilitación real. | Registro de pruebas y validación del proveedor. |

El calendario se ajustará según el tiempo disponible y el avance de cada iteración. La integración de pagos se reserva para una etapa posterior al recorrido académico principal.

## 2. Prioridades si falta tiempo

**Imprescindible:** Android en Kotlin ejecutable, web de cliente, una fuente común de datos, publicación, listado, postulación, revisión y selección; estados y datos de demostración claramente indicados.

**Siguiente:** identidad real con permisos, acuerdo, finalización, panel administrativo mínimo y reportes.

**Después del recorrido principal:** el asistente con Gemini para preparar borradores, si el plazo y la configuración lo permiten. El entrenamiento y evaluación de un modelo propio se prepararán por separado cuando sea posible. También quedan para iteraciones posteriores la mensajería, notificaciones, reseñas, pagos, validación profesional y funciones de colas. Mantenerlos documentados sin simular que existen.

**Para demostrar los temas del curso:** priorizar Kotlin, Compose, manejo de estado, navegación, Material 3, Git, MVVM/UDF y Firebase durante el recorrido principal. Añadir Room para oportunidades guardadas o caché si cabe en el plazo. Retrofit requiere una API REST real definida; Google Maps, animaciones, Glance y WorkManager quedan condicionados al tiempo, las credenciales y la necesidad del producto. Consultar [Temas del curso y aplicación en TuEmpuje](05-temas-del-curso.md).

Si la autenticación real no se termina para la presentación, se puede mostrar una demostración local con cuentas de prueba o datos ficticios, rotulada como prototipo. Eso impide abrirla a usuarios reales y debe indicarse expresamente. Sin autenticación y autorización reales no se puede afirmar que la plataforma es segura.

## 3. Backlog inicial y dependencias

| ID | Entregable | Depende de | Estado inicial |
|---|---|---|---|
| B-01 | Revisar restricciones del curso y preparar entorno. | Ninguno. | Pendiente |
| B-02 | Diseñar navegación y pantallas iniciales. | Requisitos iniciales. | Pendiente |
| B-03 | Crear proyecto Android y web. | B-01. | Pendiente |
| B-04 | Configurar proveedor de identidad y datos de prueba. | B-01, cuenta y acceso disponibles. | Pendiente |
| B-05 | Registrar y mantener perfiles. | B-03, B-04. | Pendiente |
| B-06 | Publicar oportunidades desde web. | B-04, B-05. | Pendiente |
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
3. Formulario de publicación web.
4. Pantalla Android de búsqueda y detalle de oportunidad.
5. Explicación del dato compartido o aclaración de que todavía es ficticio.
6. Requisitos, arquitectura, prioridades y calendario restante.

**Regla:** no afirmar que Android y web están conectados si aún no comparten una fuente; no mostrar transacciones ficticias como pagos reales; no presentar una pantalla como funcional si es una maqueta.

## 5. Pruebas de aceptación

| ID | Escenario | Pasos y resultado esperado | Estado |
|---|---|---|---|
| PA-01 | Publicación correcta. | Cliente autenticado ingresa campos válidos en web → oferta guardada con su identificador. | Pendiente |
| PA-02 | Publicación incompleta. | Dejar remuneración o tareas vacías → mensaje claro y no se crea oferta. | Pendiente |
| PA-03 | Información compartida. | Crear oferta en web y consultar Android → título y condiciones coinciden. | Pendiente |
| PA-04 | Postulación y duplicado. | Candidato postula dos veces → solo una postulación activa y confirmación correcta. | Pendiente |
| PA-05 | Privacidad entre clientes. | Otro cliente intenta consultar postulaciones ajenas → acceso denegado en backend. | Pendiente |
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

## 6. Criterios de salida

### Para entregar al curso

- El recorrido seleccionado funciona de inicio a fin o se muestra de forma transparente qué queda simulado.
- Hay instrucciones reproducibles para abrir Android y web.
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
