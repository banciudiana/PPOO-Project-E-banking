package service;

import exceptions.DateInvalideException;
import model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Clasa {@code Banca} gestioneaza logica principala a aplicatiei.
 * <p>
 * Include operatii de autentificare, creare conturi, aplicare dobanzi si gestionare tranzactii.
 * </p>
 *
 * @author
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

        for (Client c : clienti) nextClientId = Math.max(nextClientId, c.getId() + 1);
        for (Integer id : conturi.keySet()) nextContId = Math.max(nextContId, id + 1);

        LocalDate ultimaData = FileManager.citesteUltimaDataDobanda();
        if (ultimaData.isBefore(LocalDate.now().withDayOfMonth(1))) {
            aplicaDobandaLunaraPentruToateConturile();
            FileManager.salveazaUltimaDataDobanda(LocalDate.now());
            AuditService.log("Dobanda lunara aplicata automat la pornirea aplicatiei.");
        }

        AuditService.log("Initializare sistem Banca completata. Clienti: " + clienti.size() +
                ", Conturi: " + conturi.size() + ", Tranzactii: " + tranzactii.size());
    }

    /**
     * Autentifica un client dupa email si parola.
     *
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
        for (Client c : clienti) {
            if (c.getEmail().equalsIgnoreCase(email)) {
                AuditService.log("Eroare creare client: email deja existent (" + email + ")");
                throw new DateInvalideException("Exista deja un cont asociat acestui email.");
            }
        }

        int idClient = nextClientId++;
        Client client = new Client(idClient, nume, email, parola);
        clienti.add(client);
        AuditService.log("Client nou creat: " + nume + " (" + email + ")");

        int idCont = nextContId++;
        ContBancar cont;
        if ("ECONOMII".equalsIgnoreCase(tipCont)) {
            cont = new ContEconomii(idCont, soldInitial, client, valuta,
                    LocalDateTime.now(), ContEconomii.TipEconomii.ECONOMII, 0.0);
        } else if ("CREDIT".equalsIgnoreCase(tipCont)) {
            cont = new ContCredit(idCont, soldInitial, client, valuta);
        } else {
            cont = new ContCurent(idCont, soldInitial, client, valuta);
        }

        conturi.put(idCont, cont);
        AuditService.log("Cont nou creat pentru client " + email + " | tip=" + tipCont +
                " | valuta=" + valuta + " | sold initial=" + soldInitial);

        salveazaDate();
        return client;
    }

    /**
     * Creaza un cont pentru un client existent.
     */
    public ContBancar creaContPentruClient(Client client, String tip, double soldInitial, String valuta) {
        int id = nextContId++;
        ContBancar cont;
        if ("CURENT".equalsIgnoreCase(tip)) {
            cont = new ContCurent(id, soldInitial, client, valuta);
        } else if ("ECONOMII".equalsIgnoreCase(tip)) {
            cont = new ContEconomii(id, soldInitial, client, valuta,
                    LocalDateTime.now(), ContEconomii.TipEconomii.ECONOMII, 0.0);
        } else if ("CREDIT".equalsIgnoreCase(tip)) {
            cont = new ContCredit(id, soldInitial, client, valuta);
        } else {
            cont = new ContCurent(id, soldInitial, client, valuta);
        }
        conturi.put(id, cont);
        AuditService.log("Cont suplimentar creat pentru client: " + client.getEmail() +
                " | tip=" + tip + " | valuta=" + valuta);
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
        AuditService.log("Cont economii creat pentru client: " + client.getEmail() +
                " | tip=" + tipEconomii + " | valuta=" + valuta);
        return cont;
    }

    /**
     * Aplica dobanda lunara pentru toate conturile de economii.
     */
    public void aplicaDobandaLunaraPentruToateConturile() {
        int nr = 0;
        for (ContBancar c : conturi.values()) {
            if (c instanceof ContEconomii) {
                ((ContEconomii) c).aplicaDobandaLunara();
                nr++;
            }
        }
        salveazaDate();
        AuditService.log("Dobanda lunara aplicata pentru " + nr + " conturi de economii.");
    }

    public ArrayList<Client> getClienti() { return clienti; }
    public HashMap<Integer, ContBancar> getConturi() { return conturi; }

    public void adaugaTranzactie(Tranzactie t) {
        tranzactii.add(t);
        FileManager.salveazaTranzactii(tranzactii);
        AuditService.log("Tranzactie adaugata: " + " | suma=" + t.getSuma() +
                " | " + t.getSursa().getClient().getNume() + " -> " + t.getDestinatie().getClient().getNume());
    }

    public ArrayList<Tranzactie> getTranzactii() {
        return tranzactii;
    }

    public void salveazaDate() {
        FileManager.salveazaClienti(clienti);
        FileManager.salveazaConturi(conturi);
        FileManager.salveazaTranzactii(tranzactii);
        AuditService.log("Datele au fost salvate pe disc.");
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
        AuditService.log("Conturi obtinute pentru client: " + client.getEmail() +
                " | numar conturi=" + lista.size());
        return lista;
    }

    /**
     * Executa retragere cu reguli speciale pentru conturile BONUS.
     */
    public void retrage(Client client, int contId, double suma) throws Exception {
        ContBancar cont = conturi.get(contId);
        if (cont == null || cont.getClient().getId() != client.getId()) {
            AuditService.log("Eroare retragere: cont inexistent sau apartine altui client (" +
                    client.getEmail() + ")");
            throw new Exception("Cont inexistent sau nu apartine clientului.");
        }

        if (cont instanceof ContEconomii) {
            ContEconomii ce = (ContEconomii) cont;
            if (ce.getTip() == ContEconomii.TipEconomii.BONUS
                    && ce.calculeazaLuniDeLaCreare() < 4) {

                throw new exceptions.RetragereInainteDePerioadaException(
                        "Retragere inainte de 4 luni: pierzi dobanda acumulata.");
            }
        }

        cont.retrage(suma);
        AuditService.log("Retragere efectuata: " + suma + " " + cont.getValuta() +
                " | cont=" + contId + " | client=" + client.getEmail());
        salveazaDate();
    }

    /**
     * Inchide un cont daca regulile permit acest lucru.
     */
    public void inchideCont(int contId, int clientId) throws Exception {
        ContBancar cont = conturi.get(contId);

        if (cont == null) {
            AuditService.log("Eroare: incercare de inchidere cont inexistent (id=" + contId + ")");
            throw new Exception("Contul nu exista!");
        }

        if (cont.getClient().getId() != clientId) {
            AuditService.log("Eroare: incercare de inchidere cont care nu apartine clientului (id=" + contId + ")");
            throw new Exception("Nu poti inchide un cont care nu iti apartine!");
        }

        if (Math.abs(cont.getSold()) > 0.01) {
            AuditService.log("Eroare inchidere cont: sold diferit de zero pentru cont " + contId);
            throw new Exception(String.format(
                    "Contul trebuie sa aiba sold 0 pentru a fi inchis!\nSold curent: %.2f %s",
                    cont.getSold(), cont.getValuta()
            ));
        }

        conturi.remove(contId);
        tranzactii.removeIf(t ->
                t.getSursa().getId() == contId || t.getDestinatie().getId() == contId
        );

        salveazaDate();
        AuditService.log("Cont inchis cu succes: " + contId + " (Client: " +
                cont.getClient().getNume() + ")");
    }
}
