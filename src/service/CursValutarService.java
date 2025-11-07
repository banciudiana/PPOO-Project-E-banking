package service;

import java.io.*;
import java.util.*;

/**
 * Incarca cursul valutar din fisier si ofera conversii simple.
 * Include vector pentru istoric si matrice pentru conversii multiple valute.
 */
public class CursValutarService {

    private static final String PATH = "data/curs.txt";

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

    /**
     * Initializeaza matricea cu ajutorul fisierului
     */

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
                    matriceCursuri[0][1] = val;
                } else if ("EUR_TO_RON".equalsIgnoreCase(key)) {
                    matriceCursuri[1][0] = val;
                }
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
     * Converteste folosind matricea de cursuri
     */
    public static double convertCuMatrice(double suma, String from, String to) {
        int indexFrom = getIndexValuta(from);
        int indexTo = getIndexValuta(to);

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

}