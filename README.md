#Music Player App 🎵
https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white
https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white
https://img.shields.io/badge/Jetpack%2520Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white

Una moderna aplicación reproductor de música para Android, desarrollada con las últimas tecnologías de Android Jetpack.

Características ✨
� Reproductor local de archivos MP3

📁 Explorador de archivos musicales del dispositivo

🎶 Creación y gestión de playlists

💾 Almacenamiento persistente con SQLite

🎨 Interfaz moderna con Jetpack Compose

🔍 Búsqueda de canciones

🔄 Fondo animado que reacciona a la música

Tecnologías Utilizadas 🛠️
Lenguaje: Kotlin

UI: Jetpack Compose (Android 10+)

Persistencia: SQLite con Room

Reproducción: MediaPlayer/ExoPlayer

Inyección de dependencias: Hilt

Arquitectura: MVVM (Model-View-ViewModel)

Navegación: Navigation Compose

Gestión de permisos: Accompanist Permissions

Corrutinas: Para operaciones asíncronas

Requisitos 📋
Android 10 (API 29) o superior

Permisos de almacenamiento para acceder a los archivos MP3

Instalación ⚙️
Clona el repositorio:

bash
git clone https://github.com/tu-usuario/music-player-app.git
Abre el proyecto en Android Studio

Sincroniza las dependencias de Gradle

Ejecuta la app en un emulador o dispositivo físico

Estructura del Proyecto 🗂️
text
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/RiberasPlayer/
│   │   │   ├── model/          # Capa de datos (Room, DAOs, Repositorios)
│   │   │   ├── view/        # Lógica de negocio
│   │   │   ├── ui/            # Componentes de UI y ViewModels
│   │   │   │   ├── components # Componentes reutilizables
│   │   │   │   ├── screens    # Pantallas principales
│   │   │   │   └── theme      # Estilos y temas
│   │   │   └── utils/         # Utilidades y extensiones
│   │   └── res/               # Recursos tradicionales

Contribución 🤝
Las contribuciones son bienvenidas! Por favor abre un issue primero para discutir los cambios que te gustaría hacer.

Licencia 📄
Este proyecto está licenciado bajo la Licencia MIT - ver el archivo LICENSE para más detalles.

Hecho con ❤️ usando Jetpack Compose

