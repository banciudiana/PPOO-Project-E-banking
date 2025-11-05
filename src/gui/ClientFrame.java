package gui;

import model.*;
import service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;

/**
 * Interfata GUI pentru un client logat.
 */
public class ClientFrame extends JFrame {

    private final Banca banca;
    private final Client client;
    private JTable conturiTable;
    private DefaultTableModel tableModel;

    // COMISION general pentru operațiuni (poți modifica aici)
    private static final double COMISION_PROCENT = 0.02; // 2%

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
        tabbedPane.addTab("Istoric tranzacții", creeazaPanelTranzactii());

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
        tableModel.setRowCount(0);
        for (ContBancar c : banca.getConturi().values()) {
            if (c.getClient().getId() == client.getId()) {
                tableModel.addRow(new Object[]{
                        c.getId(),
                        c.getClass().getSimpleName(),
                        String.format("%.2f", c.getSold()),
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
        JPanel panel = new JPanel(new BorderLayout());
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(150);

        // Meniu lateral
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        JButton depunereBtn = new JButton("Depunere");
        JButton retragereBtn = new JButton("Retragere");
        JButton transferBtn = new JButton("Transfer");

        depunereBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        retragereBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        transferBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuPanel.add(Box.createVerticalStrut(20));
        menuPanel.add(depunereBtn);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(retragereBtn);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(transferBtn);
        menuPanel.add(Box.createVerticalGlue());

        JPanel contentPanel = new JPanel(new BorderLayout());
        splitPane.setLeftComponent(menuPanel);
        splitPane.setRightComponent(contentPanel);

        // ===================== DEPUNERE =====================
        depunereBtn.addActionListener(e -> {
            contentPanel.removeAll();
            contentPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JComboBox<String> contBox = new JComboBox<>();
            for (ContBancar c : banca.getConturi().values()) {
                if (c.getClient().getId() == client.getId()) {
                    contBox.addItem(c.getId() + " - " + c.getClass().getSimpleName() + " (" + c.getValuta() + ")");
                }
            }

            JTextField sumaField = new JTextField();
            JComboBox<String> valutaBox = new JComboBox<>(new String[]{"RON", "EUR"});
            JButton depuneBtn = new JButton("Depune");

            gbc.gridx = 0; gbc.gridy = 0; contentPanel.add(new JLabel("Alege contul:"), gbc);
            gbc.gridx = 1; contentPanel.add(contBox, gbc);
            gbc.gridx = 0; gbc.gridy = 1; contentPanel.add(new JLabel("Suma:"), gbc);
            gbc.gridx = 1; contentPanel.add(sumaField, gbc);
            gbc.gridx = 0; gbc.gridy = 2; contentPanel.add(new JLabel("Valuta:"), gbc);
            gbc.gridx = 1; contentPanel.add(valutaBox, gbc);
            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
            contentPanel.add(depuneBtn, gbc);

            depuneBtn.addActionListener(ev -> {
                try {
                    if (contBox.getSelectedItem() == null) {
                        JOptionPane.showMessageDialog(this, "Selectează un cont.");
                        return;
                    }

                    int contId = Integer.parseInt(contBox.getSelectedItem().toString().split(" ")[0]);
                    ContBancar cont = banca.getConturi().get(contId);

                    double suma = Double.parseDouble(sumaField.getText().trim());
                    String valutaSelectata = (String) valutaBox.getSelectedItem();

                    if (suma <= 0) {
                        JOptionPane.showMessageDialog(this, "Introdu o sumă validă.");
                        return;
                    }

                    double comision = suma * COMISION_PROCENT;
                    double sumaFinala = suma - comision;

                    if (!cont.getValuta().equals(valutaSelectata)) {
                        double convertita = CursValutarService.convert(sumaFinala, valutaSelectata, cont.getValuta());

                        int confirm = JOptionPane.showConfirmDialog(this,
                                String.format("""
                                    Valutele sunt diferite!
                                    Suma depusă: %.2f %s
                                    Comision: %.2f %s
                                    Echivalent în cont: %.2f %s
                                    
                                    Dorești să continui?
                                    """, suma, valutaSelectata, comision, valutaSelectata,
                                        convertita, cont.getValuta()),
                                "Conversie valutară",
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        if (confirm != JOptionPane.OK_OPTION) return;
                        sumaFinala = convertita;
                    }

                    cont.setSold(cont.getSold() + sumaFinala);
                    banca.salveazaDate();
                    JOptionPane.showMessageDialog(this, "Depunere efectuată! (cu comision " + (COMISION_PROCENT * 100) + "%)");
                    actualizeazaConturi();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Eroare: " + ex.getMessage());
                }
            });

            contentPanel.revalidate();
            contentPanel.repaint();
        });

        // ===================== RETRAGERE =====================
        retragereBtn.addActionListener(e -> {
            contentPanel.removeAll();
            contentPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JComboBox<String> contBox = new JComboBox<>();
            for (ContBancar c : banca.getConturi().values()) {
                if (c.getClient().getId() == client.getId()) {
                    contBox.addItem(c.getId() + " - " + c.getClass().getSimpleName() + " (" + c.getValuta() + ")");
                }
            }

            JTextField sumaField = new JTextField();
            JComboBox<String> valutaBox = new JComboBox<>(new String[]{"RON", "EUR"});
            JButton retrageBtn = new JButton("Retrage");

            gbc.gridx = 0; gbc.gridy = 0; contentPanel.add(new JLabel("Alege contul:"), gbc);
            gbc.gridx = 1; contentPanel.add(contBox, gbc);
            gbc.gridx = 0; gbc.gridy = 1; contentPanel.add(new JLabel("Suma:"), gbc);
            gbc.gridx = 1; contentPanel.add(sumaField, gbc);
            gbc.gridx = 0; gbc.gridy = 2; contentPanel.add(new JLabel("Valuta:"), gbc);
            gbc.gridx = 1; contentPanel.add(valutaBox, gbc);
            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
            contentPanel.add(retrageBtn, gbc);

            retrageBtn.addActionListener(ev -> {
                try {
                    if (contBox.getSelectedItem() == null) {
                        JOptionPane.showMessageDialog(this, "Selectează un cont.");
                        return;
                    }

                    int contId = Integer.parseInt(contBox.getSelectedItem().toString().split(" ")[0]);
                    ContBancar cont = banca.getConturi().get(contId);

                    double suma = Double.parseDouble(sumaField.getText().trim());
                    String valutaSelectata = (String) valutaBox.getSelectedItem();

                    if (suma <= 0) {
                        JOptionPane.showMessageDialog(this, "Introdu o sumă validă.");
                        return;
                    }

                    double comision = suma * COMISION_PROCENT;
                    double sumaTotalaRetrasa = suma + comision;
                    double sumaFinala = sumaTotalaRetrasa;

                    if (!cont.getValuta().equals(valutaSelectata)) {
                        double convertita = CursValutarService.convert(sumaTotalaRetrasa, valutaSelectata, cont.getValuta());
                        int confirm = JOptionPane.showConfirmDialog(this,
                                String.format("""
                                    Valutele sunt diferite!
                                    Suma inițială: %.2f %s
                                    Comision: %.2f %s
                                    Total retras din cont: %.2f %s
                                    
                                    Dorești să continui?
                                    """, suma, valutaSelectata, comision, valutaSelectata,
                                        convertita, cont.getValuta()),
                                "Conversie valutară",
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        if (confirm != JOptionPane.OK_OPTION) return;
                        sumaFinala = convertita;
                    }

                    if (cont.getSold() < sumaFinala) {
                        JOptionPane.showMessageDialog(this, "Fonduri insuficiente.");
                        return;
                    }

                    cont.setSold(cont.getSold() - sumaFinala);
                    banca.salveazaDate();
                    JOptionPane.showMessageDialog(this, "Retragere efectuată! (cu comision " + (COMISION_PROCENT * 100) + "%)");
                    actualizeazaConturi();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Eroare: " + ex.getMessage());
                }
            });

            contentPanel.revalidate();
            contentPanel.repaint();
        });

        transferBtn.addActionListener(e -> {
            contentPanel.removeAll();
            contentPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Dropdown conturi sursă (doar ale clientului curent)
            JComboBox<String> contSursaBox = new JComboBox<>();
            for (ContBancar c : banca.getConturi().values()) {
                if (c.getClient().getId() == client.getId()) {
                    contSursaBox.addItem(c.getId() + " - " + c.getClass().getSimpleName() + " (" + c.getValuta() + ")");
                }
            }

            // Dropdown conturi destinație (toate conturile din bancă)
            JComboBox<String> contDestBox = new JComboBox<>();
            for (ContBancar c : banca.getConturi().values()) {
                contDestBox.addItem(c.getId() + " - " + c.getClient().getNume() + " (" + c.getValuta() + ")");
            }

            JTextField sumaField = new JTextField();
            sumaField.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyTyped(java.awt.event.KeyEvent evt) {
                    char ch = evt.getKeyChar();
                    if (!Character.isDigit(ch) && ch != '.' && ch != '\b') evt.consume();
                }
            });

            JComboBox<String> valutaBox = new JComboBox<>(new String[]{"RON", "EUR"});
            JButton transferaBtn = new JButton("Transferă");

            gbc.gridx = 0; gbc.gridy = 0; contentPanel.add(new JLabel("Din contul:"), gbc);
            gbc.gridx = 1; contentPanel.add(contSursaBox, gbc);
            gbc.gridx = 0; gbc.gridy = 1; contentPanel.add(new JLabel("În contul:"), gbc);
            gbc.gridx = 1; contentPanel.add(contDestBox, gbc);
            gbc.gridx = 0; gbc.gridy = 2; contentPanel.add(new JLabel("Suma:"), gbc);
            gbc.gridx = 1; contentPanel.add(sumaField, gbc);
            gbc.gridx = 0; gbc.gridy = 3; contentPanel.add(new JLabel("Valuta:"), gbc);
            gbc.gridx = 1; contentPanel.add(valutaBox, gbc);
            gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
            contentPanel.add(transferaBtn, gbc);

            // ======== LOGICA TRANSFER ========
            transferaBtn.addActionListener(ev -> {
                try {
                    if (contSursaBox.getSelectedItem() == null || contDestBox.getSelectedItem() == null) {
                        JOptionPane.showMessageDialog(this, "Selectează ambele conturi.");
                        return;
                    }

                    int idSursa = Integer.parseInt(contSursaBox.getSelectedItem().toString().split(" ")[0]);
                    int idDest = Integer.parseInt(contDestBox.getSelectedItem().toString().split(" ")[0]);

                    if (idSursa == idDest) {
                        JOptionPane.showMessageDialog(this, "Nu poți transfera între același cont!");
                        return;
                    }

                    ContBancar sursa = banca.getConturi().get(idSursa);
                    ContBancar destinatie = banca.getConturi().get(idDest);

                    double suma = Double.parseDouble(sumaField.getText().trim());
                    if (suma <= 0) {
                        JOptionPane.showMessageDialog(this, "Introdu o sumă validă!");
                        return;
                    }

                    String valutaSelectata = (String) valutaBox.getSelectedItem();
                    double comision = suma * 0.02; // 2% comision
                    double sumaNet = suma - comision;
                    double sumaFinala = sumaNet;

                    if (!sursa.getValuta().equals(destinatie.getValuta())) {
                        double convertita = CursValutarService.convert(sumaNet, sursa.getValuta(), destinatie.getValuta());

                        int confirm = JOptionPane.showConfirmDialog(this,
                                String.format("""
                            Valutele conturilor sunt diferite!
                            Suma transferată: %.2f %s
                            Comision: %.2f %s
                            Echivalent în contul destinație: %.2f %s
                            
                            Dorești să continui?
                            """,
                                        suma, sursa.getValuta(),
                                        comision, sursa.getValuta(),
                                        convertita, destinatie.getValuta()),
                                "Conversie valutară",
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.WARNING_MESSAGE);

                        if (confirm != JOptionPane.OK_OPTION) return;
                        sumaFinala = convertita;
                    }

                    if (sursa.getSold() < suma) {
                        JOptionPane.showMessageDialog(this, "Fonduri insuficiente în contul sursă!");
                        return;
                    }

                    // Actualizare solduri
                    sursa.setSold(sursa.getSold() - suma);
                    destinatie.setSold(destinatie.getSold() + sumaFinala);

                    // Log tranzacție
                    Tranzactie tranz = new Tranzactie(
                            new Random().nextInt(999999),
                            sursa,
                            destinatie,
                            suma,
                            "TRANSFER"
                    );

                    banca.adaugaTranzactie(tranz);
                    banca.salveazaDate();

                    JOptionPane.showMessageDialog(this, "Transfer efectuat cu succes!");
                    actualizeazaConturi();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Eroare la transfer: " + ex.getMessage());
                }
            });

            contentPanel.revalidate();
            contentPanel.repaint();
        });



        panel.add(splitPane, BorderLayout.CENTER);
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
            statsArea.setText(String.format(
                    "Sold total: %.2f%nCont Curent: %.2f%nCont Economii: %.2f%nCont Credit: %.2f",
                    total, curent, economii, credit));
        });

        panel.add(refreshBtn, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel creeazaPanelTranzactii() {
        JPanel panel = new JPanel(new BorderLayout());

        // Dropdown pentru filtrare după conturile clientului logat
        JComboBox<String> conturiClientBox = new JComboBox<>();
        conturiClientBox.addItem("Toate conturile"); // opțiune implicită

        for (ContBancar c : banca.getConturi().values()) {
            if (c.getClient().getId() == client.getId()) {
                conturiClientBox.addItem(c.getId() + " - " + c.getClass().getSimpleName() + " (" + c.getValuta() + ")");
            }
        }

        // Modelul tabelului
        String[] coloane = {"ID", "Sursa", "Destinatie", "Suma", "Tip", "Data"};
        DefaultTableModel model = new DefaultTableModel(coloane, 0);
        JTable tabela = new JTable(model);
        tabela.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(tabela);

        // Panou sus pentru filtrare
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Alege contul:"));
        topPanel.add(conturiClientBox);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Funcție internă pentru actualizarea tabelului
        Runnable actualizeazaTabel = () -> {
            model.setRowCount(0);
            String selectie = (String) conturiClientBox.getSelectedItem();

            for (Tranzactie t : banca.getTranzactii()) {
                // Dacă e "Toate conturile", le afișăm pe toate
                if (selectie.equals("Toate conturile")) {
                    model.addRow(new Object[]{
                            t.getId(),
                            t.getSursa().getClient().getNume() + " (" + t.getSursa().getId() + ")",
                            t.getDestinatie().getClient().getNume() + " (" + t.getDestinatie().getId() + ")",
                            String.format("%.2f %s", t.getSuma(), t.getSursa().getValuta()),
                            t.getTip(),
                            t.getData()
                    });
                    continue;
                }

                // Extragem ID-ul contului selectat
                int contIdSelectat = Integer.parseInt(selectie.split(" ")[0]);
                if (t.getSursa().getId() == contIdSelectat || t.getDestinatie().getId() == contIdSelectat) {
                    model.addRow(new Object[]{
                            t.getId(),
                            t.getSursa().getClient().getNume() + " (" + t.getSursa().getId() + ")",
                            t.getDestinatie().getClient().getNume() + " (" + t.getDestinatie().getId() + ")",
                            String.format("%.2f %s", t.getSuma(), t.getSursa().getValuta()),
                            t.getTip(),
                            t.getData()
                    });
                }
            }
        };

        // Populăm inițial tabelul
        actualizeazaTabel.run();

        // Eveniment: schimbarea selecției din dropdown
        conturiClientBox.addActionListener((ActionEvent e) -> actualizeazaTabel.run());

        return panel;
    }


}
