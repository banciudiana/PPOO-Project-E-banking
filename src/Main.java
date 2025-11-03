import model.*;

public class Main {


    public static void main(String[] args) {
        Client c1 = new Client(1, "Ion Popescu", "ion@gmail.com", "1234",false);
        Client c2 = new Client(2, "Maria Ionescu", "maria@yahoo.com", "abcd", false);


        ContCurent cc = new ContCurent(1, 1500, c1, "RON");
        ContEconomii ce = new ContEconomii(2, 2000, c2, "EUR");
        ContCredit cd = new ContCredit(3, -1200, c1, "RON");

        try {
            cc.retrage(200);
            ce.depune(500);
            cd.retrage(1000);
        } catch (Exception e) {
            System.out.println("Eroare: " + e.getMessage());
        }


        System.out.println(cc);
        System.out.println(ce + " | Dobândă: " + ce.calculeazaDobanda());
        System.out.println(cd + " | Dobândă datorie: " + cd.calculeazaDobandaDatorie());

        Tranzactie t = new Tranzactie(1, cc, ce, 100, "Transfer");
        System.out.println(t);
    }
}