# AdminUsuariosActivity Redesign Spec

**Goal:** Enhance `AdminUsuariosActivity` with standardized navigation (Side Menu, Toolbar) and improve date readability by formatting them.

## 1. UI Components

### 1.1 Layout (`activity_admin_usuarios.xml`)
- Wrap the existing content in a `androidx.drawerlayout.widget.DrawerLayout`.
- Add a `androidx.coordinatorlayout.widget.CoordinatorLayout` as the first child of the drawer.
- Add an `com.google.android.material.appbar.AppBarLayout` containing a `com.google.android.material.appbar.MaterialToolbar`.
- The existing `LinearLayout` content will be moved inside a `NestedScrollView` or kept as is but set with `app:layout_behavior="@string/appbar_scrolling_view_behavior"`.
- Add the `com.google.android.material.navigation.NavigationView` and the footer layout for the side menu, matching `activity_main.xml`.

### 1.2 Toolbar
- ID: `@+id/toolbar`
- Title: "Administración de Usuarios"

## 2. Functionality

### 2.1 Side Navigation
- Implement `ActionBarDrawerToggle` to enable the hamburger icon.
- Implement `NavigationView.OnNavigationItemSelectedListener`.
- Reuse `nav_menu` actions (specifically "Añadir Tarjeta" and "Cerrar Sesión").

### 2.2 Date Formatting
- Parse backend dates (ISO 8601 strings) into `Date` objects.
- Format `Date` objects to `"dd MMMM yyyy"` using `SimpleDateFormat` with Spanish `Locale`.
- Display "N/A" or "Nunca" if the date is null.

## 3. Technical Details

### 3.1 Date Parsing/Formatting Helper
```java
private String formatBackendDate(String rawDate) {
    if (rawDate == null || rawDate.isEmpty()) return "N/A";
    try {
        // Handle ISO 8601 with or without milliseconds
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        Date date = inputFormat.parse(rawDate);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd 'de' MMMM yyyy", new Locale("es", "ES"));
        return outputFormat.format(date);
    } catch (Exception e) {
        return rawDate; // Fallback to raw string if parsing fails
    }
}
```
*Note: The user asked for "01 Enero 2026". "01 de Enero 2026" is also common, but I will stick to the exact "01 Enero 2026" format unless "de" is preferred.*
Actually, the user said "01 Enero 2026", so I'll use `dd MMMM yyyy`.

### 3.2 Navigation Setup
- Similar to `MainActivity.java`, using `DrawerLayout`, `Toolbar`, and `ActionBarDrawerToggle`.
