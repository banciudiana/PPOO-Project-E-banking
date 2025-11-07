package gui;

import model.*;
import service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import java.text.DecimalFormat;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

/**
 * Fereastra GUI pentru clientii logati.
 * Permite vizualizarea conturilor, crearea de conturi noi,
 * efectuarea de operatiuni (depunere, retragere, transfer),
 * vizualizarea statisticilor si istoricul tranzactiilor.
 */
public class ClientFrame extends JFrame {

    private final Banca banca;
    private final Client client;
    private JTable conturiTable;
    private DefaultTableModel tableModel;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");


    /**
     * Constructor pentru ClientFrame.
     * Initializeaza interfata cu tab-uri pentru conturi, creare cont, operatiuni, statistici si istoric tranzactii.
     * @param banca obiectul Banca care contine datele clientilor si conturilor
     * @param client clientul logat
     */

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


    /**
     * Creeaza panoul cu lista de conturi ale clientului.
     * Permite modificarea monedei si inchiderea contului.
     * @return JPanel cu tabelul conturilor
     */

    private JPanel creeazaPanelConturi() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] coloane = {"ID", "Tip", "Sold", "Valuta", "Data Creare", "Dobanda Acumulata"};
        tableModel = new DefaultTableModel(coloane, 0);
        conturiTable = new JTable(tableModel);
        actualizeazaConturi();

        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem modificaMonedaItem = new JMenuItem("Modifică moneda");
        JMenuItem inchideContItem = new JMenuItem("Închide contul");

        popupMenu.addSeparator();
        popupMenu.add(modificaMonedaItem);
        popupMenu.addSeparator();
        popupMenu.add(inchideContItem);

        modificaMonedaItem.addActionListener(e -> {
            int selectedRow = conturiTable.getSelectedRow();
            if (selectedRow != -1) {
                int contId = (int) tableModel.getValueAt(selectedRow, 0);
                ContBancar cont = banca.getConturi().get(contId);

                if (cont == null) {
                    JOptionPane.showMessageDialog(this, "Cont inexistent!");
                    return;
                }

                if (cont.getClient().getId() != client.getId()) {
                    JOptionPane.showMessageDialog(this, "Nu poți modifica un cont care nu iti apartine!");
                    return;
                }


                String valutaCurenta = cont.getValuta();
                String[] valuteDisponibile = {"RON", "EUR", "USD", "GBP"};
                String valutaNoua = (String) JOptionPane.showInputDialog(
                        this,
                        "Alege valuta noua pentru contul " + contId + ":",
                        "Selectie valuta",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        valuteDisponibile,
                        valutaCurenta
                );

                if (valutaNoua == null || valutaNoua.equals(valutaCurenta)) {
                    return;
                }

                try {
                    double soldCurent = cont.getSold();
                    double soldConvertit = CursValutarService.convertCuMatrice(soldCurent, valutaCurenta, valutaNoua);


                    String mesaj = String.format("""
                ATENȚIE: Conversie valutară
                
                Contul %d va fi convertit din %s în %s.
                
                 Detalii conversie:
                • Sold actual: %.2f %s
                • Sold după conversie: %.2f %s
              
                
                IMPORTANT:
                Această operațiune va converti permanent 
                soldul contului la cursul curent de schimb.
                
                
                
                Dorești să continui?
                """,
                            contId, valutaCurenta, valutaNoua,
                            soldCurent, valutaCurenta,
                            soldConvertit, valutaNoua

                    );

                    int confirm = JOptionPane.showConfirmDialog(
                            this,
                            mesaj,
                            "Confirmare conversie valutara",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

                    if (confirm != JOptionPane.YES_OPTION) {
                        return;
                    }


                    cont.schimbaValuta(valutaNoua);
                    banca.salveazaDate();


                    JOptionPane.showMessageDialog(
                            this,
                            String.format("""
                    Conversie realizata cu succes!
                    
                    Contul %d este acum în %s
                    Sold nou: %.2f %s
                    """,
                                    contId, valutaNoua, soldConvertit, valutaNoua),
                            "Succes",
                            JOptionPane.INFORMATION_MESSAGE
                    );


                    actualizeazaConturi();


                    AuditService.log(String.format(
                            "Conversie valută: Cont %d din %s în %s (%.2f → %.2f)",
                            contId, valutaCurenta, valutaNoua, soldCurent, soldConvertit
                    ));

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Eroare la conversie: " + ex.getMessage(),
                            "Eroare",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        inchideContItem.addActionListener(e -> {
            int selectedRow = conturiTable.getSelectedRow();
            if (selectedRow != -1) {
                int contId = (int) tableModel.getValueAt(selectedRow, 0);
                ContBancar cont = banca.getConturi().get(contId);

                if (cont == null) {
                    JOptionPane.showMessageDialog(this, "Cont inexistent!");
                    return;
                }


                if (cont.getClient().getId() != client.getId()) {
                    JOptionPane.showMessageDialog(this,
                            "Nu poți inchide un cont care nu iti apartine!",
                            "Eroare",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }


                double sold = cont.getSold();
                if (Math.abs(sold) > 0.01) {

                    String mesaj = String.format("""
                Nu poți inchide acest cont!
                
                Contul %d are un sold de %.2f %s
                
                Pentru a inchide contul, soldul trebuie să fie 0.
                
                Sugestii:
                • Retrage suma disponibila
                • Transfera banii în alt cont
               
                """,
                            contId, sold, cont.getValuta()
                    );

                    JOptionPane.showMessageDialog(
                            this,
                            mesaj,
                            "Sold diferit de zero",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }


                String tipCont = cont.getClass().getSimpleName();
                if (cont instanceof ContEconomii) {
                    ContEconomii ce = (ContEconomii) cont;
                    tipCont += " (" + ce.getTip() + ")";
                }

                String mesajConfirmare = String.format("""
            Ești sigur ca vrei sa inchizi acest cont?
            
           Detalii cont:
            • ID: %d
            • Tip: %s
            • Valută: %s
            • Data creare: %s
            
            ATENȚIE:
            Aceasta acțiune este PERMANENTĂ și IREVERSIBILĂ!
           
            
            Doresti sa continui?
            """,
                        contId,
                        tipCont,
                        cont.getValuta(),
                        cont.getCreationDate().format(formatter)
                );

                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        mesajConfirmare,
                        "Confirmare închidere cont",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }


                try {
                    banca.inchideCont(contId, client.getId());

                    JOptionPane.showMessageDialog(
                            this,
                            String.format("""
                     Contul %d a fost inchis cu succes!
                    
                    Contul si tranzactiile asociate 
                    au fost șterse definitiv.
                    """,
                                    contId),
                            "Succes",
                            JOptionPane.INFORMATION_MESSAGE
                    );


                    actualizeazaConturi();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Eroare la inchiderea contului:\n" + ex.getMessage(),
                            "Eroare",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        conturiTable.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            private void showPopup(MouseEvent e) {
                int row = conturiTable.rowAtPoint(e.getPoint());
                if (row >= 0 && row < conturiTable.getRowCount()) {
                    conturiTable.setRowSelectionInterval(row, row);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        panel.add(new JScrollPane(conturiTable), BorderLayout.CENTER);
        return panel;
    }

    private void actualizeazaConturi() {
        tableModel.setRowCount(0);
        for (ContBancar c : banca.getConturi().values()) {
            if (c.getClient().getId() == client.getId()) {
                tableModel.addRow(new Object[]{
                        c.getId(),
                        c.getClass().getSimpleName() + (c instanceof ContEconomii ce ? " (" + ce.getTip() + ")" : ""),
                        String.format("%.2f", c.getSold()),
                        c.getValuta(),
                        c.getCreationDate().toLocalDate(),
                        c instanceof ContEconomii ce ? String.format("%.2f", ce.getDobandaAcumulata()) : "-"
                });
            }
        }
    }


    // ==================== Panel Creare Cont ====================

    /**
     * Creeaza panoul pentru crearea unui cont nou.
     * Permite alegerea tipului de cont si a monedei.
     * @return JPanel cu formularul de creare cont
     */

    private JPanel creeazaPanelCreareCont() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> tipBox = new JComboBox<>(new String[]{"CURENT", "ECONOMII", "CREDIT"});
        JComboBox<String> valutaBox = new JComboBox<>(new String[]{"RON", "EUR", "USD", "GBP"});
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

                if (tip.equals("ECONOMII")) {
                    // Pop-up pentru alegerea tipului de cont Economii
                    String mesaj = "Alege tipul contului de economii:\n" +
                            "1. Bonus: dobanda 5%, retragere doar dupa 4 luni, pierzi dobanda daca retragi mai devreme.\n" +
                            "2. Economii: dobanda 2%, poti retrage oricand, poti alimenta cardul, pastrezi dobanda la retragere.";
                    String[] optiuni = {"Bonus", "Economii"};
                    int selectie = JOptionPane.showOptionDialog(
                            this,
                            mesaj,
                            "Tip cont Economii",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            optiuni,
                            optiuni[0]
                    );

                    if (selectie == -1) {

                        return;
                    }

                    String tipEconomii = optiuni[selectie];



                    ContEconomii cont = banca.creaContEconomiiPentruClient(client, sold, valuta, tipEconomii);
                    banca.salveazaDate();
                    JOptionPane.showMessageDialog(this, "Cont economii creat cu succes! ID: " + cont.getId() +
                            "\nTip: " + tipEconomii);
                } else {

                    ContBancar cont = banca.creaContPentruClient(client, tip, sold, valuta);
                    banca.salveazaDate();
                    JOptionPane.showMessageDialog(this, "Cont creat cu succes! ID: " + cont.getId());
                }

                actualizeazaConturi();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Sold initial invalid.");
            }
        });

        return panel;
    }


    /**
     * Creeaza panoul pentru operatiuni financiare: depunere, retragere, transfer.
     * @return JPanel cu meniul de operatiuni
     */

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
            JComboBox<String> valutaBox = new JComboBox<>(new String[]{"RON", "EUR", "USD", "GBP"});
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
                        JOptionPane.showMessageDialog(this, "Selecteaza un cont.");
                        return;
                    }

                    int contId = Integer.parseInt(contBox.getSelectedItem().toString().split(" ")[0]);
                    ContBancar cont = banca.getConturi().get(contId);

                    double suma = Double.parseDouble(sumaField.getText().trim());
                    String valutaSelectata = (String) valutaBox.getSelectedItem();

                    if (suma <= 0) {
                        JOptionPane.showMessageDialog(this, "Introdu o suma validă.");
                        return;
                    }


                    double sumaDupaComision = suma;


                    if (cont.getValuta().equalsIgnoreCase(valutaSelectata)) {
                        int confirm = JOptionPane.showConfirmDialog(this,
                                String.format("""
                                    Depunere în %s:
                                    Suma depusa: %.2f %s
                                    Se adauga în cont: %.2f %s
                                    
                                    Doresti sa continui?
                                    """,
                                        cont.getValuta(), suma, valutaSelectata,
                                        sumaDupaComision, cont.getValuta()),
                                "Confirmare depunere",
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if (confirm != JOptionPane.OK_OPTION) return;
                        cont.setSold(cont.getSold() + sumaDupaComision);
                    }

                    else {
                        double sumaConvertita = CursValutarService.convertCuMatrice(
                                sumaDupaComision, valutaSelectata, cont.getValuta()
                        );

                        int confirm = JOptionPane.showConfirmDialog(this,
                                String.format("""
                                Conversie valutara:
                                Suma depusa: %.2f %s
                                Echivalent in cont: %.2f %s
                                Curs valutar aplicat
                                
                                Doresti sa continui?
                                """,
                                        suma, valutaSelectata,
                                        sumaConvertita, cont.getValuta()),
                                "Conversie valutara",
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        if (confirm != JOptionPane.OK_OPTION) return;
                        cont.setSold(cont.getSold() + sumaConvertita);
                    }

                    banca.salveazaDate();
                    JOptionPane.showMessageDialog(this, "Depunere efectuata cu succes!");
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
            JComboBox<String> valutaBox = new JComboBox<>(new String[]{"RON", "EUR", "USD", "GBP"});
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
                        JOptionPane.showMessageDialog(this, "Selecteaza un cont.");
                        return;
                    }

                    int contId = Integer.parseInt(contBox.getSelectedItem().toString().split(" ")[0]);
                    ContBancar cont = banca.getConturi().get(contId);

                    double suma = Double.parseDouble(sumaField.getText().trim());
                    String valutaSelectata = (String) valutaBox.getSelectedItem();

                    if (suma <= 0) {
                        JOptionPane.showMessageDialog(this, "Introdu o sumă valida.");
                        return;
                    }


                    double sumaInValutaCont;
                    if (cont.getValuta().equalsIgnoreCase(valutaSelectata)) {
                        sumaInValutaCont = suma;
                    } else {
                        sumaInValutaCont = CursValutarService.convertCuMatrice(suma, valutaSelectata, cont.getValuta());
                    }


                    int confirm = JOptionPane.showConfirmDialog(this,
                            String.format("""
                Retragere:
                Suma: %.2f %s
                Echivalent în cont: %.2f %s
                
                Doresti sa continui?
                """,
                                    suma, valutaSelectata,
                                    sumaInValutaCont, cont.getValuta()),
                            "Confirmare retragere",
                            JOptionPane.OK_CANCEL_OPTION);

                    if (confirm != JOptionPane.OK_OPTION) return;


                    cont.retrage(sumaInValutaCont);
                    banca.salveazaDate();

                    JOptionPane.showMessageDialog(this, "Retragere efectuată cu succes!");
                    actualizeazaConturi();

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Suma nu este un număr valid.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
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


            JComboBox<String> contSursaBox = new JComboBox<>();
            for (ContBancar c : banca.getConturi().values()) {
                if (c.getClient().getId() == client.getId()) {
                    contSursaBox.addItem(c.getId() + " - " + c.getClass().getSimpleName() + " (" + c.getValuta() + ")");
                }
            }


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

            JComboBox<String> valutaBox = new JComboBox<>(new String[]{"RON", "EUR", "USD", "GBP"});
            JButton transferaBtn = new JButton("Transfera");

            gbc.gridx = 0; gbc.gridy = 0; contentPanel.add(new JLabel("Din contul:"), gbc);
            gbc.gridx = 1; contentPanel.add(contSursaBox, gbc);
            gbc.gridx = 0; gbc.gridy = 1; contentPanel.add(new JLabel("In contul:"), gbc);
            gbc.gridx = 1; contentPanel.add(contDestBox, gbc);
            gbc.gridx = 0; gbc.gridy = 2; contentPanel.add(new JLabel("Suma:"), gbc);
            gbc.gridx = 1; contentPanel.add(sumaField, gbc);
            gbc.gridx = 0; gbc.gridy = 3; contentPanel.add(new JLabel("Valuta:"), gbc);
            gbc.gridx = 1; contentPanel.add(valutaBox, gbc);
            gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
            contentPanel.add(transferaBtn, gbc);


            transferaBtn.addActionListener(ev -> {
                try {
                    if (contSursaBox.getSelectedItem() == null || contDestBox.getSelectedItem() == null) {
                        JOptionPane.showMessageDialog(this, "Selecteaza ambele conturi.");
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
                        JOptionPane.showMessageDialog(this, "Introdu o suma valida!");
                        return;
                    }

                    String valutaSelectata = (String) valutaBox.getSelectedItem();


                    double sumaInValutaSursa;
                    if (valutaSelectata.equalsIgnoreCase(sursa.getValuta())) {
                        sumaInValutaSursa = suma;
                    } else {
                        sumaInValutaSursa = CursValutarService.convertCuMatrice(suma, valutaSelectata, sursa.getValuta());
                    }



                    double sumaTotalaSursa = sumaInValutaSursa;


                    if (sursa.getSold() < sumaTotalaSursa) {
                        JOptionPane.showMessageDialog(this,
                                String.format("Fonduri insuficiente!\nNecesar: %.2f %s\nDisponibil: %.2f %s",
                                        sumaTotalaSursa, sursa.getValuta(), sursa.getSold(), sursa.getValuta()));
                        return;
                    }

                    double sumaInValutaDest;
                    if (sursa.getValuta().equalsIgnoreCase(destinatie.getValuta())) {
                        sumaInValutaDest = sumaInValutaSursa;
                    } else {
                        sumaInValutaDest = CursValutarService.convertCuMatrice(
                                sumaInValutaSursa, sursa.getValuta(), destinatie.getValuta()
                        );
                    }


                    StringBuilder mesaj = new StringBuilder();
                    mesaj.append("Transfer bancar:\n\n");
                    mesaj.append(String.format("Suma initiala: %.2f %s\n", suma, valutaSelectata));
                    if (!valutaSelectata.equalsIgnoreCase(sursa.getValuta())) {
                        mesaj.append(String.format("Echivalent în cont sursa: %.2f %s\n",
                                sumaInValutaSursa, sursa.getValuta()));
                    }

                    mesaj.append(String.format("Total retras din sursa: %.2f %s\n\n",
                            sumaTotalaSursa, sursa.getValuta()));
                    if (!sursa.getValuta().equalsIgnoreCase(destinatie.getValuta())) {
                        mesaj.append(String.format("Conversie: %.2f %s → %.2f %s\n\n",
                                sumaInValutaSursa, sursa.getValuta(),
                                sumaInValutaDest, destinatie.getValuta()));
                    }
                    mesaj.append(String.format("Se adaugă în destinatie: %.2f %s\n\n",
                            sumaInValutaDest, destinatie.getValuta()));
                    mesaj.append("Doresti sa continui?");

                    int confirm = JOptionPane.showConfirmDialog(this, mesaj.toString(),
                            "Confirmare transfer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

                    if (confirm != JOptionPane.OK_OPTION) return;


                    banca.transfera(client, idSursa, idDest, sumaInValutaSursa);

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



    /**
     * Creeaza panoul pentru statistici conturi.
     * Afiseaza soldurile pe tipuri de cont si graficul pieselor.
     * @return JPanel cu statistici si grafic
     */
    private JPanel creeazaPanelStatistici() {
        JPanel panel = new JPanel(new BorderLayout());

        // Panel pentru grafic
        JPanel chartPanel = new JPanel(new BorderLayout());

        // Panel pentru statistici text
        JTextArea statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(statsArea);
        scrollPane.setPreferredSize(new Dimension(200, 0));

        // Split panel între grafic și text
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chartPanel, scrollPane);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.7);

        panel.add(splitPane, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> actualizeazaStatistici(chartPanel, statsArea));

        panel.add(refreshBtn, BorderLayout.SOUTH);

        // Actualizare inițială
        actualizeazaStatistici(chartPanel, statsArea);

        return panel;
    }

    /**
     * Actualizeaza panoul de statistici cu datele curente.
     * @param chartPanel JPanel pentru graficul circular
     * @param statsArea JTextArea pentru statistici textuale
     */

    private void actualizeazaStatistici(JPanel chartPanel, JTextArea statsArea) {
        double total = 0, curent = 0, economii = 0, credit = 0;
        int nrCurent = 0, nrEconomii = 0, nrCredit = 0;

        for (ContBancar c : banca.getConturi().values()) {
            if (c.getClient().getId() == client.getId()) {
                double sold = c.getSold();
                total += sold;

                if (c instanceof ContCurent) {
                    curent += sold;
                    nrCurent++;
                } else if (c instanceof ContEconomii) {
                    economii += sold;
                    nrEconomii++;
                } else if (c instanceof ContCredit) {
                    credit += sold;
                    nrCredit++;
                }
            }
        }

        // Actualizare text statistici
        StringBuilder stats = new StringBuilder();
        stats.append("     STATISTICI CONTURI\n");
        stats.append(String.format("SOLD TOTAL: %.2f RON\n\n", total));
        stats.append(String.format("Conturi Curente (%d):\n", nrCurent));
        stats.append(String.format("%.2f RON (%.1f%%)\n\n", curent, total > 0 ? (curent/total)*100 : 0));
        stats.append(String.format("Conturi Economii (%d):\n", nrEconomii));
        stats.append(String.format("%.2f RON (%.1f%%)\n\n", economii, total > 0 ? (economii/total)*100 : 0));
        stats.append(String.format("Conturi Credit (%d):\n", nrCredit));
        stats.append(String.format("%.2f RON (%.1f%%)\n", credit, total > 0 ? (credit/total)*100 : 0));
        statsArea.setText(stats.toString());

        // Creare dataset pentru pie chart
        DefaultPieDataset dataset = new DefaultPieDataset();

        if (curent > 0) dataset.setValue("Cont Curent", curent);
        if (economii > 0) dataset.setValue("Cont Economii", economii);
        if (credit > 0) dataset.setValue("Cont Credit", credit);

        // Creare pie chart
        JFreeChart chart = ChartFactory.createPieChart(
                "Distribuția Soldurilor",
                dataset,
                true,  // legend
                true,  // tooltips
                false  // urls
        );

        // Personalizare aspect grafic
        chart.setBackgroundPaint(new Color(245, 245, 245));

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);

        // Culori personalizate pentru fiecare tip de cont
        plot.setSectionPaint("Cont Curent", new Color(52, 152, 219));      // Albastru
        plot.setSectionPaint("Cont Economii", new Color(46, 204, 113));    // Verde
        plot.setSectionPaint("Cont Credit", new Color(231, 76, 60));       // Roșu

        // Stil pentru labels
        plot.setLabelFont(new Font("SansSerif", Font.BOLD, 12));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);

        // Format pentru labels cu procent și sumă
        plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(
                "{0}: {2} ({1})",
                new DecimalFormat("#,##0.00"),
                new DecimalFormat("0.0%")
        ));



//        // Stil pentru legendă
//        chart.getLegend().setBackgroundPaint(new Color(255, 255, 255, 200));
//        chart.getLegend().setFrame(new org.jfree.chart.block.BlockBorder(Color.LIGHT_GRAY));

        // Adaugare în panel
        chartPanel.removeAll();
        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(400, 400));
        cp.setMouseWheelEnabled(true);
        chartPanel.add(cp, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }


    /**
     * Creeaza panoul pentru vizualizarea istoricului tranzactiilor.
     * Permite filtrarea dupa contul clientului logat.
     * @return JPanel cu tabelul tranzactiilor
     */
    private JPanel creeazaPanelTranzactii() {
        JPanel panel = new JPanel(new BorderLayout());

        // Dropdown pentru filtrare dupa conturile clientului logat
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
                            t.getData().format(formatter)
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
                            t.getData().format(formatter)
                    });
                }
            }
        };


        actualizeazaTabel.run();


        conturiClientBox.addActionListener((ActionEvent e) -> actualizeazaTabel.run());

        return panel;
    }


}
