# TuEmpuje: aplicación de los temas del semestre

**Fecha:** 11 de noviembre de 2026 · **Fuente:** relación de temas proporcionada para las semanas 1 a 15 · **Estado:** propuesta antes de implementar.

El proyecto debe demostrar lo aprendido en el curso sin añadir funciones que no aporten al flujo. Por el plazo de una persona y aproximadamente una semana a semana y media, se prioriza primero el recorrido publicador → oportunidad → postulación → selección. Los temas avanzados se incorporan si hay tiempo y se cumplen sus requisitos técnicos.

## 1. Relación de los temas con TuEmpuje

| Semana | Tema | Aplicación propuesta | Cómo se comprobaría | Prioridad |
|---|---|---|---|---|
| 1 | Introducción a Android y Kotlin esencial. | Configurar el proyecto Android, definir modelos y lógica en Kotlin y ejecutar la aplicación. | La app compila y abre en un emulador o dispositivo. | Principal |
| 2 | Programación orientada a objetos en Kotlin. | Representar `Usuario`, `Oportunidad` y `Postulacion`, y separar responsabilidades entre clases y repositorios. | Crear y procesar datos de ejemplo con modelos coherentes. | Principal |
| 3 | Jetpack Compose: layouts y controles. | Construir tarjetas de oportunidad, formularios y pantallas con componentes reutilizables. | Consultar el listado, abrir un detalle y visualizar los campos correctamente. | Principal |
| 4 | Manejo del estado y Git. | Mantener filtros, campos, carga y errores como estado observable; registrar cambios del proyecto en Git. | Cambiar un filtro actualiza los resultados; el historial registra avances. | Principal |
| 5 | Navegación entre múltiples vistas. | Navegar entre inicio, exploración, detalle, postulación y perfil. | Abrir una oportunidad concreta y volver sin perder el flujo esperado. | Principal |
| 6 | Menús y Material Design 3. | Usar navegación inferior, botones, campos, diálogos y mensajes de Material 3. | Los controles son coherentes y la navegación inferior lleva a la sección correcta. | Principal |
| 7 | SQLite con Room. | Guardar en el dispositivo oportunidades marcadas como favoritas y, si se define, la copia local de resultados ya consultados. | Guardar una oportunidad, cerrar y abrir la app y comprobar que continúa disponible. | Siguiente |
| 8 | MVVM + UDF con persistencia. | Separar pantalla, ViewModel y repositorio; las acciones actualizan el estado que observa la interfaz. | Una acción de guardar actualiza la pantalla y persiste mediante el repositorio. | Principal |
| 9 | Consumo de API con Retrofit. | Consumir un servicio REST concreto cuando se elija y documente uno que TuEmpuje realmente necesite. | Una petición obtiene y presenta datos reales del servicio, con carga y error. | Condicionado |
| 10 | CRUD de API con Retrofit II. | Si se decide construir una API REST propia, usar Retrofit para las operaciones autorizadas que esa API exponga. | Crear, consultar, modificar y eliminar los recursos realmente implementados y permitidos. | Condicionado |
| 11 | Firebase: Firestore y Authentication. | Gestionar cuentas y compartir oportunidades y postulaciones entre Android y web. | La web publica una oferta de prueba y la app la consulta; solo el propietario ve postulantes. | Principal, sujeto a configuración |
| 12 | Google Maps SDK. | Mostrar, como mejora, la ubicación aproximada de una oportunidad presencial. | Abrir un mapa con una ubicación de ejemplo sin revelar información privada. | Opcional |
| 13 | Animaciones en Jetpack Compose. | Añadir una transición breve al mostrar confirmación de postulación o filtros. | La animación acompaña el cambio de estado y no bloquea el uso. | Opcional |
| 14 | Widgets con Jetpack Glance. | Mostrar en un widget el número de oportunidades guardadas o un acceso a ellas. | Agregar el widget y abrir la sección correcta de la app. | Opcional |
| 15 | Notificaciones y WorkManager. | Usar notificaciones para avisos permitidos y WorkManager para sincronizaciones diferibles o recordatorios no exactos. | La tarea se ejecuta cuando el sistema lo permite y la notificación respeta los permisos. | Opcional |

«Opcional» significa que la función no debe impedir completar el recorrido principal. No se afirma que estos elementos estén implementados.

## 2. Arquitectura móvil

```text
Pantallas Compose
       │ acciones del usuario
       ▼
ViewModel ─────────► Estado visible en la pantalla
       │
       ▼
Repositorio
  ├── Firebase Authentication: identidad de usuario
  ├── Firestore: oportunidades y postulaciones compartidas
  ├── Room: favoritos y copia local definida
  └── Retrofit: API REST concreta, solo si se incorpora
```

La interfaz presenta el estado y envía acciones; el ViewModel coordina la lógica de pantalla; el repositorio decide cómo consultar o guardar datos. Las comprobaciones de permisos y operaciones sensibles también deben ejecutarse en el sistema confiable, no solamente en Android. La [guía de arquitectura Android](https://developer.android.com/topic/architecture/recommendations) recomienda separar interfaz y datos y utilizar un flujo de datos unidireccional.

## 3. Cómo se combinan Room y Firestore

**Firestore** sería la fuente compartida para que un cliente publique desde la web y un candidato consulte desde Android. **Room** puede conservar favoritos y una copia local de los datos que se decida mostrar sin conexión.

Firestore también ofrece persistencia local propia. Si se utilizan ambas opciones, el repositorio debe decidir qué información gestiona cada una, cuándo se actualiza y qué se muestra si la conexión falla. Para un primer avance, es más claro utilizar Room para favoritos y Firestore para los datos compartidos; la caché más amplia puede añadirse después si resulta necesaria. [Persistencia sin conexión de Firestore](https://firebase.google.com/docs/firestore/manage-data/enable-offline).

Las ofertas guardadas pueden consultarse sin conexión si ya están disponibles en el dispositivo. Su estado podría haber cambiado; se debe indicar cuándo se consultaron o actualizarlas al reconectar. La app no debe afirmar que una postulación o una selección fue confirmada sin recibir confirmación del servicio. Las transacciones de Firestore, por ejemplo, requieren conexión. [Transacciones de Firestore](https://firebase.google.com/docs/firestore/manage-data/transactions).

## 4. Dónde encaja Retrofit

Retrofit es un cliente HTTP para una API REST. El SDK de Firestore es otra forma de acceder a datos; **no hace falta usar Retrofit para que Android lea o escriba en Firestore**.

Opciones reales para aplicar Retrofit, si el tiempo y el curso lo requieren:

1. Construir una API propia con una función necesaria y documentar sus rutas, permisos y datos; Android la consumiría mediante Retrofit.
2. Integrar una API externa que se haya elegido y comprobado para una necesidad concreta del producto.

La API y sus rutas todavía no están definidas. No se debe inventar un servicio externo ni duplicar las mismas escrituras en Firebase y en otra API solo para marcar el tema como utilizado. Si el docente exige demostrar CRUD con Retrofit, esa condición debe definir la estrategia del backend antes de programar.

## 5. Maps, animaciones, widget y tareas diferidas

- **Maps:** empezar con distrito escrito o seleccionado. Un mapa es una mejora para visualizar una zona aproximada; no hace falta rastrear continuamente al postulante. Google indica que Maps SDK requiere una clave y un proyecto con facturación habilitada; verificar condiciones y costos antes de activarlo. [Configuración de Maps SDK](https://developers.google.com/maps/documentation/android-sdk/get-api-key?setupProd=configure).
- **Animaciones:** usar transiciones cortas que ayuden a reconocer una acción, por ejemplo una confirmación de postulación. Evitar movimiento innecesario y respetar las preferencias del sistema. [Animaciones en Compose](https://developer.android.com/develop/ui/compose/animation/quick-guide).
- **Glance:** un widget puede mostrar favoritos y abrir la app. Es una mejora posterior al flujo principal; no sustituye la pantalla de oportunidades.
- **WorkManager:** sirve para trabajo persistente y diferible; el sistema decide cuándo ejecutarlo según restricciones. No garantiza recordatorios a una hora exacta ni reemplaza por sí solo las notificaciones remotas.

## 6. Qué mostrar en la presentación

1. El recorrido principal de negocio entre la web y Android.
2. El código de una pantalla Compose y su ViewModel.
3. La navegación hacia el detalle de una oportunidad.
4. La persistencia de un favorito con Room, si llega a implementarse.
5. Una oferta publicada en web y consultada desde Android con Firebase, si está configurado.
6. Una petición Retrofit real, únicamente si se integró una API y se puede explicar su finalidad.
7. Las mejoras restantes y su prioridad, sin presentarlas como funciones terminadas.

La maqueta de Figma Make sirve para revisar el diseño. La implementación Android debe desarrollarse y probarse en Kotlin y Android Studio.

## Referencias

- [Recomendaciones de arquitectura Android](https://developer.android.com/topic/architecture/recommendations)
- [Persistencia sin conexión con Firestore](https://firebase.google.com/docs/firestore/manage-data/enable-offline)
- [Transacciones de Firestore](https://firebase.google.com/docs/firestore/manage-data/transactions)
- [Configuración y requisitos de Maps SDK](https://developers.google.com/maps/documentation/android-sdk/get-api-key?setupProd=configure)
- [Guía de animaciones de Compose](https://developer.android.com/develop/ui/compose/animation/quick-guide)
- [Widgets con Jetpack Glance](https://developer.android.com/develop/ui/compose/glance)
- [Trabajo persistente con WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Retrofit](https://square.github.io/retrofit/)
