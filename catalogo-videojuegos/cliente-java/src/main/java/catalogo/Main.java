package catalogo;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        // Dirección de la API. Se puede cambiar pasando otra como primer argumento.
        String url = args.length > 0 ? args[0] : "http://127.0.0.1:8000/api";

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorada) {
            // Si falla, Swing usa su apariencia por defecto.
        }

        SwingUtilities.invokeLater(() -> new VentanaPrincipal(new ApiClient(url)).setVisible(true));
    }
}
