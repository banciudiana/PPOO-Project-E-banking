package service;

import model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class FileManager {

    private static final String CLIENTI_FILE = "data/clienti.txt";
    private static final String CONTURI_FILE = "data/conturi.txt";
    private static final String TRANZACTII_FILE = "data/tranzactii.txt";

    /** Încarcă toți clienții din fișier. */
    public static ArrayList<Client> incarcaClienti() {
        ArrayList<Client> clienti = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(CLIENTI_FILE))) {
            String linie;
            while ((linie = br.readLine()) != null) {
                String[] c = linie.split(";");
                if (c.length == 5) {
                    Client cl = new Client(
                            Integer.parseInt(c[0]),
                            c[1],
                            c[2],
                            c[3],
                            Boolean.parseBoolean(c[4])
                    );
                    clienti.add(cl);
                }
            }
        } catch (IOException e) {
            System.out.println(" Eroare la citirea fișierului clienti.txt: " + e.getMessage());
        }
        return clienti;
    }

    /** Salvează toți clienții în fișier. */
    public static void salveazaClienti(ArrayList<Client> clienti) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CLIENTI_FILE))) {
            for (Client c : clienti) {
                pw.println(c.getId() + ";" + c.getNume() + ";" + c.getEmail() + ";" + c.getParola() + ";" + c.esteAdmin());
            }
        } catch (IOException e) {
            System.out.println(" Eroare la scrierea fișierului clienti.txt: " + e.getMessage());
        }
    }

    /** Încarcă conturile din fișier. */
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

                    Client client = clienti.stream().filter(cl -> cl.getId() == idClient).findFirst().orElse(null);
                    ContBancar cont = null;

                    if ("CURENT".equalsIgnoreCase(tip))
                        cont = new ContCurent(id, sold, client, valuta);
                    else if ("ECONOMII".equalsIgnoreCase(tip))
                        cont = new ContEconomii(id, sold, client, valuta);
                    else if ("CREDIT".equalsIgnoreCase(tip))
                        cont = new ContCredit(id, sold, client, valuta);

                    if (cont != null) conturi.put(id, cont);
                }
            }
        } catch (IOException e) {
            System.out.println("️ Eroare la citirea fișierului conturi.txt: " + e.getMessage());
        }
        return conturi;
    }

    /** Salvează conturile în fișier. */
    public static void salveazaConturi(HashMap<Integer, ContBancar> conturi) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CONTURI_FILE))) {
            for (ContBancar c : conturi.values()) {
                String tip = c instanceof ContCurent ? "CURENT"
                        : c instanceof ContEconomii ? "ECONOMII" : "CREDIT";
                pw.println(c.getId() + ";" + c.getSold() + ";" + c.getClient().getId() + ";" + c.getValuta() + ";" + tip);
            }
        } catch (IOException e) {
            System.out.println(" Eroare la scrierea fișierului conturi.txt: " + e.getMessage());
        }
    }
}