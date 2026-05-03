# Diseño: Pestañas de Moneda (CUP/USD) en MainActivity

## Resumen
Reorganización de la pantalla principal para separar las tarjetas por moneda (CUP y USD) utilizando una interfaz de pestañas con navegación fluida.

## Componentes de Interfaz
- **TabLayout:** Situado debajo de la Toolbar, con dos pestañas: "CUP" y "USD".
- **ViewPager2:** Permite el desplazamiento horizontal entre las listas de tarjetas.
- **TarjetasFragment:** Fragmento reutilizable que contiene la lógica de visualización de la lista (`RecyclerView`, `SwipeRefreshLayout`, `EmptyState`).
- **Saldo Total Dinámico:** El `tvSaldoTotal` en la Toolbar se actualizará para mostrar la suma de los saldos de la moneda activa en la pestaña actual.

## Arquitectura Técnica
- **Navegación:** `FragmentStateAdapter` para gestionar las instancias de `TarjetasFragment`.
- **Comunicación:** `MainActivity` descarga los datos y los distribuye a los fragmentos. Los fragmentos notifican a la Activity sobre eventos de refresco o transacciones añadidas.
- **Lógica de Filtrado:** Las tarjetas se filtran por el campo `moneda` de la respuesta de la API.

## Flujo de Usuario
1. El usuario entra en la App y ve por defecto la pestaña "CUP".
2. Al deslizar o tocar "USD", la lista cambia y el "Saldo Total" de la cabecera se actualiza al total de USD.
3. El botón de ordenar afecta solo a la lista visible.
4. Añadir una transacción o tarjeta refresca ambas listas a través de la Activity principal.
