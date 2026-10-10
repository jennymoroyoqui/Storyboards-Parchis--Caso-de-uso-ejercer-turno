package parchis.modelo;

import parchis.fachada.IFachadaJuego;
import parchis.fachada.ResultadoMovimiento;
import java.util.*;

/**
 * Modelo MVC, sujeto observable mantiene turnoActual, resultado del dado,
 * fichas elegibles y fase
 */
public class ModeloPartida {

    public static final int MAX_TIRADAS_CONSECUTIVAS = 3;

    private final List<IObservador> observadores = new ArrayList<>();
    private final List<Jugador> jugadores;
    private final Tablero tablero;
    private final IFachadaJuego fachada;

    private int indiceTurno;
    private Fase fase = Fase.SIN_INICIAR;
    private Integer ultimoValorDado;           // suma de los dos dados (se utiliza para el avance y la regla del par)
    private int[] ultimosDados;                // valores individuales de los dos dados compartidos
    private List<Integer> fichasElegibles = List.of();
    private int tiradasConsecutivas;
    private final Map<Color, int[]> ultimaTirada = new EnumMap<>(Color.class);
    private Jugador ganador;
    private String mensaje = "";

    public ModeloPartida(List<Jugador> jugadores, Tablero tablero, IFachadaJuego fachada) {
        this.jugadores = List.copyOf(jugadores);
        this.tablero = tablero;
        this.fachada = fachada;
    }

    public void agregarObservador(IObservador o) {
        observadores.add(o);
    }

    public void notificarObservadores() {
        observadores.forEach(o -> o.actualizar(this));
    }

    public void iniciarPartida(int indiceJugadorInicial) {
        indiceTurno = indiceJugadorInicial;
        fase = Fase.ESPERANDO_LANZAMIENTO;
        mensaje = "La partida inicio --- Turno de " + getTurnoActual().getNombre() + ".";
        notificarObservadores();
    }

    public void iniciarPartida() {
        iniciarPartida(new Random().nextInt(jugadores.size()));
    }

    public boolean intentarLanzamiento(Jugador solicitante) {
        if (fase != Fase.ESPERANDO_LANZAMIENTO || !esTurnoDe(solicitante)) {
            return false;
        }

        int valor = fachada.generarValor();
        ultimoValorDado = valor;
        ultimosDados = fachada.ultimosDados();
        ultimaTirada.put(solicitante.getColor(), ultimosDados);
        tiradasConsecutivas++;
        fichasElegibles = List.copyOf(fachada.validarMovimientos(solicitante.getColor(), ultimosDados));

        if (fichasElegibles.isEmpty()) {
            mensaje = solicitante.getNombre() + " saco " + detalleDados() + " y no tiene movimientos posibles";
            pasarTurno();
        } else {
            fase = Fase.ESPERANDO_MOVIMIENTO;
            mensaje = solicitante.getNombre() + " saco " + detalleDados() + ". Fichas elegibles: " + fichasElegibles;
        }
        notificarObservadores();
        return true;
    }

    /**
     * Mover ficha, devuelve false si el movimiento no es del jugador activo o
     * la ficha no es elegible
     */
    public boolean intentarMovimiento(Jugador solicitante, int idFicha) {
        if (fase != Fase.ESPERANDO_MOVIMIENTO || !esTurnoDe(solicitante) || !fichasElegibles.contains(idFicha)) {
            return false;
        }
        ResultadoMovimiento r = fachada.ejecutarMovimiento(solicitante.getColor(), idFicha, ultimosDados);
        if (!r.valido()) {
            return false;
        }

        mensaje = solicitante.getNombre() + " movio la ficha #" + idFicha + "."
                + (r.capturo() ? " ¡Capturo una ficha rival!" : "")
                + (r.llegoAMeta() ? " ¡Una ficha llego a la meta!" : "");

        if (tablero.fichasEnMeta(solicitante.getColor()) == Tablero.FICHAS_POR_JUGADOR) {
            ganador = solicitante;
            fase = Fase.FIN_PARTIDA;
            fichasElegibles = List.of();
            mensaje += " " + solicitante.getNombre() + " gana la partida";
        } else if (ultimoValorDado % 2 == 0 && tiradasConsecutivas < MAX_TIRADAS_CONSECUTIVAS) {
            fase = Fase.ESPERANDO_LANZAMIENTO;   // numero par: otra tirada max. 3 seguidas
            fichasElegibles = List.of();
            mensaje += " Saco par: " + solicitante.getNombre() + " vuelve a tirar";
        } else {
            pasarTurno();
        }
        notificarObservadores();
        return true;
    }

    private void pasarTurno() {
        tiradasConsecutivas = 0;
        fichasElegibles = List.of();
        ultimoValorDado = null;               // los dados nuevos aun no tienen resultado
        ultimosDados = null;
        indiceTurno = (indiceTurno + 1) % jugadores.size();
        fase = Fase.ESPERANDO_LANZAMIENTO;
        mensaje += " Turno de " + getTurnoActual().getNombre() + ".";
    }

    public boolean esTurnoDe(Jugador j) {
        return j != null && j.equals(getTurnoActual());
    }

    public boolean esFichaElegible(Jugador j, int idFicha) {
        return fase == Fase.ESPERANDO_MOVIMIENTO && esTurnoDe(j) && fichasElegibles.contains(idFicha);
    }

    /**
     * Posicion destino de la ficha si el jugador activo la moviera con el
     * resultado actual (vista previa)
     */
    public OptionalInt getPasosDestino(Jugador j, int idFicha) {
        if (!esFichaElegible(j, idFicha) || ultimoValorDado == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(fachada.calcularDestino(j.getColor(), idFicha, ultimosDados));
    }

    public Jugador getTurnoActual() {
        return jugadores.get(indiceTurno);
    }

    public Fase getFase() {
        return fase;
    }

    public Optional<Integer> getUltimoValorDado() {
        return Optional.ofNullable(ultimoValorDado);
    }

    public Optional<int[]> getUltimosDados() {
        return Optional.ofNullable(ultimosDados).map(int[]::clone);
    }

    public Optional<int[]> getUltimaTirada(Jugador j) {
        return Optional.ofNullable(ultimaTirada.get(j.getColor())).map(int[]::clone);
    }

    private String detalleDados() {
        return ultimosDados[0] + " y " + ultimosDados[1];
    }

    public List<Integer> getFichasElegibles() {
        return fichasElegibles;
    }

    public List<Jugador> getJugadores() {
        return jugadores;
    }

    public Tablero getTablero() {
        return tablero;
    }

    public Optional<Jugador> getGanador() {
        return Optional.ofNullable(ganador);
    }

    public String getMensaje() {
        return mensaje;
    }
}
