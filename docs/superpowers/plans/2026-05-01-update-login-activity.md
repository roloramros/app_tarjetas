# Update LoginActivity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integrate login API and session management into LoginActivity, and update its layout.

**Architecture:** Update `LoginActivity.java` to use `ApiClient` for network calls and `SessionManager` for persistence. Update `activity_login.xml` for UI changes and view identification.

**Tech Stack:** Android, Java, Retrofit, Material Components.

---

### Task 1: Update Login Layout

**Files:**
- Modify: `app/src/main/res/layout/activity_login.xml`

- [ ] **Step 1: Modify layout elements**
  - Change `android:text="Recordar contraseña"` to `android:text="Mantener logueado"` in `cbRemember`.
  - Add `android:id="@+id/etUser"` to `TextInputEditText` inside `tilUser`.
  - Add `android:id="@+id/etPassword"` to `TextInputEditText` inside `tilPassword`.

### Task 2: Update LoginActivity Logic

**Files:**
- Modify: `app/src/main/java/com/codram/limitx/LoginActivity.java`

- [ ] **Step 1: Replace LoginActivity content**
  - Use the provided code to handle session checking, login API call, and saving the token.

### Task 3: Verification and Commit

- [ ] **Step 1: Verify changes**
  - Check if the project builds successfully (using `./gradlew assembleDebug`).
- [ ] **Step 2: Commit changes**
  - `git add app/src/main/res/layout/activity_login.xml app/src/main/java/com/codram/limitx/LoginActivity.java`
  - `git commit -m "feat: integrate login api and session management"`
