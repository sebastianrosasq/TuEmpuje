# Asistencia y ubicación

Las funciones de asistencia están previstas para una etapa posterior, los accesos de la aplicación explican su propósito y no envían información a un modelo

## Redacción de oportunidades

La persona podrá escribir lo que necesita en lenguaje cotidiano y solicitar un borrador con título, tareas y conocimientos, luego podrá revisar y modificar cada campo antes de publicar

Los datos que no se hayan proporcionado deben quedar pendientes de completar, especialmente la ubicación, la fecha, la remuneración y el número de vacantes, la generación no publicará por sí sola ni sustituirá las validaciones de la aplicación

## Preparación de CV

El asistente podrá orientar sobre cómo presentar estudios, habilidades y proyectos, y elaborar un borrador a partir de datos entregados por la persona, sin inventar experiencia, títulos o certificaciones

Antes de enviar información a un proveedor deberá mostrarse qué datos se compartirán y solicitarse una acción expresa, el contenido generado deberá poder editarse y descartarse, la exportación y la eliminación de los archivos se definirán con la implementación

## Conexión con Gemini

La integración se diseñará detrás de un servicio controlado por el proyecto, con autenticación, límites de uso, manejo de errores y credenciales fuera del APK, el formulario manual seguirá disponible si el servicio falla

Un modelo propio es una línea de investigación separada, no se recolectarán datos para entrenamiento sin definir previamente su finalidad, autorización y política de conservación

## Ubicación y mapas

La ubicación escrita sigue siendo una referencia proporcionada por quien publica, no una dirección validada ni una ubicación en tiempo real

Una primera mejora puede abrir una búsqueda en la aplicación de mapas instalada sin solicitar ubicación del dispositivo, para un mapa integrado se propone que quien publica elija voluntariamente un punto aproximado y que quien postula pueda consultarlo sin exponer una dirección privada

El SDK de Google Maps requiere habilitar facturación y restringir la clave por aplicación y API, antes de activarlo deben revisarse las cuotas, los costos de servicios adicionales y los límites de uso

Referencias: [selector de fecha de Material](https://developer.android.com/develop/ui/compose/components/datepickers), [configuración de Maps SDK](https://developers.google.com/maps/documentation/android-sdk/get-api-key) y [consumo y facturación de Maps SDK](https://developers.google.com/maps/documentation/android-sdk/usage-and-billing)

## Fechas en la publicación

El calendario sigue el patrón de selección y confirmación de Material, y conserva la alternativa de entrada mediante teclado, la interfaz ofrece accesos a hoy y mañana, una fecha por coordinar y un campo independiente para duración u horario

Una fecha exacta o una indicación flexible se guardan actualmente junto con la duración como texto legible, todavía no existe un campo estructurado que permita filtrar por fecha desde el servidor

La propuesta considera las recomendaciones de [Atlassian sobre selección de fechas](https://atlassian.design/components/calendar/usage) y las [pautas de GOV.UK para fechas](https://design-system.service.gov.uk/patterns/dates/), ambas recomiendan mantener una alternativa al calendario visual
