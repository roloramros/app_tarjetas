# Display Cards in MainActivity Design Spec

## 1. Objective
Display a list of the authenticated user's cards in `MainActivity`. This involves fetching data from the `GET /tarjetas` endpoint and displaying it in a `RecyclerView`.

## 2. UI Components

### 2.1. `activity_main.xml`
- Replace the `TextView` (`tvWelcome`) with a `RecyclerView` with ID `rvTarjetas`.
- Add a `ProgressBar` with ID `progressBar`.
- Add a `TextView` with ID `tvEmptyState` to show messages when the list is empty or fails to load.

### 2.2. Card Item Layout (`item_tarjeta.xml`)
- A `MaterialCardView` as the root element.
- `TextView` for card name (`tvCardName`).
- `TextView` for card number, which will be partially obfuscated (`tvCardNumber`).
- `TextView` for bank name (`tvBankName`).
- `TextView` for monthly limit (`tvCardLimit`).

## 3. Data Flow and Logic

### 3.1. API Service (`ApiService.java`)
- Add a new method to fetch the user's cards:
  ```java
  @GET("/tarjetas")
  Call<List<TarjetaResponse>> getTarjetas(@Header("Authorization") String token);
  ```

### 3.2. RecyclerView Adapter (`TarjetasAdapter.java`)
- Extends `RecyclerView.Adapter`.
- Takes a `List<TarjetaResponse>` as its data source.
- Binds the data to the `item_tarjeta.xml` layout in its `onBindViewHolder` method.
- Implements logic to obfuscate the card number (e.g., show only the last 4 digits).

### 3.3. `MainActivity.java` Logic
- **`onCreate`**:
    - Initialize `RecyclerView`, `ProgressBar`, and `tvEmptyState`.
    - Call a new method `loadTarjetas()`.
- **`loadTarjetas()`**:
    - Show `progressBar` and hide `RecyclerView` and `tvEmptyState`.
    - Get the auth token from `SessionManager`.
    - Call `apiService.getTarjetas()`.
    - **On Success**:
        - Hide `progressBar`.
        - If the list is empty, show `tvEmptyState` with "No cards added yet."
        - Otherwise, show `RecyclerView`, create an instance of `TarjetasAdapter` with the list, and set it to the `RecyclerView`.
    - **On Failure**:
        - Hide `progressBar`.
        - Show `tvEmptyState` with an error message.

### 3.4. Refresh on Card Creation
- To refresh the list after a new card is added, `MainActivity` will need to be aware of the successful creation.
- A simple approach is to use `startActivityForResult` or a `BroadcastReceiver`.
- For this implementation, we will use a simpler, though less robust, method: `MainActivity` will reload the cards in its `onResume` lifecycle method. This ensures that when the user returns to the activity, the list is fresh.

## 4. Testing Strategy
- **Unit Tests**: Test the card number obfuscation logic in `TarjetasAdapter`.
- **Integration**: Verify that the `GET /tarjetas` endpoint is called and the response is correctly parsed and displayed in the `RecyclerView`.
- **UI**:
    - Verify that the `ProgressBar` shows during loading.
    - Verify that the empty/error state `TextView` is displayed correctly.
    - Verify that a list of cards is rendered correctly.
