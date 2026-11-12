# Empu en Rive

La escena editable contiene la mascota y la máquina de estados utilizada por Android, el archivo publicado está en `android/app/src/main/res/raw/empu.riv`

La propiedad numérica `mood` controla las expresiones: 0 reposo, 1 mirada, 2 contraseña, 3 pensando, 4 carga, 5 alegría, 6 éxito, 7 error y 8 estado vacío

Para verificar la fuente y generar una vista previa con Rive CLI:

```sh
rive . --verify
rive . --data=mood=2 --advance=30 --screenshot=empu-password.png
```

Después de editar la escena, iniciar sesión con `rive login`, ejecutar `rive . --publish` y actualizar el recurso Android con el archivo generado en `build/empu.riv`

El proyecto utiliza el plan gratuito de Rive, la publicación incluye su presentación según las condiciones del plan, si el motor no puede renderizar o se reduce el movimiento, Android usa la versión nativa en Compose

El controlador recibe estados de la interfaz y nunca el contenido de la contraseña, el estado de éxito solo debe asociarse a una operación confirmada

Referencias: [Rive CLI](https://rive.app/agents) y [runtime de Android](https://github.com/rive-app/rive-android)
