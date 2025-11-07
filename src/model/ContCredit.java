package model;

import service.CursValutarService;

import java.time.LocalDateTime;

/**
 * Clasa {@code ContCredit} reprezinta un cont bancar de tip credit.
 * <p>
 * Permite retrageri pana la o anumita limita de credit (-5000 lei) si calculeaza dobanda pentru soldul negativ.
 * </p>
 */
public class ContCredit extends ContBancar {

    /** Limita maxima de credit permisa (sold negativ maxim). */
    private static final double LIMITA_CREDIT = -5000;

    /** Dobanda aplicata datoriei (pentru sold negativ). */
    private static final double DOBANDA = 0.1;

    /**
     * Creeaza un nou obiect {@code ContCredit} cu toate datele specificate.
     *
     * @param id            identificatorul unic al contului
     * @param sold          soldul initial
     * @param client        clientul asociat contului
     * @param valuta        valuta contului
     * @param creationDate  data crearii contului
     */
    public ContCredit(int id, double sold, Client client, String valuta, LocalDateTime creationDate) {
        super(id, sold, client, valuta, creationDate);
    }

    /**
     * Creeaza un nou obiect {@code ContCredit} fara a specifica data crearii.
     *
     * @param id      identificatorul unic al contului
     * @param sold    soldul initial
     * @param client  clientul asociat contului
     * @param valuta  valuta contului
     */
    public ContCredit(int id, double sold, Client client, String valuta) {
        super(id, sold, client, valuta);
    }

    /**
     * Retrage o suma din cont, verificand daca limita de credit nu este depasita.
     *
     * @param suma suma de retras
     * @throws Exception daca retragerea depaseste limita de credit
     */
    @Override
    public void retrage(double suma) throws Exception {
        if( this.getValuta().equals("RON")){
            if (sold - suma < LIMITA_CREDIT)
                throw new Exception("Limita de credit depasita!");
        } else if (sold - suma < CursValutarService.convertCuMatrice(LIMITA_CREDIT,"RON","EUR")) {
            throw new Exception("Limita de credit depasita!");

        }
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

    /**
     * Calculeaza dobanda aferenta datoriei, daca soldul este negativ.
     *
     * @return valoarea dobanzii datorate
     */
    public double calculeazaDobandaDatorie() {
        return (sold < 0) ? -sold * DOBANDA : 0;
    }
}
