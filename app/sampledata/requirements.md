# Guía Plantilla Parking
**Aula de Empresa 2024**

## Introducción
Esta guía se otorga junto con la plantilla que será la base del proyecto de parking.  
Se presentará la arquitectura, las distintas clases de dominio y las principales *Activities*.

## Arquitectura de la aplicación
La arquitectura que seguirá la aplicación será la explicada: **arquitectura MVVM**.

La división de clases será la siguiente:

- **Modelos de dominio**: Hora, Plaza, Reserva.
- **Activities**: LoginActivity, MainActivity, RegisterActivity.
- **Interfaz**: LoginActivity, MainActivity, RegisterActivity.
- **Interfaces (layouts)**: activity_login.xml, activity_main.xml, activity_register.xml.
- **ViewModels**: LoginViewModel, MainViewModel, RegisterViewModel.

### Descripción de la arquitectura

#### Modelos de dominio
Contienen los atributos y métodos relacionados con los datos de la aplicación.

#### Activities
Son las pantallas que se mostrarán al usuario.  
Cada una de ellas tiene su propia interfaz y se encarga de gestionar el flujo de la aplicación.

#### Interfaz
Contiene los elementos visuales y su diseño.  
Cada *Activity* tendrá su propio archivo XML que definirá cómo se verá la interfaz.

#### ViewModels
Gestionan la lógica de negocio y las operaciones relacionadas con los datos.  
Se comunican con los modelos de dominio y actualizan la interfaz según sea necesario.

---

## Diagrama de clases
![Diagrama](https://raw.githubusercontent.com/jesusgonzalezperez/PlantillaParking/main/app/sampledata/class_diagram.png)

---

## Estructura de carpetas
La estructura del proyecto será la siguiente:

```
com/
└── example/
    └── parking/
        ├── activities/
        │   ├── LoginActivity.java
        │   ├── MainActivity.java
        │   └── RegisterActivity.java
        ├── domain/
        │   ├── Hora.java
        │   ├── Plaza.java
        │   └── Reserva.java
        ├── viewmodels/
        │   ├── LoginViewModel.java
        │   ├── MainViewModel.java
        │   └── RegisterViewModel.java
        └── interfaces/
            ├── activity_login.xml
            ├── activity_main.xml
            └── activity_register.xml
```

---

## Clases

### Clases de dominio

- **Modelos de dominio**: Hora, Plaza, Reserva.
- **Activities**: LoginActivity, MainActivity, RegisterActivity.
- **Interfaz**: activity_login.xml, activity_main.xml, activity_register.xml.
- **ViewModels**: LoginViewModel, MainViewModel, RegisterViewModel.
- **Interfaces (layouts)**: en su respectivo apartado.

---

## Clases de Dominio

### Hora
Representa la hora en la que se realiza la reserva de la plaza.  
Cuenta con dos atributos:
- `horaInicio`: Hora a la que empieza la reserva.
- `horaFin`: Hora a la que termina la reserva.

### Plaza
Representa la plaza sobre la que se realizará la reserva.  
Cuenta con dos atributos:
- `id`: Identificador único de la plaza.
- `tipo`: Tipo de plaza (estándar, eléctrico, discapacitados o moto).

### Reserva
Contiene los datos sobre la reserva realizada.  
Cuenta con seis atributos:
- `fecha`: Fecha en la que se realiza la reserva.
- `usuario`: Persona que realiza la reserva.
- `id`: Identificador de la reserva.
- `plaza`: Plaza sobre la que se realiza la reserva.
- `hora`: Hora sobre la que se realiza la reserva.

---

## Activities

### LoginActivity
En esta clase se integran todas las clases relacionadas con el **Login**, como el *ViewModel* y la *Interfaz*.  
Además, se comprueba qué hacer en caso de que el login sea correcto y se gestionan los saltos entre *Activities* mediante *Intents*.

### MainActivity
En esta clase se integran todas las clases relacionadas con la **página principal de las reservas**, como el *ViewModel* y la *Interfaz*.  
También se gestiona la navegación entre las distintas pestañas dentro de la aplicación.

### RegisterActivity
En esta clase se integran todas las clases relacionadas con el **registro de usuarios**, como el *ViewModel* y la *Interfaz*.  
Se comprueba qué hacer en caso de que el registro sea adecuado y se gestionan los saltos entre *Activities* mediante *Intents*.

---

## ViewModels

### LoginViewModel
Gestiona todas las operaciones y variables *LiveData* relacionadas con el **Login**.  
Por ejemplo, la variable `logged`, que indica si el usuario se ha logueado correctamente o no.

### MainViewModel
Gestiona todas las operaciones y variables *LiveData* relacionadas con la **página principal de reservas**.

### RegisterViewModel
Gestiona todas las operaciones y variables *LiveData* relacionadas con el **registro de usuarios**.