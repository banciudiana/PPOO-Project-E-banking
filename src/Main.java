import service.Banca;
import service.BancaConsole;

import javax.swing.*;

public class Main {


    public static void main(String[] args) {
        Banca banca = new Banca();
        BancaConsole console = new BancaConsole(banca);


        SwingUtilities.invokeLater(() -> new gui.LoginFrame(banca).setVisible(true));
    }
}