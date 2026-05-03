# Diseño: Adición de Transacciones (Long Press en Tarjeta)

## Resumen
Funcionalidad para añadir transacciones (Entrada/Salida) mediante una interacción de "Long Press" en las tarjetas de la pantalla principal, utilizando un `AlertDialog` para la entrada de datos.

## Flujo de Usuario
1.  **Long Press:** El usuario mantiene presionada una tarjeta en la `MainActivity`.
2.  **Menú de Selección:** Se muestra un `PopupMenu` con las opciones: "Entrada" y "Salida".
3.  **Modal de Entrada (AlertDialog):** Al seleccionar una opción, se abre un `AlertDialog` personalizado.
4.  **Formulario:**
    *   **Título:** Tipo (Entrada/Salida) + Nombre de la tarjeta.
    *   **Monto:** Campo numérico (Decimal).
    *   **Descripción:** Campo de texto.
    *   **Subtipo:** Campo de texto.
    *   **Fecha:** Selector (`MaterialDatePicker`) con fecha actual por defecto.
    *   **Afecta Límite:** `Switch` (por defecto `true`).
5.  **Acciones:**
    *   **Cancelar:** Cierra el diálogo.
    *   **Aceptar:** Por ahora, muestra un `Toast` con los datos recolectados.

## Componentes Técnicos
*   **UI:** `AlertDialog` con layout personalizado.
*   **Interacción:** `OnLongClickListener` en el `TarjetasAdapter` (o `MainActivity`).
*   **Navegación:** `PopupMenu` para la elección de tipo.
*   **Verificación:** `Toast` para confirmar la captura de datos antes de la integración con la API.
