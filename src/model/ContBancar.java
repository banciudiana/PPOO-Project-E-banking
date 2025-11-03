package model;

public abstract class ContBancar {
    protected int id;
    protected double sold;
    protected Client client;
    protected  String valuta;

    public ContBancar(int id, double sold, Client client, String valuta) {
        this.id = id;
        this.sold = sold;
        this.client = client;
        this.valuta = valuta;
    }

    public abstract  void retrage( double suma) throws Exception;
    public abstract  void depune( double suma );

    public double getSold() { return sold; }

    public void setSold(double sold) {
        this.sold = sold;
    }

    public int getId() { return id; }
    public Client getClient() { return client; }
    public String getValuta() { return valuta; }

    @Override
    public String toString() {
        return "Cont ID: " + id + " | Sold: " + sold + " " + valuta + " | Client: " + client.getNume();
    }

}
