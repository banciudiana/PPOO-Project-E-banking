package service;

import exceptions.DateInvalideException;
import model.*;

import java.time.LocalDateTime;
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
        CursValutarService.incarcaCursuri();
        clienti = FileManager.incarcaClienti();
        conturi = FileManager.incarcaConturi(clienti);
        tranzactii = FileManager.incarcaTranzactii(conturi);

        // calculeaza next ids pe baza datelor incarcate
        for (Client c : clienti) nextClientId = Math.max(nextClientId, c.getId() + 1);
        for (Integer id : conturi.keySet()) nextContId = Math.max(nextContId, id + 1);

        System.out.println("Incarcat tranzactii: " + tranzactii.size());
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
     */
    public Client creeazaClientSiCont(String nume, String email, String parola,
                                      String tipCont, String valuta, double soldInitial) throws DateInvalideException {
        // verificam daca exista deja client cu acelasi email
        for (Client c : clienti) {
            if (c.getEmail().equalsIgnoreCase(email)) {
                throw new DateInvalideException("Exista deja un cont asociat acestui email.");
            }
        }

        // creare client
        int idClient = nextClientId++;
        Client client = new Client(idClient, nume, email, parola, false);
        clienti.add(client);

        // creare cont
        int idCont = nextContId++;
        ContBancar cont;
        if ("ECONOMII".equalsIgnoreCase(tipCont)) {
            // folosim tip implicit ECONOMII pentru înregistrare nouă
            cont = new ContEconomii(idCont, soldInitial, client, valuta,
                    LocalDateTime.now(), ContEconomii.TipEconomii.ECONOMII, 0.0);
        } else if ("CREDIT".equalsIgnoreCase(tipCont)) {
            cont = new ContCredit(idCont, soldInitial, client, valuta);
        } else {
            cont = new ContCurent(idCont, soldInitial, client, valuta);
        }



        conturi.put(idCont, cont);

        // salvam datele imediat
        salveazaDate();

        // audit
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
            // folosim tip implicit ECONOMII cu data curentă
            cont = new ContEconomii(id, soldInitial, client, valuta,
                    LocalDateTime.now(), ContEconomii.TipEconomii.ECONOMII, 0.0);
        } else if ("CREDIT".equalsIgnoreCase(tip)) {
            cont = new ContCredit(id, soldInitial, client, valuta);
        } else {
            cont = new ContCurent(id, soldInitial, client, valuta);
        }
        conturi.put(id, cont);
        return cont;
    }

    /**
     * Creaza un cont de economii pentru un client existent, cu tip specific.
     */
    public ContEconomii creaContEconomiiPentruClient(Client client, double soldInitial,
                                                     String valuta, String tipEconomii) {
        int id = nextContId++;
        ContEconomii.TipEconomii tip = tipEconomii.equalsIgnoreCase("BONUS")
                ? ContEconomii.TipEconomii.BONUS
                : ContEconomii.TipEconomii.ECONOMII;

        ContEconomii cont = new ContEconomii(id, soldInitial, client, valuta,
                LocalDateTime.now(), tip, 0.0);
        conturi.put(id, cont);
        return cont;
    }

    /**
     * Aplica dobanda lunara pentru toate conturile de economii.
     */
    public void aplicaDobandaLunaraPentruToateConturile() {
        for (ContBancar c : conturi.values()) {
            if (c instanceof ContEconomii) {
                ContEconomii ce = (ContEconomii) c;
                ce.aplicaDobandaLunara();
            }
        }
        salveazaDate();
    }

    public ArrayList<Client> getClienti() { return clienti; }
    public HashMap<Integer, ContBancar> getConturi() { return conturi; }

    public void adaugaTranzactie(Tranzactie t) {
        tranzactii.add(t);
        FileManager.salveazaTranzactii(tranzactii);
    }

    public ArrayList<Tranzactie> getTranzactii() {
        return tranzactii;
    }

    public void salveazaDate() {
        FileManager.salveazaClienti(clienti);
        FileManager.salveazaConturi(conturi);
        FileManager.salveazaTranzactii(tranzactii);
    }

    /**
     * Gaseste toate conturile unui client.
     */
    public List<ContBancar> getConturiClient(Client client) {
        List<ContBancar> lista = new ArrayList<>();
        for (ContBancar c : conturi.values()) {
            if (c.getClient().getId() == client.getId()) {
                lista.add(c);
            }
        }
        return lista;
    }

    /**
     * Executa retragere cu reguli speciale pentru conturile BONUS.
     */
    public void retrage(Client client, int contId, double suma) throws Exception {
        ContBancar cont = conturi.get(contId);
        if (cont == null || cont.getClient().getId() != client.getId()) {
            throw new Exception("Cont inexistent sau nu apartine clientului.");
        }

        if (cont instanceof ContEconomii) {
            ContEconomii ce = (ContEconomii) cont;
            if (ce.getTip() == ContEconomii.TipEconomii.BONUS
                    && ce.calculeazaLuniDeLaCreare() < 4) {
                // arunca exceptie pentru UI care va cere confirmare
                throw new exceptions.RetragereInainteDePerioadaException(
                        "Retragere inainte de 4 luni: pierzi dobanda acumulata.");
            }
        }

        cont.retrage(suma);
        salveazaDate();
    }
}
