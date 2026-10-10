package parchis.vista;

import parchis.controlador.ControladorTurno;
import parchis.modelo.*;

import java.util.stream.Collectors;

/**
 * Vista por consola (equivale a IU_Tablero / VENTANA TABLERO). Observa al modelo y habilita o
 * deshabilita los botones según el turnoActual y el jugador local. No calcula reglas ni mueve fichas.
 */
public class VistaPartida implements IObservador {
    private final ModeloPartida modelo;
    private final ControladorTurno controlador;
    private final Jugador jugadorLocal;

    private Integer fichaSeleccionada;      // foco local: no se comparte con las demás vistas
    private boolean confirmacionPendiente;

    public VistaPartida(ModeloPartida modelo, ControladorTurno controlador, Jugador jugadorLocal) {
        this.modelo = modelo;
        this.controlador = controlador;
        this.jugadorLocal = jugadorLocal;
        modelo.agregarObservador(this);
    }

    // ---------- Observer ----------
    @Override
    public void actualizar(ModeloPartida m) {
        if (!m.esFichaElegible(jugadorLocal, fichaSeleccionada == null ? -1 : fichaSeleccionada)) {
            fichaSeleccionada = null;
        }
        confirmacionPendiente = false;
        render();
    }

    // ---------- Estado de los controles ----------
    public boolean lanzarDadoHabilitado() {
        return modelo.getFase() == Fase.ESPERANDO_LANZAMIENTO && modelo.esTurnoDe(jugadorLocal);
    }

    public boolean moverFichaHabilitado() {
        return fichaSeleccionada != null && modelo.esFichaElegible(jugadorLocal, fichaSeleccionada);
    }

    // ---------- Acciones del usuario ----------
    public void clicLanzarDado() {
        System.out.println("  [" + jugadorLocal.getNombre() + "] clic en 'Lanzar dado'");
        if (!lanzarDadoHabilitado()) {
            System.out.println("  -> Botón deshabilitado, no se envía nada al controlador.");
            return;
        }
        controlador.procesarLanzamiento(jugadorLocal);
    }

    public void seleccionarFicha(int idFicha) {
        System.out.println("  [" + jugadorLocal.getNombre() + "] selecciona la ficha #" + idFicha);
        if (!controlador.seleccionarFicha(jugadorLocal, idFicha)) {
            System.out.println("  -> Ficha no elegible.");
            return;
        }
        fichaSeleccionada = idFicha;   // solo cambia el foco local; el tablero no se modifica
        render();
    }

    /** Primer paso: muestra la confirmación previa. El tablero no cambia hasta recibir la respuesta. */
    public void clicMoverFicha() {
        System.out.println("  [" + jugadorLocal.getNombre() + "] clic en 'Mover ficha'");
        if (!moverFichaHabilitado()) {
            System.out.println("  -> Botón deshabilitado.");
            return;
        }
        confirmacionPendiente = true;
        System.out.println("  -> ¿Confirmar el movimiento de la ficha #" + fichaSeleccionada + "?");
    }

    /** Segundo paso: confirma y delega en el controlador. */
    public void confirmarMovimiento() {
        if (!confirmacionPendiente || fichaSeleccionada == null) return;
        System.out.println("  [" + jugadorLocal.getNombre() + "] confirma el movimiento");
        controlador.ejecutarMovimiento(jugadorLocal, fichaSeleccionada);
    }

    // ---------- Dibujo ----------
    public void render() {
        Jugador turno = modelo.getTurnoActual();
        System.out.println();
        System.out.println("┌── Vista de " + jugadorLocal + " ──────────────────────────");
        System.out.println("│ Turno de: " + turno.getNombre() + (modelo.esTurnoDe(jugadorLocal) ? "  (es tu turno)" : "  (solo observas)"));
        System.out.println("│ Dados compartidos: " + modelo.getUltimosDados().map(d -> "[" + d[0] + "] [" + d[1] + "] = " + (d[0] + d[1])).orElse("[-] [-]"));
        for (Jugador j : modelo.getJugadores()) {
            System.out.println("│ " + j.getNombre() + " [" + j.getColor().etiqueta() + "]  última tirada: "
                    + modelo.getUltimaTirada(j).map(d -> d[0] + "+" + d[1] + "=" + (d[0] + d[1])).orElse("-")
                    + "  | Meta: " + modelo.getTablero().fichasEnMeta(j.getColor()) + "/" + Tablero.FICHAS_POR_JUGADOR
                    + "  | " + modelo.getTablero().fichasDe(j.getColor()).stream()
                            .map(Ficha::descripcion).collect(Collectors.joining(", ")));
        }
        if (modelo.esTurnoDe(jugadorLocal) && !modelo.getFichasElegibles().isEmpty()) {
            System.out.println("│ Fichas elegibles (solo las ve el jugador activo): " + modelo.getFichasElegibles());
        }
        System.out.println("│ Selección local: " + (fichaSeleccionada == null ? "ninguna" : "#" + fichaSeleccionada));
        System.out.println("│ [Lanzar dado: " + (lanzarDadoHabilitado() ? "HABILITADO" : "deshabilitado")
                + "]   [Mover ficha: " + (moverFichaHabilitado() ? "HABILITADO" : "deshabilitado") + "]");
        System.out.println("│ " + modelo.getMensaje());
        System.out.println("└────────────────────────────────────────────────");
    }
}
