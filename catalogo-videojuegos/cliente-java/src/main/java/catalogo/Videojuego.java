package catalogo;

import java.util.LinkedHashMap;
import java.util.Map;

/** Un videojuego del catálogo (los mismos campos que el modelo de Django). */
public class Videojuego {

    private final Integer id; // null cuando todavía no existe en la base de datos
    private final String nombre;
    private final String descripcion;
    private final double precioPromedio;
    private final String plataforma;

    public Videojuego(Integer id, String nombre, String descripcion, double precioPromedio, String plataforma) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioPromedio = precioPromedio;
        this.plataforma = plataforma;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public double getPrecioPromedio() { return precioPromedio; }
    public String getPlataforma() { return plataforma; }

    /** Objeto -> Map, para convertirlo a JSON con Json.escribir(). */
    public Map<String, Object> aMapa() {
        Map<String, Object> mapa = new LinkedHashMap<>();
        if (id != null) mapa.put("id", id);
        mapa.put("nombre", nombre);
        mapa.put("descripcion", descripcion);
        mapa.put("precio_promedio", precioPromedio);
        mapa.put("plataforma", plataforma);
        return mapa;
    }

    /** Map (leído con Json.parse()) -> objeto. */
    public static Videojuego desde(Map<?, ?> mapa) {
        Object descripcion = mapa.get("descripcion");
        return new Videojuego(
                ((Number) mapa.get("id")).intValue(),
                (String) mapa.get("nombre"),
                descripcion == null ? "" : (String) descripcion,
                ((Number) mapa.get("precio_promedio")).doubleValue(),
                (String) mapa.get("plataforma"));
    }
}
