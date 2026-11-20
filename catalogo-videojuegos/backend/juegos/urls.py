from django.urls import path

from . import views

# Todas estas rutas cuelgan de /api/ (ver catalogo_api/urls.py)
urlpatterns = [
    path("videojuegos/", views.lista_crear),
    path("videojuegos/<int:pk>/", views.detalle),
    path("exportar/", views.exportar),
    path("importar/", views.importar),
]
