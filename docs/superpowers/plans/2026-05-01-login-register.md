# Login and Register Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a functional UI for Login and Register with navigation between them, using Material Design and a custom logo.

**Architecture:** Use `ConstraintLayout` for layouts, `Intent` for navigation, and `TextInputLayout` for form fields.

**Tech Stack:** Android (Java/Kotlin), Material Design Components.

---

### Task 1: Resources Setup

**Files:**
- Create: `app/src/main/res/drawable/logo_app.png` (Copy from `icono.png`)
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Move and rename logo**

Run: `cp icono.png app/src/main/res/drawable/logo_app.png`
Expected: `app/src/main/res/drawable/logo_app.png` exists.

- [ ] **Step 2: Update App Name**

Modify `app/src/main/res/values/strings.xml`:
```xml
<resources>
    <string name="app_name">LimitTx</string>
</resources>
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/drawable/logo_app.png app/src/main/res/values/strings.xml
git commit -m "chore: setup resources and app name"
```

### Task 2: Login Activity Layout

**Files:**
- Create: `app/src/main/res/layout/activity_login.xml`

- [ ] **Step 1: Create activity_login.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="24dp">

    <ImageView
        android:id="@+id/ivLogo"
        android:layout_width="120dp"
        android:layout_height="120dp"
        android:layout_marginTop="64dp"
        android:src="@drawable/logo_app"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/tilUser"
        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="48dp"
        android:hint="Usuario"
        app:layout_constraintTop_toBottomOf="@id/ivLogo">

        <com.google.android.material.textfield.TextInputEditText
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />
    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/tilPassword"
        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:hint="Contraseña"
        app:endIconMode="password_toggle"
        app:layout_constraintTop_toBottomOf="@id/tilUser">

        <com.google.android.material.textfield.TextInputEditText
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:inputType="textPassword" />
    </com.google.android.material.textfield.TextInputLayout>

    <CheckBox
        android:id="@+id/cbRemember"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="Recordar contraseña"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/tilPassword" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnEnter"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:padding="12dp"
        android:text="ENTRAR"
        app:layout_constraintTop_toBottomOf="@id/cbRemember" />

    <TextView
        android:id="@+id/tvGoToRegister"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:text="¿No tienes cuenta? Regístrate"
        android:textColor="?attr/colorPrimary"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/btnEnter" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/res/layout/activity_login.xml
git commit -m "feat: add login layout"
```

### Task 3: Register Activity Layout

**Files:**
- Create: `app/src/main/res/layout/activity_register.xml`

- [ ] **Step 1: Create activity_register.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="24dp">

    <TextView
        android:id="@+id/tvTitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="64dp"
        android:text="Registro"
        android:textSize="24sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/tilUser"
        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="48dp"
        android:hint="Usuario"
        app:layout_constraintTop_toBottomOf="@id/tvTitle">

        <com.google.android.material.textfield.TextInputEditText
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />
    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/tilPassword"
        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:hint="Contraseña"
        app:endIconMode="password_toggle"
        app:layout_constraintTop_toBottomOf="@id/tilUser">

        <com.google.android.material.textfield.TextInputEditText
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:inputType="textPassword" />
    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnRegister"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="32dp"
        android:padding="12dp"
        android:text="REGISTRARSE"
        app:layout_constraintTop_toBottomOf="@id/tilPassword" />

    <TextView
        android:id="@+id/tvGoToLogin"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:text="¿Ya tienes cuenta? Inicia sesión"
        android:textColor="?attr/colorPrimary"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/btnRegister" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/res/layout/activity_register.xml
git commit -m "feat: add register layout"
```

### Task 4: Java Activities and Navigation

**Files:**
- Create: `app/src/main/java/com/codram/limitx/LoginActivity.java`
- Create: `app/src/main/java/com/codram/limitx/RegisterActivity.java`

- [ ] **Step 1: Create LoginActivity.java**

```java
package com.codram.limitx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);
        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
```

- [ ] **Step 2: Create RegisterActivity.java**

```java
package com.codram.limitx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);
        tvGoToLogin.setOnClickListener(v -> {
            finish();
        });
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/codram/limitx/LoginActivity.java app/src/main/java/com/codram/limitx/RegisterActivity.java
git commit -m "feat: implement activities and navigation logic"
```

### Task 5: Android Manifest Configuration

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Update Manifest with Activities**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.LimiTx">

        <activity
            android:name=".LoginActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity
            android:name=".RegisterActivity"
            android:exported="false" />

    </application>

</manifest>
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/AndroidManifest.xml
git commit -m "config: register activities in manifest"
```
