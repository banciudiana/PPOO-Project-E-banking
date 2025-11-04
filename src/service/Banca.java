
package service;

import exceptions.DateInvalideException;
import model.*;
import java.util.*;

/**
 * Gestioneaza logica principala (extensie cu autentificare si creare cont).
 */
public class Banca {
    private ArrayList<Client> clienti;
    private HashMap<Integer, ContBancar> conturi;
    private ArrayList<Tranzactie> tranzactii;
    private int nextClientId = 1;
    private int nextContId = 1000;

    public Banca() {
        clienti = FileManager.incarcaClienti();
        conturi = FileManager.incarcaConturi(clienti);
        //tranzactii = FileManager.incarcaTranzactii(); // optional, daca exista
        // calculeaza next ids pe baza datelor incarcate
        for (Client c : clienti) nextClientId = Math.max(nextClientId, c.getId() + 1);
        for (Integer id : conturi.keySet()) nextContId = Math.max(nextContId, id + 1);
    }

    /**
     * Autentifica un client dupa email si parola.
     * @return client daca autentificarea reuseste, null altfel
     */
    public Client autentifica(String email, String parola) {
        for (Client c : clienti) {
            if (c.getEmail().equalsIgnoreCase(email) && c.getParola().equals(parola)) {
                AuditService.log("LOGIN reusit pentru: " + email);
                return c;
            }
        }
        AuditService.log("LOGIN esuat pentru: " + email);
        return null;
    }

    /**
     * Creaza un client nou si optional un cont initial de tipul specificat.
     * @param nume
     * @param email
     * @param parola

     * @param valuta valuta initiala (ex: "RON" sau "EUR")
     * @param soldInitial sold initial pentru cont (0 daca nu se doreste)
     * @return client creat
     */


    public Client creeazaClientSiCont(String nume, String email, String parola,
                                      String tipCont, String valuta, double soldInitial) throws DateInvalideException {
        // 1. verificam daca exista deja client cu acelasi email
        for (Client c : clienti) {
            if (c.getEmail().equalsIgnoreCase(email)) {
                throw new DateInvalideException("Exista deja un cont asociat acestui email.");
            }
        }

        // 2. creare client
        int idClient = clienti.size() + 1;
        Client client = new Client(idClient, nume, email, parola, false);
        clienti.add(client);

        // 3. creare cont
        int idCont = conturi.size() + 1;
        ContBancar cont;

        if (tipCont.equalsIgnoreCase("ECONOMII")) {
            cont = new ContEconomii(idCont,  soldInitial, client, valuta); // 3% dobanda
        } else if (tipCont.equalsIgnoreCase("CREDIT")) {
            cont = new ContCredit(idCont,  soldInitial, client, valuta); // 7% rata dobanda
        } else {
            cont = new ContCurent(idCont,  soldInitial, client, valuta);
        }

        conturi.put(idCont, cont);

        // 4. salvam datele imediat
        salveazaDate();

        // 5. audit
        AuditService.log("Creare client nou (GUI): " + email + " | tip=" + tipCont + " | valuta=" + valuta);

        return client;
    }




    /**
     * Creaza un cont pentru un client existent (nu salveaza automat).
     */
    public ContBancar creaContPentruClient(Client client, String tip, double soldInitial, String valuta) {
        int id = nextContId++;
        ContBancar cont;
        if ("CURENT".equalsIgnoreCase(tip)) {
            cont = new ContCurent(id, soldInitial, client, valuta);
        } else if ("ECONOMII".equalsIgnoreCase(tip)) {
            cont = new ContEconomii(id, soldInitial, client, valuta);
        } else if ("CREDIT".equalsIgnoreCase(tip)) {
            cont = new ContCredit(id, soldInitial, client, valuta);
        } else {
            // default -> cont curent
            cont = new ContCurent(id, soldInitial, client, valuta);
        }
        conturi.put(id, cont);
        return cont;
    }

    public ArrayList<Client> getClienti() { return clienti; }
    public HashMap<Integer, ContBancar> getConturi() { return conturi; }

    public void salveazaDate() {
        FileManager.salveazaClienti(clienti);
        FileManager.salveazaConturi(conturi);
        // eventual: FileManager.salveazaTranzactii(tranzactii)
    }
}
