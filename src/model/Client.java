package model;

/**
 * Reprezintă un client al băncii.
 * Conține informații personale, parola, scorul de fidelitate și rolul (client/admin).
 */

public class Client {

    private int id;
    private String nume;
    private String email;
    private String parola;
    private int scor;
    private boolean esteAdmin;


    /**
     * Creează un nou client.
     *
     * @param id         identificatorul unic al clientului
     * @param nume       numele complet
     * @param email      adresa de email
     * @param parola     parola de autentificare
     * @param esteAdmin  true dacă este administrator
     */

    public Client(int id, String nume, String email, String parola, boolean esteAdmin) {
        this.id = id;
        this.nume = nume;
        this.email = email;
        this.parola = parola;
        this.scor = 0;
        this.esteAdmin = esteAdmin;
    }

    public int getId() {
        return id;
    }

    public String getNume() {
        return nume;
    }

    public String getEmail() {
        return email;
    }

    public String getParola() {
        return parola;
    }

    public int getScor() {
        return scor;
    }

    public boolean isEsteAdmin() {
        return esteAdmin;
    }

    /**
     * Actualizează scorul de fidelitate.
     *
     * @param modificare valoarea cu care se modifică scorul (poate fi negativă)
     */

    public void actualizeazaScor(int modificare) { scor += modificare; }

    @Override
    public String toString() {
        return id + " | " + nume + " | " + email + " | scor: " + scor + (esteAdmin ? " [ADMIN]" : "");
    }
}
