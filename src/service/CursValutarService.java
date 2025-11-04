package service;

import java.io.*;
import java.util.*;

/**
 * Incarca cursul valutar din fisier si ofera conversii simple.
 */
public class CursValutarService {
    private static double ronToEur = 0.20;
    private static double eurToRon = 5.0;
    private static final String PATH = "data/curs.txt";

    public static void incarcaCursuri() {
        File f = new File(PATH);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linie;
            while ((linie = br.readLine()) != null) {
                String[] parts = linie.split("=");
                if (parts.length != 2) continue;
                String key = parts[0].trim();
                double val = Double.parseDouble(parts[1].trim());
                if ("RON_TO_EUR".equalsIgnoreCase(key)) ronToEur = val;
                else if ("EUR_TO_RON".equalsIgnoreCase(key)) eurToRon = val;
            }
        } catch (Exception e) {
            System.out.println("Eroare la incarcarea cursului: " + e.getMessage());
        }
    }

    public static double convert(double suma, String from, String to) {
        if (from.equalsIgnoreCase(to)) return suma;
        if ("RON".equalsIgnoreCase(from) && "EUR".equalsIgnoreCase(to)) return suma * ronToEur;
        if ("EUR".equalsIgnoreCase(from) && "RON".equalsIgnoreCase(to)) return suma * eurToRon;
        // altfel: nu suportat
        return suma;
    }

    public static double getRonToEur() { return ronToEur; }
    public static double getEurToRon() { return eurToRon; }
}
