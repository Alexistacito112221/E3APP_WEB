"""Configuración del proyecto Django (API del catálogo de videojuegos)."""
import os
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent

# Solo para desarrollo local. Cámbiala si algún día publicas la API.
SECRET_KEY = os.environ.get("DJANGO_SECRET_KEY", "clave-solo-para-desarrollo")
DEBUG = True
ALLOWED_HOSTS = ["localhost", "127.0.0.1"]

INSTALLED_APPS = [
    "juegos",
]

# Es una API para un cliente de escritorio (no un sitio con formularios web),
# por eso no se incluye CsrfViewMiddleware ni sesiones.
MIDDLEWARE = [
    "django.middleware.common.CommonMiddleware",
]

ROOT_URLCONF = "catalogo_api.urls"
WSGI_APPLICATION = "catalogo_api.wsgi.application"

# --- Base de datos -----------------------------------------------------------
# Por defecto usa MySQL. Los datos de conexión se leen de variables de entorno
# para no escribir contraseñas en el código.
# Para probar sin MySQL:  DB_ENGINE=sqlite  (usa un archivo db.sqlite3)
if os.environ.get("DB_ENGINE", "mysql") == "sqlite":
    DATABASES = {
        "default": {
            "ENGINE": "django.db.backends.sqlite3",
            "NAME": BASE_DIR / "db.sqlite3",
        }
    }
else:
    DATABASES = {
        "default": {
            "ENGINE": "django.db.backends.mysql",
            "NAME": os.environ.get("DB_NAME", "catalogo_videojuegos"),
            "USER": os.environ.get("DB_USER", "root"),
            "PASSWORD": os.environ.get("DB_PASSWORD", ""),
            "HOST": os.environ.get("DB_HOST", "127.0.0.1"),
            "PORT": os.environ.get("DB_PORT", "3306"),
            "OPTIONS": {"charset": "utf8mb4"},
        }
    }

DEFAULT_AUTO_FIELD = "django.db.models.AutoField"

LANGUAGE_CODE = "es-mx"
TIME_ZONE = "America/Hermosillo"
USE_TZ = True

# Archivo JSON donde se guarda una copia de todos los videojuegos.
RESPALDO_JSON = BASE_DIR / "data" / "videojuegos.json"
