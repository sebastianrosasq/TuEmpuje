# Guía de desarrollo

Esta guía reúne los acuerdos de trabajo para mantener el proyecto ordenado desde su primera etapa

## Organización

La raíz contiene la documentación general y la configuración compartida del repositorio

Las carpetas de código se añadirán cuando exista una implementación que las necesite

```text
TuEmpuje/
├── android/       Aplicación móvil en Kotlin
├── web/           Plataforma web para clientes y administración
├── backend/       Lógica que deba ejecutarse en servidor
├── infra/         Reglas de acceso y configuración de servicios
└── docs/          Documentación del proyecto
```

Esta estructura representa una dirección de trabajo y puede ajustarse a las necesidades de cada componente

## Flujo de trabajo

1. Elegir un requisito o recorrido del [plan de trabajo](02-plan-y-pruebas.md)
2. Revisar su alcance y sus criterios de aceptación
3. Implementar una parte que pueda ejecutarse y comprobarse
4. Probar los casos relevantes y registrar las limitaciones
5. Actualizar la documentación si cambió alguna decisión
6. Agrupar los archivos relacionados en un commit

La primera meta de implementación es conectar la publicación desde la web con la consulta y postulación en Android

## Commits

Cada commit debe representar un cambio comprensible, como completar una función, corregir un problema o establecer una parte de la arquitectura

Los mensajes deben ser breves y explicar el resultado del cambio

Ejemplos:

```text
Agrega el listado de oportunidades
Implementa el registro de postulaciones
Corrige la validación de vacantes
Documenta la arquitectura móvil
```

No es necesario crear un commit por cada ajuste de formato o cambio mínimo

## Ramas

La rama principal conserva una versión integrada del proyecto

Cuando una tarea necesite trabajo aislado se puede crear una rama descriptiva y reunir los cambios relacionados antes de integrarla

## Configuración local

Las rutas del SDK, variables de entorno, cuentas de servicio y credenciales permanecen fuera de Git

Si un componente necesita variables de entorno, debe documentar su propósito y proporcionar un archivo de ejemplo sin valores secretos

El archivo `.gitignore` excluye configuraciones locales, credenciales habituales y archivos generados

## Datos de prueba

Usar nombres, oportunidades y montos ficticios durante el desarrollo y las presentaciones

Una función simulada debe identificarse como tal para no confundirse con una integración terminada

## Calidad y seguridad

- Comprobar formularios, estados vacíos y errores de conexión
- Verificar que cada cuenta solo acceda a la información autorizada
- Probar la prevención de postulaciones duplicadas y el límite de vacantes
- Evitar dependencias y complejidad que no resuelvan una necesidad concreta
- Mantener separados el estado de un trabajo y el estado de su pago
- Revisar los requisitos de seguridad y privacidad antes de utilizar información real

Los escenarios de aceptación están en el [plan de trabajo y pruebas](02-plan-y-pruebas.md)
