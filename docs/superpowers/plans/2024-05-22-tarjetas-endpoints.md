# Tarjetas Endpoints Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement `POST /tarjetas` and `GET /tarjetas` endpoints for authenticated users.

**Architecture:** Standard FastAPI + SQLAlchemy patterns, ensuring models are correctly registered with the common `Base` class.

**Tech Stack:** FastAPI, SQLAlchemy (Async), Pydantic.

---

### Task 1: Fix Base Redundancy

**Files:**
- Modify: `api/models.py`
- Modify: `api/database.py`

- [ ] **Step 1: Export Base from database.py**
(Already exported, but let's double check content)

- [ ] **Step 2: Update models.py to use Base from database.py**

Modify `api/models.py`:
```python
from sqlalchemy import Column, Text, Boolean, DateTime, func, Numeric, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.dialects.postgresql import UUID
# Remove: from sqlalchemy.ext.declarative import declarative_base
import uuid
from database import Base # Add this

# Remove: Base = declarative_base()
...
```

- [ ] **Step 3: Commit**

```bash
git add api/models.py
git commit -m "refactor: use common Base for models to ensure schema synchronization"
```

### Task 2: Implement Tarjetas Endpoints

**Files:**
- Modify: `api/main.py`

- [ ] **Step 1: Update imports in main.py**

```python
from models import Usuario, Tarjeta # Update this
from schemas import (
    UsuarioCreate, UsuarioUpdate, UsuarioResponse,
    TokenResponse, LoginRequest,
    TarjetaCreate, TarjetaResponse # Add these
)
```

- [ ] **Step 2: Add endpoints to main.py**

```python
# === CRUD Tarjetas ===
@app.post("/tarjetas", response_model=TarjetaResponse, status_code=201)
async def crear_tarjeta(
    data: TarjetaCreate,
    db: AsyncSession = Depends(get_db),
    current_user: Usuario = Depends(get_current_user)
):
    nueva = Tarjeta(
        usuario_id=current_user.id,
        nombre=data.nombre,
        numero=data.numero,
        banco=data.banco,
        moneda=data.moneda,
        limite_mensual=data.limite_mensual,
        activa=data.activa
    )
    db.add(nueva)
    await db.commit()
    await db.refresh(nueva)
    return nueva

@app.get("/tarjetas", response_model=list[TarjetaResponse])
async def listar_tarjetas(
    db: AsyncSession = Depends(get_db),
    current_user: Usuario = Depends(get_current_user)
):
    result = await db.execute(select(Tarjeta).where(Tarjeta.usuario_id == current_user.id))
    return result.scalars().all()
```

- [ ] **Step 3: Commit**

```bash
git add api/main.py
git commit -m "feat: implement post and get endpoints for tarjetas"
```

### Task 3: Verification

**Files:**
- Create: `api/test_tarjetas.py`

- [ ] **Step 1: Create test script**

```python
import asyncio
import httpx
import uuid
import sys

async def test():
    async with httpx.AsyncClient(base_url="http://localhost:8000") as client:
        # 1. Login or create user
        # (Assuming a user exists or creating one)
        user_data = {"nombre": f"testuser_{uuid.uuid4().hex[:6]}", "password": "password123"}
        print(f"Creating user: {user_data['nombre']}")
        resp = await client.post("/usuarios", json=user_data)
        if resp.status_code != 201:
            print(f"Error creating user: {resp.text}")
            return

        print("Logging in...")
        resp = await client.post("/login", json=user_data)
        token = resp.json()["access_token"]
        headers = {"Authorization": f"Bearer {token}"}

        # 2. Create Tarjeta
        print("Creating tarjeta...")
        tarjeta_data = {
            "nombre": "Mi Visa",
            "numero": "1234567890123456",
            "banco": "Banco Central",
            "moneda": "USD",
            "limite_mensual": 1000.00,
            "activa": True
        }
        resp = await client.post("/tarjetas", json=tarjeta_data, headers=headers)
        print(f"POST /tarjetas: {resp.status_code}")
        assert resp.status_code == 201
        tarjeta_id = resp.json()["id"]

        # 3. List Tarjetas
        print("Listing tarjetas...")
        resp = await client.get("/tarjetas", headers=headers)
        print(f"GET /tarjetas: {resp.status_code}")
        assert resp.status_code == 200
        tarjetas = resp.json()
        assert len(tarjetas) >= 1
        assert any(t["id"] == tarjeta_id for t in tarjetas)
        
        print("Verification SUCCESS")

if __name__ == "__main__":
    try:
        asyncio.run(test())
    except Exception as e:
        print(f"Verification FAILED: {e}")
        sys.exit(1)
```

- [ ] **Step 2: Run verification**

Note: Requires the API to be running. I will use `uvicorn` in the background if needed, or just assume I can start it.
Run: `pip install httpx && python api/test_tarjetas.py`

- [ ] **Step 3: Cleanup test file**
Run: `rm api/test_tarjetas.py`
