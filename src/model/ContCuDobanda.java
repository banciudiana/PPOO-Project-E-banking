package model;

/**
 * Interfata {@code ContCuDobanda} defineste comportamentul pentru conturile care genereaza dobanda.
 * <p>
 * Clasele care implementeaza aceasta interfata trebuie sa ofere o metoda pentru calculul dobanzii.
 * </p>
 *
 * @author
 */
public interface ContCuDobanda {

    /**
     * Calculeaza dobanda aferenta contului.
     *
     * @return valoarea dobanzii calculate
     */
    double calculeazaDobanda();
}
