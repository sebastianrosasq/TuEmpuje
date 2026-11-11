# Diseño de interfaces

El diseño de TuEmpuje debe facilitar la búsqueda y publicación de oportunidades mediante pantallas claras y recorridos sencillos

La identidad visual del proyecto, incluidos el logotipo y los colores, está pendiente de definición

## Objetivos

- Mostrar las tareas, el pago y las condiciones antes de postular
- Facilitar la publicación de oportunidades desde la web
- Mantener una experiencia coherente entre Android y la plataforma web
- Comunicar con claridad los cambios de estado y las acciones disponibles

## Aplicación móvil

| Sección | Contenido principal |
|---|---|
| Inicio | Oportunidades y acceso a la búsqueda |
| Explorar | Filtros por categoría, modalidad y ubicación |
| Detalle | Tareas, requisitos, duración, ubicación y pago ofrecido |
| Postulación | Revisión del perfil y confirmación de disponibilidad |
| Mis postulaciones | Solicitudes enviadas y estado de cada una |
| Perfil | Habilidades, disponibilidad y oportunidades guardadas |

La navegación y los controles se diseñarán para Android con Jetpack Compose y Material Design 3

## Plataforma web

| Sección | Contenido principal |
|---|---|
| Panel de cliente | Oportunidades publicadas y postulaciones recibidas |
| Publicación | Formulario con tareas, requisitos, ubicación, horario, pago y vacantes |
| Postulantes | Perfiles y opciones de revisión y selección |
| Administración | Gestión de publicaciones y reportes según permisos |

## Criterios de interfaz

- Utilizar textos directos y etiquetas visibles en los formularios
- Indicar la unidad de pago, como hora, jornada o proyecto
- Mostrar mensajes útiles cuando una lista esté vacía o exista un error
- Evitar que el color sea el único medio para comunicar un estado
- Mantener controles accesibles y zonas táctiles adecuadas
- Identificar de forma clara los datos de demostración y las funciones simuladas

## Recorrido principal

1. El cliente publica una oportunidad desde la web
2. La oportunidad aparece en la aplicación móvil
3. El postulante revisa los detalles y envía su solicitud
4. El cliente revisa el perfil y selecciona a un candidato
5. El postulante consulta el estado actualizado

Las pantallas y el comportamiento deberán revisarse con los [requisitos del proyecto](01-requisitos.md) antes de su implementación
