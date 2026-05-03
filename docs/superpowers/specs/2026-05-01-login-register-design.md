# Diseño de Login y Registro para LimitTx

Este documento detalla la implementación de las pantallas de Login y Registro para la aplicación Android **LimitTx**.

## 1. Descripción General
La aplicación requiere un sistema de autenticación inicial que conste de dos actividades principales (`LoginActivity` y `RegisterActivity`) con navegación fluida entre ellas. El diseño seguirá las guías de Material Design (Opción 1 elegida por el usuario).

## 2. Pantalla de Login (`LoginActivity`)
Esta será la actividad de inicio (Launcher).

### Componentes Visuales:
- **Logo**: `ImageView` que muestra `icono.png` (renombrado a `logo_app.png`). Centrado en la parte superior.
- **Campo Usuario**: `TextInputLayout` con `TextInputEditText`.
- **Campo Contraseña**: `TextInputLayout` con `TextInputEditText` (tipo `textPassword`).
- **Recordar Contraseña**: `CheckBox` con el texto "Recordar contraseña".
- **Botón Entrar**: `MaterialButton` con el texto "ENTRAR".
- **Enlace Registro**: `TextView` con el texto "¿No tienes cuenta? Regístrate".

## 3. Pantalla de Registro (`RegisterActivity`)

### Componentes Visuales:
- **Campo Usuario**: `TextInputLayout` con `TextInputEditText`.
- **Campo Contraseña**: `TextInputLayout` con `TextInputEditText` (tipo `textPassword`).
- **Botón Registrarse**: `MaterialButton` con el texto "REGISTRARSE".
- **Enlace Login**: `TextView` con el texto "¿Ya tienes cuenta? Inicia sesión".

## 4. Navegación
- **Login -> Registro**: Al pulsar en el `TextView` de registro.
- **Registro -> Login**: Al pulsar en el `TextView` de inicio de sesión.
- Implementado mediante `Intent` explícitos.

## 5. Recursos
- `icono.png`: Ubicado originalmente en la raíz, se copiará a `app/src/main/res/drawable/logo_app.png`.
- Colores y temas: Se mantendrán los predeterminados de Material Design definidos en el proyecto.

## 6. Lógica (Futura)
- No se implementará validación de credenciales ni almacenamiento en esta fase, solo la estructura UI y navegación.
- El estado del CheckBox "Recordar contraseña" quedará listo para su futura implementación con `SharedPreferences`.
