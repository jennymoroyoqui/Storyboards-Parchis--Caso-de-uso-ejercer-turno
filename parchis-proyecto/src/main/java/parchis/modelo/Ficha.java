package parchis.modelo;

public class Ficha {

    public static final int PASOS_META = 71;
    public static final int PASOS_EN_ANILLO = 63;
    public static final int TOTAL_CASILLAS = 68;

    public enum Estado {
        BASE, EN_JUEGO, META
    }

    private final int id;
    private final Color color;
    private Estado estado = Estado.BASE;
    private int pasos = -1;

    public Ficha(int id, Color color) {
        this.id = id;
        this.color = color;
    }

    public void actualizarPosicion(int nuevosPasos) {
        this.pasos = nuevosPasos;
        this.estado = nuevosPasos >= PASOS_META ? Estado.META : Estado.EN_JUEGO;
    }

    public void regresarABase() {
        this.pasos = -1;
        this.estado = Estado.BASE;
    }

    public int casillaAbsoluta() {
        if (estado != Estado.EN_JUEGO || pasos > PASOS_EN_ANILLO) {
            return -1;
        }
        return (color.casillaSalida() + pasos) % TOTAL_CASILLAS;
    }

    public int getId() {
        return id;
    }

    public Color getColor() {
        return color;
    }

    public Estado getEstado() {
        return estado;
    }

    public int getPasos() {
        return pasos;
    }

    public String descripcion() {
        return switch (estado) {
            case BASE ->
                "#" + id + ":base";
            case META ->
                "#" + id + ":META";
            case EN_JUEGO ->
                pasos == 0 ? "#" + id + ":salida"
                : pasos > PASOS_EN_ANILLO ? "#" + id + ":pasillo(" + pasos + ")"
                : "#" + id + ":casilla " + casillaAbsoluta();
        };
    }
}
