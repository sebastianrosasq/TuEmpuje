# Empu en Rive

Esta carpeta conserva la escena editable del personaje, creada con Rive CLI

La máquina de estados de esta versión contiene una animación de reposo, las demás expresiones se implementan temporalmente en Compose y no forman parte de este archivo

Para comprobar y previsualizar la fuente con Rive CLI desde esta carpeta

```sh
rive . --verify
rive . --screenshot=empu.png --advance=30
```

La vista previa no equivale a un archivo publicado para Android, para obtener un recurso firmado, iniciar sesión con `rive login` y ejecutar `rive . --publish`, después se debe verificar la exportación, añadir el runtime compatible y conectar los estados antes de reemplazar la versión de Compose

Guía de referencia: [Rive para agentes](https://rive.app/agents)
