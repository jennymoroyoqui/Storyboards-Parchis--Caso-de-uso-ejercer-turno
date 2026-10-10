package parchis;

import parchis.controlador.ControladorTurno;
import parchis.fachada.FachadaJuegoMock;
import parchis.modelo.Color;
import parchis.modelo.*;
import parchis.vista.VistaPartidaGUI;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Fernando y Sofia observan el mismo ModeloPartida Los dados se lanzan con clic
 * en los dados o en Lanzar dado, las fichas se eligen con clic Hay un panel de
 * pruebas (mock) para programar el valor de la siguiente tirada
 */
public class MainGUI {

    public static void main(String[] args) {
        boolean demo = args.length > 0 && args[0].equals("--demo");
        SwingUtilities.invokeLater(() -> {
            Jugador fernando = new Jugador("Fernando", Color.VERDE, "avatar_fernando");
            Jugador sofia = new Jugador("Sofia", Color.ROJO, "avatar_sofia");

            Tablero tablero = new Tablero(List.of(Color.VERDE, Color.ROJO));
            FachadaJuegoMock fachada = new FachadaJuegoMock(tablero);
            if (demo) {
                fachada.programarTiradas(new int[]{2, 3});
            }
            ModeloPartida modelo = new ModeloPartida(List.of(fernando, sofia), tablero, fachada);
            ControladorTurno controlador = new ControladorTurno(modelo);

            new VistaPartidaGUI(modelo, controlador, fernando).mostrarEnVentana(30, 20);
            new VistaPartidaGUI(modelo, controlador, sofia).mostrarEnVentana(640, 20);
            mostrarPanelPruebas(fachada);

            modelo.iniciarPartida(0);   // Fernando comienza mientras Sofia solo observa
        });
    }

    //Ventana de ayuda de mock: permite elegir los valores de la siguiente tirada 
    private static void mostrarPanelPruebas(FachadaJuegoMock fachada) {
        JFrame f = new JFrame("Panel de pruebas");
        f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        JSpinner d1 = new JSpinner(new SpinnerNumberModel(2, 1, 6, 1));
        JSpinner d2 = new JSpinner(new SpinnerNumberModel(3, 1, 6, 1));
        JLabel estado = new JLabel("Los dados son aleatorios");
        JButton programar = new JButton("Programar proxima tirada");
        JButton quitar = new JButton("Volver a aleatorio");
        programar.addActionListener(e -> {
            fachada.limpiarTiradasProgramadas();
            fachada.programarTiradas(new int[]{(Integer) d1.getValue(), (Integer) d2.getValue()});
            estado.setText("Proxima tirada: " + d1.getValue() + " + " + d2.getValue() + " = "
                    + ((Integer) d1.getValue() + (Integer) d2.getValue()));
        });
        quitar.addActionListener(e -> {
            fachada.limpiarTiradasProgramadas();
            estado.setText("Los dados son aleatorios");
        });

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        fila.add(new JLabel("Dado 1:"));
        fila.add(d1);
        fila.add(new JLabel("Dado 2:"));
        fila.add(d2);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        botones.add(programar);
        botones.add(quitar);
        estado.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(fila, BorderLayout.NORTH);
        raiz.add(botones, BorderLayout.CENTER);
        raiz.add(estado, BorderLayout.SOUTH);
        f.setContentPane(raiz);
        f.pack();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        f.setLocation(1250 < pantalla.width - f.getWidth() ? 1250 : Math.max(0, pantalla.width - f.getWidth()),
                Math.max(0, pantalla.height - f.getHeight() - 60));
        f.setVisible(true);
    }
}
