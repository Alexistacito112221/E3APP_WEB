"""Respaldo en JSON: guarda y carga todos los videojuegos en un archivo.

La base de datos MySQL es la fuente principal. Este archivo es una copia que
se actualiza después de cada cambio, y que también se puede importar de vuelta.
"""
import json
from decimal import Decimal, InvalidOperation
from pathlib import Path

from django.conf import settings

from .models import Videojuego


def ruta_json():
    return Path(settings.RESPALDO_JSON)


def guardar_json():
    """Escribe todos los videojuegos de la base de datos en el archivo JSON."""
    datos = [juego.to_dict() for juego in Videojuego.objects.all()]
    ruta = ruta_json()
    ruta.parent.mkdir(parents=True, exist_ok=True)
    with open(ruta, "w", encoding="utf-8") as archivo:
        json.dump(datos, archivo, ensure_ascii=False, indent=2)
    return len(datos)


def cargar_json():
    """Lee el archivo JSON y crea o actualiza cada videojuego en la base de datos."""
    ruta = ruta_json()
    if not ruta.exists():
        raise FileNotFoundError(f"No existe el archivo {ruta}")

    with open(ruta, encoding="utf-8") as archivo:
        try:
            datos = json.load(archivo)
        except ValueError as exc:
            raise ValueError(f"El archivo JSON no es válido: {exc}") from exc

    if not isinstance(datos, list):
        raise ValueError("El archivo JSON debe contener una lista de videojuegos.")

    importados = 0
    for item in datos:
        try:
            valores = {
                "nombre": str(item["nombre"]).strip(),
                "descripcion": str(item.get("descripcion", "")),
                "precio_promedio": Decimal(str(item["precio_promedio"])).quantize(Decimal("0.01")),
                "plataforma": str(item["plataforma"]).strip(),
            }
            pk = item.get("id")
        except (KeyError, TypeError, AttributeError, InvalidOperation) as exc:
            raise ValueError(f"Un elemento del JSON está incompleto o mal formado: {item!r}") from exc

        if pk is not None:
            Videojuego.objects.update_or_create(pk=pk, defaults=valores)
        else:
            Videojuego.objects.create(**valores)
        importados += 1
    return importados
