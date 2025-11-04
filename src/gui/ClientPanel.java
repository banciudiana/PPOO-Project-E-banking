package gui;

import model.*;
import service.*;
import exceptions.*;

import javax.swing.*;
import java.awt.*;

/**
 * Panou pentru afisarea si adaugarea clientilor.
 */
public class ClientPanel extends JPanel {

    private Banca banca;
    private JTextArea textArea;
    private JTextField numeField, emailField, parolaField;

    public ClientPanel(Banca banca) {
        this.banca = banca;
        setLayout(new BorderLayout());

        // zona afisare clienti
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        // zona adaugare client
        JPanel addPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        addPanel.setBorder(BorderFactory.createTitledBorder("Adauga client nou"));

        numeField = new JTextField();
        emailField = new JTextField();
        parolaField = new JTextField();
        JButton addButton = new JButton("Adauga");

        addPanel.add(new JLabel("Nume:"));
        addPanel.add(numeField);
        addPanel.add(new JLabel("Email:"));
        addPanel.add(emailField);
        addPanel.add(new JLabel("Parola:"));
        addPanel.add(parolaField);
        addPanel.add(new JLabel(""));
        addPanel.add(addButton);

        add(addPanel, BorderLayout.SOUTH);

        // buton actiune
        addButton.addActionListener(e -> adaugaClient());

        // incarcare initiala clienti
        refreshLista();
    }

    private void adaugaClient() {
        try {
            String nume = numeField.getText().trim();
            String email = emailField.getText().trim();
            String parola = parolaField.getText().trim();

            Validare.valideazaEmail(email);
            Validare.valideazaParola(parola);

            int id = banca.getClienti().size() + 1;
            Client client = new Client(id, nume, email, parola, false);
            banca.getClienti().add(client);

            AuditService.log("Client adaugat din GUI: ID=" + id + ", nume=" + nume + ", email=" + email);
            JOptionPane.showMessageDialog(this, "Client adaugat cu succes!");
            refreshLista();

            numeField.setText("");
            emailField.setText("");
            parolaField.setText("");

        } catch (DateInvalideException ex) {
            JOptionPane.showMessageDialog(this, "Eroare: " + ex.getMessage());
            AuditService.log("Eroare la adaugare client din GUI: " + ex.getMessage());
        }
    }

    private void refreshLista() {
        StringBuilder sb = new StringBuilder();
        for (Client c : banca.getClienti()) {
            sb.append(c.getId())
                    .append(" | ")
                    .append(c.getNume())
                    .append(" | ")
                    .append(c.getEmail())
                    .append("\n");
        }
        textArea.setText(sb.toString());
    }
}
