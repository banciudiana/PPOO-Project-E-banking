package gui;

import model.*;
import service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Interfata GUI pentru un client logat.
 */
public class ClientFrame extends JFrame {

    private final Banca banca;
    private final Client client;

    private JTable conturiTable;
    private DefaultTableModel tableModel;

    public ClientFrame(Banca banca, Client client) {
        this.banca = banca;
        this.client = client;

        setTitle("eBanking - Client: " + client.getNume());
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Conturi", creeazaPanelConturi());
        tabbedPane.addTab("Creaza cont nou", creeazaPanelCreareCont());
        tabbedPane.addTab("Operatiuni", creeazaPanelOperatiuni());
        tabbedPane.addTab("Statistici", creeazaPanelStatistici());

        add(tabbedPane);
    }

    // ==================== Panel Conturi ====================
    private JPanel creeazaPanelConturi() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] coloane = {"ID", "Tip", "Sold", "Valuta"};
        tableModel = new DefaultTableModel(coloane, 0);
        conturiTable = new JTable(tableModel);
        actualizeazaConturi();

        panel.add(new JScrollPane(conturiTable), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> actualizeazaConturi());
        panel.add(refreshBtn, BorderLayout.SOUTH);

        return panel;
    }

    private void actualizeazaConturi() {
        tableModel.setRowCount(0); // sterge datele vechi
        for (ContBancar c : banca.getConturi().values()) {
            if (c.getClient().getId() == client.getId()) {
                tableModel.addRow(new Object[]{
                        c.getId(),
                        c.getClass().getSimpleName(),
                        c.getSold(),
                        c.getValuta()
                });
            }
        }
    }

    // ==================== Panel Creare Cont ====================
    private JPanel creeazaPanelCreareCont() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> tipBox = new JComboBox<>(new String[]{"CURENT", "ECONOMII", "CREDIT"});
        JComboBox<String> valutaBox = new JComboBox<>(new String[]{"RON", "EUR"});
        JTextField soldField = new JTextField("0");

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Tip cont:"), gbc);
        gbc.gridx = 1; panel.add(tipBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Valuta:"), gbc);
        gbc.gridx = 1; panel.add(valutaBox, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Sold initial:"), gbc);
        gbc.gridx = 1; panel.add(soldField, gbc);

        JButton createBtn = new JButton("Creaza cont");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(createBtn, gbc);

        createBtn.addActionListener(e -> {
            try {
                String tip = (String) tipBox.getSelectedItem();
                String valuta = (String) valutaBox.getSelectedItem();
                double sold = Double.parseDouble(soldField.getText().trim());

                ContBancar cont = banca.creaContPentruClient(client, tip, sold, valuta);
                banca.salveazaDate();
                JOptionPane.showMessageDialog(this, "Cont creat cu succes! ID: " + cont.getId());
                actualizeazaConturi();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Sold initial invalid.");
            }
        });

        return panel;
    }

    // ==================== Panel Operatiuni ====================
    private JPanel creeazaPanelOperatiuni() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField contIdField = new JTextField();
        JTextField sumaField = new JTextField();

        JButton depunereBtn = new JButton("Depunere");
        JButton retragereBtn = new JButton("Retragere");

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("ID cont:"), gbc);
        gbc.gridx = 1; panel.add(contIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Suma:"), gbc);
        gbc.gridx = 1; panel.add(sumaField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(depunereBtn, gbc);
        gbc.gridx = 1; panel.add(retragereBtn, gbc);

        depunereBtn.addActionListener((ActionEvent e) -> {
            try {
                int id = Integer.parseInt(contIdField.getText().trim());
                double suma = Double.parseDouble(sumaField.getText().trim());
                ContBancar cont = banca.getConturi().get(id);
                if (cont != null && cont.getClient().getId() == client.getId()) {
                    cont.setSold(cont.getSold() + suma);
                    banca.salveazaDate();
                    JOptionPane.showMessageDialog(this, "Depunere efectuata!");
                    actualizeazaConturi();
                } else {
                    JOptionPane.showMessageDialog(this, "Cont inexistent sau nu iti apartine.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Date invalide.");
            }
        });

        retragereBtn.addActionListener((ActionEvent e) -> {
            try {
                int id = Integer.parseInt(contIdField.getText().trim());
                double suma = Double.parseDouble(sumaField.getText().trim());
                ContBancar cont = banca.getConturi().get(id);
                if (cont != null && cont.getClient().getId() == client.getId()) {
                    if (cont.getSold() >= suma) {
                        cont.setSold(cont.getSold() - suma);
                        banca.salveazaDate();
                        JOptionPane.showMessageDialog(this, "Retragere efectuata!");
                        actualizeazaConturi();
                    } else {
                        JOptionPane.showMessageDialog(this, "Fonduri insuficiente.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Cont inexistent sau nu iti apartine.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Date invalide.");
            }
        });

        return panel;
    }

    // ==================== Panel Statistici ====================
    private JPanel creeazaPanelStatistici() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea statsArea = new JTextArea();
        statsArea.setEditable(false);
        panel.add(new JScrollPane(statsArea), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            double total = 0, curent = 0, economii = 0, credit = 0;
            for (ContBancar c : banca.getConturi().values()) {
                if (c.getClient().getId() == client.getId()) {
                    total += c.getSold();
                    if (c instanceof ContCurent) curent += c.getSold();
                    else if (c instanceof ContEconomii) economii += c.getSold();
                    else if (c instanceof ContCredit) credit += c.getSold();
                }
            }
            statsArea.setText(String.format("Sold total: %.2f%nCont Curent: %.2f%nCont Economii: %.2f%nCont Credit: %.2f",
                    total, curent, economii, credit));
        });

        panel.add(refreshBtn, BorderLayout.SOUTH);
        return panel;
    }
}
