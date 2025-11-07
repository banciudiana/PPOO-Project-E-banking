package service;

import exceptions.DateInvalideException;

public class Validare {
    public static void valideazaEmail(String email) throws DateInvalideException {
        if (email == null || !email.contains("@") || !email.contains("."))
            throw new DateInvalideException("Email invalid: " + email);
    }

    public static void valideazaParola(String parola) throws DateInvalideException {
        if (parola == null || parola.length() < 4)
            throw new DateInvalideException("Parola prea scurtă!");
    }


}
