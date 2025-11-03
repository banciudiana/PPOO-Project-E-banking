import exceptions.DateInvalideException;
import model.*;
import service.Banca;
import service.BancaConsole;
import service.Validare;

public class Main {


    public static void main(String[] args) {
        Banca banca = new Banca();
        BancaConsole console = new BancaConsole(banca);
        console.start();
    }
}