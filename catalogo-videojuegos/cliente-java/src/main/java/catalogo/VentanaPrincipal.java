package catalogo;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/** Ventana Swing con la tabla de videojuegos, el formulario y los botones del CRUD. */
public class VentanaPrincipal extends JFrame {

    private static final String[] PLATAFORMAS = {
        "PC", "PlayStation 5", "PlayStation 4", "Xbox Series X|S", "Xbox One", "Nintendo Switch", "Móvil"
    };

    /** Una acción de botón que puede fallar con IOException (error de red o de la API). */
    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws IOException;
    }

    private final ApiClient api;

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Nombre", "Descripción", "Precio promedio", "Plataforma"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false; // la tabla solo muestra; se edita en el formulario
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    private final JTextField txtId = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextArea txtDescripcion = new JTextArea(4, 20);
    private final JTextField txtPrecio = new JTextField();
    private final JComboBox<String> cmbPlataforma = new JComboBox<>(PLATAFORMAS);
    private final JLabel lblEstado = new JLabel("Listo.");

    /** true mientras se llena la tabla por código, para no disparar la selección. */
    private boolean cargando = false;

    public VentanaPrincipal(ApiClient api) {
        super("Catálogo de videojuegos");
        this.api = api;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(crearPanelTabla(), BorderLayout.CENTER);
        add(crearPanelFormulario(), BorderLayout.EAST);
        add(crearPanelInferior(), BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(900, 480));
        setLocationRelativeTo(null);

        // Al abrir la ventana, lee los datos de la API.
        ejecutar(this::leer);
    }

    // ------------------------------------------------------- construir la UI

    private JScrollPane crearPanelTabla() {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.getColumnModel().getColumn(0).setMaxWidth(60);
        tabla.getColumnModel().getColumn(3).setMaxWidth(120);

        // Al seleccionar una fila: pide ese videojuego a la API (GET por id) y llena el formulario.
        tabla.getSelectionModel().addListSelectionListener(evento -> {
            if (evento.getValueIsAdjusting() || cargando) return;
            int fila = tabla.getSelectedRow();
            if (fila < 0) return;
            int id = (Integer) modeloTabla.getValueAt(fila, 0);
            ejecutar(() -> mostrarEnFormulario(api.obtener(id)));
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(560, 360));
        return scroll;
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos del videojuego"));
        panel.setPreferredSize(new Dimension(300, 360));

        txtId.setEditable(false); // el id lo asigna la base de datos
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        cmbPlataforma.setEditable(true); // permite escribir una plataforma que no esté en la lista

        agregarFila(panel, 0, "ID:", txtId);
        agregarFila(panel, 1, "Nombre:", txtNombre);
        agregarFila(panel, 2, "Descripción:", new JScrollPane(txtDescripcion));
        agregarFila(panel, 3, "Precio promedio:", txtPrecio);
        agregarFila(panel, 4, "Plataforma:", cmbPlataforma);
        return panel;
    }

    private void agregarFila(JPanel panel, int fila, String etiqueta, java.awt.Component campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.gridy = fila;
        c.anchor = GridBagConstraints.NORTHWEST;

        c.gridx = 0;
        c.weightx = 0;
        panel.add(new JLabel(etiqueta), c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(campo, c);
    }

    private JPanel crearPanelInferior() {
        JButton btnCrear = new JButton("Crear");
        JButton btnLeer = new JButton("Leer");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar formulario");
        JButton btnExportar = new JButton("Exportar JSON");
        JButton btnImportar = new JButton("Importar JSON");

        btnCrear.addActionListener(e -> ejecutar(this::crear));
        btnLeer.addActionListener(e -> ejecutar(this::leer));
        btnActualizar.addActionListener(e -> ejecutar(this::actualizar));
        btnEliminar.addActionListener(e -> ejecutar(this::eliminar));
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnExportar.addActionListener(e -> ejecutar(this::exportar));
        btnImportar.addActionListener(e -> ejecutar(this::importar));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        botones.add(btnCrear);
        botones.add(btnLeer);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);
        botones.add(new JLabel("   |   "));
        botones.add(btnExportar);
        botones.add(btnImportar);

        JPanel inferior = new JPanel(new BorderLayout(0, 6));
        inferior.add(botones, BorderLayout.NORTH);
        inferior.add(lblEstado, BorderLayout.SOUTH);
        return inferior;
    }

    // ------------------------------------------------------ operaciones CRUD

    /** Read: trae todos los videojuegos y los muestra en la tabla. */
    private void leer() throws IOException {
        List<Videojuego> lista = api.listar();
        cargando = true;
        try {
            modeloTabla.setRowCount(0);
            for (Videojuego v : lista) {
                modeloTabla.addRow(new Object[]{
                    v.getId(), v.getNombre(), v.getDescripcion(),
                    String.format(Locale.US, "%.2f", v.getPrecioPromedio()), v.getPlataforma()
                });
            }
        } finally {
            cargando = false;
        }
        limpiarFormulario();
        estado("Se leyeron " + lista.size() + " videojuegos.");
    }

    /** Create: manda los datos del formulario como un videojuego nuevo. */
    private void crear() throws IOException {
        Videojuego nuevo = leerFormulario(null);
        Videojuego creado = api.crear(nuevo);
        leer();
        estado("Videojuego creado con ID " + creado.getId() + ".");
    }

    /** Update: modifica el videojuego seleccionado con los datos del formulario. */
    private void actualizar() throws IOException {
        if (txtId.getText().isEmpty()) {
            throw new IllegalArgumentException("Selecciona primero un videojuego de la tabla.");
        }
        int id = Integer.parseInt(txtId.getText());
        api.actualizar(leerFormulario(id));
        leer();
        estado("Videojuego " + id + " actualizado.");
    }

    /** Delete: elimina el videojuego seleccionado (pide confirmación). */
    private void eliminar() throws IOException {
        if (txtId.getText().isEmpty()) {
            throw new IllegalArgumentException("Selecciona primero un videojuego de la tabla.");
        }
        int id = Integer.parseInt(txtId.getText());
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Eliminar \"" + txtNombre.getText() + "\"? Esta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) return;
        api.eliminar(id);
        leer();
        estado("Videojuego " + id + " eliminado.");
    }

    private void exportar() throws IOException {
        estado(api.exportarJson());
    }

    private void importar() throws IOException {
        String mensaje = api.importarJson();
        leer();
        estado(mensaje);
    }

    // --------------------------------------------------------------- apoyo

    /** Lee el formulario y arma un Videojuego. Lanza IllegalArgumentException si hay datos inválidos. */
    private Videojuego leerFormulario(Integer id) {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        Object seleccionada = cmbPlataforma.getSelectedItem();
        String plataforma = seleccionada == null ? "" : seleccionada.toString().trim();
        if (plataforma.isEmpty()) {
            throw new IllegalArgumentException("La plataforma es obligatoria.");
        }

        double precio;
        try {
            precio = Double.parseDouble(txtPrecio.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El precio debe ser un número, por ejemplo 59.99.");
        }
        if (!Double.isFinite(precio) || precio < 0) {
            throw new IllegalArgumentException("El precio debe ser un número mayor o igual a 0.");
        }
        return new Videojuego(id, nombre, txtDescripcion.getText().trim(), precio, plataforma);
    }

    private void mostrarEnFormulario(Videojuego v) {
        txtId.setText(String.valueOf(v.getId()));
        txtNombre.setText(v.getNombre());
        txtDescripcion.setText(v.getDescripcion());
        txtPrecio.setText(String.format(Locale.US, "%.2f", v.getPrecioPromedio()));
        cmbPlataforma.setSelectedItem(v.getPlataforma());
        estado("Mostrando el videojuego " + v.getId() + ".");
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtPrecio.setText("");
        cmbPlataforma.setSelectedIndex(0);
        tabla.clearSelection();
    }

    private void estado(String texto) {
        lblEstado.setText(texto);
    }

    /** Ejecuta una acción y muestra un mensaje si algo sale mal, en lugar de cerrar el programa. */
    private void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Revisa los datos", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            estado("Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo completar", JOptionPane.ERROR_MESSAGE);
        }
    }
}
