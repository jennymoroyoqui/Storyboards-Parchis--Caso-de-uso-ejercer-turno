package parchis.fachada;

import parchis.modelo.Color;
import java.util.List;

public interface IFachadaJuego {

    /**
     * Lanza los dos dados compartidos de la partida y devuelve el valor del
     * turno (suma de ambos)
     */
    int generarValor();

    //Valores individuales de la ultima tirada de los dos dados
    int[] ultimosDados();

    int calcularDestino(Color color, int idFicha, int[] dados);

    List<Integer> validarMovimientos(Color color, int[] dados);

    ResultadoMovimiento ejecutarMovimiento(Color color, int idFicha, int[] dados);

    default int mockObtenerValorDado() {
        return generarValor();
    }

    default boolean mockValidarMovimiento(Color color, int idFicha, int[] dados) {
        return validarMovimientos(color, dados).contains(idFicha);
    }
}
