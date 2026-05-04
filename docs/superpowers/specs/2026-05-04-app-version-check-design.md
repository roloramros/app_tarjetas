# Design Doc - App Version Check

Implementation of a version verification system to ensure users are notified of new updates.

## Problem Statement
The app currently has a hardcoded version string in `strings.xml` ("v1.0"). There is no mechanism to notify users when a newer version is available on the official Telegram channel.

## Proposed Solution
Introduce a backend endpoint that provides the latest version number stored in the database. The Android app will query this endpoint on startup and compare it with its local version string. If a mismatch is detected, an alert dialog will prompt the user to update.

## Detailed Design

### 1. Backend (FastAPI / PostgreSQL)
- **Model:** New `AppVersion` class in `models.py`.
    - Table name: `app_version`
    - Column: `version` (character varying(50))
- **Schema:** New `AppVersionResponse` in `schemas.py`.
- **Endpoint:** `GET /app-version` (Public)
    - Returns: `{"version": "..."}`
    - Logic: Fetches the first record from the `app_version` table.

### 2. Android (Java)
- **Hardcoded Constants:**
    - Local version: `"1.0"` (to match the logic of `nav_header_welcome`).
    - Update URL: `https://t.me/codram_software/3`
- **API Service:** Add `Call<AppVersionResponse> getAppVersion()` to `ApiService.java`.
- **Version Check Logic:**
    - Perform an asynchronous call to `/app-version`.
    - Compare the returned string with the local constant.
    - If different, show an `AlertDialog`.
- **Integration Points:**
    - `LoginActivity`: Check on start to catch users before login.
    - `MainActivity`: Check on start for users with "Remember Me" sessions.

### 3. User Interface
- **AlertDialog:**
    - Title: "Actualización disponible"
    - Message: "Hay una nueva actualización disponible de LimiTx. Por favor, descarga la última versión."
    - Positive Button: "Actualizar" (Redirects to Telegram).
    - Negative Button: "Cerrar" (Dismisses dialog).

## Testing Plan
- **Backend:** Verify `GET /app-version` returns the correct version from the DB.
- **Android (Manual):**
    - Set local version to "1.0" and DB version to "1.1" -> Verify dialog appears.
    - Set local version to "1.1" and DB version to "1.1" -> Verify no dialog appears.
    - Verify clicking "Actualizar" opens the Telegram link correctly.
