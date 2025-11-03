package service;

import model.*;
import java.io.*;
import java.util.*;

/**
 * Serviciu pentru generarea de rapoarte text din aplicatie.
 */
public class RaportService {

    private static final String RAPORT_FILE = "data/raport.txt";

    public static void genereazaRaport(Banca banca) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(RAPORT_FILE))) {
            pw.println("=== RAPORT BANCA ===");
            pw.println("Clienti in sistem: " + banca.getClienti().size());
            pw.println("Conturi in sistem: " + banca.getConturi().size());
            pw.println();

            double sumaTotala = 0;
            for (ContBancar c : banca.getConturi().values()) {
                sumaTotala += c.getSold();
            }
            pw.println("Suma totala in toate conturile: " + sumaTotala);

            pw.println("\n=== DETALII PE CLIENT ===");
            for (Client cl : banca.getClienti()) {
                pw.println("Client: " + cl.getNume() + " (" + cl.getEmail() + ")");
                for (ContBancar c : banca.getConturi().values()) {
                    if (c.getClient().getId() == cl.getId()) {
                        pw.println("   - " + c.getClass().getSimpleName() +
                                ": " + c.getSold() + " " + c.getValuta());
                    }
                }
            }

            pw.println("\nRaport generat automat.");
            System.out.println("Raport generat in " + RAPORT_FILE);
            AuditService.log("Raport generat.");

        } catch (IOException e) {
            System.out.println("Eroare la generarea raportului: " + e.getMessage());
        }
    }
}
