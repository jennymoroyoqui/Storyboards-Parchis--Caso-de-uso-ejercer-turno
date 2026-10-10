package parchis.vista;

import parchis.modelo.Color;

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * Geometría del tablero en forma de cruz sobre una rejilla de 19x19 casillas (como en los storyboards):
 * bases de 8x8 en las esquinas, brazos de 3 casillas de ancho y 8 de largo.
 * Anillo de 68 casillas = 4 brazos x 17 (8 + casilla final + 8). El pasillo de meta es el carril central.
 */
final class GeometriaTablero {
    static final int N = 19;
    private static final Point[] ANILLO = construirAnillo();

    private GeometriaTablero() { }

    private static Point[] construirAnillo() {
        Point[] a = new Point[68];
        int i = 0;
        // Brazo superior (amarillo)
        for (int r = 7; r >= 0; r--) a[i++] = new Point(10, r);
        a[i++] = new Point(9, 0);
        for (int r = 0; r <= 7; r++) a[i++] = new Point(8, r);
        // Brazo izquierdo (verde)
        for (int c = 7; c >= 0; c--) a[i++] = new Point(c, 8);
        a[i++] = new Point(0, 9);
        for (int c = 0; c <= 7; c++) a[i++] = new Point(c, 10);
        // Brazo inferior (azul)
        for (int r = 11; r <= 18; r++) a[i++] = new Point(8, r);
        a[i++] = new Point(9, 18);
        for (int r = 18; r >= 11; r--) a[i++] = new Point(10, r);
        // Brazo derecho (rojo)
        for (int c = 11; c <= 18; c++) a[i++] = new Point(c, 10);
        a[i++] = new Point(18, 9);
        for (int c = 18; c >= 11; c--) a[i++] = new Point(c, 8);
        return a;
    }

    static Point anillo(int indice) { return ANILLO[indice]; }

    /** Brazo del color: 0 = superior, 1 = izquierdo, 2 = inferior, 3 = derecho. */
    static int segmento(Color c) {
        return switch (c) { case AMARILLO -> 0; case VERDE -> 1; case AZUL -> 2; case ROJO -> 3; };
    }

    /** Casilla del carril de meta: k = 0 es la casilla final del anillo, k = 1..7 el pasillo. */
    static Point pasillo(Color c, int k) {
        return switch (segmento(c)) {
            case 0 -> new Point(9, k);
            case 1 -> new Point(k, 9);
            case 2 -> new Point(9, 18 - k);
            default -> new Point(18 - k, 9);
        };
    }

    static Point origenBase(Color c) {
        return switch (c) {
            case AMARILLO -> new Point(0, 0);
            case ROJO -> new Point(11, 0);
            case VERDE -> new Point(0, 11);
            case AZUL -> new Point(11, 11);
        };
    }

    /** Centro del hueco de una ficha en su base (la ficha 2 queda arriba a la derecha, como en el storyboard). */
    static Point2D.Double slotBase(Color c, int idFicha) {
        Point o = origenBase(c);
        double dx = (idFicha == 2 || idFicha == 4) ? 5.5 : 2.5;
        double dy = (idFicha == 3 || idFicha == 4) ? 5.5 : 2.5;
        return new Point2D.Double(o.x + dx, o.y + dy);
    }

    /** Posición dentro del triángulo central del color. */
    static Point2D.Double slotMeta(Color c, int idFicha) {
        double off = (idFicha - 2.5) * 0.22;
        return switch (segmento(c)) {
            case 0 -> new Point2D.Double(9.5 + off, 8.85);
            case 1 -> new Point2D.Double(8.85, 9.5 + off);
            case 2 -> new Point2D.Double(9.5 + off, 10.15);
            default -> new Point2D.Double(10.15, 9.5 + off);
        };
    }
}
