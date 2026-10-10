package parchis.fachada;

//Respuesta de la fachada a una solicitud de movimiento

public record ResultadoMovimiento(boolean valido, int idFicha, boolean capturo, boolean llegoAMeta, String mensaje) {

    public static ResultadoMovimiento invalido(int idFicha, String motivo) {
        return new ResultadoMovimiento(false, idFicha, false, false, motivo);
    }
}
