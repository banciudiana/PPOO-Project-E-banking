package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Clasa {@code Tranzactie} reprezinta o tranzactie efectuata intre doua conturi bancare.
 * <p>
 * O tranzactie contine informatii despre contul sursa, contul destinatie, suma transferata,
 * tipul tranzactiei si data la care a avut loc.
 * </p>
 *
 * @author
 */
public class Tranzactie {

    /** Identificatorul unic al tranzactiei. */
    private int id;

    /** Contul din care se efectueaza tranzactia. */
    private ContBancar sursa;

    /** Contul in care se efectueaza tranzactia. */
    private ContBancar destinatie;

    /** Suma transferata. */
    private double suma;

    /** Data si ora efectuarii tranzactiei. */
    private LocalDateTime data;


    /**
     * Creeaza o noua tranzactie intre doua conturi.
     *
     * @param id          identificatorul unic al tranzactiei
     * @param sursa       contul sursa
     * @param destinatie  contul destinatie
     * @param suma        suma transferata

     */
    public Tranzactie(int id, ContBancar sursa, ContBancar destinatie, double suma) {
        this.id = id;
        this.sursa = sursa;
        this.destinatie = destinatie;
        this.suma = suma;

        this.data = LocalDateTime.now();
    }

    /**
     * Returneaza identificatorul unic al tranzactiei.
     *
     * @return id-ul tranzactiei
     */
    public int getId() {
        return id;
    }


    /**
     * Returneaza contul sursa al tranzactiei.
     *
     * @return contul sursa
     */
    public ContBancar getSursa() {
        return sursa;
    }

    /**
     * Seteaza contul sursa al tranzactiei.
     *
     * @param sursa contul sursa
     */
    public void setSursa(ContBancar sursa) {
        this.sursa = sursa;
    }

    /**
     * Returneaza contul destinatie al tranzactiei.
     *
     * @return contul destinatie
     */
    public ContBancar getDestinatie() {
        return destinatie;
    }

    /**
     * Seteaza contul destinatie al tranzactiei.
     *
     * @param destinatie contul destinatie
     */
    public void setDestinatie(ContBancar destinatie) {
        this.destinatie = destinatie;
    }

    /**
     * Returneaza suma tranzactionata.
     *
     * @return suma transferata
     */
    public double getSuma() {
        return suma;
    }

    /**
     * Seteaza suma tranzactionata.
     *
     * @param suma suma transferata
     */
    public void setSuma(double suma) {
        this.suma = suma;
    }

    /**
     * Returneaza data si ora la care a fost efectuata tranzactia.
     *
     * @return data tranzactiei
     */
    public LocalDateTime getData() {
        return data;
    }

    /**
     * Seteaza data tranzactiei pe baza unui sir de caractere formatat.
     *
     * @param dataStr data in format "yyyy-MM-dd HH:mm:ss"
     */
    public void setData(String dataStr) {
        this.data = LocalDateTime.parse(dataStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }




    /**
     * Returneaza o reprezentare textuala a tranzactiei.
     * Format: "[data] tip | sursa → destinatie : suma valuta"
     *
     * @return un sir care descrie tranzactia
     */
    @Override
    public String toString() {
        return "[" + data + "] " + sursa.getClient().getNume() +
                " → " + destinatie.getClient().getNume() + " : " + suma + " " + sursa.getValuta();
    }
}

