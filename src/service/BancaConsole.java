package service;

import model.*;
import exceptions.*;
import java.util.*;

/**
 * Clasa care gestioneaza interactiunea prin consola cu utilizatorul.
 */
public class BancaConsole {

    private Scanner scanner = new Scanner(System.in);
    private Banca banca;

    public BancaConsole(Banca banca) {
        this.banca = banca;
    }

    public void start() {
        boolean ruleaza = true;
        AuditService.log("Aplicatia a fost pornita.");

        while (ruleaza) {
            System.out.println("\n=== Meniu Principal ===");
            System.out.println("1. Afisare clienti");
            System.out.println("2. Afisare conturi");
            System.out.println("3. Adauga client");
            System.out.println("4. Depunere");
            System.out.println("5. Retragere");
            System.out.println("6. Transfer");
            System.out.println("7. Statistici");
            System.out.println("8. Genereaza raport text");
            System.out.println("0. Iesire");
            System.out.print("Alege optiunea: ");

            int opt = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (opt) {
                case 1 -> afiseazaClienti();
                case 2 -> afiseazaConturi();
                case 3 -> adaugaClient();
                case 4 -> depunere();
                case 5 -> retragere();
                case 6 -> transfer();
                case 7 -> statistici();
                case 8 -> RaportService.genereazaRaport(banca);
                case 0 -> {
                    banca.salveazaDate();
                    System.out.println("Date salvate. Aplicatia se inchide...");
                    AuditService.log("Aplicatia a fost inchisa si datele au fost salvate.");
                    ruleaza = false;
                }
                default -> {
                    System.out.println("Optiune invalida.");
                    AuditService.log("Utilizatorul a introdus o optiune invalida.");
                }
            }
        }
    }

    private void afiseazaClienti() {
        System.out.println("\n=== Lista clienti ===");
        for (Client c : banca.getClienti()) {
            System.out.println(c.getId() + " | " + c.getNume() + " | " + c.getEmail());
        }
        AuditService.log("Afisare lista clienti (" + banca.getClienti().size() + " inregistrari).");
    }

    private void afiseazaConturi() {
        System.out.println("\n=== Lista conturi ===");
        for (ContBancar c : banca.getConturi().values()) {
            System.out.println(c.getId() + " | " + c.getClient().getNume() + " | " + c.getSold() + " " + c.getValuta());
        }
        AuditService.log("Afisare lista conturi (" + banca.getConturi().size() + " inregistrari).");
    }

    private void adaugaClient() {
        try {
            System.out.print("Nume: ");
            String nume = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            Validare.valideazaEmail(email);
            System.out.print("Parola: ");
            String parola = scanner.nextLine();
            Validare.valideazaParola(parola);

            int id = banca.getClienti().size() + 1;
            Client client = new Client(id, nume, email, parola, false);
            banca.getClienti().add(client);

            System.out.println("Client adaugat cu succes!");
            AuditService.log("Client adaugat: ID=" + id + ", nume=" + nume + ", email=" + email);

        } catch (DateInvalideException e) {
            System.out.println("Eroare: " + e.getMessage());
            AuditService.log("Eroare la adaugare client: " + e.getMessage());
        }
    }

    private void depunere() {
        System.out.print("ID cont: ");
        int id = scanner.nextInt();
        System.out.print("Suma: ");
        double suma = scanner.nextDouble();

        ContBancar cont = banca.getConturi().get(id);
        if (cont != null) {
            cont.setSold(cont.getSold() + suma);
            System.out.println("Depunere efectuata.");
            AuditService.log("Depunere efectuata: cont ID=" + id + ", suma=" + suma);
        } else {
            System.out.println("Cont inexistent.");
            AuditService.log("Eroare depunere: cont inexistent (ID=" + id + ")");
        }
    }

    private void retragere() {
        System.out.print("ID cont: ");
        int id = scanner.nextInt();
        System.out.print("Suma: ");
        double suma = scanner.nextDouble();

        ContBancar cont = banca.getConturi().get(id);
        if (cont != null) {
            if (cont.getSold() >= suma) {
                cont.setSold(cont.getSold() - suma);
                System.out.println("Retragere efectuata.");
                AuditService.log("Retragere efectuata: cont ID=" + id + ", suma=" + suma);
            } else {
                System.out.println("Fonduri insuficiente.");
                AuditService.log("Eroare retragere: fonduri insuficiente (cont ID=" + id + ", sold=" + cont.getSold() + ", suma ceruta=" + suma + ")");
            }
        } else {
            System.out.println("Cont inexistent.");
            AuditService.log("Eroare retragere: cont inexistent (ID=" + id + ")");
        }
    }

    private void transfer() {
        System.out.print("ID cont sursa: ");
        int sursa = scanner.nextInt();
        System.out.print("ID cont destinatie: ");
        int dest = scanner.nextInt();
        System.out.print("Suma: ");
        double suma = scanner.nextDouble();

        ContBancar contSursa = banca.getConturi().get(sursa);
        ContBancar contDest = banca.getConturi().get(dest);

        if (contSursa == null || contDest == null) {
            System.out.println("Unul dintre conturi nu exista.");
            AuditService.log("Eroare transfer: cont sursa/destinatie inexistent (sursa=" + sursa + ", dest=" + dest + ")");
            return;
        }

        if (contSursa.getSold() < suma) {
            System.out.println("Fonduri insuficiente pentru transfer.");
            AuditService.log("Eroare transfer: fonduri insuficiente (cont sursa ID=" + sursa + ", sold=" + contSursa.getSold() + ", suma=" + suma + ")");
            return;
        }

        contSursa.setSold(contSursa.getSold() - suma);
        contDest.setSold(contDest.getSold() + suma);
        System.out.println("Transfer reusit.");
        AuditService.log("Transfer reusit din cont ID=" + sursa + " in cont ID=" + dest + ", suma=" + suma);
    }

    private void statistici() {
        System.out.println("\n=== Statistici ===");

        double[] solduri = new double[banca.getConturi().size()];
        int i = 0;
        for (ContBancar c : banca.getConturi().values()) {
            solduri[i++] = c.getSold();
        }

        double sumaTotala = 0;
        for (double s : solduri) sumaTotala += s;
        double medie = (solduri.length > 0) ? sumaTotala / solduri.length : 0;

        System.out.println("Suma totala in sistem: " + sumaTotala);
        System.out.println("Sold mediu: " + medie);

        // matrice 2D de solduri per client si tip de cont
        double[][] matrice = new double[banca.getClienti().size()][3];
        int idxClient = 0;
        for (Client cl : banca.getClienti()) {
            for (ContBancar c : banca.getConturi().values()) {
                if (c.getClient().getId() == cl.getId()) {
                    if (c instanceof model.ContCurent) matrice[idxClient][0] += c.getSold();
                    else if (c instanceof model.ContEconomii) matrice[idxClient][1] += c.getSold();
                    else if (c instanceof model.ContCredit) matrice[idxClient][2] += c.getSold();
                }
            }
            idxClient++;
        }

        System.out.println("\nMatrice solduri per client:");
        for (int r = 0; r < matrice.length; r++) {
            System.out.printf("Client %s -> Curent: %.2f | Economii: %.2f | Credit: %.2f%n",
                    banca.getClienti().get(r).getNume(),
                    matrice[r][0], matrice[r][1], matrice[r][2]);
        }

        AuditService.log("Statistici generate: " + banca.getClienti().size() + " clienti, " + banca.getConturi().size() + " conturi.");
    }
}