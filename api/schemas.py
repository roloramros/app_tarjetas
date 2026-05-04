from pydantic import BaseModel, ConfigDict, Field
from datetime import datetime
from decimal import Decimal
from typing import Optional
import uuid

class UsuarioBase(BaseModel):
    nombre: Optional[str] = None

class UsuarioCreate(UsuarioBase):
    password: str = Field(..., min_length=6)

class UsuarioUpdate(UsuarioBase):
    password: Optional[str] = Field(None, min_length=6)
    suscripcion_activa: Optional[bool] = None
    suscripcion_hasta: Optional[datetime] = None

class UsuarioResponse(UsuarioBase):
    id: uuid.UUID
    suscripcion_activa: bool
    suscripcion_hasta: Optional[datetime] = None       
    last_login: Optional[datetime] = None
    fecha_creacion: Optional[datetime] = None
    cantidad_transacciones: Optional[int] = 0
    model_config = ConfigDict(from_attributes=True)

class AdminStatsResponse(BaseModel):
    total_usuarios: int
    total_tarjetas: int
    total_transacciones: int

class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"

class LoginRequest(BaseModel):
    nombre: str
    password: str

class TarjetaBase(BaseModel):
    nombre: str
    numero: str
    banco: Optional[str] = None
    moneda: str
    limite_mensual: Decimal
    activa: bool = True

class TarjetaCreate(TarjetaBase):
    pass

class TarjetaUpdate(BaseModel):
    nombre: Optional[str] = None
    numero: Optional[str] = None
    banco: Optional[str] = None
    moneda: Optional[str] = None
    limite_mensual: Optional[Decimal] = None
    activa: Optional[bool] = None

class TarjetaResponse(TarjetaBase):
    id: uuid.UUID
    usuario_id: uuid.UUID
    saldo_tarjeta: Optional[Decimal] = Decimal('0.00')
    extraccion_disponible: Optional[Decimal] = Decimal('0.00')
    deposito_disponible: Optional[Decimal] = Decimal('0.00')
    model_config = ConfigDict(from_attributes=True)

class TransaccionBase(BaseModel):
    tarjeta_id: uuid.UUID
    tipo: str
    monto: Decimal
    descripcion: Optional[str] = None
    subtipo: Optional[str] = None
    afecta_limite: bool = True
    fecha: datetime

class TransaccionCreate(TransaccionBase):
    pass

class TransaccionResponse(TransaccionBase):
    id: uuid.UUID
    fecha_creacion: datetime
    fecha_actualizacion: Optional[datetime] = None
    model_config = ConfigDict(from_attributes=True)

class AppVersionResponse(BaseModel):
    version: str
    model_config = ConfigDict(from_attributes=True)
