package parchis.modelo;

//Colores del tablero, cada color tiene su casilla de salida en el recorrido circular
public enum Color {
    VERDE("Verde", 30), ROJO("Rojo", 64), AZUL("Azul", 47), AMARILLO("Amarillo", 13);

    private final String etiqueta;
    private final int casillaSalida;

    Color(String etiqueta, int casillaSalida) {
        this.etiqueta = etiqueta;
        this.casillaSalida = casillaSalida;
    }

    public String etiqueta() {
        return etiqueta;
    }

    public int casillaSalida() {
        return casillaSalida;
    }
}
