from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession
from datetime import datetime, timezone
import uuid

from database import engine, get_db, Base
from models import Usuario, Tarjeta
from schemas import (
    UsuarioCreate, UsuarioUpdate, UsuarioResponse,
    TokenResponse, LoginRequest,
    TarjetaCreate, TarjetaUpdate, TarjetaResponse
)
from utils import verify_password, hash_password, create_access_token, decode_token

# Inicializar tablas (solo en desarrollo)
async def lifespan(app: FastAPI):
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield

app = FastAPI(title="API Nueva App", version="1.0.0", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/login")

# === Autenticación ===
async def get_current_user(token: str = Depends(oauth2_scheme), db: AsyncSession = Depends(get_db)):
    credentials = HTTPException(status_code=401, detail="Credenciales inválidas", headers={"WWW-Authenticate": "Bearer"})
    payload = decode_token(token)
    if not payload or "sub" not in payload:
        raise credentials
    user_id = uuid.UUID(payload["sub"])
    result = await db.execute(select(Usuario).where(Usuario.id == user_id))
    user = result.scalars().first()
    if not user:
        raise credentials
    return user

# === Rutas Públicas ===
@app.get("/")
def read_root():
    return {"msg": "API Nueva App corriendo. Ve a /docs para probar endpoints."}

@app.post("/login", response_model=TokenResponse)
async def login(
    data: LoginRequest,
    db: AsyncSession = Depends(get_db)
):
    result = await db.execute(select(Usuario).where(Usuario.nombre == data.nombre))
    user = result.scalars().first()
    if not user or not verify_password(data.password, user.password_hash):
        raise HTTPException(status_code=401, detail="Credenciales inválidas")
    
    # Actualizar last_login
    await db.execute(
        update(Usuario).where(Usuario.id == user.id).values(last_login=datetime.now())
    )
    await db.commit()
    
    token = create_access_token(data={"sub": str(user.id), "nombre": user.nombre})
    return TokenResponse(access_token=token)

# === CRUD Usuarios ===
@app.post("/usuarios", response_model=UsuarioResponse, status_code=201)
async def crear_usuario(
    data: UsuarioCreate,
    db: AsyncSession = Depends(get_db)
):
    # Verificar si ya existe
    result = await db.execute(select(Usuario).where(Usuario.nombre == data.nombre))
    if result.scalars().first():
        raise HTTPException(status_code=400, detail="El nombre de usuario ya existe")
    
    nuevo = Usuario(
        nombre=data.nombre,
        password_hash=hash_password(data.password)
    )
    db.add(nuevo)
    await db.commit()
    await db.refresh(nuevo)
    return nuevo

@app.get("/usuarios", response_model=list[UsuarioResponse])
async def listar_usuarios(
    db: AsyncSession = Depends(get_db),
    current_user: Usuario = Depends(get_current_user)
):
    result = await db.execute(select(Usuario).order_by(Usuario.fecha_creacion.desc()))
    return result.scalars().all()

@app.get("/usuarios/{user_id}", response_model=UsuarioResponse)
async def obtener_usuario(
    user_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
    current_user: Usuario = Depends(get_current_user)
):
    result = await db.execute(select(Usuario).where(Usuario.id == user_id))
    user = result.scalars().first()
    if not user:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    return user

@app.put("/usuarios/{user_id}", response_model=UsuarioResponse)
async def actualizar_usuario(
    user_id: uuid.UUID,
    data: UsuarioUpdate,
    db: AsyncSession = Depends(get_db),
    current_user: Usuario = Depends(get_current_user)
):
    result = await db.execute(select(Usuario).where(Usuario.id == user_id))
    user = result.scalars().first()
    if not user:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    
    if data.nombre is not None:
        user.nombre = data.nombre
    if data.password is not None:
        user.password_hash = hash_password(data.password)
    if data.suscripcion_activa is not None:
        user.suscripcion_activa = data.suscripcion_activa
    if data.suscripcion_hasta is not None:
        user.suscripcion_hasta = data.suscripcion_hasta
    
    await db.commit()
    await db.refresh(user)
    return user

@app.delete("/usuarios/{user_id}", status_code=200)
async def eliminar_usuario(
    user_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
    current_user: Usuario = Depends(get_current_user)
):
    result = await db.execute(select(Usuario).where(Usuario.id == user_id))
    user = result.scalars().first()
    if not user:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    
    await db.delete(user)
    await db.commit()
    return {"msg": f"Usuario {user.nombre} eliminado"}

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

@app.get("/tarjetas/{tarjeta_id}", response_model=TarjetaResponse)
async def obtener_tarjeta(
    tarjeta_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
    current_user: Usuario = Depends(get_current_user)
):
    result = await db.execute(
        select(Tarjeta).where(Tarjeta.id == tarjeta_id, Tarjeta.usuario_id == current_user.id)
    )
    tarjeta = result.scalars().first()
    if not tarjeta:
        raise HTTPException(status_code=404, detail="Tarjeta no encontrada")
    return tarjeta

@app.put("/tarjetas/{tarjeta_id}", response_model=TarjetaResponse)
async def actualizar_tarjeta(
    tarjeta_id: uuid.UUID,
    data: TarjetaUpdate,
    db: AsyncSession = Depends(get_db),
    current_user: Usuario = Depends(get_current_user)
):
    result = await db.execute(
        select(Tarjeta).where(Tarjeta.id == tarjeta_id, Tarjeta.usuario_id == current_user.id)
    )
    tarjeta = result.scalars().first()
    if not tarjeta:
        raise HTTPException(status_code=404, detail="Tarjeta no encontrada")
    
    if data.nombre is not None:
        tarjeta.nombre = data.nombre
    if data.numero is not None:
        tarjeta.numero = data.numero
    if data.banco is not None:
        tarjeta.banco = data.banco
    if data.moneda is not None:
        tarjeta.moneda = data.moneda
    if data.limite_mensual is not None:
        tarjeta.limite_mensual = data.limite_mensual
    if data.activa is not None:
        tarjeta.activa = data.activa
        
    await db.commit()
    await db.refresh(tarjeta)
    return tarjeta

@app.delete("/tarjetas/{tarjeta_id}", status_code=200)
async def eliminar_tarjeta(
    tarjeta_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
    current_user: Usuario = Depends(get_current_user)
):
    result = await db.execute(
        select(Tarjeta).where(Tarjeta.id == tarjeta_id, Tarjeta.usuario_id == current_user.id)
    )
    tarjeta = result.scalars().first()
    if not tarjeta:
        raise HTTPException(status_code=404, detail="Tarjeta no encontrada")
    
    await db.delete(tarjeta)
    await db.commit()
    return {"msg": f"Tarjeta {tarjeta.nombre} eliminada"}
