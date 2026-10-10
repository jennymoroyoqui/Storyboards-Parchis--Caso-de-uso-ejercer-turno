package parchis.modelo;

public enum Fase {
    SIN_INICIAR,
    ESPERANDO_LANZAMIENTO, // el jugador activo puede pulsar Lanzar dado
    ESPERANDO_MOVIMIENTO, // ya hay resultado, el jugador activo elige ficha y pulsa "Mover ficha"
    FIN_PARTIDA
}
