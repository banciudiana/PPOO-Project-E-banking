package service;

import exceptions.DateInvalideException;

/**
 * Clasa Validare contine metode pentru validarea datelor utilizatorului,
 * cum ar fi email si parola.
 */

public class Validare {

    /**
     * Verifica daca un email este valid.
     * Emailul trebuie sa contina '@' si '.'.
     * @param email emailul de verificat
     * @throws DateInvalideException daca emailul este null sau formatul este invalid
     */
    public static void valideazaEmail(String email) throws DateInvalideException {
        if (email == null || !email.contains("@") || !email.contains("."))
            throw new DateInvalideException("Email invalid: " + email);
    }

    /**
     * Verifica daca o parola este valida.
     * Parola trebuie sa aiba cel putin 4 caractere.
     * @param parola parola de verificat
     * @throws DateInvalideException daca parola este null sau prea scurta
     */

    public static void valideazaParola(String parola) throws DateInvalideException {
        if (parola == null || parola.length() < 4)
            throw new DateInvalideException("Parola prea scurtă!");
    }


}
