package service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Clasa {@code AuditService} gestioneaza logarea actiunilor aplicatiei intr-un fisier text.
 * <p>
 * Toate evenimentele importante sunt inregistrate impreuna cu data si ora la care au avut loc.
 * </p>
 *
 * @author
 */
public class AuditService {

    /** Calea catre fisierul in care se salveaza logurile. */
    private static final String LOG_FILE = "data/log.txt";

    /**
     * Inregistreaza o actiune in fisierul de log.
     * <p>
     * Fiecare inregistrare contine data, ora si descrierea actiunii efectuate.
     * </p>
     *
     * @param actiune descrierea actiunii care trebuie logata
     */
    public static void log(String actiune) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            String timp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            out.println(timp + " - " + actiune);

        } catch (IOException e) {
            System.out.println("Eroare la scrierea in fisierul de log: " + e.getMessage());
        }
    }
}
