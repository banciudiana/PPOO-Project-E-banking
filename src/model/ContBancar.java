package model;

import java.time.LocalDateTime;

public abstract class ContBancar {
    protected int id;
    protected double sold;
    protected Client client;
    protected  String valuta;
    protected LocalDateTime creationDate;

    public ContBancar(int id, double sold, Client client, String valuta, LocalDateTime creationDate) {
        this.id = id;
        this.sold = sold;
        this.client = client;
        this.valuta = valuta;
        this.creationDate = creationDate;
    }

    public ContBancar(int id, double sold, Client client, String valuta) {
        this(id, sold, client, valuta, LocalDateTime.now());
    }

    public abstract void retrage(double suma) throws Exception;
    public abstract void depune(double suma);

    public double getSold() { return sold; }
    public void setSold(double sold) { this.sold = sold; }
    public int getId() { return id; }
    public Client getClient() { return client; }
    public String getValuta() { return valuta; }
    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public void schimbaValuta(String valutaNoua) throws Exception {
        if (this.valuta.equalsIgnoreCase(valutaNoua)) {
            throw new Exception("Contul este deja în " + valutaNoua);
        }

        // Convertește soldul la noua valută
        double soldNou = service.CursValutarService.convert(this.sold, this.valuta, valutaNoua);

        this.sold = soldNou;
        this.valuta = valutaNoua;
    }

    @Override
    public String toString() {
        return "Cont ID: " + id + " | Sold: " + sold + " " + valuta + " | Client: " + client.getNume()
                + " | Creat: " + creationDate.toLocalDate().toString();
    }
}


