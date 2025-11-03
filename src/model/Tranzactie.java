package model;
import java.time.LocalDateTime;

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

    public String toString() {
        return "[" + data + "] " + tip + " | " + sursa.getClient().getNume() +
                " → " + destinatie.getClient().getNume() + " : " + suma + " " + sursa.getValuta();
    }
}