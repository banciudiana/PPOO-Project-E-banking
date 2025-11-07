package model;

import java.time.LocalDateTime;

/**
 * Clasa abstracta {@code ContBancar} reprezinta un cont bancar.
 * <p>
 * Contine informatii despre sold, valuta, clientul asociat si data crearii contului.
 * Aceasta clasa trebuie extinsa de tipuri concrete de conturi (de exemplu, ContCurent, ContEconomii).
 * </p>
 *
 * @author
 */
public abstract class ContBancar {

    /** Identificatorul unic al contului. */
    protected int id;

    /** Soldul curent al contului. */
    protected double sold;

    /** Clientul asociat contului. */
    protected Client client;

    /** Valuta in care este exprimat soldul. */
    protected String valuta;

    /** Data si ora crearii contului. */
    protected LocalDateTime creationDate;

    /**
     * Creeaza un cont bancar cu toate datele specificate.
     *
     * @param id           identificatorul unic al contului
     * @param sold         soldul initial al contului
     * @param client       clientul asociat contului
     * @param valuta       valuta contului
     * @param creationDate data si ora crearii contului
     */
    public ContBancar(int id, double sold, Client client, String valuta, LocalDateTime creationDate) {
        this.id = id;
        this.sold = sold;
        this.client = client;
        this.valuta = valuta;
        this.creationDate = creationDate;
    }

    /**
     * Creeaza un cont bancar cu data crearii setata la momentul actual.
     *
     * @param id     identificatorul unic al contului
     * @param sold   soldul initial al contului
     * @param client clientul asociat contului
     * @param valuta valuta contului
     */
    public ContBancar(int id, double sold, Client client, String valuta) {
        this(id, sold, client, valuta, LocalDateTime.now());
    }

    /**
     * Retrage o suma din cont.
     *
     * @param suma suma de retras
     * @throws Exception daca fondurile sunt insuficiente
     */
    public abstract void retrage(double suma) throws Exception;

    /**
     * Depune o suma in cont.
     *
     * @param suma suma de depus
     */
    public abstract void depune(double suma);

    /**
     * Returneaza soldul curent al contului.
     *
     * @return soldul contului
     */
    public double getSold() {
        return sold;
    }

    /**
     * Seteaza soldul contului.
     *
     * @param sold noul sold
     */
    public void setSold(double sold) {
        this.sold = sold;
    }

    /**
     * Returneaza identificatorul contului.
     *
     * @return id-ul contului
     */
    public int getId() {
        return id;
    }

    /**
     * Returneaza clientul asociat contului.
     *
     * @return clientul contului
     */
    public Client getClient() {
        return client;
    }

    /**
     * Returneaza valuta contului.
     *
     * @return valuta contului
     */
    public String getValuta() {
        return valuta;
    }

    /**
     * Returneaza data si ora crearii contului.
     *
     * @return data si ora crearii
     */
    public LocalDateTime getCreationDate() {
        return creationDate;
    }



    /**
     * Schimba valuta contului si converteste soldul in noua valuta.
     *
     * @param valutaNoua noua valuta dorita
     * @throws Exception daca contul este deja in aceasta valuta
     */
    public void schimbaValuta(String valutaNoua) throws Exception {
        if (this.valuta.equalsIgnoreCase(valutaNoua)) {
            throw new Exception("Contul este deja in " + valutaNoua);
        }
        double soldNou = service.CursValutarService.convert(this.sold, this.valuta, valutaNoua);
        this.sold = soldNou;
        this.valuta = valutaNoua;
    }

    /**
     * Returneaza o reprezentare textuala a contului, ptr fisierul txt
     *
     * @return un sir de caractere care descrie contul
     */
    @Override
    public String toString() {
        return "Cont ID: " + id + " | Sold: " + sold + " " + valuta + " | Client: " + client.getNume()
                + " | Creat: " + creationDate.toLocalDate().toString();
    }
}
