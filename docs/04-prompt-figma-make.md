# TuEmpuje: prompt para Figma Make

Este documento sirve para crear un **prototipo visual e interactivo** del proyecto. No genera automáticamente una aplicación Android en Kotlin ni un sistema seguro listo para producción. El prototipo usará datos ficticios y conservará, como máximo, información local de demostración.

Figma recomienda explicar qué se quiere construir, para quién y con qué restricciones; también sugiere trabajar primero la estructura y después las interacciones. Por eso hay un prompt principal e instrucciones opcionales para corregir o completar el resultado. [Guía oficial de Figma Make](https://help.figma.com/hc/en-us/articles/31304485164695-Create-a-Figma-Make-file).

## Cómo utilizarlo

1. Crear un archivo nuevo en Figma Make.
2. Copiar y pegar **solo el contenido del bloque «Prompt principal»**.
3. Revisar que se vea bien el recorrido móvil y que los textos estén en español.
4. Aplicar los prompts de ajuste, uno por uno, cuando falte alguna parte o no funcione una interacción.
5. Presentarlo como prototipo con datos de ejemplo. Después se utilizará como referencia para implementar Android y web.

El botón para cambiar entre vistas de demostración pertenece a la presentación del prototipo; no representa un requisito para la aplicación final.

El diseño móvil se tomará como referencia para una app en Kotlin con Jetpack Compose y Material 3. Figma Make no implementa Room, Firebase, Retrofit ni el widget Android por el hecho de incluirlos en una descripción visual. Su incorporación al proyecto se explica en [Temas del curso y aplicación en TuEmpuje](05-temas-del-curso.md).

---

## Prompt principal

```text
Quiero diseñar un prototipo interactivo de TuEmpuje, una plataforma de oportunidades de trabajo para Arequipa, Perú.

TuEmpuje conecta a jóvenes y otras personas que buscan sus primeras experiencias pagadas con profesionales y negocios que necesitan apoyo para tareas, proyectos, turnos o ayudantías. Un fotógrafo, por ejemplo, puede publicar que necesita ayuda para una sesión; un estudiante puede leer las condiciones y postular. Una persona también puede publicar o postular desde la misma cuenta si corresponde.

El prototipo tiene dos superficies que comparten la misma identidad visual y los mismos datos de demostración:

1. Una experiencia móvil Android para quien busca oportunidades y postula.
2. Una web de cliente para un profesional o negocio que publica una oportunidad, consulta postulantes y selecciona a una persona.

Incluye una zona de administración sencilla solo como vista de la estructura futura. El flujo que debe funcionar en la demostración es: crear una oportunidad en la web, verla en la experiencia móvil, postular y después revisar esa postulación desde la web.

RESULTADO QUE NECESITO

Crea un prototipo responsive y navegable, no una landing page. Quiero poder recorrer las pantallas y entender cómo funcionaría el producto. Usa datos locales ficticios; no conectes bases de datos, servicios externos ni cuentas reales. Las acciones y los cambios de estado son parte de la demostración y deben funcionar dentro de la misma sesión del prototipo. Si hay una limitación para compartir estado entre vistas, explica en una nota de demostración qué parte es simulada.

Coloca un selector discreto de demostración para alternar entre «App para postulantes» y «Web para clientes». Este selector está fuera de la interfaz del producto y solo sirve para presentar el prototipo. En una pantalla amplia, muestra la app móvil dentro de un área de tamaño razonable; en un teléfono, debe ocupar el ancho disponible sin una maqueta de teléfono decorativa que reduzca el contenido.

IDENTIDAD Y TONO

La marca se llama «TuEmpuje». Usa esa escritura de forma consistente. No conviertas el diseño en una campaña motivacional: es una herramienta para encontrar trabajo y contratar apoyo. La interfaz debe ser cercana, confiable y fácil de entender tanto para una estudiante como para una persona que administra un negocio.

Escribe en español usado en Perú, con frases cortas y naturales. Usa «oportunidad», «postular», «postulantes» y «publicar». Los montos se muestran en soles, por ejemplo «S/ 60 por jornada», y cada anuncio debe aclarar si el monto es por hora, jornada o proyecto. Evita mensajes grandilocuentes, frases motivacionales genéricas y contenido de relleno.

DIRECCIÓN VISUAL

Busco una interfaz contemporánea y cuidada, pero sobria. Debe parecer un producto de trabajo real, con jerarquía clara y contenido útil. Evita el estilo típico de plantilla: degradados fuertes, tarjetas enormes y repetidas, ilustraciones de cohetes, gráficos decorativos sin significado o una página de inicio que parezca una presentación de ventas.

Usa esta dirección como punto de partida:

- Fondo cálido muy claro, como #F7F8F5.
- Texto principal oscuro, como #162923.
- Verde profundo como color principal, como #195A4B, para acciones y elementos activos.
- Un acento ocre suave, como #D7A64E, usado con moderación en detalles; no uses texto blanco pequeño sobre ese acento porque pierde contraste.
- Bordes discretos, sombras muy suaves o inexistentes y superficies claras.
- Tipografía sans serif legible, con títulos de tamaño moderado y texto cómodo de leer.
- Espaciado consistente basado en 4, 8, 12, 16, 24 y 32 unidades. Márgenes móviles cercanos a 20 px.
- Iconos lineales consistentes; siempre acompañar de texto cuando la acción no sea evidente.

El verde sobre blanco debe mantener contraste suficiente. En web, considera contraste de texto, foco visible y navegación por teclado. En móvil, usa áreas de interacción amplias, equivalentes a un mínimo de 48 dp en los controles principales, y no dependas únicamente del color para comunicar estados.

DISEÑO DE LA APP MÓVIL

Diseña a partir de un ancho de referencia cercano a 390 px, adaptándolo a otros tamaños. La interfaz será una referencia para Android con Jetpack Compose y Material Design 3: usa patrones móviles reconocibles, controles coherentes y una navegación inferior con «Inicio», «Explorar», «Mis postulaciones» y «Perfil». Mantén los títulos, márgenes y componentes consistentes.

Pantalla 1: Inicio.
- Encabezado con «TuEmpuje», saludo simple y acceso a notificaciones de demostración.
- Buscador con texto «Busca una oportunidad».
- Un bloque pequeño de filtros rápidos: «Todas», «Por jornada», «Por proyecto» y «Con acompañamiento».
- Lista de oportunidades con información útil. Cada tarjeta muestra título, tipo, lugar o modalidad, fecha, pago, requisito de experiencia y nombre del publicador ficticio.
- Una invitación discreta a completar el perfil, sin bloquear la exploración.

Pantalla 2: Explorar.
- Buscador, filtro por categoría, modalidad, distrito y experiencia.
- Resultados legibles y una opción para limpiar filtros.
- Si no hay coincidencias, muestra «No encontramos oportunidades con esos filtros» y la acción «Limpiar filtros».

Pantalla 3: Detalle de oportunidad.
- Título, publicador, distrito o modalidad, fecha, duración, monto y forma de pago expresada como condición acordada, sin afirmar que el pago ya se realizó.
- Apartados «Qué harás», «Qué necesitas» y «Condiciones».
- Si es una ayudantía, mostrar quién supervisa y qué equipo aporta el profesional.
- Acción principal «Postular» y acción secundaria para guardar.
- No mostrar insignias de verificación ni garantías que el prototipo no puede comprobar.

Pantalla 4: Postulación.
- Resumen de la oportunidad y del perfil de prueba.
- Campo opcional para un mensaje breve y confirmación de disponibilidad.
- Validar la disponibilidad antes del envío.
- Al enviar, mostrar «Postulación enviada» y permitir regresar al listado o consultar «Mis postulaciones».
- Si se intenta postular de nuevo a la misma oportunidad en la sesión, mostrar «Ya postulaste» y evitar una segunda postulación de demostración.

Pantalla 5: Mis postulaciones.
- Lista con estado visible mediante texto: «Enviada», «Seleccionada» o «No seleccionada».
- Mostrar título y fecha del trabajo. Los estados deben cambiar en la demostración solo cuando corresponda.
- Si la lista está vacía, explicar que aún no hay postulaciones y ofrecer explorar oportunidades.

Pantalla 6: Perfil.
- Nombre ficticio, zona, habilidades, disponibilidad y muestras opcionales de trabajo.
- Botón para editar información básica.
- Una sección de experiencia que pueda mostrarse vacía sin inventar trabajos finalizados.
- No mostrar fotos de identidad, documentos, números de teléfono o información sensible.
- Incluye una entrada a «Oportunidades guardadas» como parte del perfil, con una lista local de demostración. En la app final esta función podrá usar Room; no afirmes que Room ya está conectado en este prototipo.

DISEÑO DE LA WEB PARA CLIENTES

En escritorio, usa un panel con navegación lateral compacta; en móvil, adapta la navegación sin ocultar funciones esenciales. Evita una portada comercial que obligue a desplazarse antes de usar el panel.

Pantalla 1: Resumen.
- Encabezado «Panel de cliente» y botón «Publicar oportunidad».
- Lista de oportunidades creadas con título, fecha, estado y número de postulaciones calculado a partir de los datos de prueba.
- Un estado vacío claro si no existen publicaciones. No inventes cifras grandes, valoraciones ni indicadores de crecimiento.

Pantalla 2: Publicar oportunidad.
- Formulario ordenado en secciones: información general, tareas y requisitos, lugar y horario, pago y vacantes.
- Incluye una opción «Ayúdame a redactar» con un campo para describir el trabajo. En este prototipo puede mostrar un borrador predefinido y editable, marcado como demostración; no afirmes que Gemini está conectado.
- Campos: título, tipo de oportunidad, categoría, descripción, tareas, experiencia o conocimientos requeridos, modalidad, distrito si es presencial, fecha o duración, monto en soles, unidad de pago y número de vacantes.
- Para «Ayudantía», agregar campos breves de supervisión y equipo proporcionado.
- Etiquetas siempre visibles y mensajes de validación junto al campo. No dependas solo del placeholder.
- Botones «Guardar borrador» y «Publicar». Al publicar correctamente, crear la oportunidad en los datos locales de demostración y mostrarla en el panel y en la app.
- La ubicación exacta y los datos de contacto no se hacen públicos en el prototipo.

Pantalla 3: Detalle de una publicación.
- Datos del aviso, vacantes y lista de postulantes.
- Cada postulante debe mostrar nombre ficticio, habilidades, disponibilidad y mensaje, si lo proporcionó.
- Permitir abrir el detalle de un postulante y seleccionar para la demostración.
- La selección cambia el estado del candidato a «Seleccionada» y actualiza «Mis postulaciones» en la vista móvil durante la misma sesión.
- No permitir seleccionar más personas que las vacantes disponibles; mostrar una explicación clara si ya están cubiertas.

Pantalla 4: Administración, vista futura.
- Una tabla simple con publicaciones de ejemplo y la opción visual de revisar un reporte.
- Identificarla como «Vista de demostración»; no prometer que hay verificación de identidad ni moderación real.
- No introducir datos sensibles ni mostrar conversaciones privadas.

DATOS DE EJEMPLO

Usa tres oportunidades ficticias al inicio. Estos montos son solo ejemplos de interfaz, no precios investigados:

1. «Ayudante para sesión de fotografía» — Cercado de Arequipa — sábado, 4 horas — S/ 60 por jornada — no requiere experiencia laboral previa; se valoran conocimientos básicos de fotografía. Publica «Estudio Luz Sur», nombre ficticio.
2. «Apoyo en inventario de tienda» — Yanahuara — una jornada — S/ 70 por jornada — instrucciones al iniciar. Publica «Tienda Nativa», nombre ficticio.
3. «Edición de tres videos cortos» — remoto — por proyecto — S/ 150 por proyecto — requiere muestras de edición. Publica «Proyecto Central», nombre ficticio.

Usa una postulante ficticia llamada Lucía Torres, estudiante de diseño con habilidades en edición y fotografía básica. No añadas calificaciones, contrataciones previas o credenciales verificadas que no estén definidas en estos datos.

INTERACCIONES MÍNIMAS QUE DEBEN FUNCIONAR

1. Cambiar entre la app móvil y la web para clientes.
2. Abrir una oportunidad y regresar al listado sin perder el contexto.
3. Filtrar oportunidades, limpiar filtros y mostrar el estado vacío cuando corresponda.
4. Publicar una oportunidad válida desde la web y verla en el listado móvil dentro de la misma sesión.
5. Completar una postulación desde la app y verla en la web asociada a la oportunidad correcta.
6. Seleccionar a Lucía en la web y ver el estado «Seleccionada» en la app.
7. Impedir postulaciones duplicadas y selecciones que superen las vacantes en la demostración.
8. Guardar y quitar una oportunidad y verla en «Oportunidades guardadas» durante la sesión.

Si no puedes implementar una acción, no la presentes como operativa. Identifícala en la vista de demostración o deja la opción para una iteración posterior. Los datos locales del prototipo pueden reiniciarse; no uses esto como si fuera una base de datos real.

LÍMITES

- No implementes pasarela de pago en esta etapa y no muestres botones de pagar o saldo disponible.
- No afirmes «pago garantizado», «identidad verificada», «trabajo asegurado» ni muestres estadísticas sin datos.
- No inventes integración con Firebase, verificación de documentos, mensajería real ni notificaciones reales.
- No afirmes que la ayuda para redactar usa Gemini de forma real en este prototipo; la integración se hará en una etapa posterior.
- No expongas datos personales de personas reales.
- Mantén separados los estados «trabajo finalizado» y «pago realizado» si aparecen en alguna vista futura.
- Prioriza que el recorrido principal funcione y sea entendible antes de añadir pantallas secundarias.

Antes de terminar, revisa que no queden botones sin respuesta dentro del recorrido principal, que las vistas móvil y web usen los mismos datos de demostración y que cada pantalla muestre textos completos y legibles en español.
```

---

## Ajuste 1: si la app móvil necesita más detalle

```text
Mantén la identidad y los datos de TuEmpuje. Mejora solo el recorrido móvil: Inicio → detalle de oportunidad → postulación → Mis postulaciones → Perfil. Revisa que las etiquetas estén en español, que el pago y su unidad se vean antes de postular y que el aviso seleccionado mantenga los mismos datos en todas las pantallas. Corrige estados de carga, lista vacía y postulación duplicada. No modifiques el panel web salvo que sea necesario para conservar los datos compartidos.
```

## Ajuste 2: si la web necesita más detalle

```text
Mantén la app móvil y la identidad actual. Completa la web para clientes con estas vistas: resumen de publicaciones, formulario de oportunidad, detalle de publicación y lista de postulantes. El formulario debe validar los campos obligatorios y, al publicar, la oportunidad debe aparecer en la app durante la misma sesión. Mantén la web responsive y evita añadir estadísticas o verificaciones inventadas.
```

## Ajuste 3: si el recorrido entre plataformas falla

```text
Revisa únicamente el flujo entre la web y la app móvil. Desde la web, crea una oportunidad con una vacante. Comprueba que aparece en la app, postula con Lucía y verifica que esa postulación aparece en la publicación correcta. Selecciona a Lucía y comprueba que su estado cambia a «Seleccionada» en Mis postulaciones. Evita postulaciones duplicadas y no permitas superar la cantidad de vacantes. Todo debe funcionar con datos de demostración en la misma sesión, sin afirmar que existe un backend real.
```

## Ajuste 4: revisión visual y de accesibilidad

```text
Revisa TuEmpuje sin cambiar el alcance funcional. Mejora la jerarquía tipográfica, los espacios, los estados de foco y la legibilidad en pantallas pequeñas. Asegúrate de que los formularios tengan etiquetas visibles y mensajes de error concretos, que los botones principales tengan una zona táctil amplia y que el estado de una postulación se entienda por texto, no solo por color. Elimina bloques decorativos y textos que no ayuden a tomar una decisión.
```

## Ajuste 5: vista del asistente con Gemini

```text
En la web de clientes de TuEmpuje, añade al formulario de publicación una opción «Ayúdame a redactar». La persona escribe qué trabajo necesita y puede ver un borrador de ejemplo editable con título, tareas, duración y pago, dejando los datos faltantes sin completar. Incluye opciones para editar el borrador o continuar manualmente. Es una interacción simulada para el prototipo: no afirmes que existe conexión real con Gemini, no publiques automáticamente y no agregues botones de pago. Conserva el resto de pantallas y el flujo ya construido.
```

## Comprobación antes de presentar

- El prototipo se identifica como demostración con datos ficticios.
- El nombre se escribe siempre «TuEmpuje».
- El monto indica si corresponde a hora, jornada o proyecto.
- La oferta creada desde web aparece en la vista móvil durante la sesión, si esa función se pudo construir.
- La postulación y la selección se reflejan en la oportunidad y el perfil correctos.
- No hay botones de pago, insignias de verificación ni estadísticas inventadas.
- No se presenta el prototipo web de Figma Make como si ya fuera la app Android en Kotlin.
