# MainActivity UI Refinement Design Spec

## 1. Objective
Refactor the `MainActivity` UI to achieve a modern, edge-to-edge look. This involves adding a title bar, making the system bars transparent, and reorganizing the layout structure for better aesthetics and usability.

## 2. Core UI/UX Changes
- **Edge-to-Edge Display**: The app content will draw behind the system status and navigation bars.
- **Title Bar**: A `MaterialToolbar` will be added at the top to display the title "Mis Tarjetas".
- **Layout Structure**: The root layout will be a `CoordinatorLayout` to manage interactions between the app bar and the scrolling content.
- **Button Repositioning**: The logout button will be moved to the bottom-left corner, styled as a secondary `FloatingActionButton`.

## 3. Technical Implementation

### 3.1. Theme and Window Configuration
- **`themes.xml` & `themes.xml (night)`**:
    - The parent theme will remain `Theme.MaterialComponents.DayNight.NoActionBar`.
    - `android:statusBarColor` will be set to `@android:color/transparent`.
    - `android:navigationBarColor` will be added and set to `@android:color/transparent`.
- **`MainActivity.java`**:
    - In `onCreate`, before `setContentView`, `WindowCompat.setDecorFitsSystemWindows(getWindow(), false)` will be called to enable drawing behind system bars.

### 3.2. Layout Structure (`activity_main.xml`)
- The root view will be changed from `ConstraintLayout` to `androidx.coordinatorlayout.widget.CoordinatorLayout`.
- An `com.google.android.material.appbar.AppBarLayout` will be added at the top.
    - It will contain a `com.google.android.material.appbar.MaterialToolbar` with ID `toolbar` and the title "Mis Tarjetas".
- The `RecyclerView` (`rvTarjetas`) will have `app:layout_behavior="@string/appbar_scrolling_view_behavior"` to ensure it scrolls correctly under the `AppBarLayout`.
- The `ProgressBar` and `TextView` for empty/error states will be centered within the main content area.
- The `btnLogout` will be changed to a `FloatingActionButton` and constrained to the bottom-left of the `CoordinatorLayout`.
- The `fabAdd` will remain a `FloatingActionButton` constrained to the bottom-right.

### 3.3. Handling Window Insets
- **`MainActivity.java`**:
    - `ViewCompat.setOnApplyWindowInsetsListener` will be attached to the root `CoordinatorLayout`.
    - The listener will receive the system bar `insets` (top, bottom, left, right).
    - **Padding**: The top padding of the `AppBarLayout` and the bottom padding of the `RecyclerView` will be adjusted to account for the insets, preventing content from being hidden.
    - **Margins**: The bottom margin of both `FloatingActionButton`s (`btnLogout` and `fabAdd`) will be adjusted to sit correctly above the navigation bar.

## 4. Code Implementation (`MainActivity.java`)
- `onCreate` will be updated to:
    1. Enable edge-to-edge.
    2. Set the content view.
    3. Find the new `Toolbar` and set it as the support action bar using `setSupportActionBar(toolbar)`.
    4. Set up the window insets listener.
    5. The existing logic for initializing the RecyclerView, FABs, and loading data will be preserved.

## 5. Testing Strategy
- **Visual Verification**:
    - Confirm the app content (specifically the `RecyclerView`) scrolls behind the status bar.
    - Confirm the navigation bar is transparent and the FABs are positioned correctly above it.
    - Check both light and dark modes to ensure text on the `Toolbar` and system bars is legible.
- **Functional Verification**:
    - Confirm that the `RecyclerView` is still scrollable and its items are fully visible.
    - Confirm that the "Logout" and "Add" FABs are still clickable and functional.
    - Confirm that the loading, empty, and error states are still displayed correctly within the new layout.
