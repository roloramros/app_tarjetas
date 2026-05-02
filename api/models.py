from sqlalchemy import Column, Text, Boolean, DateTime, func, Numeric, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.ext.declarative import declarative_base
import uuid

Base = declarative_base()

class Usuario(Base):
    __tablename__ = "usuarios"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    nombre = Column(Text, nullable=True)
    password_hash = Column(Text, nullable=False)
    suscripcion_activa = Column(Boolean, default=True)
    
    # ✅ Quitamos server_default: la BD ya maneja el default con '30 days'::interval
    suscripcion_hasta = Column(DateTime, nullable=True)
    
    last_login = Column(DateTime, nullable=True)
    fecha_creacion = Column(DateTime, server_default=func.now())

    tarjetas = relationship("Tarjeta", back_populates="usuario", cascade="all, delete-orphan")


class Tarjeta(Base):
    __tablename__ = "tarjetas"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    usuario_id = Column(UUID(as_uuid=True), ForeignKey("usuarios.id", ondelete="CASCADE"), nullable=False)
    nombre = Column(Text, nullable=False)
    numero = Column(Text, nullable=False)
    banco = Column(Text, nullable=True)
    moneda = Column(Text, nullable=False)
    limite_mensual = Column(Numeric(12, 2), nullable=False)
    activa = Column(Boolean, default=True)

    usuario = relationship("Usuario", back_populates="tarjetas")
