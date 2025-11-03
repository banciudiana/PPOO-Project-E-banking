package service;

import model.*;
import java.util.*;

/**
 * Gestionează logica principală a aplicației.
 * Încarcă și salvează datele automat la pornire/închidere.
 */
public class Banca {
    private ArrayList<Client> clienti;
    private HashMap<Integer, ContBancar> conturi;
    private ArrayList<Tranzactie> tranzactii;

    public Banca() {
        clienti = FileManager.incarcaClienti();
        conturi = FileManager.incarcaConturi(clienti);
        tranzactii = new ArrayList<>();
    }

    public void salveazaDate() {
        FileManager.salveazaClienti(clienti);
        FileManager.salveazaConturi(conturi);
    }

    public ArrayList<Client> getClienti() { return clienti; }
    public HashMap<Integer, ContBancar> getConturi() { return conturi; }
}