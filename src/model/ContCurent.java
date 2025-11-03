package model;

/**
 * Clasă derivată din ContBancar care modelează un cont curent.
 * Are comision fix pentru fiecare retragere.
 */

public class ContCurent extends ContBancar {
    private static final double COMISION = 2.5;

    /**
     * Creează un cont curent.
     *
     * @param id      ID-ul contului
     * @param sold    soldul inițial
     * @param client  clientul deținător
     * @param valuta  moneda contului
     */

    public ContCurent(int id, double sold, Client client, String valuta) {
        super(id, sold, client, valuta);
    }

    /**
     * Retrage o sumă din cont, aplicând comisionul fix.
     *
     * @param suma suma retrasă
     * @throws Exception dacă soldul este insuficient
     */

    @Override
    public void retrage(double suma) throws Exception {
        if (sold - suma - COMISION < 0)
            throw new Exception("Sold insuficient!");
        sold -= (suma + COMISION);
    }


    /** Depune o sumă în cont. */
    @Override
    public void depune(double suma) {
        sold += suma;
    }
}
