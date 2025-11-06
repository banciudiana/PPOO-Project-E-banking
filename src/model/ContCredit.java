package model;

import java.time.LocalDateTime;

public class ContCredit extends  ContBancar{

    private static final double LIMITA_CREDIT = -5000;
    private static final double DOBANDA = 0.1;

    public ContCredit(int id, double sold, Client client, String valuta, LocalDateTime creationDate) {
        super(id, sold, client, valuta, creationDate);
    }

    public ContCredit(int id, double sold, Client client, String valuta) {
        super(id, sold, client, valuta);
    }


    @Override
    public void retrage(double suma) throws Exception {
        if (sold - suma < LIMITA_CREDIT)
            throw new Exception("Limita de credit depășită!");
        sold -= suma;
    }

    @Override
    public void depune(double suma) {
        sold += suma;

    }

    public double calculeazaDobandaDatorie() {
        return (sold < 0) ? -sold * DOBANDA : 0;
    }
}
