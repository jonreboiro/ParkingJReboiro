# ParkingApp - Sistema de Gestión de Reservas de Parking

## Descripción General

ParkingApp es una aplicación móvil que permite a los usuarios gestionar reservas de plazas de aparcamiento de manera sencilla e intuitiva. Diseñada para optimizar el uso de espacios de parking y mejorar la experiencia del usuario al aparcar, esta aplicación ofrece una interfaz amigable y funcionalidades completas para gestionar todo el ciclo de vida de una reserva de aparcamiento.

## Tecnologías y Arquitectura

- **Plataforma**: Android
- **Lenguaje**: Java
- **Entorno de desarrollo**: Android Studio
- **Arquitectura**: Modelo-Vista-ViewModel (MVVM)
- **Almacenamiento de datos**: Firebase Firestore
- **Autenticación**: Firebase Authentication

## Características Principales

### Autenticación de Usuario

- **Registro de usuario**: Creación de cuenta con correo electrónico y contraseña
- **Inicio de sesión estándar**: Acceso mediante correo electrónico y contraseña
- **Inicio de sesión con Google**: Integración con cuentas de Google para acceso rápido
- **Recuperación de contraseña**: Sistema para restablecer contraseñas olvidadas

### Dashboard Principal

- **Vista centralizada**: Acceso rápido a todas las funcionalidades principales
- **Reservas activas**: Visualización inmediata de las reservas en curso
- **Navegación intuitiva**: Acceso a las diferentes secciones de la aplicación
- **Diseño minimalista**: Interfaz ligera que no entorpece la experiencia de usuario

### Sistema de Reservas

#### Reserva Automática
- **Búsqueda inteligente**: Encuentra automáticamente plazas disponibles según preferencias
- **Selección de tipo de plaza**: Opciones para plazas estándar, para minusválidos, para vehículos eléctricos y motos
- **Selección de fecha y hora**: Calendario y selector de tiempo para programar la reserva
- **Duración personalizable**: Selección flexible del tiempo de reserva

#### Reserva Manual
- **Plano interactivo**: Visualización gráfica del parking con plazas disponibles y ocupadas
- **Selección directa**: Posibilidad de elegir exactamente la plaza deseada
- **Vista por plantas**: Navegación entre diferentes niveles del parking
- **Información de plazas**: Detalles sobre tipo y disponibilidad de cada plaza

### Gestión de Reservas

- **Reservas activas**: Visualización de reservas en curso con temporizador en tiempo real
- **Reservas próximas**: Listado de reservas programadas con opción de cancelación
- **Historial de reservas**: Registro completo de reservas pasadas
- **Detalles de reserva**: Información completa incluyendo ubicación visual en el plano del parking
- **Cancelación de reservas**: Posibilidad de cancelar reservas futuras

### Sistema de Notificaciones

- **Confirmación de reserva**: Notificación al realizar una reserva exitosamente
- **Recordatorio previo**: Aviso 5 minutos antes del inicio de la reserva
- **Inicio de reserva**: Notificación cuando comienza el período reservado
- **Finalización próxima**: Aviso 5 minutos antes de que termine la reserva
- **Fin de reserva**: Notificación al finalizar el tiempo de reserva
- **Cancelación**: Aviso al cancelar una reserva existente

### Perfil de Usuario

- **Datos personales**: Visualización y edición de información del usuario
- **Gestión de matrículas**: Añadir, editar y eliminar matrículas de vehículos
- **Cambio de contraseña**: Opción para usuarios con autenticación estándar
- **Cierre de sesión**: Función para salir de la cuenta actual

## Experiencia de Usuario

La aplicación ha sido diseñada pensando en la experiencia del usuario, con un enfoque en la simplicidad y eficiencia:

- **Dashboard centralizado**: Acceso rápido a todas las funcionalidades sin necesidad de menús redundantes
- **Flexibilidad**: Múltiples opciones para realizar reservas según las preferencias del usuario
- **Feedback visual**: Confirmaciones claras de todas las acciones realizadas
- **Sistema de notificaciones**: Mantiene al usuario informado en todo momento sobre el estado de sus reservas

## Sobre el Proyecto

Esta aplicación ha sido desarrollada como parte del programa formativo en el aula empresa de LKSNext, aplicando buenas prácticas de desarrollo y enfocándose en crear una solución útil y práctica para la gestión de espacios de parking.

---

*© 2025 Park W'Us - Todos los derechos reservados*
