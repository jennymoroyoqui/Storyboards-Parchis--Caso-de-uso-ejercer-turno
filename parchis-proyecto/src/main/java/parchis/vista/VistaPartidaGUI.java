package parchis.vista;

import parchis.controlador.ControladorTurno;
import parchis.modelo.Color;
import parchis.modelo.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Vista gráfica (Swing) de la partida: IU_Tablero / VistaPartida. Observa al modelo y se redibuja
 * cuando cambia el estado. Habilita los botones según el turnoActual y el jugador local.
 * No calcula reglas ni mueve fichas: solo envía las acciones al controlador.
 */
public class VistaPartidaGUI implements IObservador {
    private final ModeloPartida modelo;
    private final ControladorTurno controlador;
    private final Jugador jugadorLocal;

    private final JPanel raiz = new JPanel(new BorderLayout());
    private final PanelTablero panelTablero = new PanelTablero();
    private final PanelDados panelDados = new PanelDados();
    private final JButton btnLanzar = new JButton("Lanzar dado");
    private final JButton btnMover = new JButton("Mover ficha");
    private final JLabel lblTurno = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblMensaje = new JLabel(" ", SwingConstants.CENTER);

    private Integer fichaSeleccionada;   // foco local: no se comparte con las demás vistas

    public VistaPartidaGUI(ModeloPartida modelo, ControladorTurno controlador, Jugador jugadorLocal) {
        this.modelo = modelo;
        this.controlador = controlador;
        this.jugadorLocal = jugadorLocal;
        construirInterfaz();
        modelo.agregarObservador(this);
        refrescar();
    }

    // ---------- Construcción de la interfaz ----------
    private void construirInterfaz() {
        lblTurno.setFont(lblTurno.getFont().deriveFont(Font.BOLD, 16f));
        lblMensaje.setFont(lblMensaje.getFont().deriveFont(13f));

        JPanel norte = new JPanel(new GridLayout(2, 1));
        norte.add(lblTurno);
        norte.add(lblMensaje);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        controles.add(panelDados);
        controles.add(btnLanzar);
        controles.add(btnMover);

        btnLanzar.addActionListener(e -> controlador.procesarLanzamiento(jugadorLocal));
        btnMover.addActionListener(e -> clicMoverFicha());

        raiz.add(norte, BorderLayout.NORTH);
        raiz.add(panelTablero, BorderLayout.CENTER);
        raiz.add(controles, BorderLayout.SOUTH);
    }

    public JPanel getPanelRaiz() { return raiz; }

    public JFrame mostrarEnVentana(int x, int y) {
        JFrame f = new JFrame("Parchís - vista de " + jugadorLocal);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setContentPane(raiz);
        f.pack();
        f.setLocation(x, y);
        f.setVisible(true);
        return f;
    }

    // ---------- Observer ----------
    @Override
    public void actualizar(ModeloPartida m) {
        if (fichaSeleccionada != null && !m.esFichaElegible(jugadorLocal, fichaSeleccionada)) {
            fichaSeleccionada = null;
        }
        if (SwingUtilities.isEventDispatchThread()) refrescar();
        else SwingUtilities.invokeLater(this::refrescar);
    }

    // ---------- Estado de los controles ----------
    public boolean lanzarDadoHabilitado() {
        return modelo.getFase() == Fase.ESPERANDO_LANZAMIENTO && modelo.esTurnoDe(jugadorLocal);
    }

    public boolean moverFichaHabilitado() {
        return fichaSeleccionada != null && modelo.esFichaElegible(jugadorLocal, fichaSeleccionada);
    }

    private void refrescar() {
        btnLanzar.setEnabled(lanzarDadoHabilitado());
        btnMover.setEnabled(moverFichaHabilitado());
        boolean esMiTurno = modelo.esTurnoDe(jugadorLocal);
        if (modelo.getFase() == Fase.FIN_PARTIDA) {
            lblTurno.setText("Fin de la partida");
        } else {
            lblTurno.setText("Turno de " + modelo.getTurnoActual().getNombre()
                    + (esMiTurno ? "  (es tu turno)" : "  (solo observas)"));
        }
        lblMensaje.setText(modelo.getMensaje().isEmpty() ? " " : modelo.getMensaje());
        panelDados.setCursor(Cursor.getPredefinedCursor(lanzarDadoHabilitado() ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        panelDados.setToolTipText(lanzarDadoHabilitado() ? "Haz clic para lanzar los dados" : null);
        panelTablero.repaint();
        panelDados.repaint();
    }

    // ---------- Acciones del usuario ----------
    /** Mensaje temporal de ayuda para el jugador local; se reemplaza en la siguiente actualización del modelo. */
    private void avisar(String texto) { lblMensaje.setText(texto); }

    /** Clic sobre los dados: equivale a pulsar "Lanzar dado". */
    private void clicDados() {
        if (lanzarDadoHabilitado()) {
            controlador.procesarLanzamiento(jugadorLocal);
        } else if (modelo.getFase() == Fase.FIN_PARTIDA) {
            avisar("La partida terminó.");
        } else if (!modelo.esTurnoDe(jugadorLocal)) {
            avisar("No es tu turno, espera a " + modelo.getTurnoActual().getNombre() + ".");
        } else {
            avisar("Ya lanzaste: elige una ficha y pulsa \"Mover ficha\".");
        }
    }

    /** Selección local de una ficha (clic sobre la ficha en el tablero). */
    public void seleccionarFicha(int idFicha) {
        if (fichaSeleccionada != null && fichaSeleccionada == idFicha) {   // segundo clic: quita la selección
            fichaSeleccionada = null;
            refrescar();
            return;
        }
        if (!controlador.seleccionarFicha(jugadorLocal, idFicha)) return;   // no elegible
        fichaSeleccionada = idFicha;      // solo cambia el foco local; el tablero no se modifica
        refrescar();
    }

    /** "Mover ficha": confirmación previa; el tablero no cambia hasta recibir la respuesta del modelo. */
    private void clicMoverFicha() {
        if (!moverFichaHabilitado()) return;
        int id = fichaSeleccionada;
        int r = JOptionPane.showConfirmDialog(raiz, "¿Mover la ficha #" + id + "?",
                "Confirmar movimiento", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) controlador.ejecutarMovimiento(jugadorLocal, id);
    }

    // ---------- Utilidades de color ----------
    private static java.awt.Color awt(Color c) {
        return switch (c) {
            case VERDE -> new java.awt.Color(0, 214, 0);
            case ROJO -> new java.awt.Color(230, 20, 20);
            case AZUL -> new java.awt.Color(20, 50, 230);
            case AMARILLO -> new java.awt.Color(255, 200, 0);
        };
    }

    private static java.awt.Color alfa(java.awt.Color c, int a) {
        return new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    private static String textoTirada(int[] d) { return d[0] + "+" + d[1] + "=" + (d[0] + d[1]); }

    // ---------- Panel de los dados compartidos ----------
    private final class PanelDados extends JPanel {
        PanelDados() {
            setPreferredSize(new Dimension(130, 56));
            setOpaque(false);
            addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { clicDados(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Optional<int[]> d = modelo.getUltimosDados();
            dibujarDado(g, 4, 4, 48, d.map(v -> v[0]).orElse(0));
            dibujarDado(g, 68, 4, 48, d.map(v -> v[1]).orElse(0));
            g.dispose();
        }

        private void dibujarDado(Graphics2D g, int x, int y, int t, int valor) {
            g.setColor(java.awt.Color.WHITE);
            g.fill(new RoundRectangle2D.Double(x, y, t, t, 10, 10));
            g.setColor(java.awt.Color.BLACK);
            g.setStroke(new BasicStroke(2f));
            g.draw(new RoundRectangle2D.Double(x, y, t, t, 10, 10));
            double[] p = {0.25, 0.5, 0.75};
            int[][] pips = switch (valor) {
                case 1 -> new int[][]{{1, 1}};
                case 2 -> new int[][]{{0, 0}, {2, 2}};
                case 3 -> new int[][]{{0, 0}, {1, 1}, {2, 2}};
                case 4 -> new int[][]{{0, 0}, {2, 0}, {0, 2}, {2, 2}};
                case 5 -> new int[][]{{0, 0}, {2, 0}, {1, 1}, {0, 2}, {2, 2}};
                case 6 -> new int[][]{{0, 0}, {2, 0}, {0, 1}, {2, 1}, {0, 2}, {2, 2}};
                default -> new int[0][];
            };
            for (int[] q : pips) {
                double cx = x + p[q[0]] * t, cy = y + p[q[1]] * t;
                g.fill(new Ellipse2D.Double(cx - 4.5, cy - 4.5, 9, 9));
            }
        }
    }

    // ---------- Panel del tablero ----------
    private final class PanelTablero extends JPanel {
        private static final double ALTO_VIRTUAL = 23;   // 19 del tablero + 2 arriba y 2 abajo para las etiquetas

        PanelTablero() {
            setPreferredSize(new Dimension(570, 690));
            setBackground(new java.awt.Color(245, 245, 245));
            addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { clic(e.getPoint()); }
            });
        }

        private double s() { return Math.min(getWidth() / (double) GeometriaTablero.N, getHeight() / ALTO_VIRTUAL); }
        private double ox() { return (getWidth() - GeometriaTablero.N * s()) / 2; }
        private double oy() { return (getHeight() - ALTO_VIRTUAL * s()) / 2 + 2 * s(); }
        private double px(double x) { return ox() + x * s(); }
        private double py(double y) { return oy() + y * s(); }

        private Rectangle2D celda(double x, double y, double w, double h) {
            return new Rectangle2D.Double(px(x), py(y), w * s(), h * s());
        }

        /** Centro (en casillas) de una posición expresada en pasos (-1 = base, 71 = meta). */
        private Point2D.Double centroPara(Color color, int idFicha, int pasos) {
            if (pasos < 0) return GeometriaTablero.slotBase(color, idFicha);
            if (pasos >= Ficha.PASOS_META) return GeometriaTablero.slotMeta(color, idFicha);
            Point c = pasos <= Ficha.PASOS_EN_ANILLO
                    ? GeometriaTablero.anillo((color.casillaSalida() + pasos) % Ficha.TOTAL_CASILLAS)
                    : GeometriaTablero.pasillo(color, pasos - Ficha.PASOS_EN_ANILLO);
            return new Point2D.Double(c.x + 0.5, c.y + 0.5);
        }

        private Point2D.Double centro(Ficha f) {
            return centroPara(f.getColor(), f.getId(), f.getEstado() == Ficha.Estado.BASE ? -1 : f.getPasos());
        }

        private double radio(Ficha f) { return f.getEstado() == Ficha.Estado.META ? 0.2 : 0.38; }

        /** Centros de todas las fichas, separando las que comparten casilla. */
        private Map<Ficha, Point2D.Double> posiciones() {
            Map<Ficha, Point2D.Double> res = new HashMap<>();
            Map<String, Integer> ocupacion = new HashMap<>();
            for (Jugador j : modelo.getJugadores()) {
                for (Ficha f : modelo.getTablero().fichasDe(j.getColor())) {
                    Point2D.Double c = centro(f);
                    if (f.getEstado() == Ficha.Estado.EN_JUEGO) {
                        String k = c.x + "," + c.y;
                        int n = ocupacion.merge(k, 1, Integer::sum) - 1;
                        c = new Point2D.Double(c.x + n * 0.22, c.y - n * 0.22);
                    }
                    res.put(f, c);
                }
            }
            return res;
        }

        private void clic(Point p) {
            Map<Ficha, Point2D.Double> pos = posiciones();
            Ficha tocada = null;
            for (Ficha f : modelo.getTablero().fichasDe(jugadorLocal.getColor())) {
                Point2D.Double c = pos.get(f);
                double r = Math.max(radio(f), 0.3) * s();
                if (Point2D.distance(px(c.x), py(c.y), p.x, p.y) <= r) tocada = f;
            }
            if (tocada == null) return;
            if (modelo.getFase() == Fase.FIN_PARTIDA) return;
            if (!modelo.esTurnoDe(jugadorLocal)) {
                avisar("No es tu turno, espera a " + modelo.getTurnoActual().getNombre() + ".");
            } else if (modelo.getFase() == Fase.ESPERANDO_LANZAMIENTO) {
                avisar("Primero lanza los dados (clic en los dados o en \"Lanzar dado\").");
            } else if (!controlador.seleccionarFicha(jugadorLocal, tocada.getId())) {
                avisar("La ficha #" + tocada.getId() + " no puede moverse con este resultado.");
            } else {
                seleccionarFicha(tocada.getId());
            }
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            double s = s();

            // Marco del tablero
            g.setColor(new java.awt.Color(217, 217, 217));
            g.fill(new RoundRectangle2D.Double(px(-0.3), py(-0.3), 19.6 * s, 19.6 * s, s, s));
            g.setColor(new java.awt.Color(120, 120, 120));
            g.setStroke(new BasicStroke(1.5f));
            g.draw(new RoundRectangle2D.Double(px(-0.3), py(-0.3), 19.6 * s, 19.6 * s, s, s));

            List<Color> activos = new ArrayList<>();
            modelo.getJugadores().forEach(j -> activos.add(j.getColor()));

            // Bases (los colores sin jugador quedan inactivos y sin fichas)
            for (Color c : Color.values()) {
                Point o = GeometriaTablero.origenBase(c);
                boolean activo = activos.contains(c);
                java.awt.Color col = awt(c);
                RoundRectangle2D rr = new RoundRectangle2D.Double(px(o.x + 0.2), py(o.y + 0.2), 7.6 * s, 7.6 * s, s, s);
                g.setColor(alfa(col, activo ? 90 : 55));
                g.fill(rr);
                g.setColor(activo ? col : alfa(col, 140));
                g.setStroke(new BasicStroke(activo ? 3f : 2f));
                g.draw(rr);
                if (activo) {
                    for (int id = 1; id <= Tablero.FICHAS_POR_JUGADOR; id++) {   // huecos de la base
                        Point2D.Double h = GeometriaTablero.slotBase(c, id);
                        g.setColor(java.awt.Color.WHITE);
                        g.setStroke(new BasicStroke(2f));
                        g.draw(new Ellipse2D.Double(px(h.x) - 0.5 * s, py(h.y) - 0.5 * s, s, s));
                    }
                }
            }

            // Casillas de los brazos
            for (int r = 0; r < GeometriaTablero.N; r++) {
                for (int c = 0; c < GeometriaTablero.N; c++) {
                    boolean ejeV = c >= 8 && c <= 10, ejeH = r >= 8 && r <= 10;
                    if ((ejeV || ejeH) && !(ejeV && ejeH)) {
                        g.setColor(new java.awt.Color(240, 240, 240));
                        g.fill(celda(c, r, 1, 1));
                        g.setColor(new java.awt.Color(150, 150, 150));
                        g.setStroke(new BasicStroke(1f));
                        g.draw(celda(c, r, 1, 1));
                    }
                }
            }
            // Carriles de meta y casillas de salida
            for (Color c : Color.values()) {
                for (int k = 0; k <= 7; k++) {
                    Point p = GeometriaTablero.pasillo(c, k);
                    g.setColor(awt(c));
                    g.fill(celda(p.x, p.y, 1, 1));
                    g.setColor(new java.awt.Color(110, 110, 110));
                    g.draw(celda(p.x, p.y, 1, 1));
                }
                Point sal = GeometriaTablero.anillo(c.casillaSalida());
                g.setColor(awt(c));
                g.fill(celda(sal.x, sal.y, 1, 1));
                g.setColor(java.awt.Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, (int) (s * 0.6)));
                g.drawString("★", (float) (px(sal.x) + 0.2 * s), (float) (py(sal.y) + 0.75 * s));
            }

            // Centro: cuatro triángulos
            triangulo(g, awt(Color.AMARILLO), 8, 8, 11, 8);
            triangulo(g, awt(Color.ROJO), 11, 8, 11, 11);
            triangulo(g, awt(Color.AZUL), 11, 11, 8, 11);
            triangulo(g, awt(Color.VERDE), 8, 11, 8, 8);

            // Vista previa del destino de la ficha seleccionada (solo en la vista local)
            if (fichaSeleccionada != null) {
                OptionalInt destino = modelo.getPasosDestino(jugadorLocal, fichaSeleccionada);
                if (destino.isPresent()) {
                    Point2D.Double d = centroPara(jugadorLocal.getColor(), fichaSeleccionada, destino.getAsInt());
                    double rd = 0.5 * s;
                    g.setColor(alfa(awt(jugadorLocal.getColor()), 110));
                    g.fill(new Ellipse2D.Double(px(d.x) - rd, py(d.y) - rd, 2 * rd, 2 * rd));
                    g.setColor(java.awt.Color.BLACK);
                    g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{5f, 4f}, 0f));
                    g.draw(new Ellipse2D.Double(px(d.x) - rd, py(d.y) - rd, 2 * rd, 2 * rd));
                }
            }

            // Fichas
            Map<Ficha, Point2D.Double> pos = posiciones();
            boolean activoLocal = modelo.esTurnoDe(jugadorLocal);
            for (Jugador j : modelo.getJugadores()) {
                for (Ficha f : modelo.getTablero().fichasDe(j.getColor())) {
                    dibujarFicha(g, f, pos.get(f), activoLocal && j.equals(jugadorLocal));
                }
            }

            // Etiquetas de jugadores
            for (Jugador j : modelo.getJugadores()) dibujarEtiqueta(g, j);
            g.dispose();
        }

        private void triangulo(Graphics2D g, java.awt.Color col, double x1, double y1, double x2, double y2) {
            Path2D t = new Path2D.Double();
            t.moveTo(px(x1), py(y1));
            t.lineTo(px(x2), py(y2));
            t.lineTo(px(9.5), py(9.5));
            t.closePath();
            g.setColor(col);
            g.fill(t);
        }

        private void dibujarFicha(Graphics2D g, Ficha f, Point2D.Double c, boolean propiaYActiva) {
            double s = s(), r = radio(f) * s;
            double x = px(c.x), y = py(c.y);
            Ellipse2D e = new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r);
            boolean elegible = propiaYActiva && modelo.getFichasElegibles().contains(f.getId());
            boolean seleccionada = f.getColor() == jugadorLocal.getColor()
                    && fichaSeleccionada != null && fichaSeleccionada == f.getId();
            if (elegible) {   // halo: solo lo ve el jugador activo
                g.setColor(alfa(java.awt.Color.BLACK, 70));
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f, 3f}, 0f));
                g.draw(new Ellipse2D.Double(x - r - 4, y - r - 4, 2 * r + 8, 2 * r + 8));
            }
            g.setColor(awt(f.getColor()));
            g.fill(e);
            g.setColor(seleccionada ? java.awt.Color.WHITE : awt(f.getColor()).darker().darker());
            g.setStroke(new BasicStroke(seleccionada ? 4f : 2f));
            g.draw(e);
            if (f.getEstado() != Ficha.Estado.META) {   // identificador visible sobre la ficha
                g.setColor(f.getColor() == Color.AMARILLO ? java.awt.Color.BLACK : java.awt.Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, Math.max(9, (int) (r * 1.1))));
                String t = String.valueOf(f.getId());
                FontMetrics fm = g.getFontMetrics();
                g.drawString(t, (float) (x - fm.stringWidth(t) / 2.0), (float) (y + fm.getAscent() / 2.0 - 2));
            }
        }

        /** Etiqueta con avatar, nombre, color, fichas en meta y última tirada. Resalta al jugador con turno. */
        private void dibujarEtiqueta(Graphics2D g, Jugador j) {
            double s = s();
            Color c = j.getColor();
            boolean arriba = c == Color.AMARILLO || c == Color.ROJO;
            boolean izquierda = c == Color.AMARILLO || c == Color.VERDE;
            double w = 9.2, h = 1.7;
            double x = izquierda ? 0 : 19 - w;
            double y = arriba ? -2.0 : 19.4;
            boolean turno = modelo.esTurnoDe(j) && modelo.getFase() != Fase.FIN_PARTIDA;
            RoundRectangle2D caja = new RoundRectangle2D.Double(px(x), py(y), w * s, h * s, 0.6 * s, 0.6 * s);
            g.setColor(turno ? alfa(awt(c), 70) : java.awt.Color.WHITE);
            g.fill(caja);
            g.setColor(awt(c));
            g.setStroke(new BasicStroke(turno ? 5f : 2f));
            g.draw(caja);

            // Avatar
            double d = 1.3 * s, ax = px(x + 0.2), ay = py(y + 0.2);
            g.setColor(awt(c).darker());
            g.fill(new Ellipse2D.Double(ax, ay, d, d));
            g.setColor(java.awt.Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, (int) (d * 0.6)));
            String ini = j.getNombre().substring(0, 1).toUpperCase();
            FontMetrics fm = g.getFontMetrics();
            g.drawString(ini, (float) (ax + (d - fm.stringWidth(ini)) / 2), (float) (ay + d / 2 + fm.getAscent() / 2.0 - 3));

            // Textos
            double tx = px(x + 1.7);
            g.setColor(java.awt.Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, (int) (s * 0.6)));
            g.drawString(j.getNombre() + (turno ? "  ▶ TURNO" : ""), (float) tx, (float) py(y + 0.65));
            g.setFont(new Font("SansSerif", Font.PLAIN, (int) (s * 0.45)));
            g.drawString(c.etiqueta() + "  |  Meta: " + modelo.getTablero().fichasEnMeta(c) + "/" + Tablero.FICHAS_POR_JUGADOR,
                    (float) tx, (float) py(y + 1.1));
            g.drawString("Última tirada: " + modelo.getUltimaTirada(j).map(VistaPartidaGUI::textoTirada).orElse("-"),
                    (float) tx, (float) py(y + 1.5));
        }
    }
}
