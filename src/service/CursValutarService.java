package service;

import java.io.*;
import java.util.*;

/**
 * Incarca cursul valutar din fisier si ofera conversii simple.
 * Include vector pentru istoric si matrice pentru conversii multiple valute.
 */
public class CursValutarService {
    private static double ronToEur;
    private static double eurToRon;
    private static final String PATH = "data/curs.txt";

    // VECTOR: Istoric de cursuri RON->EUR (ultimele N valori)
    private static ArrayList<Double> istoricCursuriRON = new ArrayList<>();
    private static final int MAX_ISTORIC = 10; // maxim 10 valori in istoric

    // MATRICE: Cursuri de schimb între multiple valute
    // Valutele suportate: RON (0), EUR (1), USD (2), GBP (3)
    private static final String[] VALUTE = {"RON", "EUR", "USD", "GBP"};
    private static double[][] matriceCursuri = new double[4][4];

    /**
     * Initializeaza matricea cu valori identitate (1.0 pe diagonala)
     */
    private static void initializeazaMatrice() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                matriceCursuri[i][j] = (i == j) ? 1.0 : 0.0;
            }
        }
    }

    public static void incarcaCursuri() {
        initializeazaMatrice();

        File f = new File(PATH);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linie;
            while ((linie = br.readLine()) != null) {
                String[] parts = linie.split("=");
                if (parts.length != 2) continue;
                String key = parts[0].trim();
                double val = Double.parseDouble(parts[1].trim());

                if ("RON_TO_EUR".equalsIgnoreCase(key)) {
                    ronToEur = val;
                    adaugaInIstoric(val); // Adaugă în vectorul de istoric
                    matriceCursuri[0][1] = val; // RON -> EUR
                } else if ("EUR_TO_RON".equalsIgnoreCase(key)) {
                    eurToRon = val;
                    matriceCursuri[1][0] = val; // EUR -> RON
                }
                // Exemple de alte cursuri pentru matrice
                else if ("RON_TO_USD".equalsIgnoreCase(key)) {
                    matriceCursuri[0][2] = val;
                } else if ("USD_TO_RON".equalsIgnoreCase(key)) {
                    matriceCursuri[2][0] = val;
                } else if ("RON_TO_GBP".equalsIgnoreCase(key)) {
                    matriceCursuri[0][3] = val;
                } else if ("GBP_TO_RON".equalsIgnoreCase(key)) {
                    matriceCursuri[3][0] = val;
                } else if ("EUR_TO_USD".equalsIgnoreCase(key)) {
                    matriceCursuri[1][2] = val;
                } else if ("USD_TO_EUR".equalsIgnoreCase(key)) {
                    matriceCursuri[2][1] = val;
                } else if ("EUR_TO_GBP".equalsIgnoreCase(key)) {
                    matriceCursuri[1][3] = val;
                } else if ("GBP_TO_EUR".equalsIgnoreCase(key)) {
                    matriceCursuri[3][1] = val;
                } else if ("USD_TO_GBP".equalsIgnoreCase(key)) {
                    matriceCursuri[2][3] = val;
                } else if ("GBP_TO_USD".equalsIgnoreCase(key)) {
                    matriceCursuri[3][2] = val;
                }
            }
        } catch (Exception e) {
            System.out.println("Eroare la incarcarea cursului: " + e.getMessage());
        }
    }

    /**
     * OPERATII PE VECTOR
     * Adauga un curs in istoric (vector)
     */
    private static void adaugaInIstoric(double curs) {
        istoricCursuriRON.add(curs);
        // Pastreaza doar ultimele MAX_ISTORIC valori
        if (istoricCursuriRON.size() > MAX_ISTORIC) {
            istoricCursuriRON.remove(0);
        }
    }

    /**
     * Returneaza vectorul de istoric
     */
    public static ArrayList<Double> getIstoric() {
        return new ArrayList<>(istoricCursuriRON);
    }

    /**
     * Calculeaza media cursurilor din istoric
     */
    public static double getMedieCursRON() {
        if (istoricCursuriRON.isEmpty()) return 0.0;
        double suma = 0.0;
        for (double curs : istoricCursuriRON) {
            suma += curs;
        }
        return suma / istoricCursuriRON.size();
    }

    /**
     * Gaseste cursul minim din istoric
     */
    public static double getMinCursRON() {
        if (istoricCursuriRON.isEmpty()) return 0.0;
        double min = istoricCursuriRON.get(0);
        for (double curs : istoricCursuriRON) {
            if (curs < min) min = curs;
        }
        return min;
    }

    /**
     * Gaseste cursul maxim din istoric
     */
    public static double getMaxCursRON() {
        if (istoricCursuriRON.isEmpty()) return 0.0;
        double max = istoricCursuriRON.get(0);
        for (double curs : istoricCursuriRON) {
            if (curs > max) max = curs;
        }
        return max;
    }

    /**
     * OPERATII PE MATRICE
     * Converteste folosind matricea de cursuri
     */
    public static double convertCuMatrice(double suma, String from, String to) {
        int indexFrom = getIndexValuta(from);
        int indexTo = getIndexValuta(to);

        if (indexFrom == -1 || indexTo == -1) {
            return suma; // Valuta necunoscuta
        }

        double curs = matriceCursuri[indexFrom][indexTo];
        if (curs == 0.0 && indexFrom != indexTo) {
            System.out.println("Curs nesetat intre " + from + " si " + to);
            return suma;
        }

        return suma * curs;
    }

    /**
     * Returneaza indexul valutei in matrice
     */
    private static int getIndexValuta(String valuta) {
        for (int i = 0; i < VALUTE.length; i++) {
            if (VALUTE[i].equalsIgnoreCase(valuta)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Afiseaza matricea de cursuri
     */
    public static void afiseazaMatriceCursuri() {
        System.out.println("\n=== Matrice Cursuri Valutare ===");
        System.out.print("       ");
        for (String valuta : VALUTE) {
            System.out.printf("%8s", valuta);
        }
        System.out.println();

        for (int i = 0; i < 4; i++) {
            System.out.printf("%6s ", VALUTE[i]);
            for (int j = 0; j < 4; j++) {
                System.out.printf("%8.4f", matriceCursuri[i][j]);
            }
            System.out.println();
        }
    }

    /**
     * Actualizeaza un curs in matrice
     */
    public static void actualizeazaCurs(String from, String to, double curs) {
        int indexFrom = getIndexValuta(from);
        int indexTo = getIndexValuta(to);

        if (indexFrom != -1 && indexTo != -1) {
            matriceCursuri[indexFrom][indexTo] = curs;

            // Actualizeaza si variabilele vechi pentru compatibilitate
            if (from.equalsIgnoreCase("RON") && to.equalsIgnoreCase("EUR")) {
                ronToEur = curs;
                adaugaInIstoric(curs);
            } else if (from.equalsIgnoreCase("EUR") && to.equalsIgnoreCase("RON")) {
                eurToRon = curs;
            }
        }
    }


    public static double getRonToEur() { return ronToEur; }
    public static double getEurToRon() { return eurToRon; }

    public static String getCurs(String din, String in) {
        if (din.equalsIgnoreCase(in)) {
            return "1.00";
        }
        if (din.equalsIgnoreCase("RON") && in.equalsIgnoreCase("EUR")) {
            return String.format("1 RON = %.4f EUR", ronToEur);
        }
        if (din.equalsIgnoreCase("EUR") && in.equalsIgnoreCase("RON")) {
            return String.format("1 EUR = %.4f RON", eurToRon);
        }
        return "necunoscut";
    }
}