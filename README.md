# Recibo P3 - Sistema de Recibos Electrónicos

## Descripción
Aplicación Android profesional para generar recibos y comprobantes de pago en Perú (Soles - S/). 

## Características
- ✅ Gestión de empresa con firma digital
- ✅ Generación de recibos con numeración automática
- ✅ Pago total o parcial
- ✅ Exportación a PDF
- ✅ Compartir recibos por WhatsApp, email, etc.
- ✅ Búsqueda por fecha
- ✅ Anulación de recibos
- ✅ Modo claro/oscuro automático
- ✅ Material Design 3

## Requisitos
- Android Studio Hedgehog o superior
- Min SDK: 23 (Android 6.0)
- Target SDK: 34 (Android 14)
- Kotlin 1.9.0

## Instalación
1. Abrir el proyecto en Android Studio
2. Esperar a que Gradle sincronice las dependencias
3. Ejecutar en un dispositivo o emulador

## Arquitectura
- **MVVM** (Model-View-ViewModel)
- **Room** para base de datos local
- **LiveData** para observación de datos
- **Coroutines** para operaciones asíncronas
- **ViewBinding** para acceso a vistas

## Estructura del Proyecto
```
app/
├── src/main/
│   ├── java/com/p3/recibop3/
│   │   ├── data/
│   │   │   ├── entity/      # Entidades Room
│   │   │   ├── dao/         # DAOs
│   │   │   └── repository/  # Repositorios
│   │   ├── ui/
│   │   │   ├── viewmodel/   # ViewModels
│   │   │   ├── adapter/     # RecyclerView Adapters
│   │   │   └── view/        # Custom Views
│   │   └── utils/           # Utilidades
│   └── res/
│       ├── layout/          # Layouts XML
│       ├── values/          # Recursos (strings, colors, themes)
│       └── mipmap/          # Iconos de la app
```

## Uso
1. **Primera vez**: Registrar datos de la empresa y firma
2. **Nuevo Recibo**: Ingresar datos del cliente y productos/servicios
3. **Generar PDF**: El sistema crea automáticamente el PDF
4. **Compartir**: Enviar el recibo por cualquier medio

## Desarrollado por
P3 Development Team

## Licencia
Todos los derechos reservados © 2026
