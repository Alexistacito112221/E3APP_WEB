package catalogo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lector y escritor de JSON mínimo, sin librerías externas.
 *
 * parse():    texto JSON -> Map (objeto), List (arreglo), String, Double, Boolean o null
 * escribir(): Map / List / String / Number / Boolean / null -> texto JSON
 */
public final class Json {

    private final String texto;
    private int pos = 0;

    private Json(String texto) {
        this.texto = texto;
    }

    // ------------------------------------------------------------------ leer

    public static Object parse(String texto) {
        Json lector = new Json(texto);
        lector.saltarEspacios();
        Object valor = lector.leerValor();
        lector.saltarEspacios();
        if (lector.pos != texto.length()) {
            throw lector.error("hay texto después del JSON");
        }
        return valor;
    }

    private Object leerValor() {
        char c = actual();
        if (c == '{') return leerObjeto();
        if (c == '[') return leerArreglo();
        if (c == '"') return leerCadena();
        if (c == 't') { leerPalabra("true"); return Boolean.TRUE; }
        if (c == 'f') { leerPalabra("false"); return Boolean.FALSE; }
        if (c == 'n') { leerPalabra("null"); return null; }
        return leerNumero();
    }

    private Map<String, Object> leerObjeto() {
        Map<String, Object> mapa = new LinkedHashMap<>();
        pos++; // '{'
        saltarEspacios();
        if (actual() == '}') {
            pos++;
            return mapa;
        }
        while (true) {
            saltarEspacios();
            if (actual() != '"') throw error("se esperaba el nombre de un campo");
            String clave = leerCadena();
            saltarEspacios();
            if (actual() != ':') throw error("se esperaba ':'");
            pos++;
            saltarEspacios();
            mapa.put(clave, leerValor());
            saltarEspacios();
            char c = actual();
            pos++;
            if (c == '}') return mapa;
            if (c != ',') throw error("se esperaba ',' o '}'");
        }
    }

    private List<Object> leerArreglo() {
        List<Object> lista = new ArrayList<>();
        pos++; // '['
        saltarEspacios();
        if (actual() == ']') {
            pos++;
            return lista;
        }
        while (true) {
            saltarEspacios();
            lista.add(leerValor());
            saltarEspacios();
            char c = actual();
            pos++;
            if (c == ']') return lista;
            if (c != ',') throw error("se esperaba ',' o ']'");
        }
    }

    private String leerCadena() {
        pos++; // comilla inicial
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = actual();
            pos++;
            if (c == '"') return sb.toString();
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            char e = actual();
            pos++;
            switch (e) {
                case '"':  sb.append('"');  break;
                case '\\': sb.append('\\'); break;
                case '/':  sb.append('/');  break;
                case 'b':  sb.append('\b'); break;
                case 'f':  sb.append('\f'); break;
                case 'n':  sb.append('\n'); break;
                case 'r':  sb.append('\r'); break;
                case 't':  sb.append('\t'); break;
                case 'u':
                    if (pos + 4 > texto.length()) throw error("secuencia \\u incompleta");
                    sb.append((char) Integer.parseInt(texto.substring(pos, pos + 4), 16));
                    pos += 4;
                    break;
                default:
                    throw error("secuencia de escape no válida");
            }
        }
    }

    private Double leerNumero() {
        int inicio = pos;
        while (pos < texto.length() && "+-0123456789.eE".indexOf(texto.charAt(pos)) >= 0) {
            pos++;
        }
        if (inicio == pos) throw error("valor no reconocido");
        try {
            return Double.valueOf(texto.substring(inicio, pos));
        } catch (NumberFormatException ex) {
            throw error("número no válido");
        }
    }

    private void leerPalabra(String palabra) {
        if (!texto.startsWith(palabra, pos)) throw error("valor no reconocido");
        pos += palabra.length();
    }

    private char actual() {
        if (pos >= texto.length()) throw error("el JSON terminó antes de tiempo");
        return texto.charAt(pos);
    }

    private void saltarEspacios() {
        while (pos < texto.length() && Character.isWhitespace(texto.charAt(pos))) {
            pos++;
        }
    }

    private IllegalArgumentException error(String detalle) {
        return new IllegalArgumentException("JSON inválido (posición " + pos + "): " + detalle);
    }

    // --------------------------------------------------------------- escribir

    public static String escribir(Object valor) {
        StringBuilder sb = new StringBuilder();
        escribir(valor, sb);
        return sb.toString();
    }

    private static void escribir(Object valor, StringBuilder sb) {
        if (valor == null) {
            sb.append("null");
        } else if (valor instanceof String) {
            escribirCadena((String) valor, sb);
        } else if (valor instanceof Boolean) {
            sb.append(valor);
        } else if (valor instanceof Number) {
            double d = ((Number) valor).doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                throw new IllegalArgumentException("Un número no es válido para JSON: " + valor);
            }
            sb.append(valor);
        } else if (valor instanceof Map) {
            sb.append('{');
            boolean primero = true;
            for (Map.Entry<?, ?> entrada : ((Map<?, ?>) valor).entrySet()) {
                if (!primero) sb.append(',');
                primero = false;
                escribirCadena(String.valueOf(entrada.getKey()), sb);
                sb.append(':');
                escribir(entrada.getValue(), sb);
            }
            sb.append('}');
        } else if (valor instanceof List) {
            sb.append('[');
            boolean primero = true;
            for (Object elemento : (List<?>) valor) {
                if (!primero) sb.append(',');
                primero = false;
                escribir(elemento, sb);
            }
            sb.append(']');
        } else {
            throw new IllegalArgumentException("Tipo no soportado en JSON: " + valor.getClass());
        }
    }

    private static void escribirCadena(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }
}
