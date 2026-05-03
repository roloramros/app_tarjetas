# Add Card (Tarjetas) Android Design Spec

## 1. Objective
Implement a feature in the Android application to allow authenticated users to add new credit/debit cards. This involves a UI entry point (FAB), a data entry form (BottomSheet), and integration with the backend API's `/tarjetas` endpoint.

## 2. UI Components

### 2.1. FloatingActionButton (FAB)
- **Location**: `MainActivity`.
- **Icon**: `ic_add` (standard Material icon).
- **Action**: Opens the `AddTarjetaBottomSheet`.

### 2.2. AddTarjetaBottomSheet (`BottomSheetDialogFragment`)
- **Layout**: `layout_add_tarjeta.xml`.
- **Fields**:
    - **Nombre**: `TextInputLayout` + `TextInputEditText`.
    - **Número**: `TextInputLayout` + `TextInputEditText` (Numeric input).
    - **Banco**: `Spinner` with options: `BPA`, `BANDEC`, `METRO`, `CLASICA`, `TROPICAL`.
    - **Moneda**: `Spinner` with options: `CUP`, `USD`.
    - **Límite Mensual**: `TextInputLayout` + `TextInputEditText` (Decimal input).
    - **Save Button**: `MaterialButton`.

## 3. Interaction Logic (UI)
- **Currency Constraint**: 
    - When `Banco` is `CLASICA` or `TROPICAL`, the `Moneda` spinner must be set to `USD` and disabled (`setEnabled(false)`).
    - For other banks, `Moneda` is enabled and defaults to `CUP`.
- **Validation**:
    - Ensure all fields are filled before sending the request.
    - Show error messages using `setError` on `TextInputLayout`.

## 4. API Integration

### 4.1. Data Models (`com.codram.limitx.data.api`)
- **`TarjetaRequest.java`**:
    - `String nombre`
    - `String numero`
    - `String banco`
    - `String moneda`
    - `double limite_mensual`
    - `boolean activa` (default true)
- **`TarjetaResponse.java`**:
    - `UUID id`
    - `UUID usuario_id`
    - (Include fields from request for completeness)

### 4.2. API Service (`ApiService.java`)
- New method:
  ```java
  @POST("/tarjetas")
  Call<TarjetaResponse> createTarjeta(
      @Header("Authorization") String token,
      @Body TarjetaRequest request
  );
  ```

## 5. Implementation Flow
1. User clicks FAB in `MainActivity`.
2. `AddTarjetaBottomSheet` is displayed.
3. User enters data; logic handles bank-currency restrictions.
4. On "Save":
    - Retrieve JWT token from `SessionManager`.
    - Format token as `"Bearer <token>"`.
    - Call `createTarjeta` via Retrofit.
    - **On Success**: Close modal, show `Toast` in `MainActivity`.
    - **On Failure**: Show `Toast` or message in modal.

## 6. Testing Strategy
- **Unit Tests**: Verify `TarjetaRequest` and `TarjetaResponse` serialization/deserialization.
- **UI Interaction**: Manually verify that selecting "CLASICA" or "TROPICAL" correctly locks the currency to "USD".
- **Integration**: Verify that a valid card is correctly saved to the backend database.
