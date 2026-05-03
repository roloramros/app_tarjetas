# Diseño: Historial de Transacciones (Vista Tabla)

## Resumen
Pantalla de historial de transacciones para una tarjeta específica, mostrando los movimientos del mes en curso en una estructura de tabla de dos columnas (Entradas y Salidas) con scroll sincronizado.

## Diseño de Pantalla
- **Cabecera Fija:** Dos columnas etiquetadas "Entradas" y "Salidas".
- **Cuerpo (Tabla):**
    - Dos listas (`RecyclerView` o `LinearLayout` dinámico) colocadas una al lado de la otra (`weight=1` cada una).
    - **Contenido por celda:** `Monto` y `(Fecha)` en la misma línea.
- **Scroll:** Sincronizado para que ambas columnas se desplacen simultáneamente.
- **Ámbito:** Limitado exclusivamente al mes en curso.

## Componentes Técnicos
- **Layout:** `LinearLayout` horizontal contenedor, con dos `RecyclerView` (o `ScrollView` con listas) configurados para compartir el mismo listener de scroll.
- **Formato:** `Monto` en negrita, seguido de `(Fecha)` con formato corto (DD/MM).
- **Lógica:** Filtrado inicial de transacciones según `fecha` dentro del rango del mes actual.

## Acciones Futuras
- Selector de mes/año para ver historial histórico.
