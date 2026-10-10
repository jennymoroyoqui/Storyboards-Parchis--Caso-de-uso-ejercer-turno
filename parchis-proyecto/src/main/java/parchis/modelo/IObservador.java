package parchis.modelo;

/**
 * Patron Observer las vistas se registran en ModeloPartida y se redibujan al
 * cambiar el estado
 */
public interface IObservador {

    void actualizar(ModeloPartida modelo);
}
