# TuEmpuje: requisitos y criterios de aceptación

**Versión:** 0.2 · **Estado:** requisitos del proyecto, con un primer recorrido conectado al entorno de desarrollo

Ya se implementaron cuenta, perfil privado, publicación, postulación y selección con reglas probadas en emulador, los demás requisitos conservan sus prioridades y no se consideran completos solo por figurar en este documento

## 1. Alcance y actores

La aplicación Android permitirá publicar, encontrar y gestionar oportunidades de trabajo y ayudantías. Actores: visitante, persona que busca trabajo y cliente (negocio o profesional); la administración y el proveedor de pagos corresponden a etapas posteriores. Una cuenta puede actuar como cliente y como postulante sin crear identidades separadas. Los roles son permisos asociados a acciones; no presuponen que una persona tenga una profesión o acreditación.

Una página web de presentación podrá explicar el proyecto y enlazar a la descarga cuando la aplicación esté disponible. No forma parte del recorrido de publicación y postulación.

### Modalidades de trabajo

| Modalidad | Diferencia esencial | Información adicional |
|---|---|---|
| Turno/campaña | Jornada y lugar definidos. | Fecha, horas, vacantes, responsable y herramientas. |
| Proyecto | Entregable y criterios de aceptación. | Alcance, fecha, revisiones y archivos. |
| Ayudantía | Una persona apoya a un profesional. | Quién supervisa y qué tareas realiza el ayudante. |
| Servicio profesional | Actividad realizada por quien posee la competencia necesaria. | Credenciales y habilitación cuando correspondan. |
| Encargo puntual | Tarea concreta para un particular o negocio. | Condiciones y límites de la tarea; etapa posterior. |

Las colas se plantean para después y solo en lugares donde se permita ceder la posición. Las prácticas formales requieren sus convenios y no se representan automáticamente como tales.

## 2. Reglas de negocio

| ID | Regla |
|---|---|
| RN-01 | Toda oportunidad tiene responsable identificado, tareas, modalidad, ubicación o condición remota, duración o fecha, remuneración y vacantes. |
| RN-02 | Una persona puede acceder a varias funciones si cumple las condiciones del perfil y permisos correspondientes. |
| RN-03 | «Sin experiencia laboral» no significa «sin conocimientos»; la publicación distingue ambos requisitos. |
| RN-04 | No se cobra a quien busca empleo por postular; evaluar los requisitos aplicables si el modelo constituye colocación laboral. |
| RN-05 | El cliente selecciona sin exceder vacantes; cada persona tiene su propia contratación. |
| RN-06 | Una persona no puede mantener dos postulaciones activas a la misma oportunidad. |
| RN-07 | Solo el dueño de una oportunidad o un administrador autorizado accede a sus postulantes, según los permisos y la política de datos. |
| RN-08 | El acuerdo registra quién contrata, a quién, tareas, condiciones y pago; confirmarlo no implica que una pasarela haya cobrado. |
| RN-09 | Trabajo y pago tienen estados independientes. Una confirmación de trabajo no demuestra un pago. |
| RN-10 | Solo participantes de un trabajo finalizado pueden valorarse y la reseña debe relacionarse con ese trabajo. |
| RN-11 | Las credenciales verificadas se identifican específicamente; un perfil registrado no acredita capacidad profesional. |
| RN-12 | El contratante y la modalidad se determinan según condiciones reales. La app no sustituye contratos ni obligaciones aplicables. |
| RN-13 | Una tarea peligrosa o regulada exige competencias, supervisión y condiciones apropiadas; la app no autoriza por sí misma su ejecución. |
| RN-14 | En una oportunidad publicada por un profesional para un ayudante, el cliente original del profesional no se convierte automáticamente en empleador o pagador del ayudante. |
| RN-15 | La primera etapa está pensada para personas mayores de edad; ampliar a menores requiere análisis y controles específicos. |
| RN-16 | La IA solo sugiere contenido: el cliente revisa, completa y confirma antes de publicar; los datos no proporcionados permanecen como pendientes. |
| RN-17 | La IA no selecciona ni rechaza automáticamente candidatos. Un modelo propio se evaluará con datos ficticios o datos cuyo uso esté debidamente autorizado. |

## 3. Requisitos funcionales

Prioridades: P0 = recorrido básico para el curso; P1 = siguiente corte si hay tiempo; P2 = posterior, incluida la pasarela. Un requisito P0 no implica que ya esté construido.

| ID | Requisito | Prioridad | Criterio de aceptación |
|---|---|---|---|
| RF-01 | Crear cuenta, iniciar y cerrar sesión. | P0 | Una cuenta válida inicia sesión; una sesión cerrada no accede a funciones protegidas. |
| RF-02 | Completar perfil y elegir funciones de cliente o postulante. | P0 | Los cambios del usuario autenticado se guardan y reaparecen en su perfil. |
| RF-03 | Crear oportunidad desde Android. | P0 | Sin campos obligatorios no se publica; con datos válidos se guarda con responsable y estado. |
| RF-04 | Listar y ver el detalle desde Android. | P0 | La app muestra las oportunidades compartidas y maneja carga, ausencia de resultados y error de conexión. |
| RF-05 | Buscar o filtrar oportunidades. | P0 | Al elegir una categoría o modalidad solo se muestran coincidencias; se pueden limpiar filtros. |
| RF-06 | Postular desde Android. | P0 | Se crea la postulación asociada a usuario y oportunidad; un segundo envío duplicado se rechaza o se muestra como ya postulado. |
| RF-07 | Revisar postulaciones desde Android. | P0 | El dueño consulta sus candidatos; otro cliente no puede acceder a la misma lista. |
| RF-08 | Seleccionar a un candidato. | P0 | Se actualiza su estado sin superar vacantes y se conserva quién tomó la decisión. |
| RF-09 | Consultar el estado de postulación. | P0 | El postulante ve el estado actualizado desde su cuenta. |
| RF-10 | Registrar condiciones y estado de trabajo. | P0 | La contratación muestra participantes, tareas y condiciones aceptadas y admite cambios de estado autorizados. |
| RF-11 | Finalizar trabajo y registrar experiencia. | P1 | La finalización queda vinculada a una contratación; la experiencia refleja tareas y visibilidad elegida. |
| RF-12 | Mensajería. | P1 | Únicamente participantes autorizados pueden leer y escribir en la conversación. |
| RF-13 | Notificaciones. | P1 | Una selección produce un aviso pertinente; el usuario puede administrar el permiso del dispositivo. |
| RF-14 | Reportar una oportunidad o usuario. | P2 | El reporte guarda motivo y estado y aparece en la cola de administración autorizada. |
| RF-15 | Administrar reportes y publicaciones. | P2 | El administrador autorizado puede revisar, cambiar estado y registrar motivo; otros usuarios reciben denegación. |
| RF-16 | Reseñas bidireccionales. | P1 | Solo participantes de una contratación finalizada pueden dejar una reseña vinculada a ella. |
| RF-17 | Solicitar eliminación de cuenta. | P1 | El usuario puede iniciar el trámite dentro de la app y mediante recurso web; se informa la conservación aplicable. |
| RF-18 | Procesar pagos mediante un proveedor. | P2 | Solo el servidor confirma un evento válido del proveedor y actualiza el estado sin procesar duplicados. |
| RF-19 | Varias vacantes. | P1 | Cada contratación es independiente y las selecciones concurrentes no superan el límite. |
| RF-20 | Ofrecer servicios profesionales. | P2 | Se muestran servicios y credenciales comprobadas cuando correspondan, sin prometer validación no realizada. |
| RF-21 | Encargos de espera/cola. | P2 | Solo se publican donde el establecimiento permita la sustitución; se registran tiempo y condiciones. |
| RF-22 | Sugerir un borrador de oportunidad mediante Gemini. | P1 | A partir del texto aportado, muestra campos sugeridos editables y señala lo que falta; no publica sin confirmación. |
| RF-23 | Mantener disponible la publicación manual si falla la IA. | P1 | Si la solicitud falla o se agota el tiempo de espera, informa el problema y conserva lo que el cliente escribió. |
| RF-24 | Evaluar un modelo propio de categorías. | P2 | Se compara con un método simple usando datos de prueba separados; no sustituye la elección del cliente sin evaluación. |

## 4. Permisos mínimos

| Operación | Visitante | Postulante | Dueño de oportunidad | Otro cliente | Administrador autorizado |
|---|---|---|---|---|---|
| Ver ofertas públicas | Sí, si se habilita | Sí | Sí | Sí | Sí |
| Publicar oferta | No | Si activa función de cliente | Sí | Sí, para sí | Según función administrativa |
| Postular | No | Sí | No a su propia oferta | Sí, en otra oferta | Según política, sin privilegio especial |
| Ver postulantes de una oferta | No | No | Sí, solo la suya | No | Solo para moderación autorizada |
| Seleccionar candidato | No | No | Sí, solo la suya | No | No sustituye al contratante |
| Leer conversación privada | No | Solo participante | Solo participante | No | Solo mediante procedimiento de soporte definido |
| Moderar y resolver reporte | No | No | No | No | Sí, con registro |

La matriz es una propuesta de política. La autorización se aplica en el backend y las reglas de datos; ocultar botones no es suficiente.

## 5. Estados y transiciones

- **Oportunidad:** borrador → publicada → cerrada o cancelada. Opcional: revisión → publicada/rechazada; pausada.
- **Postulación:** enviada → seleccionada/no seleccionada o retirada. La selección no significa que el trabajo ya se realizó.
- **Contratación:** pendiente de acuerdo → confirmada → en curso → finalizada. Puede pasar a cancelada o revisión según reglas documentadas.
- **Reporte:** recibido → en revisión → resuelto/cerrado, con responsable y motivo.
- **Pago, etapa posterior:** pendiente → confirmado/rechazado → abonado o reembolsado, según los estados reales del proveedor. No inventar estados de garantía.

Las transiciones deben comprobarse en servidor; un usuario no debe poder convertir su propio registro en pagado ni marcar como propio el trabajo de otra cuenta.

## 6. Requisitos no funcionales

| ID | Requisito | Evidencia esperada |
|---|---|---|
| RNF-01 | La aplicación Android ofrece controles accesibles y mensajes claros; la página informativa, si se construye, se adapta a distintas pantallas. | Revisión en Android y, cuando corresponda, de la página informativa. |
| RNF-02 | Android separa presentación, estado y acceso a datos. | Código organizado por responsabilidades y revisable. |
| RNF-03 | Autenticación y autorización se aplican del lado confiable. | Intentos de lectura/escritura de otra cuenta reciben denegación. |
| RNF-04 | Datos en tránsito protegidos en ambientes públicos; secretos fuera del cliente y Git. | Configuración HTTPS y revisión del repositorio. |
| RNF-05 | Información personal mínima, finalidad y eliminación definidas. | Inventario de datos y flujo de solicitud de eliminación. |
| RNF-06 | Ninguna clave de tarjeta llega al servidor propio. | Integración con interfaz/tokenización del proveedor si este producto se elige. |
| RNF-07 | Estados de carga, error y recuperación de red. | Pruebas con conexión interrumpida. |
| RNF-08 | Operaciones críticas no se duplican. | Prueba de doble postulación y concurrencia de selección. |
| RNF-09 | Registro de errores y cambios de administración sin exponer secretos. | Revisión de logs y evento auditado. |
| RNF-10 | Publicación trazable y sistema recuperable. | Procedimientos de backup, restauración y despliegue definidos antes de producción. |
| RNF-11 | Código de prueba y datos ficticios separados de producción. | Configuración por ambiente y muestra sin datos personales reales. |

## 7. Historias prioritarias

**HU-01 — Publicar oportunidad.** Como profesional quiero indicar tareas, fecha y pago para encontrar un ayudante. Aceptación: el formulario exige campos obligatorios, informa errores y guarda la oferta con mi identidad; otra persona no puede editarla.

**HU-02 — Encontrar oportunidad.** Como joven quiero revisar tareas, ubicación, requisitos y pago antes de postular. Aceptación: el detalle coincide con la oferta guardada; la app presenta un mensaje si no hay conexión.

**HU-03 — Postular.** Como candidato quiero enviar mi perfil a una oferta. Aceptación: la postulación queda asociada a mi cuenta y solo se crea una vez; puedo consultar su estado.

**HU-04 — Seleccionar.** Como contratante quiero revisar personas interesadas y seleccionar a una. Aceptación: solo veo las postulaciones de mi oferta, no excedo las vacantes y la persona seleccionada puede ver el cambio.

**HU-05 — Confirmar trabajo.** Como participante quiero ver las condiciones del acuerdo. Aceptación: se registra cada contratante y trabajador, las tareas, tiempo y monto acordados, sin afirmar que el pago fue realizado.

**HU-06 — Reportar.** Como usuario quiero informar una publicación sospechosa. Aceptación: el reporte se registra y solo la administración autorizada puede gestionarlo.

**HU-07 — Preparar un borrador.** Como profesional quiero describir con mis palabras el trabajo que necesito para que Gemini sugiera el formulario. Aceptación: puedo editar las sugerencias, completar los datos faltantes y publicar de forma manual; la IA no publica por mí.

## 8. Trazabilidad inicial

| Flujo | Requisitos | Prueba de aceptación |
|---|---|---|
| Publicar desde Android | RF-01, RF-02, RF-03 | PA-01 y PA-02 en plan de pruebas. |
| Consultar y postular desde Android | RF-04, RF-05, RF-06 | PA-03 y PA-04. |
| Revisar y seleccionar | RF-07, RF-08, RF-09, RF-19 | PA-05 y PA-06. |
| Trabajo y experiencia | RF-10, RF-11 | PA-07. |
| Seguridad y reportes | RF-14, RF-15, RNF-03 | PA-08. |
| Pago al final | RF-18, RNF-06 | PA-09, solo cuando se implemente. |
| Borrador con Gemini | RF-22, RF-23, RN-16 | PA-12 y PA-13, cuando se implemente. |
| Evaluación de modelo propio | RF-24, RN-17 | PA-14, en investigación posterior. |

## 9. Pendientes de definición

- Fecha y hora exacta de entrega y mínimos exigidos por el docente.
- Si la universidad exige arquitectura, servidor o herramientas específicas.
- Configuración de datos e identidad y disponibilidad real del plan de Firebase; la app debe compartir datos entre cuentas autorizadas.
- Política concreta de cancelación, supervisión y disputa; identidad del responsable de soporte.
- Condiciones legales de un piloto; inscripción aplicable y modalidad de contratación.
- Marca TuEmpuje, dominio y disponibilidad en tiendas.
- Selección de la pasarela y condiciones de abono; se estudia al final.
