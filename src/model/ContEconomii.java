package model;

import exceptions.RetragereInainteDePerioadaException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ContEconomii extends ContBancar implements ContCuDobanda {

    public enum TipEconomii { BONUS, ECONOMII }

    private static final double DOBANDA_ECONOMII = 0.02; // 2%
    private static final double DOBANDA_BONUS = 0.05; // 5% (după 4 luni)
    private double dobandaAcumulata = 0.0;
    private TipEconomii tipEconomii;


    public ContEconomii(int id, double sold, Client client, String valuta,
                        LocalDateTime creationDate, TipEconomii tipEconomii, double dobandaAcumulataInitiala) {
        super(id, sold, client, valuta, creationDate);
        this.tipEconomii = tipEconomii;
        this.dobandaAcumulata = dobandaAcumulataInitiala;
    }


    public ContEconomii(int id, double sold, Client client, String valuta) {
        this(id, sold, client, valuta, LocalDateTime.now(), TipEconomii.ECONOMII, 0.0);
    }

    @Override
    public double calculeazaDobanda() {

        if (tipEconomii == TipEconomii.BONUS) {

            long luni = calculeazaLuniDeLaCreare();
            if (luni >= 4) {
                return sold * DOBANDA_BONUS;
            } else {

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
        return ChronoUnit.MONTHS.between(creationDate, LocalDateTime.now());
    }

    /** Aplica dobanda lunara (sau apelata de banca odata pe luna). Adauga in dobandaAcumulata. */
    public void aplicaDobandaLunara() {
        double procent = 0.0;

        if (tipEconomii == TipEconomii.BONUS) {
            long luni = calculeazaLuniDeLaCreare();
            if (luni >= 4) {
                procent = DOBANDA_BONUS; // 5% după 4 luni
            }
        } else if (tipEconomii == TipEconomii.ECONOMII) {
            procent = DOBANDA_ECONOMII; // 2%
        }

        if (procent > 0) {
            double dobanda = sold * procent;
            sold += dobanda;
            dobandaAcumulata = dobanda;
        }
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

    @Override
    public void schimbaValuta(String valutaNoua) throws Exception {
        if (this.valuta.equalsIgnoreCase(valutaNoua)) {
            throw new Exception("Contul este deja în " + valutaNoua);
        }

        String valutaVeche = this.valuta;

        double soldNou = service.CursValutarService.convert(this.sold, valutaVeche, valutaNoua);
        double dobandaNoua = this.dobandaAcumulata > 0
                ? service.CursValutarService.convert(this.dobandaAcumulata, valutaVeche, valutaNoua)
                : 0.0;

        this.sold = soldNou;
        this.dobandaAcumulata = dobandaNoua;
        this.valuta = valutaNoua;
    }



}
