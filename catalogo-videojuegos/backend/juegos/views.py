"""Vistas de la API. Cada una recibe JSON y responde JSON."""
import json
from decimal import Decimal, InvalidOperation

from django.http import JsonResponse
from django.views.decorators.http import require_http_methods

from . import respaldo
from .models import Videojuego


def _error(mensaje, status=400):
    return JsonResponse({"error": mensaje}, status=status)


def _leer_datos(request):
    """Lee y valida el JSON recibido. Devuelve (datos_limpios, mensaje_de_error)."""
    try:
        datos = json.loads(request.body.decode("utf-8"))
    except (ValueError, UnicodeDecodeError):
        return None, "El cuerpo de la petición no es un JSON válido."
    if not isinstance(datos, dict):
        return None, "Se esperaba un objeto JSON con los datos del videojuego."

    nombre = str(datos.get("nombre") or "").strip()
    if not nombre:
        return None, "El nombre es obligatorio."
    if len(nombre) > 150:
        return None, "El nombre no puede pasar de 150 caracteres."

    plataforma = str(datos.get("plataforma") or "").strip()
    if not plataforma:
        return None, "La plataforma es obligatoria."
    if len(plataforma) > 60:
        return None, "La plataforma no puede pasar de 60 caracteres."

    try:
        precio = Decimal(str(datos.get("precio_promedio"))).quantize(Decimal("0.01"))
    except (InvalidOperation, ValueError):
        return None, "El precio promedio debe ser un número, por ejemplo 59.99."
    if precio < 0 or precio > Decimal("999999.99"):
        return None, "El precio promedio debe estar entre 0 y 999999.99."

    return {
        "nombre": nombre,
        "descripcion": str(datos.get("descripcion") or "").strip(),
        "precio_promedio": precio,
        "plataforma": plataforma,
    }, None


@require_http_methods(["GET", "POST"])
def lista_crear(request):
    """GET  /api/videojuegos/  -> lista todos (Read)
    POST /api/videojuegos/  -> crea uno nuevo (Create)"""
    if request.method == "GET":
        juegos = [juego.to_dict() for juego in Videojuego.objects.all()]
        return JsonResponse(juegos, safe=False)

    datos, error = _leer_datos(request)
    if error:
        return _error(error)
    juego = Videojuego.objects.create(**datos)
    respaldo.guardar_json()
    return JsonResponse(juego.to_dict(), status=201)


@require_http_methods(["GET", "PUT", "DELETE"])
def detalle(request, pk):
    """GET    /api/videojuegos/<id>/ -> obtiene uno (Read)
    PUT    /api/videojuegos/<id>/ -> lo actualiza (Update)
    DELETE /api/videojuegos/<id>/ -> lo elimina (Delete)"""
    juego = Videojuego.objects.filter(pk=pk).first()
    if juego is None:
        return _error(f"No existe un videojuego con id {pk}.", status=404)

    if request.method == "GET":
        return JsonResponse(juego.to_dict())

    if request.method == "PUT":
        datos, error = _leer_datos(request)
        if error:
            return _error(error)
        for campo, valor in datos.items():
            setattr(juego, campo, valor)
        juego.save()
        respaldo.guardar_json()
        return JsonResponse(juego.to_dict())

    juego.delete()
    respaldo.guardar_json()
    return JsonResponse({"mensaje": f"Videojuego {pk} eliminado."})


@require_http_methods(["POST"])
def exportar(request):
    """POST /api/exportar/ -> guarda toda la base de datos en el archivo JSON."""
    cantidad = respaldo.guardar_json()
    return JsonResponse({
        "mensaje": f"Se exportaron {cantidad} videojuegos al archivo JSON.",
        "ruta": str(respaldo.ruta_json()),
        "cantidad": cantidad,
    })


@require_http_methods(["POST"])
def importar(request):
    """POST /api/importar/ -> carga el archivo JSON a la base de datos."""
    try:
        cantidad = respaldo.cargar_json()
    except FileNotFoundError as exc:
        return _error(str(exc), status=404)
    except ValueError as exc:
        return _error(str(exc))
    return JsonResponse({
        "mensaje": f"Se importaron {cantidad} videojuegos desde el archivo JSON.",
        "cantidad": cantidad,
    })
