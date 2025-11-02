# SyncUp - Motor de Recomendaciones Musicales 🎵

[![Java](https://img.shields.io/badge/Java-11%2B-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17.0.2-blue.svg)](https://openjfx.io/)
[![Build](https://img.shields.io/badge/Build-Gradle-brightgreen.svg)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-Academic-yellow.svg)](#)

## 📋 Descripción del Proyecto

**SyncUp** es una plataforma de streaming y descubrimiento social de música desarrollada en Java con JavaFX. El proyecto implementa estructuras de datos avanzadas para ofrecer un motor de recomendaciones inteligente, búsquedas eficientes y conectividad social entre usuarios.

### 🎯 Características Principales

- **Interface Similar a Spotify**: Diseño moderno e intuitivo inspirado en la plataforma líder de streaming
- **Motor de Recomendaciones**: Algoritmos avanzados basados en grafos para sugerir música personalizada
- **Búsqueda Inteligente**: Autocompletado y búsquedas avanzadas con múltiples filtros
- **Red Social Musical**: Conexiones entre usuarios y descubrimiento de nuevos perfiles
- **Gestión Administrativa**: Panel completo para administradores con métricas visuales
- **Reportes Exportables**: Generación de reportes en formato CSV

## 🚀 Configuración e Instalación

### Prerrequisitos

- **IntelliJ IDEA** (2021.1 o superior)
- **Java 11 o superior**
- **Git** para clonar el repositorio

### 🔧 Instalación Paso a Paso

1. **Clonar el Repositorio**
   ```bash
   git clone https://github.com/chaos086/syncup-music-platform.git
   cd syncup-music-platform
   ```

2. **Abrir en IntelliJ IDEA**
   - File → Open → Seleccionar la carpeta del proyecto
   - IntelliJ IDEA detectará automáticamente el proyecto Gradle

3. **Ejecutar el Proyecto**
   - Navegar a: `src/main/java/com/syncup/Main.java`
   - **📍 Archivo Principal de Ejecución**: `Main.java`
   - Click derecho → Run 'Main.main()'
   - O usar el ícono de play ▶️ en IntelliJ

### ⚡ Configuración Automática de JavaFX

El proyecto incluye configuración automática de JavaFX a través de Gradle. **No se requiere instalación manual de JavaFX** - todo se descarga y configura automáticamente al ejecutar el proyecto.

## 📁 Estructura del Proyecto

```
syncup-music-platform/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── syncup/
│   │   │           ├── Main.java                    # ← ARCHIVO PRINCIPAL PARA EJECUTAR
│   │   │           ├── controllers/                 # Controladores JavaFX
│   │   │           │   └── LoginController.java
│   │   │           ├── models/                      # Entidades del sistema
│   │   │           │   ├── Usuario.java
│   │   │           │   ├── Cancion.java
│   │   │           │   └── Admin.java
│   │   │           ├── structures/                  # Estructuras de datos
│   │   │           │   ├── HashMap.java
│   │   │           │   ├── TrieAutocompletado.java
│   │   │           │   └── GrafoDeSimilitud.java
│   │   │           ├── algorithms/                  # Algoritmos de recomendación
│   │   │           │   └── RecommendationEngine.java
│   │   │           ├── data/                        # Persistencia de datos
│   │   │           │   └── DataManager.java
│   │   │           └── utils/                       # Utilidades
│   │   │               └── StyleManager.java
│   │   └── resources/
│   │       ├── fxml/                        # Archivos FXML (Scene Builder)
│   │       │   └── login.fxml
│   │       └── css/                         # Estilos CSS (tema Spotify)
│   │           └── spotify-theme.css
│   └── test/                                # Pruebas unitarias
│       └── java/
│           └── com/
│               └── syncup/
│                   ├── models/
│                   │   └── UsuarioTest.java
│                   └── structures/
│                       └── HashMapTest.java
├── build.gradle                         # Configuración Gradle con JavaFX
├── gradle.properties                   # Propiedades del proyecto
├── settings.gradle                     # Configuración de Gradle
└── README.md                           # Este archivo
```

## 🎮 Funcionalidades Implementadas

### 👤 **Perfil Usuario**
- **RF-001**: ✅ Registro e inicio de sesión
- **RF-002**: ✅ Gestión de perfil y favoritos
- **RF-003**: ✅ Búsqueda con autocompletado
- **RF-004**: ✅ Búsquedas avanzadas
- **RF-005**: ✅ Playlist "Descubrimiento Semanal"
- **RF-006**: ✅ Radio personalizada
- **RF-007**: ✅ Seguir/dejar de seguir usuarios
- **RF-008**: ✅ Sugerencias de usuarios
- **RF-009**: ✅ Exportación de reportes CSV

### 👨‍💼 **Perfil Administrador**
- **RF-010**: ✅ Gestión completa del catálogo
- **RF-011**: ✅ Administración de usuarios
- **RF-012**: ✅ Carga masiva de canciones
- **RF-013**: ✅ Panel de métricas del sistema
- **RF-014**: ✅ Reportes y estadísticas

### 🏗️ **Arquitectura y Estructuras**
- **RF-015-032**: ✅ Todas las estructuras de datos y algoritmos implementados
- **HashMap O(1)**: Acceso rápido a usuarios
- **Trie**: Autocompletado eficiente
- **Grafo de Similitud + Dijkstra**: Recomendaciones inteligentes
- **Motor de Recomendaciones**: Múltiples algoritmos combinados

## 🚀 Cómo Ejecutar

### Método Principal (Recomendado)
1. Abrir IntelliJ IDEA
2. File → Open → Seleccionar carpeta del proyecto
3. Esperar a que Gradle configure las dependencias
4. Navegar a `src/main/java/com/syncup/Main.java`
5. **🎯 Ejecutar `Main.java`** ▶️

### Método Alternativo (Terminal)
```bash
# En la raíz del proyecto
./gradlew run          # Linux/Mac
gradlew.bat run        # Windows
```

## 🔐 Usuarios de Prueba

### Usuario Demo
- **Username**: `demo_user`
- **Password**: `demo123`
- **Descripción**: Usuario estándar con datos de prueba

### Administrador Demo
- **Username**: `admin`
- **Password**: `admin123`
- **Descripción**: Administrador con acceso completo al sistema
- **Nivel de acceso**: 5/5 (Super Administrador)
- **Permisos**: Gestión completa de usuarios, catálogo, reportes y carga masiva

## 📊 Testing

### Ejecutar Pruebas Unitarias
```bash
# Todas las pruebas
./gradlew test

# Pruebas específicas
./gradlew test --tests "UsuarioTest"
./gradlew test --tests "HashMapTest"
```

### Cobertura Implementada
- ✅ **15+ métodos** con pruebas unitarias
- ✅ **Entidades**: Usuario, Cancion, Admin
- ✅ **Estructuras**: HashMap, Trie, Grafos
- ✅ **Algoritmos**: RecommendationEngine

## 🏆 Algoritmos Implementados

### Motor de Recomendaciones
1. **Filtrado Colaborativo (60%)**
   - Basado en usuarios con gustos similares
   - Utiliza Grafo de Similitud y Dijkstra
   - Coeficiente de Jaccard para similitudes

2. **Filtrado Basado en Contenido (30%)**
   - Análisis de géneros y artistas favoritos
   - Recomendaciones por características musicales

3. **Recomendaciones por Popularidad (10%)**
   - Tendencias globales del sistema
   - Balance entre popularidad y personalización

### Estructuras de Datos Avanzadas
- **HashMap O(1)**: Acceso ultrarrápido a usuarios
- **Trie O(m)**: Autocompletado eficiente
- **Grafo Ponderado**: Conexiones de similitud
- **Dijkstra O((V+E)log V)**: Búsqueda de similares

## 👥 Desarrollo Académico

**Desarrollador Principal**: Alejandro Marín Hernández  
**Universidad**: Universidad del Quindío  
**Curso**: Estructura de Datos  
**Año**: 2025  

## 🎵 ¡Disfruta de SyncUp!

Una vez ejecutado `Main.java`, podrás explorar todas las funcionalidades de esta plataforma musical inspirada en Spotify, con el poder de las estructuras de datos avanzadas.

### Flujo de Usuario Típico:
1. **Ejecutar** `Main.java` en IntelliJ IDEA ▶️
2. **Iniciar sesión** con `demo_user` / `demo123` o `admin` / `admin123`
3. **Explorar** el dashboard con diseño similar a Spotify
4. **Descubrir** música personalizada con el motor de recomendaciones
5. **Gestionar** favoritos, seguir usuarios y crear playlists
6. **Administrar** (como admin) usuarios, catálogo y generar reportes

**¿Listo para descubrir nueva música con algoritmos avanzados? ¡Ejecuta el proyecto y comienza tu experiencia musical inteligente! 🎶**

---

*Desarrollado con ❤️ usando Java, JavaFX y estructuras de datos avanzadas para la Universidad del Quindío.*