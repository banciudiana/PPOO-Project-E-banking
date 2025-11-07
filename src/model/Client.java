package model;

/**
 * Clasa {@code Client} reprezinta un client al bancii.
 * <p>
 * Contine informatii personale precum identificatorul unic, numele complet, adresa de email si parola.
 * </p>
 *
 * @author
 */
public class Client {

    /** Identificatorul unic al clientului. */
    private int id;

    /** Numele  clientului */
    private String nume;

    /** Adresa de email  */
    private String email;

    /** Parola  */
    private String parola;

    /**
     * Creeaza un nou obiect {@code Client} cu datele specificate.
     *
     * @param id     identificatorul unic al clientului
     * @param nume   numele clientului
     * @param email  adresa de email
     * @param parola parola folosita pentru autentificare
     */
    public Client(int id, String nume, String email, String parola) {
        this.id = id;
        this.nume = nume;
        this.email = email;
        this.parola = parola;
    }

    /**
     * Returneaza identificatorul unic al clientului.
     *
     * @return id-ul clientului
     */
    public int getId() {
        return id;
    }

    /**
     * Returneaza numele clientului.
     *
     * @return numele clientului
     */
    public String getNume() {
        return nume;
    }

    /**
     * Returneaza adresa de email a clientului.
     *
     * @return adresa de email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returneaza parola clientului.
     *
     * @return parola clientului
     */
    public String getParola() {
        return parola;
    }

    /**
     * Returneaza o reprezentare textuala a obiectului {@code Client}.
     * Format: "id | nume | email"
     *
     * @return un sir de caractere care reprezinta clientul
     */
    @Override
    public String toString() {
        return id + " | " + nume + " | " + email;
    }
}
