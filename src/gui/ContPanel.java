package gui;

import model.*;
import service.*;
import javax.swing.*;
import java.awt.*;

/**
 * Panou pentru afisarea conturilor bancare.
 */
public class ContPanel extends JPanel {

    private Banca banca;
    private JTextArea textArea;

    public ContPanel(Banca banca) {
        this.banca = banca;
        setLayout(new BorderLayout());

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Actualizeaza lista");
        refreshButton.addActionListener(e -> refreshLista());
        add(refreshButton, BorderLayout.SOUTH);

        refreshLista();
    }

    private void refreshLista() {
        StringBuilder sb = new StringBuilder();
        for (ContBancar c : banca.getConturi().values()) {
            sb.append(c.getId())
                    .append(" | ")
                    .append(c.getClient().getNume())
                    .append(" | ")
                    .append(c.getSold())
                    .append(" ")
                    .append(c.getValuta())
                    .append(" | ")
                    .append(c.getClass().getSimpleName())
                    .append("\n");
        }
        textArea.setText(sb.toString());
        AuditService.log("Afisare lista conturi din GUI (" + banca.getConturi().size() + " inregistrari)");
    }
}
