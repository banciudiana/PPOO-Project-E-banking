package model;

public class ContEconomii extends  ContBancar implements   ContCuDobanda{

    private static final double DOBANDA = 0.03;

    public ContEconomii(int id, double sold, Client client, String valuta) {
        super(id, sold, client, valuta);
    }

    @Override
    public double calculeazaDobanda() {
        return sold * DOBANDA;
    }

    @Override
    public void retrage(double suma) throws Exception {
        if (suma > sold * 0.5)
            throw new Exception("Poți retrage maxim 50% din sold!");
        sold -= suma;
    }

    @Override
    public void depune(double suma) {
        sold += suma;
    }
}
