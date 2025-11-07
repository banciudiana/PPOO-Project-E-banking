package model;

import java.time.LocalDateTime;

/**
 * Clasa {@code ContCurent} reprezinta un cont bancar de tip curent.
 * <p>
 * Permite operatiuni de depunere si retragere, aplicand un comision fix la retrageri.
 * </p>
 */
public class ContCurent extends ContBancar {

    /**
     * Creeaza un nou obiect {@code ContCurent} cu toate datele specificate.
     *
     * @param id            identificatorul unic al contului
     * @param sold          soldul initial
     * @param client        clientul asociat contului
     * @param valuta        valuta contului
     * @param creationDate  data crearii contului
     */
    public ContCurent(int id, double sold, Client client, String valuta, LocalDateTime creationDate) {
        super(id, sold, client, valuta, creationDate);
    }

    /**
     * Creeaza un nou obiect {@code ContCurent} fara a specifica data crearii.
     *
     * @param id      identificatorul unic al contului
     * @param sold    soldul initial
     * @param client  clientul asociat contului
     * @param valuta  valuta contului
     */
    public ContCurent(int id, double sold, Client client, String valuta) {
        super(id, sold, client, valuta);
    }

    /**
     * Retrage o suma din cont.
     *
     * @param suma suma de retras
     * @throws Exception daca soldul este insuficient
     */
    @Override
    public void retrage(double suma) throws Exception {
        if (sold - suma < 0)
            throw new Exception("Sold insuficient!");
        sold -= suma;
    }

    /**
     * Depune o suma in cont.
     *
     * @param suma suma de depus
     */
    @Override
    public void depune(double suma) {
        sold += suma;
    }
}
