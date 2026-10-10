package parchis;

import parchis.controlador.ControladorTurno;
import parchis.fachada.FachadaJuegoMock;
import parchis.modelo.*;
import parchis.vista.VistaPartida;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        Jugador fernando = new Jugador("Fernando", Color.VERDE, "avatar_fernando");
        Jugador sofia = new Jugador("Sofía", Color.ROJO, "avatar_sofia");
        List<Jugador> jugadores = List.of(fernando, sofia);

        Tablero tablero = new Tablero(List.of(Color.VERDE, Color.ROJO));        // amarillo y azul inactivos
        FachadaJuegoMock fachada = new FachadaJuegoMock(tablero).programarTiradas(new int[]{2, 3}, new int[]{1, 3});
        ModeloPartida modelo = new ModeloPartida(jugadores, tablero, fachada);
        ControladorTurno controlador = new ControladorTurno(modelo);

        VistaPartida vistaFernando = new VistaPartida(modelo, controlador, fernando);
        VistaPartida vistaSofia = new VistaPartida(modelo, controlador, sofia);

        titulo("1. Inicio del turno: Fernando activo, Sofia observa");
        modelo.iniciarPartida(0);

        titulo("2. Sofia intenta lanzar fuera de turno boton deshabilitado");
        vistaSofia.clicLanzarDado();

        titulo("3. Lanzar dados: Fernando saca 2 y 3 (suman 5: puede sacar ficha)");
        vistaFernando.clicLanzarDado();

        titulo("4. Seleccionar ficha: Fernando elige la segunda ficha (seleccion local)");
        vistaFernando.seleccionarFicha(2);

        titulo("5. Confirmar movimiento: intencion previa, el tablero no cambia");
        vistaFernando.clicMoverFicha();

        titulo("6. Respuesta aplicada: ficha 2 en la salida verde y turno de Sofia");
        vistaFernando.confirmarMovimiento();

        titulo("7. Turno de Sofia: saca 1 y 3 (suma 4, sin 5), no puede sacar ficha, sigue Fernando");
        vistaSofia.clicLanzarDado();
    }

    private static void titulo(String t) {
        System.out.println("\n==================== " + t + " ====================");
    }
}
