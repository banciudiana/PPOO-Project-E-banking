package gui;

import service.*;
import model.*;
import javax.swing.*;
import java.awt.*;


/**
 * Fereastra pentru autentificarea unui client in aplicatia eBanking.
 * Permite introducerea email-ului si parolei si autentificarea
 * sau redirectionarea catre inregistrarea unui cont nou.
 */
public class LoginFrame extends JFrame {

    private Banca banca;
    private JTextField emailField;
    private JPasswordField passField;

    /**
     * Constructor pentru fereastra de login.
     *
     * @param banca Referinta la obiectul Banca, pentru acces la datele clientilor.
     */

    public LoginFrame(Banca banca) {
        this.banca = banca;
        setTitle("Login - eBanking");
        setSize(400, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(3,2,5,5));
        form.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        form.add(new JLabel("Email:"));
        emailField = new JTextField();
        form.add(emailField);
        form.add(new JLabel("Parola:"));
        passField = new JPasswordField();
        form.add(passField);

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Cont nou");

        JPanel buttons = new JPanel();
        buttons.add(loginBtn);
        buttons.add(registerBtn);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String parola = new String(passField.getPassword()).trim();
            Client c = banca.autentifica(email, parola);
            if (c != null) {
                AuditService.log("Utilizator autentificat GUI: " + email);
                JOptionPane.showMessageDialog(this, "Autentificare reusita. Bun venit, " + c.getNume());

                SwingUtilities.invokeLater(() -> {
                    new ClientFrame(banca, c).setVisible(true);
                });
                this.dispose();
            } else {
                AuditService.log("Autentificare esuata GUI: " + email);
                JOptionPane.showMessageDialog(this, "Email/parola incorecte.");
            }
        });

        registerBtn.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> new RegisterFrame(banca).setVisible(true));
            this.dispose();
        });
    }
}
