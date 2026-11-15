# TuEmpuje: plan de inteligencia artificial

**Estado:** asistencia conectada y probada en el entorno de desarrollo, pendiente de revisión y pruebas para producción.

## 1. Decisión

TuEmpuje utilizará **Gemini como proveedor inicial de asistencia generativa**. La primera función propuesta ayudará a un profesional o negocio a convertir una descripción libre en un borrador de oportunidad que podrá revisar y editar antes de publicar.

En una línea de trabajo separada, se preparará y evaluará un **modelo propio para una tarea específica** cuando existan tiempo, datos adecuados y capacidad de prueba. El primer candidato es sugerir la categoría de una oportunidad. El modelo propio no sustituirá automáticamente a Gemini y su entrenamiento no modificará Gemini.

La asistencia de oportunidades y CV está disponible en el proyecto de desarrollo, las propuestas siempre requieren revisión y el acceso manual sigue disponible. La pasarela de pagos sigue al final del proyecto.

## 2. Qué hará el asistente

El cliente podrá elegir entre completar el formulario manualmente o escribir lo que necesita en un campo de texto.

**Entrada de ejemplo, ficticia:**

> Necesito ayuda para una sesión de fotos el sábado. Son cuatro horas y pagaré S/ 60. La persona me ayudará a ordenar las luces y el equipo.

**Borrador esperado:**

- Título: ayudante para sesión de fotografía.
- Tareas: apoyar con la organización de luces y equipo.
- Duración: cuatro horas.
- Remuneración: S/ 60.
- Unidad de pago: por confirmar si el texto no la especifica.
- Ubicación: pendiente de indicar.
- Experiencia requerida: pendiente de indicar.

Los campos que no se hayan indicado deben permanecer vacíos o marcados como pendientes. La persona puede modificar cada sugerencia. El formulario valida los campos obligatorios y solo publica después de su confirmación.

**Criterios de aceptación:**

1. El cliente puede escribir o pegar una descripción y solicitar sugerencias.
2. El resultado se muestra como borrador editable; no se publica automáticamente.
3. La IA no completa como hechos el pago, ubicación, experiencia o requisitos que no recibió.
4. Si la respuesta es incompleta o inválida, se indican los campos que faltan.
5. Si el servicio falla, se conserva el texto y se permite completar la publicación manualmente.
6. Las reglas de publicación, permisos y validaciones siguen ejecutándose fuera del modelo.

## 3. Flujo técnico propuesto

```text
Aplicación Android del cliente
   │ descripción proporcionada voluntariamente
   ▼
Firebase AI Logic con App Check
   │ envía instrucciones, texto mínimo y formato esperado
   ▼
Gemini
   │ devuelve sugerencias estructuradas
   ▼
Android valida estructura y tipos de datos
   │
   ▼
Cliente revisa y completa el formulario
   │ confirma
   ▼
TuEmpuje valida y guarda la publicación en Firestore
   │
   ▼
Android consulta la oportunidad compartida
```

La app utiliza [Firebase AI Logic](https://firebase.google.com/docs/ai-logic) desde Kotlin y [App Check](https://firebase.google.com/docs/ai-logic/app-check), con una credencial de depuración por instalación durante las pruebas. El control centralizado de límites por cuenta y la configuración de producción aún deben completarse.

En ningún caso se incluirá una clave privada en el código fuente, repositorio o APK. Una salida de IA es una sugerencia no confiable: hay que comprobar su estructura, tipos, campos obligatorios y reglas antes de guardar datos. El asistente no tiene permisos para seleccionar candidatos ni efectuar pagos.

El creador de CV permite elegir un diseño, editar los datos del perfil, solicitar sugerencias y guardar una versión local. Puede iniciar una versión para una oportunidad a partir del CV general. La importación de PDF existentes y la entrega del archivo en una postulación todavía no están implementadas.

## 4. Modelo propio: investigación separada

### Tarea candidata

Clasificar descripciones en un conjunto limitado de categorías, por ejemplo: audiovisual, diseño, programación, eventos e inventario. El cliente mantiene la posibilidad de elegir o corregir la categoría. Si el modelo no ofrece una sugerencia útil, se solicita la elección manual.

### Preparación y evaluación

1. Definir categorías y reglas de etiquetado claras.
2. Crear ejemplos ficticios variados y revisarlos manualmente. No confundirlos con oportunidades reales.
3. Separar los datos por origen o plantilla para evitar que frases casi idénticas aparezcan tanto en entrenamiento como en prueba.
4. Reservar conjuntos de entrenamiento, validación y prueba final. No ajustar el modelo con el conjunto de prueba final.
5. Comparar contra una solución simple basada en palabras o reglas.
6. Medir resultados por categoría, errores y casos donde el sistema no sugiera una categoría. Revisar especialmente el comportamiento con textos ambiguos.
7. Registrar versión, datos, resultados y limitaciones antes de considerar su uso en el producto.

No se define todavía una cantidad de ejemplos ni una precisión que garantice éxito. Una prueba pequeña sirve para aprender y exponer el método; no demuestra que el modelo represente el lenguaje de todos los usuarios de Arequipa.

### Posible arquitectura futura

Se podría entrenar el clasificador fuera de la app y servirlo mediante una API. Android podría consumir una API REST real con Retrofit, un tema del curso, si esa arquitectura resulta adecuada. Otra opción es evaluar ejecución en el dispositivo, lo que requeriría conversión y pruebas adicionales. Son opciones de investigación: no hay modelo, API ni datos de entrenamiento construidos.

## 5. Diferencia entre usar y entrenar IA

| Método | Qué cambia | Aplicación prevista |
|---|---|---|
| Instrucciones y ejemplos | El contexto enviado al modelo; no cambian sus parámetros. | Primera versión con Gemini. |
| Modelo propio de clasificación | Se entrena un modelo para una tarea delimitada. | Investigación posterior o paralela. |
| Ajuste fino de un modelo existente | Se ajustan sus parámetros con datos de entrenamiento. | Solo se evaluaría si aporta mejora frente a instrucciones y reglas. |
| Entrenar un modelo generativo desde cero | Se construye y entrena un modelo completo. | Fuera del alcance del curso. |

Actualmente Google indica que la API Gemini y AI Studio no ofrecen un modelo disponible para ajuste fino directo; Google documenta esa capacidad en su entorno empresarial. Firebase AI Logic tampoco expone ajuste fino como función propia. Estas condiciones pueden cambiar y se revisarán si se decide experimentar con *fine-tuning*. [Google: ajuste fino de Gemini](https://ai.google.dev/gemini-api/docs/model-tuning/), [Firebase: capacidades no disponibles](https://firebase.google.com/docs/ai-logic/faq-and-troubleshooting).

## 6. Datos, seguridad y costos

- Usar datos ficticios en el curso y en las primeras pruebas de evaluación.
- No enviar DNI, datos bancarios, credenciales ni documentos personales cuando no sean necesarios para la tarea.
- Antes de usar información real para entrenamiento, definir finalidad, base legal o autorización aplicable, conservación, eliminación y condiciones del proveedor.
- No asumir que quitar nombres vuelve anónimos todos los datos; revisar el riesgo de reidentificación.
- Consultar los términos del servicio y el plan contratado antes de procesar información real. Los términos de la [Gemini API](https://ai.google.dev/gemini-api/terms) describen usos de contenido que varían según el servicio y sus condiciones.
- Aplicar control de acceso, límites de uso y monitoreo de costos; una cuota gratuita o precio puede cambiar.
- No ordenar, rechazar ni contratar personas automáticamente con base en predicciones. El responsable de la selección es quien contrata.

## 7. Pruebas

| Caso | Resultado esperado |
|---|---|
| Se proporciona una descripción con tareas, horario y pago. | El borrador refleja lo indicado y permite editarlo. |
| No se proporciona ubicación ni experiencia. | Ambos campos quedan pendientes; no se inventan. |
| El cliente cambia el texto propuesto. | El formulario conserva los cambios del cliente y solo publica al confirmar. |
| La IA devuelve datos inválidos, falla o tarda demasiado. | Se muestra un mensaje, se conserva la descripción y sigue disponible el modo manual. |
| Se envían datos con instrucciones ajenas a la función. | Las reglas del sistema siguen vigentes y el resultado no ejecuta acciones ni cambia permisos. |
| Un clasificador se prueba con textos reservados. | Se registran aciertos y errores por categoría y se comparan con el método simple. |

## 8. Referencias

- [Firebase AI Logic](https://firebase.google.com/docs/ai-logic)
- [Firebase App Check y Gemini](https://firebase.google.com/docs/ai-logic/app-check)
- [Gemini: respuestas estructuradas](https://ai.google.dev/gemini-api/docs/structured-output)
- [Preparación de datos para ajuste supervisado](https://docs.cloud.google.com/gemini-enterprise-agent-platform/models/tuning/supervised-tuning/prepare-data)
- [Gemini: disponibilidad de ajuste fino](https://ai.google.dev/gemini-api/docs/model-tuning/)
- [Google: identificación de sesgos](https://developers.google.com/machine-learning/crash-course/fairness/identifying-bias)
