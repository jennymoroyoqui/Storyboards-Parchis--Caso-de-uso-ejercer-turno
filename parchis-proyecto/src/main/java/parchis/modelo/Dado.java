package parchis.modelo;

import java.util.Random;

public class Dado {

    private final Random random = new Random();

    //Devuelve un valor entre 1 y 6
    public int generarValor() {
        return random.nextInt(6) + 1;
    }
}
