package model;

import exceptions.RetragereInainteDePerioadaException;

import java.time.Duration;
import java.time.LocalDateTime;

public class ContEconomii extends ContBancar implements ContCuDobanda {

    public enum TipEconomii { BONUS, ECONOMII }

    private static final double DOBANDA_ECONOMII = 0.02; // 2%
    private static final double DOBANDA_BONUS = 0.05; // 5% (după 4 luni)
    private double dobandaAcumulata = 0.0;
    private TipEconomii tipEconomii;

    // constructor care primește și creationDate + tip
    public ContEconomii(int id, double sold, Client client, String valuta,
                        LocalDateTime creationDate, TipEconomii tipEconomii, double dobandaAcumulataInitiala) {
        super(id, sold, client, valuta, creationDate);
        this.tipEconomii = tipEconomii;
        this.dobandaAcumulata = dobandaAcumulataInitiala;
    }

    // versiune compatibila
    // constructor compatibil pentru cazuri simple (fără tip explicit)
    public ContEconomii(int id, double sold, Client client, String valuta) {
        this(id, sold, client, valuta, LocalDateTime.now(), TipEconomii.ECONOMII, 0.0);
    }

    @Override
    public double calculeazaDobanda() {
        // returnează dobânda curentă (posibilă pentru o perioadă de calcul)
        if (tipEconomii == TipEconomii.BONUS) {
            // dacă are deja 4 luni sau mai mult, aplicăm 5% la sold
            long luni = calculeazaLuniDeLaCreare();
            if (luni >= 4) {
                return sold * DOBANDA_BONUS;
            } else {
                // poate afișăm 0 sau procent parțial; pentru simplitate: 0 până la 4 luni
                return 0.0;
            }
        } else {
            // tip ECONOMII => 2% oricând
            return sold * DOBANDA_ECONOMII;
        }
    }

    public double getDobandaAcumulata() {
        return dobandaAcumulata;
    }

    public TipEconomii getTip() {
        return tipEconomii;
    }

    public void setTipEconomii(TipEconomii tipEconomii) {
        this.tipEconomii = tipEconomii;
    }

    public long calculeazaLuniDeLaCreare() {
        Duration d = Duration.between(creationDate, LocalDateTime.now());
        return d.toDays() / 30; // aproximare în luni (30 zile)
    }

    /** Aplica dobanda lunara (sau apelata de banca odata pe luna). Adauga in dobandaAcumulata. */
    public void aplicaDobandaLunara() {
        double procent = (tipEconomii == TipEconomii.BONUS && calculeazaLuniDeLaCreare() >= 4)
                ? DOBANDA_BONUS
                : (tipEconomii == TipEconomii.ECONOMII ? DOBANDA_ECONOMII : 0.0);

        double valoare = sold * procent; // poți schimba formula la nevoie (ex: procent/12)
        dobandaAcumulata += valoare;
    }

    /** Retragere standard: respecta regula 50% si, pentru BONUS, arunca exceptie daca vrei confirmare (vezi UI) */
    @Override
    public void retrage(double suma) throws Exception {
        if (suma > sold * 0.5) {
            throw new Exception("Poți retrage maxim 50% din sold!");
        }

        if (tipEconomii == TipEconomii.BONUS && calculeazaLuniDeLaCreare() < 4) {
            // semnalăm UI-ului că retragerea este "early" — aruncăm o excepție specifică
            throw new RetragereInainteDePerioadaException("Retragere înainte de 4 luni: dacă continui pierzi dobânda acumulată.");
        }

        // retragere normală: scădem suma (dobandaAcumulata rămâne intactă doar dacă regula permite)
        sold -= suma;
    }

    /** Retragere forțată: se folosește atunci când UI confirmă pierderea dobânzii. */
    public void retrageFortat(double suma) throws Exception {
        if (suma > sold * 0.5) {
            throw new Exception("Poți retrage maxim 50% din sold!");
        }
        // pierzi dobânda acumulată
        dobandaAcumulata = 0.0;
        sold -= suma;
    }

    /** Aplică dobânda acumulată în sold (de exemplu la finalul unei perioade) */
    public void transferaDobandaInSold() {
        sold += dobandaAcumulata;
        dobandaAcumulata = 0.0;
    }

    @Override
    public void depune(double suma) {
        sold += suma;
    }
}
