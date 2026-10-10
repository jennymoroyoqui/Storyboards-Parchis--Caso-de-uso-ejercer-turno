package parchis.fachada;

import parchis.modelo.*;
import java.util.*;

/**
 * Mock permite programar los valores del dado para que la demostracion sea
 * reproducible (Fernando saca un resultado de 5) Si no hay tiradas programadas
 * se utilizan los dados reales
 */
public class FachadaJuegoMock implements IFachadaJuego {

    private final Tablero tablero;
    private final Dado dado1 = new Dado();
    private final Dado dado2 = new Dado();
    private final Deque<int[]> tiradasProgramadas = new ArrayDeque<>();
    private int[] ultimos = {0, 0};

    public FachadaJuegoMock(Tablero tablero) {
        this.tablero = tablero;
    }

    //Programa tiradas completas, cada una como dado1, dado2
    public FachadaJuegoMock programarTiradas(int[]... tiradas) {
        for (int[] t : tiradas) {
            tiradasProgramadas.add(t.clone());
        }
        return this;
    }

    public FachadaJuegoMock limpiarTiradasProgramadas() {
        tiradasProgramadas.clear();
        return this;
    }

    @Override
    public int calcularDestino(Color color, int idFicha, int[] dados) {
        Ficha f = tablero.obtenerFicha(color, idFicha).orElseThrow();
        return f.getEstado() == Ficha.Estado.BASE ? Tablero.avanceTrasSalir(dados) : f.getPasos() + Tablero.suma(dados);
    }

    @Override
    public int generarValor() {
        int[] t = tiradasProgramadas.poll();
        ultimos = t != null ? t : new int[]{dado1.generarValor(), dado2.generarValor()};
        return ultimos[0] + ultimos[1];
    }

    @Override
    public int[] ultimosDados() {
        return ultimos.clone();
    }

    @Override
    public List<Integer> validarMovimientos(Color color, int[] dados) {
        return tablero.validarMovimientos(color, dados);
    }

    @Override
    public ResultadoMovimiento ejecutarMovimiento(Color color, int idFicha, int[] dados) {
        if (!validarMovimientos(color, dados).contains(idFicha)) {
            return ResultadoMovimiento.invalido(idFicha, "Movimiento no valido");
        }
        Ficha ficha = tablero.obtenerFicha(color, idFicha).orElseThrow();
        int nuevosPasos = calcularDestino(color, idFicha, dados);
        ficha.actualizarPosicion(nuevosPasos);                          // Ficha.actualizarPosicion()
        List<Ficha> capturadas = tablero.actualizarEstadoTablero(ficha); // Tablero.actualizarEstadoTablero()
        boolean meta = ficha.getEstado() == Ficha.Estado.META;
        return new ResultadoMovimiento(true, idFicha, !capturadas.isEmpty(), meta, "Movimiento aplicado");
    }
}
