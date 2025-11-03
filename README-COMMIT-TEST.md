# Bitácora de Commits Bilingües - Prueba Personal

Estoy probando el flujo de `git pull` y `git push` en mi entorno local, y aprovecho para documentar una decisión que tomé sobre el formato de commits.

## Mi Reflexión Personal

Pensando en el futuro, tanto para mí como creador, como para los futuros programadores que pueden llegar a usar este repositorio como base o guía para su proyecto, busco generar algo más inclusivo tanto para los de habla inglesa como para los de habla hispana. Espero con esto pueda ser un poco más entendible, aunque es algo que me hubiera gustado implementar antes, se me ocurrió un poco tarde. Aun así, el código intenta estar de lo mejor comentado posible, tanto en el README.md principal, estructura de carpetas, y sobre todo en las propias líneas de código. Espero todo esto sea útil para un mejor entendimiento y aprendizaje de este mundo.

## Formato de Commits que Adoptaré

A partir de ahora, escribiré los mensajes de commit en formato bilingüe:

```
tipo(scope): english summary — descripción en español
```

### Ejemplos que usaré:
- `feat(user): add songs table and search — agregar tabla de canciones y búsqueda`
- `fix(admin): remove cast to prevent crash — quitar cast para evitar error`
- `docs(readme): update setup instructions — actualizar instrucciones de configuración`
- `refactor(data): optimize HashMap usage — optimizar uso de HashMap`

### Tipos de commit que manejo:
- **feat** — nueva funcionalidad
- **fix** — corrección de errores
- **docs** — documentación
- **refactor** — refactorización de código
- **test** — pruebas
- **chore** — tareas de mantenimiento
- **perf** — mejoras de rendimiento
- **style** — cambios de formato

## Proceso de Verificación

**Paso 1**: Hice `git pull origin main` y confirmé que este archivo aparece localmente.

**Paso 2**: Editaré este archivo agregando mi firma y fecha:

---
**Verificación de flujo git:**
- Autor: Alejandro Marín Hernández
- Fecha de prueba: 3 de noviembre de 2025
- Flujo verificado: ✅ Pull funciona / ⏳ Push pendiente de probar

**Paso 3**: Haré commit y push para verificar el flujo completo:
```bash
git add README-COMMIT-TEST.md
git commit -m "docs(test): verify bilingual commit format — verificar formato de commits bilingües"
git push origin main
```

---

## Nota Personal

Este enfoque bilingüe refleja mi filosofía: la programación debe ser accesible para todos, sin importar el idioma. Espero que futuros desarrolladores, ya sean de habla inglesa o hispana, puedan entender fácilmente la evolución de este proyecto.

¡SyncUp es para todos! 🎵🌎