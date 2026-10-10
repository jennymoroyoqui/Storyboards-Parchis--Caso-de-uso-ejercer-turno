package parchis.controlador;

import parchis.modelo.*;

/**
 * Controlador GestorTurno intercepta las acciones de la vista y las traduce en
 * llamadas al modelo, no aplica reglas ni modifica la vista
 */
public class ControladorTurno {

    private final ModeloPartida modelo;

    public ControladorTurno(ModeloPartida modelo) {
        this.modelo = modelo;
    }

    //ClicLanzarDado procesarLanzamiento
  
    public boolean procesarLanzamiento(Jugador solicitante) {
        return modelo.intentarLanzamiento(solicitante);
    }

    public boolean seleccionarFicha(Jugador solicitante, int idFicha) {
        return modelo.esFichaElegible(solicitante, idFicha);
    }

    /**
     * Confirmacion de Mover ficha -- ejecutarMovimiento(id)
     */
    public boolean ejecutarMovimiento(Jugador solicitante, int idFicha) {
        return modelo.intentarMovimiento(solicitante, idFicha);
    }

    public boolean lanzarDado(Jugador solicitante) {
        return procesarLanzamiento(solicitante);
    }

    public boolean moverFicha(Jugador solicitante, int idFicha) {
        return ejecutarMovimiento(solicitante, idFicha);
    }
}
