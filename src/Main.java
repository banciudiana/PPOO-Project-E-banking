import service.Banca;

import javax.swing.*;

public class Main {


    public static void main(String[] args) {
        Banca banca = new Banca();

        SwingUtilities.invokeLater(() -> new gui.LoginFrame(banca).setVisible(true));
    }
}