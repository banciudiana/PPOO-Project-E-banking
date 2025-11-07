package service;

import model.*;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Clasa FileManager se ocupa cu citirea si scrierea datelor in fisiere.
 * Include metode pentru clienti, conturi bancare si tranzactii.
 */

public class FileManager {

    private static final String CLIENTI_FILE = "data/clienti.txt";
    private static final String CONTURI_FILE = "data/conturi.txt";
    private static final String TRANZACTII_FILE = "data/tranzactii.txt";



    /**
     * Incarca toti clientii din fisierul clienti.txt.
     * @return lista de clienti
     */
    public static ArrayList<Client> incarcaClienti() {
        ArrayList<Client> clienti = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(CLIENTI_FILE))) {
            String linie;
            while ((linie = br.readLine()) != null) {
                String[] c = linie.split(";");
                if (c.length == 4) {
                    Client cl = new Client(
                            Integer.parseInt(c[0]),
                            c[1],
                            c[2],
                            c[3].trim()

                    );
                    clienti.add(cl);
                }
            }
        } catch (IOException e) {
            System.out.println(" Eroare la citirea fișierului clienti.txt: " + e.getMessage());
        }
        return clienti;
    }


    /**
     * Salveaza toti clientii in fisierul clienti.txt.
     * @param clienti lista de clienti de salvat
     */
    public static void salveazaClienti(ArrayList<Client> clienti) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CLIENTI_FILE))) {
            for (Client c : clienti) {
                pw.println(c.getId() + ";" + c.getNume() + ";" + c.getEmail() + ";" + c.getParola() );
            }
        } catch (IOException e) {
            System.out.println(" Eroare la scrierea fișierului clienti.txt: " + e.getMessage());
        }
    }

    /**
     * Incarca toate conturile din fisierul conturi.txt.
     * @param clienti lista de clienti existenti
     * @return harta cu id-ul contului ca si cheia si obiectul ContBancar ca valoare
     */
    public static HashMap<Integer, ContBancar> incarcaConturi(ArrayList<Client> clienti) {
        HashMap<Integer, ContBancar> conturi = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(CONTURI_FILE))) {
            String linie;
            while ((linie = br.readLine()) != null) {
                String[] c = linie.split(";");
                if (c.length >= 5) {
                    int id = Integer.parseInt(c[0]);
                    double sold = Double.parseDouble(c[1]);
                    int idClient = Integer.parseInt(c[2]);
                    String valuta = c[3];
                    String tip = c[4];

                    LocalDateTime creationDate = LocalDateTime.now();
                    if (c.length > 5 && !c[5].isEmpty()) {
                        creationDate = LocalDateTime.parse(c[5]);
                    }

                    ContBancar cont = null;
                    Client client = clienti.stream().filter(cl -> cl.getId() == idClient).findFirst().orElse(null);

                    if ("CURENT".equalsIgnoreCase(tip)) {
                        cont = new ContCurent(id, sold, client, valuta, creationDate);
                    } else if ("ECONOMII".equalsIgnoreCase(tip)) {
                        // citim tipEconomii si dobandaAcumulata
                        ContEconomii.TipEconomii tipEconomii = ContEconomii.TipEconomii.ECONOMII;
                        double dobandaAcumulata = 0.0;
                        if (c.length > 6 && c[6] != null && !c[6].isEmpty()) {
                            tipEconomii = ContEconomii.TipEconomii.valueOf(c[6]);
                        }
                        if (c.length > 7 && c[7] != null && !c[7].isEmpty()) {
                            dobandaAcumulata = Double.parseDouble(c[7]);
                        }
                        cont = new ContEconomii(id, sold, client, valuta, creationDate, tipEconomii, dobandaAcumulata);
                    } else if ("CREDIT".equalsIgnoreCase(tip)) {
                        cont = new ContCredit(id, sold, client, valuta, creationDate);
                    }

                    if (cont != null) conturi.put(id, cont);
                }

            }
        } catch (IOException e) {
            System.out.println("️ Eroare la citirea fișierului conturi.txt: " + e.getMessage());
        }
        return conturi;
    }

    /**
     * Salveaza toate conturile in fisierul conturi.txt.
     * @param conturi harta cu conturi de salvat
     */
    public static void salveazaConturi(HashMap<Integer, ContBancar> conturi) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CONTURI_FILE))) {
            for (ContBancar c : conturi.values()) {
                String tip = c instanceof ContCurent ? "CURENT"
                        : c instanceof ContEconomii ? "ECONOMII" : "CREDIT";

                String creation = c.getCreationDate().toString();

                if (c instanceof ContEconomii) {
                    ContEconomii ce = (ContEconomii) c;
                    pw.println(c.getId() + ";" + c.getSold() + ";" + c.getClient().getId() + ";" +
                            c.getValuta() + ";" + tip + ";" + creation + ";" +
                            ce.getTip().name() + ";" + ce.getDobandaAcumulata());
                } else {

                    pw.println(c.getId() + ";" + c.getSold() + ";" + c.getClient().getId() + ";" +
                            c.getValuta() + ";" + tip + ";" + creation + ";;0.0");
                }
            }

        } catch (IOException e) {
            System.out.println(" Eroare la scrierea fișierului conturi.txt: " + e.getMessage());
        }
    }


    /**
     * Incarca toate tranzactiile din fisierul tranzactii.txt.
     * @param conturi harta cu conturile existente
     * @return lista de tranzactii
     */
    public static ArrayList<Tranzactie> incarcaTranzactii(HashMap<Integer, ContBancar> conturi) {
        ArrayList<Tranzactie> tranzactii = new ArrayList<>();
        File file = new File(TRANZACTII_FILE);
        if (!file.exists()) return tranzactii;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(";");
                if (parts.length < 5) continue;

                int id = Integer.parseInt(parts[0]);
                int idSursa = Integer.parseInt(parts[1]);
                int idDest = Integer.parseInt(parts[2]);
                double suma = Double.parseDouble(parts[3]);
                LocalDateTime data = LocalDateTime.parse(parts[4]);

                ContBancar sursa = conturi.get(idSursa);
                ContBancar destinatie = conturi.get(idDest);

                if (sursa != null && destinatie != null) {
                    tranzactii.add(new Tranzactie(id, sursa, destinatie, suma));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tranzactii;
    }

    /**
     * Salveaza toate tranzactiile in fisierul tranzactii.txt.
     * @param tranzactii lista de tranzactii de salvat
     */
    public static void salveazaTranzactii(List<Tranzactie> tranzactii) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(TRANZACTII_FILE))) {
            for (Tranzactie t : tranzactii) {
                pw.println(t.getId() + ";" +
                        t.getSursa().getId() + ";" +
                        t.getDestinatie().getId() + ";" +
                        t.getSuma() + ";" +
                        t.getData());
            }
        } catch (Exception e) {
            System.out.println("Eroare la salvarea tranzactiilor: " + e.getMessage());
        }
    }

    /**
     * Citeste ultima data la care s-a aplicat dobanda pentru conturile de economii.
     * @return data ultimei dobanzi sau LocalDate.MIN daca fisierul nu exista
     */

    public static LocalDate citesteUltimaDataDobanda() {
        try (BufferedReader br = new BufferedReader(new FileReader("data/ultima_dobanda.txt"))) {
            return LocalDate.parse(br.readLine());
        } catch (Exception e) {
            return LocalDate.MIN;
        }
    }


    /**
     * Salveaza ultima data la care s-a aplicat dobanda pentru conturile de economii.
     * @param data data de salvat
     */
    public static void salveazaUltimaDataDobanda(LocalDate data) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("data/ultima_dobanda.txt"))) {
            pw.println(data.toString());
        } catch (IOException e) {
            System.out.println("Eroare la scrierea ultimei date de dobanda: " + e.getMessage());
        }
    }
}