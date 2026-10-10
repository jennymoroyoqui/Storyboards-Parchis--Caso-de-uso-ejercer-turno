package parchis.modelo;

import java.util.Objects;

/**
 * Participante registrado nombre, color de fichas y avatar, el color lo
 * identifica en la partida
 */
public class Jugador {

    private final String nombre;
    private final Color color;
    private final String avatar;

    public Jugador(String nombre, Color color, String avatar) {
        this.nombre = Objects.requireNonNull(nombre);
        this.color = Objects.requireNonNull(color);
        this.avatar = avatar;
    }

    public String getNombre() {
        return nombre;
    }

    public Color getColor() {
        return color;
    }

    public String getAvatar() {
        return avatar;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Jugador j && j.color == color;
    }

    @Override
    public int hashCode() {
        return color.hashCode();
    }

    @Override
    public String toString() {
        return nombre + " (" + color.etiqueta() + ")";
    }
}
