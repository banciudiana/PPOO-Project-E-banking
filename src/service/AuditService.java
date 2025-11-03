package service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Serviciu de audit - logheaza toate actiunile in fisier text.
 */
public class AuditService {

    private static final String LOG_FILE = "data/log.txt";

    public static void log(String actiune) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            String timp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            out.println(timp + " - " + actiune);

        } catch (IOException e) {
            System.out.println("Eroare la scrierea in fisierul de log: " + e.getMessage());
        }
    }
}
