package ch.steinerschule_stgallen.views;

import ch.steinerschule_stgallen.billing.EmailSenderWorker;
import ch.steinerschule_stgallen.billing.EmailService;
import ch.steinerschule_stgallen.billing.PDFCreator;
import ch.steinerschule_stgallen.billing.PDFUltimateList;
import ch.steinerschule_stgallen.model.Model;
import ch.steinerschule_stgallen.model.Sponsor;
import ch.steinerschule_stgallen.util.Currency;
import ch.steinerschule_stgallen.util.EmailType;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class SendBillsView extends JPanel {
    private final Model model;

    boolean[] selected = new boolean[0];
    JList<Sponsor> list = new JList<>();
    public SendBillsView(Model model) {
        this.model = model;
        this.setLayout(new GridLayout(1, 1));

        JPanel mainWindow = new JPanel();
        mainWindow.setLayout(new BorderLayout());

        JPanel headingBar = new JPanel();
        headingBar.setLayout(new BorderLayout());
        JLabel headingLeft = new JLabel("Sponsoren auswählen für Aktion");
        JLabel headingRight = new JLabel("<html><body width='100px'>Rechnungsstatus</body></html>");
        headingBar.add(headingLeft, BorderLayout.CENTER);
        headingBar.add(headingRight, BorderLayout.EAST);


        mainWindow.add(headingBar, BorderLayout.NORTH);

        list.setCellRenderer(new CheckboxListCellRenderer(this));

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                if(SwingUtilities.isLeftMouseButton(e)) {
                    if (index != -1)
                        selected[index] = !selected[index];
                    list.updateUI();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem item = new JMenuItem("Details anzeigen");
                    item.addActionListener(e1 -> {
                        JFrame jFrame = new JFrame();
                        jFrame.setTitle("Detailierte Informationen");
                        JDialog jd = new JDialog(jFrame);
                        jd.setLayout(new BorderLayout());

                        // Calculate screen bounds
                        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                        int screenWidth = gd.getDisplayMode().getWidth();
                        int screenHeight = gd.getDisplayMode().getHeight();

                        // Ensure the dialog doesn't go off-screen
                        int x = Math.min(e.getX(), screenWidth - 700);
                        int y = Math.min(e.getY(), screenHeight - 300);

                        jd.setBounds(x, y, 700, 300);
                        Sponsor sponsor = model.getAllSponsors().get(index);

                        JButton jButton = new JButton("Close");
                        jButton.addActionListener(e11 -> jd.setVisible(false));
                        JPanel topInfo = new JPanel(new GridLayout(5,4));
                        topInfo.add(new JLabel("Titel:"));
                        topInfo.add(new JLabel(sponsor.getTitle()));
                        topInfo.add(new JLabel("Nachname:"));
                        topInfo.add(new JLabel(sponsor.getSurname()));
                        topInfo.add(new JLabel("Name:"));
                        topInfo.add(new JLabel(sponsor.getName()));
                        topInfo.add(new JLabel("E-Mail:"));
                        topInfo.add(new JLabel(sponsor.getEmail()));
                        topInfo.add(new JLabel("Adresse:"));
                        topInfo.add(new JLabel(sponsor.getAddress()));
                        topInfo.add(new JLabel("Stadt:"));
                        topInfo.add(new JLabel(sponsor.getCity()));
                        topInfo.add(new JLabel("PLZ:"));
                        topInfo.add(new JLabel(sponsor.getPlz()));
                        topInfo.add(new JLabel("Land:"));
                        topInfo.add(new JLabel(sponsor.getLand()));
                        topInfo.add(new JLabel("Gesponsorter Betrag (CHF):"));
                        topInfo.add(new JLabel(String.valueOf(sponsor.getTotal(Currency.CHF))));
                        topInfo.add(new JLabel("Gesponsorter Betrag (EUR):"));
                        topInfo.add(new JLabel(String.valueOf(sponsor.getTotal(Currency.EUR))));

                        JScrollPane centerInfo = new JScrollPane();
                        JList<String> sponsorships = new JList<>();
                        String[] res = sponsor.getSponsorships().stream().map(sponsorship ->
                                sponsorship.getDetailedString(model) + " Tel: " + sponsorship.getStudent().getTelNumber())
                                .toList().toArray(new String[0]);
                        sponsorships.setListData(res);
                        centerInfo.setViewportView(sponsorships);

                        jd.add(topInfo, BorderLayout.NORTH);
                        jd.add(centerInfo, BorderLayout.CENTER);
                        jd.add(jButton, BorderLayout.SOUTH);
                        jd.setVisible(true);
                    });
                    menu.add(item);
                    menu.show(getParent(), e.getX(), e.getY() + 10);
                }
            }
        });

        ScrollPane sp = new ScrollPane();
        sp.add(list);
        mainWindow.add(sp, BorderLayout.CENTER);

        JPanel parent = this;

        JMenuBar lowerMenu = new JMenuBar();

        JMenu selectMenu = new JMenu("Auswahl...");
        JMenuItem selectAll = new JMenuItem("Alle auswählen");
        JMenuItem cancelSelection = new JMenuItem("Auswahl aufheben");
        JMenuItem specificSelection = new JMenuItem("Spezielle Auswahl treffen");
        selectAll.addActionListener(e1 -> {
            Arrays.fill(selected, true);
            list.updateUI();
        });
        cancelSelection.addActionListener(e1 -> {
            Arrays.fill(selected, false);
            list.updateUI();
        });
        specificSelection.addActionListener(e1 -> {
            ButtonGroup billPerMail = getYesNoDontCareGroup();
            JPanel perEmail = getChoicePanel("Rechnung per Mail ", billPerMail);

            ButtonGroup gotEmailButtons = getYesNoDontCareGroup();
            JPanel gotEmail = getChoicePanel("Email Erhalten? ", gotEmailButtons);

            ButtonGroup gotReminderButtons = getYesNoDontCareGroup();
            JPanel gotReminder = getChoicePanel("Erinnerung Erhalten? ", gotReminderButtons);

            ButtonGroup hasPayedButtons = getYesNoDontCareGroup();
            JPanel hasPayed = getChoicePanel("Hat Bezahlt? ", hasPayedButtons);

            ButtonGroup pdfExportedButtons = getYesNoDontCareGroup();
            JPanel pdfExported = getChoicePanel("PDF wurde erstellt?", pdfExportedButtons);

            ButtonGroup hasEmailAddressButtons = getYesNoDontCareGroup();
            JPanel hasEmailAddress = getChoicePanel("Hat valide Email?", hasEmailAddressButtons);

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(perEmail);
            panel.add(gotEmail);
            panel.add(gotReminder);
            panel.add(hasPayed);
            panel.add(pdfExported);
            panel.add(hasEmailAddress);

            int result = JOptionPane.showConfirmDialog(this, panel, "Spezielle Auswahl treffen",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                LinkedList<Sponsor> allSponsors = model.getAllSponsors();
                boolean[] ret = new boolean[allSponsors.size()];
                Arrays.fill(ret, true);

                String billE = billPerMail.getSelection().getActionCommand();
                if(!billE.equals("dontCare")) {
                    for(int i = 0; i < allSponsors.size(); i++){
                        ret[i] = ret[i] && allSponsors.get(i).isEmailPay() == billE.equals("yes");
                    }
                }


                String sel = gotEmailButtons.getSelection().getActionCommand();
                if(!sel.equals("dontCare")) {
                    for(int i = 0; i < allSponsors.size(); i++){
                        ret[i] = ret[i] && allSponsors.get(i).isEmailSent() == sel.equals("yes");
                    }
                }

                sel = gotReminderButtons.getSelection().getActionCommand();
                if(!sel.equals("dontCare")) {
                    for(int i = 0; i < allSponsors.size(); i++){
                        ret[i] = ret[i] && allSponsors.get(i).isReminded() == sel.equals("yes");
                    }
                }

                sel = hasPayedButtons.getSelection().getActionCommand();
                if(!sel.equals("dontCare")) {
                    for(int i = 0; i < allSponsors.size(); i++){
                        ret[i] = ret[i] && allSponsors.get(i).isPayed() == sel.equals("yes");
                    }
                }

                sel = pdfExportedButtons.getSelection().getActionCommand();
                if(!sel.equals("dontCare")) {
                    for(int i = 0; i < allSponsors.size(); i++){
                        ret[i] = ret[i] && allSponsors.get(i).isCreatedPDF() == sel.equals("yes");
                    }
                }

                sel = hasEmailAddressButtons.getSelection().getActionCommand();
                if(!sel.equals("dontCare")) {
                    for(int i = 0; i < allSponsors.size(); i++){
                        ret[i] = ret[i] && allSponsors.get(i).hasValidMail() == sel.equals("yes");
                    }
                }
                selected = ret;
                update();
            }
        });
        selectMenu.add(selectAll);
        selectMenu.add(cancelSelection);
        selectMenu.add(specificSelection);

        JMenu emailMenu = new JMenu("email...");
        JMenuItem bill = new JMenuItem("Rechnung versenden");
        JMenuItem reminder = new JMenuItem("Erinnerung versenden");
        JMenuItem thankYou = new JMenuItem("Dankesschreiben versenden");
        bill.addActionListener(e1 -> {
            if (getNumSelected() == 0) {
                displayInfo("Sie haben keine Sponsoren ausgewählt. " +
                        "Bitte markieren Sie die Sponsoren an die Sie Rechnungen versenden wollen.", parent);
                return;
            }
            int choice = JOptionPane.showConfirmDialog(parent, "Rechnungnen an " + getNumSelected()
                    + " Sponsoren versenden? ", "Confirmation", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(parent);

                //should be put in its own class...
                List<Sponsor> emailRecipients =  model.getSponsorsByTrueIndex(selected);
                parentFrame.setEnabled(false);
                LoadingBar dialog = new LoadingBar(parentFrame, emailRecipients.size());
                EmailSenderWorker worker = new EmailSenderWorker(model, emailRecipients, dialog, EmailType.BILL);
                worker.execute();
                dialog.setVisible(true);
                //Blocks and waits

                update();
            } else {
                // User clicked "No" or closed the dialog
                // Handle cancellation or perform an alternative action
            }

        });
        reminder.addActionListener(e1 -> {
            if (getNumSelected() == 0) {
                displayInfo("Sie haben keine Sponsoren ausgewählt. " +
                        "Bitte markieren Sie die Sponsoren an die Sie eine Erinnerung versenden wollen.", parent);
                return;
            }
            int choice = JOptionPane.showConfirmDialog(parent, "Erinnerung an " + getNumSelected()
                    + " Sponsoren versenden? ", "Confirmation", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                // User clicked "Yes"
                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(parent);

                //should be put in its own class...
                List<Sponsor> emailRecipients =  model.getSponsorsByTrueIndex(selected);
                parentFrame.setEnabled(false);
                LoadingBar dialog = new LoadingBar(parentFrame, emailRecipients.size());
                EmailSenderWorker worker = new EmailSenderWorker(model, emailRecipients, dialog, EmailType.REMINDER);
                worker.execute();
                dialog.setVisible(true);
                //Blocks and waits

                update();
            } else {
                // User clicked "No" or closed the dialog
                // Handle cancellation or perform an alternative action
            }
        });
        thankYou.addActionListener(e1 -> {
            if (getNumSelected() == 0) {
                displayInfo("Sie haben keine Sponsoren ausgewählt. " +
                        "Bitte markieren Sie die Sponsoren an die Sie eine Erinnerung versenden wollen.", parent);
                return;
            }
            int choice = JOptionPane.showConfirmDialog(parent, "Dankesschreiben an " + getNumSelected()
                    + " Sponsoren versenden? ", "Confirmation", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                // User clicked "Yes"
                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(parent);

                //should be put in its own class...
                List<Sponsor> emailRecipients =  model.getSponsorsByTrueIndex(selected);
                parentFrame.setEnabled(false);
                LoadingBar dialog = new LoadingBar(parentFrame, emailRecipients.size());
                EmailSenderWorker worker = new EmailSenderWorker(model, emailRecipients, dialog, EmailType.THANK_YOU);
                worker.execute();
                dialog.setVisible(true);
                //Blocks and waits

                update();
            } else {
                // User clicked "No" or closed the dialog
                // Handle cancellation or perform an alternative action
            }
        });
        emailMenu.add(bill);
        emailMenu.add(reminder);
        emailMenu.add(thankYou);

        JMenu pdfMenu = new JMenu("pdf...");
        JMenuItem pdfBill = new JMenuItem("PDF Rechnung");
        JMenuItem pdfReminder = new JMenuItem("PDF Erinnerung");
        JMenuItem pdfThankYou = new JMenuItem("PDF Dankesschreiben");
        JMenuItem pdfSelection = new JMenuItem("PDF Liste selektierter Einträge");
        JMenuItem pdfUltimateSelection = new JMenuItem("Ultimative PDF Liste selektierter Einträge");

        pdfBill.addActionListener(e1 -> {
            if (getNumSelected() == 0) {
                displayInfo("Sie haben keine Sponsoren ausgewählt. Bitte markieren Sie die Sponsoren" +
                        " für die Sie Rechnungen erstellen wollen.", parent);
                return;
            }
            final JFileChooser chooser = new JFileChooser() {
                public void approveSelection() {
                    if (!getSelectedFile().isFile()) {
                        super.approveSelection();
                    }
                }
            };
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int returnVal = chooser.showOpenDialog(parent);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File targetDir = chooser.getSelectedFile();
                model.getSponsorsByTrueIndex(selected).forEach(s ->
                        PDFCreator.writeBytesToPDFFile(PDFCreator.createPDFFromTemplate(s, EmailType.BILL, model),
                                targetDir, s, "_Rechnung.pdf"));
                model.getSponsorsByTrueIndex(selected).forEach(s -> s.setCreatedPDF(true));
                JOptionPane.showMessageDialog(parent, "Rechnungen wurden erfolgreich erstellt. " +
                                "Die Statusanzeige für \"PDF Erstellt\" wird auf Grün gesetzt.",
                        "Info: ", JOptionPane.INFORMATION_MESSAGE);
                update();
            }
        });
        pdfReminder.addActionListener(e1 -> {
            if (getNumSelected() == 0) {
                displayInfo("Sie haben keine Sponsoren ausgewählt. Bitte markieren Sie die Sponsoren" +
                        " für die Sie Erinnerungen erstellen wollen.", parent);
                return;
            }
            final JFileChooser chooser = new JFileChooser() {
                public void approveSelection() {
                    if (!getSelectedFile().isFile()) {
                        super.approveSelection();
                    }
                }
            };
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int returnVal = chooser.showOpenDialog(parent);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File targetDir = chooser.getSelectedFile();
                model.getSponsorsByTrueIndex(selected).forEach(s ->
                        PDFCreator.writeBytesToPDFFile(PDFCreator.createPDFFromTemplate(s, EmailType.REMINDER, model),
                                targetDir, s, "_Erinnerung.pdf"));
                model.getSponsorsByTrueIndex(selected).forEach(s -> {});
                JOptionPane.showMessageDialog(parent, "Erinnerungen wurden erfolgreich erstellt.",
                        "Info: ", JOptionPane.INFORMATION_MESSAGE);
                update();
            }

        });
        pdfThankYou.addActionListener(e1 -> {
            if (getNumSelected() == 0) {
                displayInfo("Sie haben keine Sponsoren ausgewählt. Bitte markieren Sie die Sponsoren" +
                        " für die Sie Dankesschreiben erstellen wollen.", parent);
                return;
            }
            final JFileChooser chooser = new JFileChooser() {
                public void approveSelection() {
                    if (!getSelectedFile().isFile()) {
                        super.approveSelection();
                    }
                }
            };
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int returnVal = chooser.showOpenDialog(parent);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File targetDir = chooser.getSelectedFile();
                model.getSponsorsByTrueIndex(selected).forEach(s ->
                        PDFCreator.writeBytesToPDFFile(PDFCreator.createPDFFromTemplate(s, EmailType.THANK_YOU, model),
                                targetDir, s, "_Dankesschreiben.pdf"));
                model.getSponsorsByTrueIndex(selected).forEach(s -> {});
                JOptionPane.showMessageDialog(parent, "Dankesschreiben wurden erfolgreich erstellt.",
                        "Info: ", JOptionPane.INFORMATION_MESSAGE);
                update();
            }
        });
        pdfSelection.addActionListener(e -> {
            if (getNumSelected() == 0) {
            displayInfo("Sie haben keine Sponsoren ausgewählt. " +
                    "Bitte markieren Sie alle Sponsoren die auf der Liste erscheinen sollen.", parent);
            return;
        }
            File f = chooseSaveLocation();
            if (f != null) {
                if (!f.getAbsolutePath().toLowerCase().endsWith(".pdf")) {
                    String filePath = f.getAbsolutePath() + ".pdf";
                    f = new File(filePath);
                }

                String[] text = model.getSponsorsByTrueIndex(selected).stream()
                        .map(Sponsor::listString)
                        .toArray(String[]::new);
                String header = Sponsor.listStringHeader();
                float[] format = Sponsor.listWidths();
                PDFCreator.createList(header, text, format, f, "");
                JOptionPane.showMessageDialog(parent, "Liste wurde erfolgreich erstellt.",
                        "Info: ", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        pdfUltimateSelection.addActionListener(e -> {
            if (getNumSelected() == 0) {
                displayInfo("Sie haben keine Sponsoren ausgewählt. " +
                        "Bitte markieren Sie alle Sponsoren die auf der Liste erscheinen sollen.", parent);
                return;
            }
            File f = chooseSaveLocation();
            if (f != null) {
                if (!f.getAbsolutePath().toLowerCase().endsWith(".pdf")) {
                    String filePath = f.getAbsolutePath() + ".pdf";
                    f = new File(filePath);
                }
                PDFUltimateList.createUltimateList(model.getSponsorsByTrueIndex(selected), f, model);

                JOptionPane.showMessageDialog(parent, "Liste wurde erfolgreich erstellt.",
                        "Info: ", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        pdfMenu.add(pdfBill);
        pdfMenu.add(pdfReminder);
        pdfMenu.add(pdfThankYou);
        pdfMenu.add(pdfSelection);
        pdfMenu.add(pdfUltimateSelection);

        JMenu edit = new JMenu("edit...");
        JMenuItem status = new JMenuItem("Status setzen");
        status.addActionListener(e1 -> {
            if (getNumSelected() == 0) {
            displayInfo("Sie haben keine Sponsoren ausgewählt. " +
                    "Bitte markieren Sie die Sponsoren für die Sie den Status verändern wollen.", parent);
            return;
        }
            // Options to display
            String[] options = {"Bezahlt", "Nicht Bezahlt", "Abbrechen"};

            // Show the option dialog
            int selectedOption = JOptionPane.showOptionDialog(parent,
                    "Bezahlstatus für ausgewählte Sponsoren setzen:",
                    "Bezahlstatus",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[2]);

            if (selectedOption == 0) {
                model.getSponsorsByTrueIndex(selected).forEach(s -> s.setPayed(true));
            } else if (selectedOption == 1) {
                model.getSponsorsByTrueIndex(selected).forEach(s -> s.setPayed(false));
            }
            update();
        });

        JMenuItem billId = new JMenuItem("Sponsor anhand Nr. aufrufen");
        billId.addActionListener(e1 -> {
            JTextField idField = new JTextField("");
            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Geben Sie die Nr. von der Rechnung ein:"));
            panel.add(idField);

            //just for focus to type away right away
            idField.addAncestorListener( new AncestorListener()
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
                    idField.requestFocusInWindow();
                }
            } );

            int result = JOptionPane.showConfirmDialog(parent, panel, "Sponsor anhand Nr. aufrufen",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                if (idField.getText().matches("[0-9]+")) {
                    Sponsor s;
                    if((s = model.getSponsorById(Integer.parseInt(idField.getText())))!= null) {
                        String[] options = {"Bezahlt", "Nicht Bezahlt", "Abbrechen"};

                        double totalFrank = s.getTotal(Currency.CHF);
                        double totalEur = s.getTotal(Currency.EUR);

                        boolean printChf = totalFrank > 0.009;
                        boolean printEur = totalEur > 0.009;
                        DecimalFormat df = new DecimalFormat("0.00");

                        StringBuilder msg =  new StringBuilder();
                        msg.append("Gefundener Sponsor: ").append(s.getTitle()).append(" ").append(s.getNameCellRender());
                        msg.append(". Mit Rechnungsbetrag: ");
                        if(printChf) {
                            msg.append("CHF ").append(df.format(s.getTotal(Currency.CHF)));
                        }
                        if(printEur) {
                            msg.append("     EUR ").append(df.format(s.getTotal(Currency.EUR)));
                        }
                        msg.append(". \nFür diesen Sponsor können Sie jetzt den Bezahlstatus setzen.");

                        // Show the option dialog
                        int selectedOption = JOptionPane.showOptionDialog(parent,
                                msg.toString(),
                                "Bezahlstatus",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[2]);

                        if (selectedOption == 0) {
                            s.setPayed(true);
                        } else if (selectedOption == 1) {
                            s.setPayed(false);
                        }
                        update();
                    } else {
                        displayInfo("Ein Sponsor mit der ID " + idField.getText() + " konnte nicht gefunden werden.", parent);
                    }
                } else {
                    displayInfo("Bitte geben Sie nur die Nr. ein, nicht den gesamten Verwendungszweck.", parent);
                }
                } else {
            }


        });

        edit.add(status);
        edit.add(billId);

        lowerMenu.add(selectMenu);
        lowerMenu.add(emailMenu);
        lowerMenu.add(pdfMenu);
        lowerMenu.add(edit);

        mainWindow.add(lowerMenu, BorderLayout.SOUTH);
        this.add(mainWindow);
    }

    private ButtonGroup getYesNoDontCareGroup() {
        JRadioButton yes = new JRadioButton("Ja");
        yes.setActionCommand("yes");
        JRadioButton no = new JRadioButton("Nein");
        no.setActionCommand("no");
        JRadioButton dontCare = new JRadioButton("Egal");
        dontCare.setActionCommand("dontCare");

        dontCare.setSelected(true);

        ButtonGroup group = new ButtonGroup();

        group.add(yes);
        group.add(no);
        group.add(dontCare);

        return group;
    }

    private JPanel getChoicePanel(String str, ButtonGroup bg) {
        JPanel selection = new JPanel(new GridLayout(0, 4));
        selection.add(new JLabel(str));
        for (Enumeration<AbstractButton> e = bg.getElements(); e.hasMoreElements();)
            selection.add(e.nextElement());

        return selection;
    }


    public void update() {
        Sponsor[] res = model.getAllSponsors().stream().toList().toArray(new Sponsor[0]);
        if (selected.length != res.length)
            selected = new boolean[res.length];
        list.setListData(res);
    }

    public boolean checkIndexSelected(int index) {
        return selected[index];
    }

    private int getNumSelected() {
        int trueCount = 0;
        for (boolean value : selected) {
            if (value)
                trueCount++;
        }
        return trueCount;
    }

    private void displayInfo(String message, JPanel parent) {
            JOptionPane.showMessageDialog(parent, message, "Info: ",
                    JOptionPane.INFORMATION_MESSAGE);
    }
    private JDialog createBlockingDialog(Frame parentFrame) {
        JDialog dialog = new JDialog(parentFrame, "Sende Emails...", true);
        JLabel text = new JLabel("Die Emails werden versendet, bitte warten.");
        dialog.setLayout(new BorderLayout());
        dialog.add(text, BorderLayout.CENTER);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setResizable(false);
        return dialog;
    }

    public File chooseSaveLocation() {
        JFileChooser fileChooser= new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF", ".pdf", "pdf");
        fileChooser.setDialogTitle("Speichern Unter");
        fileChooser.setFileFilter(filter);

        int returnVal = fileChooser.showSaveDialog(this);

        if ( returnVal == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            return null;
        }
    }
}
