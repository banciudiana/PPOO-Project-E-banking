package gui;

import service.*;
import model.*;
import exceptions.*;
import javax.swing.*;
import java.awt.*;

/**
 * Fereastra de creare cont .
 */
public class RegisterFrame extends JFrame {

    private final Banca banca;
    private JTextField numeField, emailField;
    private JPasswordField parolaField;
    private JTextField soldField;
    private JComboBox<String> tipContBox, valutaBox;

    public RegisterFrame(Banca banca) {
        this.banca = banca;
        setTitle("Creare cont - eBanking");
        setSize(480, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Formular creare cont nou");

        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        Font font = new Font("SansSerif", Font.PLAIN, 14);

        // Linie 1: nume + email
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nume:"), gbc);
        gbc.gridx = 1;
        numeField = new JTextField(); numeField.setFont(font);
        formPanel.add(numeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(); emailField.setFont(font);
        formPanel.add(emailField, gbc);

        // Linie 2: parola
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Parola:"), gbc);
        gbc.gridx = 1;
        parolaField = new JPasswordField(); parolaField.setFont(font);
        formPanel.add(parolaField, gbc);

        // Linie 3: tip cont + valuta
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Tip cont initial:"), gbc);
        gbc.gridx = 1;
        tipContBox = new JComboBox<>(new String[]{"CURENT", "ECONOMII", "CREDIT"});
        tipContBox.setFont(font);
        formPanel.add(tipContBox, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Valuta:"), gbc);
        gbc.gridx = 1;
        valutaBox = new JComboBox<>(new String[]{"RON", "EUR", "USD", "GBP"});
        valutaBox.setFont(font);
        formPanel.add(valutaBox, gbc);

        // Linie 4: sold initial
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Sold initial:"), gbc);
        gbc.gridx = 1;
        soldField = new JTextField("0");
        soldField.setFont(font);
        formPanel.add(soldField, gbc);

        // Buton creare cont
        JButton createBtn = new JButton("Creeaza cont");
        createBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(createBtn, gbc);

        add(formPanel, BorderLayout.CENTER);

        createBtn.addActionListener(e -> {
            try {
                String nume = numeField.getText().trim();
                String email = emailField.getText().trim();
                String parola = new String(parolaField.getPassword()).trim();
                Validare.valideazaEmail(email);
                Validare.valideazaParola(parola);
                String tip = (String) tipContBox.getSelectedItem();
                String valuta = (String) valutaBox.getSelectedItem();
                double sold = Double.parseDouble(soldField.getText().trim());

                Client c = banca.creeazaClientSiCont(nume, email, parola, tip, valuta, sold);
                AuditService.log("Inregistrare GUI: " + email);
                JOptionPane.showMessageDialog(this, "Cont creat cu succes! Te poti loga acum.");

                SwingUtilities.invokeLater(() -> new LoginFrame(banca).setVisible(true));
                this.dispose();
            } catch (DateInvalideException ex) {
                JOptionPane.showMessageDialog(this, "Eroare: " + ex.getMessage());
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Sold initial invalid.");
            }
        });
    }
}
