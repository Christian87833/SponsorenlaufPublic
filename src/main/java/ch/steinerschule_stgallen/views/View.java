package ch.steinerschule_stgallen.views;

import ch.steinerschule_stgallen.util.Currency;
import ch.steinerschule_stgallen.util.ListType;
import ch.steinerschule_stgallen.util.SponsorshipType;
import ch.steinerschule_stgallen.model.*;
import ch.steinerschule_stgallen.controller.*;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.List;

public class View {

    private final Model model;
    private final JFrame frame = new JFrame();//creating instance of JFrame
    private final JPanel dataInputFrame;

    private final AnalysisView dataAnalysisFrame;
    private final SendBillsView sendBillsFrame;

    private final ListView classPanel;
    private final ListView studentsPanel;
    private final ListView sponsorshipsPanel;
    private final ListView sponsorsPanel;
    private final Controller controller;

    private JMenuBar menuBar;

    public View(Model model){
        this.model = model;
        model.registerView(this);
        ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("running-icon.png"));
        frame.setIconImage(icon.getImage());



        menuBar = new JMenuBar();



        JTabbedPane tabbedPane = new JTabbedPane();
        frame.setSize(1000,500);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);


        WindowListener windowListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmClose();
            }
        };
        frame.addWindowListener(windowListener);


        controller = new Controller(model, this);


        JMenu menu = new JMenu("Datei");
        menuBar.add(menu);

        JMenuItem saveAs = new JMenuItem("Speichern unter...");
        JMenuItem load = new JMenuItem("Öffnen...");
        JMenuItem save = new JMenuItem("Speichern...");

        KeyStroke keyStrokeToOpen
                = KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke keyStrokeToSave
                = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
        load.setAccelerator(keyStrokeToOpen);
        save.setAccelerator(keyStrokeToSave);

        saveAs.addActionListener(e -> controller.saveAs());
        load.addActionListener(e -> controller.load());
        save.addActionListener(e -> controller.save());
        menu.add(load);
        menu.add(save);
        menu.add(saveAs);


        frame.setJMenuBar(menuBar);


        dataInputFrame = new JPanel();
        dataInputFrame.setLayout(new BorderLayout());

        //Start view
        JPanel fourCols = new JPanel();
        fourCols.setLayout(new GridLayout(1,4));

        classPanel = new ListView("Klassen:", "Klasse",controller, ListType.CLASSES);
        classPanel.setAddButtonEnabled(true);
        studentsPanel = new ListView("Schüler:", "Schüler", controller, ListType.STUDENTS);
        sponsorshipsPanel = new ListView("Sponsorschaften:", "Sponsorenschaft", controller, ListType.SPONSORSHIP);
        sponsorsPanel = new ListView("Sponsoren:", "Sponsor", controller, ListType.SPONSORS);
        sponsorsPanel.setAddButtonEnabled(true);

        fourCols.add(classPanel);
        fourCols.add(studentsPanel);
        fourCols.add(sponsorshipsPanel);
        fourCols.add(sponsorsPanel);

        dataInputFrame.add(fourCols, BorderLayout.CENTER);

        dataAnalysisFrame = new AnalysisView(model);
        sendBillsFrame = new SendBillsView(model);

        tabbedPane.addTab("Dateneingabe", dataInputFrame);
        tabbedPane.addTab("Datenauswertung", dataAnalysisFrame);
        tabbedPane.addTab("Rechnungen versenden", sendBillsFrame);

        tabbedPane.addChangeListener(e -> {
            if(tabbedPane.getSelectedIndex() == 1) {
                dataAnalysisFrame.refresh();
            } else if (tabbedPane.getSelectedIndex() == 2) {
                sendBillsFrame.update();
            } else if (tabbedPane.getSelectedIndex() == 0) {
                updateSponsorshipsView();
                updateStudentsView();
            }
        });

        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setVisible(true);//making the frame visible
    }

    /**
     * Ask if the user wants to save changes before closing
     */
    private void confirmClose() {
        int choice = JOptionPane.showConfirmDialog(frame, "Möchten Sie ihre Veränderungen " +
                        "speichern bevor Sie beenden?", "Bestätigen",
                JOptionPane.YES_NO_CANCEL_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            if(model.getCurrentFile() != null) {
                controller.save();
                frame.dispose();
                System.exit(0);
            } else if(controller.saveAs()) {
                // Saved successfully
                frame.dispose(); // Close the JFrame and release any resources associated with it
                System.exit(0);
            }
        } else if (choice == JOptionPane.NO_OPTION) {
            frame.dispose(); // Close the JFrame and release any resources associated with it
            System.exit(0);
        }
        // If the user selects cancel or closes the dialog, do nothing and continue with the application.
    }

    public boolean showNewClassDialog() {
        String name = JOptionPane.showInputDialog(dataInputFrame, "Name der neuen Klasse:", null);
        if(name != null && !name.isEmpty()) {
            model.addClass(name);
            return true;
        }
        return false;
    }
    public boolean showUpdateClassDialog() {
        String name = JOptionPane.showInputDialog(dataInputFrame, "Neuer Name der Klasse:", model.getSelectedClass().getName());
        if(name != null && !name.isEmpty()) {
            model.updateClass(name);
            return true;
        } else {
            //Error cannot set empy name
            return false;
        }
    }

    public boolean showNewStudentDialog() {
        JTextField nameField = new JTextField("");
        JTextField SurnameField = new JTextField("");
        JTextField telField = new JTextField("");
        JTextField lapCountField = new JTextField("0");
        JTextField lNumField = new JTextField("");


        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Vorname:"));
        panel.add(nameField);
        panel.add(new JLabel("Nachname:"));
        panel.add(SurnameField);
        panel.add(new JLabel("Läufernummer:"));
        panel.add(lNumField);
        panel.add(new JLabel("Telefonnummer:"));
        panel.add(telField);
        panel.add(new JLabel("Rundenzahl:"));
        panel.add(lapCountField);

        //just for focus to type away right away
        nameField.addAncestorListener( new AncestorListener()
        {
            @Override
            public void ancestorRemoved( final AncestorEvent event )
            {
            }
            @Override
            public void ancestorMoved( final AncestorEvent event )
            {
            }
            @Override
            public void ancestorAdded( final AncestorEvent event )
            {
                // Ask for focus
                nameField.requestFocusInWindow();
            }
        } );

        int result;
        while (true) {
            result = JOptionPane.showConfirmDialog(dataInputFrame, panel, "Schüler hinzufügen",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                if (lapCountField.getText().matches("[0-9]+") && lNumField.getText().matches("[0-9]+")) {
                    if (lNumField.getText().isEmpty()) {
                        displayInfo("Fehler: Läufernummer darf nicht leer sein.");
                    } else if (model.doesLNumExist(Integer.parseInt(lNumField.getText()))) {
                        displayInfo("Fehler: Läufernummer bereits vergeben");
                    } else {
                        model.addStudent(nameField.getText(), SurnameField.getText(), telField.getText(),
                                Integer.parseInt(lapCountField.getText()), Integer.parseInt(lNumField.getText()));
                        return true;
                    }
                } else {
                    if(!lapCountField.getText().matches("[0-9]+")) {
                        displayInfo("Ungültige Eingabe bei Rundenzahl. Erlaubte Eingaben: [0-9]+");
                    } else {
                        displayInfo("Ungültige Eingabe bei Läufer Nummer. Erlaubte Eingaben: [0-9]+");
                    }
                }
            } else {
                return false;
            }
        }
    }
    public boolean showUpdateStudentDialog() {
        JTextField nameField = new JTextField(model.selectedStudent.getName());
        JTextField surnameField = new JTextField(model.selectedStudent.getSurname());
        JTextField telField = new JTextField(model.selectedStudent.getTelNumber());
        JTextField lapCountField = new JTextField(Integer.toString(model.selectedStudent.getLapCount()));
        JTextField lNumField = new JTextField(Integer.toString(model.selectedStudent.getLNumber()));

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Vorname:"));
        panel.add(nameField);
        panel.add(new JLabel("Nachname:"));
        panel.add(surnameField);
        panel.add(new JLabel("Läufernummer:"));
        panel.add(lNumField);
        panel.add(new JLabel("Telefonnummer:"));
        panel.add(telField);
        panel.add(new JLabel("Rundenzahl:"));
        panel.add(lapCountField);

        int result;
        while (true){
            result = JOptionPane.showConfirmDialog(dataInputFrame, panel, "Schüler bearbeiten",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                if(lapCountField.getText().matches("[0-9]+") && lNumField.getText().matches("[0-9]+")) {
                    if(lNumField.getText().trim().isEmpty()) {
                        displayInfo("Fehler: Läufernummer darf nicht leer sein.");
                    } else {
                        model.updateStudent(nameField.getText(), surnameField.getText(), telField.getText(),
                                Integer.parseInt(lapCountField.getText()), Integer.parseInt(lNumField.getText()));
                        return true;
                    }
                } else {
                    if(!lapCountField.getText().matches("[0-9]+")) {
                        displayInfo("Ungültige Eingabe bei Rundenzahl. Erlaubte Eingaben: [0-9]+");
                    } else {
                        displayInfo("Ungültige Eingabe bei Läufer Nummer. Erlaubte Eingaben: [0-9]+");
                    }
                }
            } else {
                return false;
            }
        }
    }

    public boolean showNewSponsorshipDialog() {
        JPanel panel = new JPanel(new FlowLayout());
        String[] donationOptions = { "pro Runde", "einmalig"};
        JComboBox<String> donationOption = new JComboBox<>(donationOptions);
        String[] currencies = { "Franken", "Euro"};
        JComboBox<String> donationCurrency = new JComboBox<>(currencies);
        JTextField amount = new JTextField("");
        amount.setColumns(5);

        panel.add(new JLabel(model.selectedSponsor + " spendet"));
        panel.add(donationOption);
        panel.add(new JLabel("einen Betrag von:"));
        panel.add(amount);
        panel.add(donationCurrency);
        panel.add(new JLabel("an:" + model.selectedStudent));

        int result = JOptionPane.showConfirmDialog(dataInputFrame, panel, "Sponsorenschaft hinzufügen",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            if (amount.getText().matches("[0-9.,]+")) {
                String input = amount.getText().replace(',', '.');
                int decimalSeperator = input.lastIndexOf('.');

                if (decimalSeperator > -1) {
                    input = input.substring(0, decimalSeperator).replace(".", "")
                            + input.substring(decimalSeperator);
                }
                Currency curr = donationCurrency.getSelectedIndex() == 0 ? Currency.CHF : Currency.EUR;
                SponsorshipType type = donationOption.getSelectedIndex() == 0 ? SponsorshipType.PER_LAP
                        : SponsorshipType.ONCE_OFF;
                model.addSponsorship(Double.parseDouble(input), type, curr);
                return true;
            } else {
                JOptionPane.showMessageDialog(dataInputFrame, "Fehler! \nDer Betrag darf nur Zahlen und einen Punkt/Komma enthalten.");
                return false;
            }

        } else {
            return false;
        }
    }

    public boolean showUpdateSponsorshipDialog() {
        JPanel panel = new JPanel(new FlowLayout());
        String[] donationOptions = { "pro Runde", "einmalig"};
        JComboBox<String> donationOption = new JComboBox<>(donationOptions);
        int typeIndex = model.selectedSponsorship.getType() == SponsorshipType.PER_LAP ? 0 : 1;
        donationOption.setSelectedIndex(typeIndex);

        String[] currencies = { "Franken", "Euro"};
        JComboBox<String> donationCurrency = new JComboBox<>(currencies);
        int currIndex = model.selectedSponsorship.getCurrency() == Currency.CHF ? 0 : 1;
        donationCurrency.setSelectedIndex(currIndex);

        JTextField amount = new JTextField(String.valueOf(model.selectedSponsorship.getAmount()));
        amount.setColumns(5);

        panel.add(new JLabel(model.selectedSponsorship.getSponsor() + " spendet"));
        panel.add(donationOption);
        panel.add(new JLabel("einen Betrag von:"));
        panel.add(amount);
        panel.add(donationCurrency);
        panel.add(new JLabel("an:" + model.selectedSponsorship.getStudent()));

        int result = JOptionPane.showConfirmDialog(dataInputFrame, panel, "Sponsorenschaft bearbeiten",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            if (amount.getText().matches("[0-9.,]+")) {
                String input = amount.getText().replace(',', '.');
                int decimalSeperator = input.lastIndexOf('.');

                if (decimalSeperator > -1) {
                    input = input.substring(0, decimalSeperator).replace(".", "")
                            + input.substring(decimalSeperator);
                }
                Currency curr = donationCurrency.getSelectedIndex() == 0 ? Currency.CHF : Currency.EUR;
                SponsorshipType type = donationOption.getSelectedIndex() == 0 ? SponsorshipType.PER_LAP
                        : SponsorshipType.ONCE_OFF;
                model.updateSponsorship(Double.parseDouble(input), type, curr);
                return true;
            } else {
                JOptionPane.showMessageDialog(dataInputFrame, "Fehler! \nDer Betrag darf nur Zahlen und einen Punkt/Komma enthalten.");
                return false;
            }

        } else {
            return false;
        }
    }
    public boolean showNewSponsorDialog() {
        JTextField field0 = new JTextField("");
        JTextField field1 = new JTextField("");
        JTextField field2 = new JTextField("");
        JTextField field3 = new JTextField("");
        JTextField field4 = new JTextField("");
        JTextField field5 = new JTextField("");
        JTextField field6 = new JTextField("");
        JTextField field7 = new JTextField("");
        JCheckBox checkBoxEmail = new JCheckBox("Rechnung per Mail", true);

        JPanel panel = new JPanel(new GridLayout(0, 1));

        panel.add(new JLabel("Anrede:"));
        panel.add(field0);
        panel.add(new JLabel("Vorname:"));
        panel.add(field1);
        panel.add(new JLabel("Nachname:"));
        panel.add(field2);
        panel.add(new JLabel("E-Mail:"));
        panel.add(field3);
        panel.add(checkBoxEmail);
        panel.add(new JLabel("Adresse:"));
        panel.add(field4);
        panel.add(new JLabel("PLZ:"));
        panel.add(field6);
        panel.add(new JLabel("Stadt:"));
        panel.add(field5);
        panel.add(new JLabel("Land:"));
        panel.add(field7);


        //just for focus to type away right away
        field1.addAncestorListener( new AncestorListener()
        {
            @Override
            public void ancestorRemoved( final AncestorEvent event )
            {
            }
            @Override
            public void ancestorMoved( final AncestorEvent event )
            {
            }
            @Override
            public void ancestorAdded( final AncestorEvent event )
            {
                // Ask for focus (we'll lose it again)
                field0.requestFocusInWindow();
            }
        } );

        int result = JOptionPane.showConfirmDialog(dataInputFrame, panel, "Sponsor hinzufügen",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            model.addSponsor(field0.getText(), field1.getText(), field2.getText(), field3.getText(), field4.getText(),
                    field5.getText(), field6.getText(), field7.getText(), checkBoxEmail.isSelected());
            return true;
        } else {
            return false;
        }
    }
    public boolean showUpdateSponsorDialog() {
        JTextField field0 = new JTextField(model.selectedSponsor.getTitle());
        JTextField field1 = new JTextField(model.selectedSponsor.getName());
        JTextField field2 = new JTextField(model.selectedSponsor.getSurname());
        JTextField field3 = new JTextField(model.selectedSponsor.getEmail());
        JTextField field4 = new JTextField(model.selectedSponsor.getAddress());
        JTextField field5 = new JTextField(model.selectedSponsor.getCity());
        JTextField field6 = new JTextField(model.selectedSponsor.getPlz());
        JTextField field7 = new JTextField(model.selectedSponsor.getLand());
        JCheckBox checkBoxEmail = new JCheckBox("Rechnung per Mail", model.selectedSponsor.isEmailPay());



        JPanel panel = new JPanel(new GridLayout(0, 1));

        panel.add(new JLabel("Anrede:"));
        panel.add(field0);
        panel.add(new JLabel("Vorname:"));
        panel.add(field1);
        panel.add(new JLabel("Nachname:"));
        panel.add(field2);
        panel.add(new JLabel("E-Mail:"));
        panel.add(field3);
        panel.add(checkBoxEmail);
        panel.add(new JLabel("Adresse:"));
        panel.add(field4);
        panel.add(new JLabel("PLZ:"));
        panel.add(field6);
        panel.add(new JLabel("Stadt:"));
        panel.add(field5);
        panel.add(new JLabel("Land:"));
        panel.add(field7);

        int result = JOptionPane.showConfirmDialog(dataInputFrame, panel, "ch.steinerschule_stgallen.model.Sponsor Bearbeiten",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            model.updateSponsor(field0.getText(), field1.getText(), field2.getText(), field3.getText(),
                    field4.getText(), field5.getText(), field6.getText(), field7.getText(), checkBoxEmail.isSelected());
            return true;
        } else {
            return false;
        }
    }

    public void displayInfo(String message) {
        JOptionPane.showMessageDialog(dataInputFrame, message, "Info: ", JOptionPane.INFORMATION_MESSAGE);
    }

    public void updateClassView() {
        List<String> newList = model.getSearchRestrictedClasses().stream().map(StudentClass::getName).toList();
        String[] res = newList.toArray(new String[0]);
        classPanel.setListContent(res);
    }

    public void updateStudentsView() {
        if (model.selectedClass != null) {
            List<String> newList = model.getSearchRestrictedStudentsOfCurrentClass().stream().map(
                    student -> {
                        boolean allPayed = student.getSponsorships().stream().allMatch(sponsorship -> sponsorship.getSponsor().isPayed());
                        String sym = allPayed ? "\u2714" : "\u2716";
                        return student.toString() + sym;
                    }
            ).toList();
            String[] res = newList.toArray(new String[0]);
            studentsPanel.setListContent(res);
            studentsPanel.setHeading("Schüler der Klasse: " + model.selectedClass.getName());
            studentsPanel.setAddButtonEnabled(true);
        } else {
            studentsPanel.setListContent(new String[0]);
            studentsPanel.setHeading("Schüler:");
            studentsPanel.setAddButtonEnabled(false);
        }
    }

    public void updateSponsorshipsView() {
        if (model.selectedStudent != null) {
            List<String> newList = model.getSearchRestrictedSponsorshipsOfCurrentStudent().stream().map(
                    sponsorship -> {
                        String symbol = sponsorship.getSponsor().isPayed() ? "\u2714" : "\u2716";
                        return sponsorship.toString() + symbol;
                        }).toList();
            String[] res = newList.toArray(new String[0]);
            sponsorshipsPanel.setListContent(res);
            sponsorshipsPanel.setHeading("Sponsorschaften des Schülers: " + model.selectedStudent.getName());
            sponsorshipsPanel.setAddButtonEnabled(true);
        } else {
            sponsorshipsPanel.setListContent(new String[0]);
            sponsorshipsPanel.setHeading("Sponsorschaften:");
            sponsorshipsPanel.setAddButtonEnabled(false);
        }
    }

    public void updateSponsorsView() {
        List<String> newList = model.getSearchRestrictedSponsors().stream().map(Sponsor::toString).toList();
        String[] res = newList.toArray(new String[0]);
        sponsorsPanel.setListContent(res);
    }

    public boolean confirmWindow(String message) {
        return JOptionPane.showConfirmDialog(dataInputFrame, message) == 0;  // 0 = yes, 1 = no, 2 = cancel
    }

    public void refreshAll() {
        updateSponsorshipsView();
        updateClassView();
        updateSponsorsView();
        updateStudentsView();
        sendBillsFrame.update();
        dataAnalysisFrame.refresh();
    }

    public File chooseSaveLocation() {
        JFileChooser fileChooser= new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON FILES", ".json", "json");
        fileChooser.setDialogTitle("Speichern Unter");
        fileChooser.setFileFilter(filter);

        int returnVal = fileChooser.showSaveDialog(frame);

        if ( returnVal == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            return null;
        }
    }

    public File chooseOpenLocation() {
        JFileChooser fileChooser= new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON FILES", ".json", "json");
        fileChooser.setDialogTitle("Sponsorenlauf Öffnen");
        fileChooser.setFileFilter(filter);
        // Some init code, if you need one, like setting title
        int returnVal = fileChooser.showOpenDialog(frame);

        if ( returnVal == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            return null;
        }
    }
}
