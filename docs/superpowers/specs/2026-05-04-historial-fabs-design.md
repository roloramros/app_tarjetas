# Design Spec: Transaction History FABs

Add two Floating Action Buttons (FABs) to the `HistorialActivity` to allow users to quickly trigger deposit and extraction actions.

## 1. Visual Resources

### 1.1. Colors
Add the following to `res/values/colors.xml`:
- `green_500`: `#4CAF50` (Success/Add)
- `red_500`: `#F44336` (Error/Remove)

### 1.2. Icons
Create the following vector drawables in `res/drawable/`:
- `ic_add.xml`: A white plus (+) sign.
- `ic_remove.xml`: A white minus (-) sign.

## 2. Layout Changes

### 2.1. `activity_historial.xml`
Modify the layout to include a vertical `LinearLayout` at the bottom-right of the `CoordinatorLayout`:
- `android:layout_gravity="bottom|end"`
- `android:layout_margin="16dp"`
- `android:orientation="vertical"`

Inside the `LinearLayout`:
1. `FloatingActionButton` (Green):
   - ID: `fabAdd`
   - Icon: `@drawable/ic_add`
   - Background Tint: `@color/green_500`
   - `android:layout_marginBottom="8dp"`
2. `FloatingActionButton` (Red):
   - ID: `fabRemove`
   - Icon: `@drawable/ic_remove`
   - Background Tint: `@color/red_500`

## 3. Logic Implementation

### 3.1. `HistorialActivity.java`
- Initialize `fabAdd` and `fabRemove` in `onCreate`.
- Implement `OnClickListener` for both:
  - `fabAdd`: Show Toast "Añadir depósito".
  - `fabRemove`: Show Toast "Añadir extracción".

## 4. Testing Criteria
- Verify that both FABs are visible in the bottom-right corner of the transaction history screen.
- Verify that the Green button shows "Añadir depósito" Toast when clicked.
- Verify that the Red button shows "Añadir extracción" Toast when clicked.
- Verify that the FABs do not overlap with the transaction list content (using `CoordinatorLayout` and padding/margins).
