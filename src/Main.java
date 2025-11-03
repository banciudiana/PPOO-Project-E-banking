import exceptions.DateInvalideException;
import model.*;
import service.Banca;
import service.Validare;

public class Main {


    public static void main(String[] args) {
        System.out.println("=== Test initializare banca ===");

        // Initializare banca - citeste datele din fisiere
        Banca banca = new Banca();

        // Afisare clienti
        System.out.println("Clienti incarcati: " + banca.getClienti().size());
        for (Client c : banca.getClienti()) {
            System.out.println(" - " + c.getId() + " | " + c.getNume() + " | " + c.getEmail());
        }

        // Afisare conturi
        System.out.println("\nConturi incarcate: " + banca.getConturi().size());
        for (ContBancar cont : banca.getConturi().values()) {
            System.out.println(" - " + cont.getId() + " | " + cont.getClient().getNume() +
                    " | " + cont.getSold() + " " + cont.getValuta() +
                    " | " + cont.getClass().getSimpleName());
        }

        // Test validare
        System.out.println("\n=== Test validari ===");
        try {
            Validare.valideazaEmail("iongmail.com");
        } catch (DateInvalideException e) {
            System.out.println("Exceptie prinsa corect: " + e.getMessage());
        }

        try {
            Validare.valideazaSuma(-5);
        } catch (DateInvalideException e) {
            System.out.println("Exceptie prinsa corect: " + e.getMessage());
        }

        // Test salvare
        System.out.println("\n=== Test salvare date ===");
        banca.salveazaDate();
        System.out.println("Date salvate cu succes.");
    }
}