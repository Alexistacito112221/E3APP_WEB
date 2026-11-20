from django.db import models


class Videojuego(models.Model):
    """Única entidad del catálogo. Django agrega solo el campo `id` (clave primaria)."""

    nombre = models.CharField(max_length=150)
    descripcion = models.TextField(blank=True, default="")
    precio_promedio = models.DecimalField(max_digits=8, decimal_places=2)
    plataforma = models.CharField(max_length=60)

    class Meta:
        db_table = "videojuegos"  # nombre de la tabla en MySQL
        ordering = ["id"]

    def __str__(self):
        return f"{self.nombre} ({self.plataforma})"

    def to_dict(self):
        """Convierte el objeto a un diccionario listo para volverse JSON."""
        return {
            "id": self.id,
            "nombre": self.nombre,
            "descripcion": self.descripcion,
            "precio_promedio": float(self.precio_promedio),
            "plataforma": self.plataforma,
        }
