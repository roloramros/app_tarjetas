# Design Spec: Transaction Dialog Refactoring

Refactor the transaction creation dialog logic to be reusable across the application, specifically for the new FABs in `HistorialActivity`.

## 1. Architecture Changes

### 1.1. `TransactionDialogHelper.java` (New Utility)
- Package: `com.codram.limitx.utils`
- Purpose: Provide a static method to show the "Add Transaction" dialog.
- Interface: Move `OnTransactionAddedListener` from `TarjetasAdapter` to this class.
- Method: `public static void showTransactionDialog(Context context, String tipo, UUID tarjetaId, String tarjetaNombre, OnTransactionAddedListener listener)`

### 1.2. `TarjetasAdapter.java`
- Remove `OnTransactionAddedListener` interface (move to helper).
- Remove `mostrarDialogoTransaccion` private method.
- Update long-press actions to call `TransactionDialogHelper.showTransactionDialog`.
- Update item click (navigation to history) to pass `TARJETA_NOMBRE` in the Intent.

## 2. Integration with `HistorialActivity`

### 2.1. Intent Extras
- `TARJETA_ID`: Existing UUID as string.
- `TARJETA_NOMBRE`: New string extra for the card name.

### 2.2. Logic
- In `onCreate`, retrieve and store both extras in class members.
- Update FAB `OnClickListener`:
  - `fabAdd`: Call `TransactionDialogHelper.showTransactionDialog` with "Depósito".
  - `fabRemove`: Call `TransactionDialogHelper.showTransactionDialog` with "Extracción".
- Refresh logic: The listener callback will trigger `cargarTransacciones(tarjetaId)`.

## 3. Implementation Details

### 3.1. `TransactionDialogHelper.showTransactionDialog`
- Uses `LayoutInflater` to inflate `R.layout.dialog_transaccion`.
- Sets up `MaterialDatePicker` for date selection.
- Handles "Depósito" vs "Extracción" visibility for `swAfectaLimite`.
- Calls `ApiClient.getService().crearTransaccion`.
- Triggers the `OnTransactionAddedListener` on success.

## 4. Testing Criteria
- Verify Long Press on a card still opens the correct dialog and refreshes the main screen.
- Verify clicking a card correctly navigates to `HistorialActivity`.
- Verify clicking the Green FAB in `HistorialActivity` opens the "Depósito" dialog with the correct card name.
- Verify clicking the Red FAB in `HistorialActivity` opens the "Extracción" dialog with the correct card name.
- Verify that saving a transaction from `HistorialActivity` refreshes the list automatically.
