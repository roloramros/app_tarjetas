# Design Doc: Tarjetas CRUD Endpoints

**Date:** 2024-05-22
**Topic:** Implementation of POST and GET /tarjetas endpoints

## 1. Overview
This design covers the implementation of `POST /tarjetas` and `GET /tarjetas` endpoints in the FastAPI application. These endpoints allow authenticated users to create and list their own credit/debit cards.

## 2. Proposed Changes

### 2.1. api/models.py
- Fix redundancy: Import `Base` from `database.py` instead of creating a new `declarative_base()`. This ensures that `Base.metadata` in `main.py` correctly tracks the `Tarjeta` model.

### 2.2. api/main.py
- Update imports to include `Tarjeta`, `TarjetaCreate`, and `TarjetaResponse`.
- Implement `POST /tarjetas` endpoint.
- Implement `GET /tarjetas` endpoint.
- Ensure all operations are scoped to the `current_user`.

### 2.3. api/schemas.py
- No changes needed (already contains `TarjetaCreate`, `TarjetaUpdate`, and `TarjetaResponse`).

## 3. Data Flow

### POST /tarjetas
1. User sends a POST request with card details.
2. `get_current_user` dependency validates the token.
3. `crear_tarjeta` function creates a new `Tarjeta` instance, setting `usuario_id` to `current_user.id`.
4. The card is saved to the database.
5. Returns the created card.

### GET /tarjetas
1. User sends a GET request.
2. `get_current_user` dependency validates the token.
3. `listar_tarjetas` function queries cards where `usuario_id == current_user.id`.
4. Returns the list of cards.

## 4. Verification Plan

### 4.1. Automated Tests
- Create a temporary test script `api/test_tarjetas.py` (or similar) that:
    - Creates a test user.
    - Logs in to get a token.
    - Tests POST `/tarjetas`.
    - Tests GET `/tarjetas`.
    - Verifies that cards belong to the correct user.

### 4.2. Manual Verification
- Use `/docs` (Swagger UI) to manually verify the endpoints if the environment allows, or use `curl` commands.
