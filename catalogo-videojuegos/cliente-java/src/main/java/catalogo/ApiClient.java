package catalogo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Habla con la API de Django por HTTP, enviando y recibiendo JSON.
 * Cada método corresponde a una operación del CRUD.
 */
public class ApiClient {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    // ------------------------------------------------------------------ CRUD

    /** Read: todos los videojuegos. */
    public List<Videojuego> listar() throws IOException {
        Object json = enviar("GET", "/videojuegos/", null);
        try {
            List<Videojuego> resultado = new ArrayList<>();
            for (Object elemento : (List<?>) json) {
                resultado.add(Videojuego.desde((Map<?, ?>) elemento));
            }
            return resultado;
        } catch (ClassCastException | NullPointerException ex) {
            throw new IOException("La API respondió con un formato inesperado.", ex);
        }
    }

    /** Read: un videojuego por su id. */
    public Videojuego obtener(int id) throws IOException {
        return aVideojuego(enviar("GET", "/videojuegos/" + id + "/", null));
    }

    /** Create: crea un videojuego y devuelve el creado (ya con su id). */
    public Videojuego crear(Videojuego v) throws IOException {
        return aVideojuego(enviar("POST", "/videojuegos/", v.aMapa()));
    }

    /** Update: reemplaza los datos del videojuego con ese id. */
    public Videojuego actualizar(Videojuego v) throws IOException {
        if (v.getId() == null) throw new IOException("Falta el id del videojuego a actualizar.");
        return aVideojuego(enviar("PUT", "/videojuegos/" + v.getId() + "/", v.aMapa()));
    }

    /** Delete: elimina el videojuego con ese id. */
    public void eliminar(int id) throws IOException {
        enviar("DELETE", "/videojuegos/" + id + "/", null);
    }

    // ------------------------------------------------------------------ JSON

    /** Pide a la API guardar toda la base de datos en el archivo JSON. Devuelve el mensaje. */
    public String exportarJson() throws IOException {
        return mensaje(enviar("POST", "/exportar/", null));
    }

    /** Pide a la API cargar el archivo JSON a la base de datos. Devuelve el mensaje. */
    public String importarJson() throws IOException {
        return mensaje(enviar("POST", "/importar/", null));
    }

    // --------------------------------------------------------------- interno

    private Videojuego aVideojuego(Object json) throws IOException {
        try {
            return Videojuego.desde((Map<?, ?>) json);
        } catch (ClassCastException | NullPointerException ex) {
            throw new IOException("La API respondió con un formato inesperado.", ex);
        }
    }

    private String mensaje(Object json) {
        if (json instanceof Map && ((Map<?, ?>) json).get("mensaje") != null) {
            return String.valueOf(((Map<?, ?>) json).get("mensaje"));
        }
        return "Operación completada.";
    }

    /** Envía una petición HTTP y devuelve el JSON de la respuesta ya convertido (Map/List). */
    private Object enviar(String metodo, String ruta, Map<String, Object> cuerpo) throws IOException {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create(baseUrl + ruta))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json");
        if (cuerpo != null) {
            peticion.header("Content-Type", "application/json; charset=utf-8");
            peticion.method(metodo, BodyPublishers.ofString(Json.escribir(cuerpo), StandardCharsets.UTF_8));
        } else {
            peticion.method(metodo, BodyPublishers.noBody());
        }

        HttpResponse<String> respuesta;
        try {
            respuesta = http.send(peticion.build(), BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("La operación fue interrumpida.", ex);
        } catch (IOException ex) {
            throw new IOException("No se pudo conectar con la API en " + baseUrl
                    + ". ¿Está corriendo el servidor de Django?", ex);
        }

        String texto = respuesta.body();
        Object json = null;
        if (texto != null && !texto.isBlank()) {
            try {
                json = Json.parse(texto);
            } catch (IllegalArgumentException ex) {
                throw new IOException("La API respondió algo que no es JSON (código "
                        + respuesta.statusCode() + ").", ex);
            }
        }

        if (respuesta.statusCode() >= 400) {
            String detalle = "código " + respuesta.statusCode();
            if (json instanceof Map && ((Map<?, ?>) json).get("error") != null) {
                detalle = String.valueOf(((Map<?, ?>) json).get("error"));
            }
            throw new IOException(detalle);
        }
        return json;
    }
}
