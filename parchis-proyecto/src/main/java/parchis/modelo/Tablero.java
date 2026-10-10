package parchis.modelo;

import java.util.*;

//Tablero guarda las fichas de los colores activos y valida/aplica movimientos
public class Tablero {

    public static final int FICHAS_POR_JUGADOR = 4;
    public static final int VALOR_SALIR_DE_BASE = 5;

    private final Map<Color, List<Ficha>> fichas = new EnumMap<>(Color.class);

    public Tablero(Collection<Color> coloresActivos) {
        for (Color c : coloresActivos) {
            List<Ficha> lista = new ArrayList<>();
            for (int i = 1; i <= FICHAS_POR_JUGADOR; i++) {
                lista.add(new Ficha(i, c));
            }
            fichas.put(c, lista);
        }
    }

    public List<Ficha> fichasDe(Color color) {
        return Collections.unmodifiableList(fichas.getOrDefault(color, List.of()));
    }

    public Optional<Ficha> obtenerFicha(Color color, int id) {
        return fichasDe(color).stream().filter(f -> f.getId() == id).findFirst();
    }

    public List<Ficha> obtenerCasilla(int casilla) {
        List<Ficha> r = new ArrayList<>();
        fichas.values().forEach(l -> l.stream().filter(f -> f.casillaAbsoluta() == casilla).forEach(r::add));
        return r;
    }

    public int fichasEnMeta(Color color) {
        return (int) fichasDe(color).stream().filter(f -> f.getEstado() == Ficha.Estado.META).count();
    }

    //Suma de los dos dados es lo que avanza una ficha que ya esta en el recorrido
    public static int suma(int[] dados) {
        return Arrays.stream(dados).sum();
    }

    /**
     * Regla de salida si una ficha puede salir de la base si CUALQUIERA de los
     * dos dados es 5 o si la SUMA de los dos dados es 5
     */
    public static boolean permiteSalir(int[] dados) {
        return Arrays.stream(dados).anyMatch(d -> d == VALOR_SALIR_DE_BASE) || suma(dados) == VALOR_SALIR_DE_BASE;
    }

    public static int avanceTrasSalir(int[] dados) {
        return suma(dados) - VALOR_SALIR_DE_BASE;
    }

    public List<Integer> validarMovimientos(Color color, int[] dados) {
        List<Integer> elegibles = new ArrayList<>();
        for (Ficha f : fichasDe(color)) {
            switch (f.getEstado()) {
                case BASE -> {
                    if (permiteSalir(dados)) {
                        elegibles.add(f.getId());
                    }
                }
                case EN_JUEGO -> {
                    if (f.getPasos() + suma(dados) <= Ficha.PASOS_META) {
                        elegibles.add(f.getId());
                    }
                }
                case META -> {
                }
            }
        }
        return elegibles;
    }

    public List<Ficha> actualizarEstadoTablero(Ficha movida) {
        List<Ficha> capturadas = new ArrayList<>();
        int casilla = movida.casillaAbsoluta();
        if (casilla < 0 || esCasillaSegura(casilla)) {
            return capturadas;
        }
        for (Ficha otra : obtenerCasilla(casilla)) {
            if (otra.getColor() != movida.getColor()) {
                otra.regresarABase();
                capturadas.add(otra);
            }
        }
        return capturadas;
    }

    private boolean esCasillaSegura(int casilla) {
        return Arrays.stream(Color.values()).anyMatch(c -> c.casillaSalida() == casilla);
    }
}
