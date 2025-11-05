package model;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Tranzactie {
    private int id;
    private ContBancar sursa;
    private ContBancar destinatie;
    private double suma;
    private LocalDateTime data;
    private String tip;

    public Tranzactie(int id, ContBancar sursa, ContBancar destinatie, double suma, String tip) {
        this.id = id;
        this.sursa = sursa;
        this.destinatie = destinatie;
        this.suma = suma;
        this.tip = tip;
        this.data = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ContBancar getSursa() {
        return sursa;
    }

    public void setSursa(ContBancar sursa) {
        this.sursa = sursa;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(String dataStr) {
        this.data = LocalDateTime.parse(dataStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public ContBancar getDestinatie() {
        return destinatie;
    }

    public void setDestinatie(ContBancar destinatie) {
        this.destinatie = destinatie;
    }

    public double getSuma() {
        return suma;
    }

    public void setSuma(double suma) {
        this.suma = suma;
    }

    public String toString() {
        return "[" + data + "] " + tip + " | " + sursa.getClient().getNume() +
                " → " + destinatie.getClient().getNume() + " : " + suma + " " + sursa.getValuta();
    }
}