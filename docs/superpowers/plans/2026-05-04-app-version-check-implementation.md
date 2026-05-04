# App Version Check Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement app version check logic in Login and Main activities to notify users about available updates.

**Architecture:** Add a `checkVersion()` method to both `LoginActivity` and `MainActivity` that calls the `/app-version` API, compares the current version with the latest, and displays an `AlertDialog` with an update link if they differ.

**Tech Stack:** Java, Android SDK, Retrofit, OkHttp.

---

### Task 1: Implement Version Check in LoginActivity

**Files:**
- Modify: `.worktrees/app-version-check/app/src/main/java/com/codram/limitx/LoginActivity.java`

- [ ] **Step 1: Add necessary imports**
- [ ] **Step 2: Add `checkVersion()` and `showUpdateDialog()` methods**
- [ ] **Step 3: Call `checkVersion()` in `onCreate()`**

```java
// Logic to add:
private void checkVersion() {
    String currentVersion = "1.0"; // Should match nav_header_welcome and strings.xml
    ApiClient.getService().getAppVersion().enqueue(new Callback<AppVersionResponse>() {
        @Override
        public void onResponse(Call<AppVersionResponse> call, Response<AppVersionResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                String latestVersion = response.body().getVersion();
                if (!currentVersion.equals(latestVersion)) {
                    showUpdateDialog();
                }
            }
        }
        @Override
        public void onFailure(Call<AppVersionResponse> call, Throwable t) {
            // Silently ignore or log error
        }
    });
}

private void showUpdateDialog() {
    new androidx.appcompat.app.AlertDialog.Builder(this)
        .setTitle("Actualización disponible")
        .setMessage("Hay una nueva actualización disponible de LimiTx. Por favor, descarga la última versión.")
        .setPositiveButton("Actualizar", (dialog, which) -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse("https://t.me/codram_software/3"));
            startActivity(intent);
        })
        .setNegativeButton("Cerrar", null)
        .show();
}
```

### Task 2: Implement Version Check in MainActivity

**Files:**
- Modify: `.worktrees/app-version-check/app/src/main/java/com/codram/limitx/MainActivity.java`

- [ ] **Step 1: Add necessary imports**
- [ ] **Step 2: Add `checkVersion()` and `showUpdateDialog()` methods**
- [ ] **Step 3: Call `checkVersion()` in `onCreate()`**

```java
// Logic to add (same as LoginActivity)
```

### Task 3: Commit Changes

- [ ] **Step 1: Commit the changes**

Run: `git add . && git commit -m "feat(android): implement version check in Login and Main activities"`
(Note: Run this inside the worktree)
