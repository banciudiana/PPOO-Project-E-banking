package model;

import exceptions.RetragereInainteDePerioadaException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Clasa {@code ContEconomii} reprezinta un cont bancar de economii.
 * <p>
 * Acest tip de cont poate fi de doua feluri: {@code ECONOMII} sau {@code BONUS}.
 * Ofera dobanda lunara si restrictii la retrageri
 * </p>
 */
public class ContEconomii extends ContBancar implements ContCuDobanda {

    /**
     * Tipurile posibile de cont de economii.
     * <ul>
     *     <li>{@code BONUS} - ofera dobanda mai mare dupa 4 luni.</li>
     *     <li>{@code ECONOMII} - ofera dobanda constanta de 2%.</li>
     * </ul>
     */
    public enum TipEconomii { BONUS, ECONOMII }

    /** Dobanda pentru conturile de tip ECONOMII (2%). */
    private static final double DOBANDA_ECONOMII = 0.02;

    /** Dobanda pentru conturile de tip BONUS (5% dupa 4 luni). */
    private static final double DOBANDA_BONUS = 0.05;

    /** Dobanda acumulata pana in prezent. */
    private double dobandaAcumulata = 0.0;

    /** Tipul contului de economii. */
    private TipEconomii tipEconomii;

    /**
     * Creeaza un nou obiect {@code ContEconomii} cu toate datele specificate.
     *
     * @param id                        identificatorul unic al contului
     * @param sold                      soldul initial
     * @param client                    clientul asociat contului
     * @param valuta                    valuta contului
     * @param creationDate              data crearii contului
     * @param tipEconomii               tipul contului de economii
     * @param dobandaAcumulataInitiala  dobanda acumulata initial
     */
    public ContEconomii(int id, double sold, Client client, String valuta,
                        LocalDateTime creationDate, TipEconomii tipEconomii, double dobandaAcumulataInitiala) {
        super(id, sold, client, valuta, creationDate);
        this.tipEconomii = tipEconomii;
        this.dobandaAcumulata = dobandaAcumulataInitiala;
    }

    /**
     * Creeaza un nou obiect {@code ContEconomii} de tip ECONOMII cu valori implicite.
     *
     * @param id      identificatorul unic al contului
     * @param sold    soldul initial
     * @param client  clientul asociat contului
     * @param valuta  valuta contului
     */
    public ContEconomii(int id, double sold, Client client, String valuta) {
        this(id, sold, client, valuta, LocalDateTime.now(), TipEconomii.ECONOMII, 0.0);
    }

    /**
     * Calculeaza dobanda aferenta contului, in functie de tipul acestuia si perioada scursa.
     *
     * @return valoarea dobanzii calculate
     */
    @Override
    public double calculeazaDobanda() {
        if (tipEconomii == TipEconomii.BONUS) {
            long luni = calculeazaLuniDeLaCreare();
            return (luni >= 4) ? sold * DOBANDA_BONUS : 0.0;
        } else {
            return sold * DOBANDA_ECONOMII;
        }
    }

    /**
     * Returneaza dobanda acumulata.
     *
     * @return dobanda acumulata
     */
    public double getDobandaAcumulata() {
        return dobandaAcumulata;
    }

    /**
     * Returneaza tipul contului de economii.
     *
     * @return tipul contului
     */
    public TipEconomii getTip() {
        return tipEconomii;
    }

    /**
     * Seteaza tipul contului de economii.
     *
     * @param tipEconomii tipul nou de cont
     */
    public void setTipEconomii(TipEconomii tipEconomii) {
        this.tipEconomii = tipEconomii;
    }

    /**
     * Calculeaza numarul de luni scurse de la crearea contului.
     *
     * @return numarul de luni trecute
     */
    public long calculeazaLuniDeLaCreare() {
        return ChronoUnit.MONTHS.between(creationDate, LocalDateTime.now());
    }

    /**
     * Aplica dobanda lunara in functie de tipul contului si actualizeaza valoarea dobanzii acumulate.
     */
    public void aplicaDobandaLunara() {
        double procent = 0.0;

        if (tipEconomii == TipEconomii.BONUS) {
            long luni = calculeazaLuniDeLaCreare();
            if (luni >= 4) {
                procent = DOBANDA_BONUS;
            }
        } else if (tipEconomii == TipEconomii.ECONOMII) {
            procent = DOBANDA_ECONOMII;
        }

        if (procent > 0) {
            double dobanda = sold * procent;
            sold += dobanda;
            dobandaAcumulata = dobanda;
        }
    }

    /**
     * Retrage o suma din cont, respectand regula retragerii maxime de 50%.
     * <p>
     * Pentru conturile de tip BONUS, arunca o exceptie daca retragerea are loc inainte de 4 luni.
     * </p>
     *
     * @param suma suma de retras
     * @throws Exception daca suma depaseste 50% din sold sau daca retragerea este prea devreme
     */
    @Override
    public void retrage(double suma) throws Exception {
        if (suma > sold * 0.5) {
            throw new Exception("Poti retrage maxim 50% din sold!");
        }

        if (tipEconomii == TipEconomii.BONUS && calculeazaLuniDeLaCreare() < 4) {
            throw new RetragereInainteDePerioadaException(
                    "Retragere inainte de 4 luni: daca continui pierzi dobanda acumulata.");
        }

        sold -= suma;
    }

    /**
     * Retragere fortata, folosita cand utilizatorul confirma pierderea dobanzii acumulate.
     *
     * @param suma suma de retras
     * @throws Exception daca suma depaseste 50% din sold
     */
    public void retrageFortat(double suma) throws Exception {
        if (suma > sold * 0.5) {
            throw new Exception("Poti retrage maxim 50% din sold!");
        }
        dobandaAcumulata = 0.0;
        sold -= suma;
    }



    /**
     * Depune o suma in cont.
     *
     * @param suma suma de depus
     */
    @Override
    public void depune(double suma) {
        sold += suma;
    }

    /**
     * Schimba valuta contului si actualizeaza atat soldul, cat si dobanda acumulata in noua valuta.
     *
     * @param valutaNoua noua valuta in care se va converti contul
     * @throws Exception daca valuta este aceeasi cu cea curenta
     */
    @Override
    public void schimbaValuta(String valutaNoua) throws Exception {
        if (this.valuta.equalsIgnoreCase(valutaNoua)) {
            throw new Exception("Contul este deja in " + valutaNoua);
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
