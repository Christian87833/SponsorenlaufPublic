package ch.steinerschule_stgallen.views;

import ch.steinerschule_stgallen.billing.PDFCreator;
import ch.steinerschule_stgallen.model.Model;
import ch.steinerschule_stgallen.model.Sponsor;
import ch.steinerschule_stgallen.model.Student;
import ch.steinerschule_stgallen.model.StudentClass;
import ch.steinerschule_stgallen.util.Currency;
import ch.steinerschule_stgallen.util.CONSTANTS;
import ch.steinerschule_stgallen.util.ListCellRenderCompatible;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

public class AnalysisView extends JPanel {

    private JLabel totalEUR;
    private JLabel openEUR;
    private JLabel gotEUR;
    private JLabel openCHF;
    private JLabel gotCHF;
    private JLabel totalCHF;

    JLabel sum;

    private Model model;
    private JList<ListCellRenderCompatible> list;
    private JComboBox<String> selection;

    public AnalysisView(Model model) {
        this.model = model;
        this.setLayout(new BorderLayout());


        JPanel center = new JPanel();
        center.setLayout(new BorderLayout());
        JPanel inc = new JPanel();
        inc.setLayout(new GridLayout(1,7));
        String[] selChoices = { "Klasse", "Schüler", "Sponsoren" };
        selection = new JComboBox<String>(selChoices);
        selection.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateView();
            }
        });
        inc.add(selection);
        JLabel chf = new JLabel("CHF");
        chf.setHorizontalAlignment(SwingConstants.RIGHT);
        inc.add(chf);
        JLabel chfOpen = new JLabel("davon offen");
        chfOpen.setHorizontalAlignment(SwingConstants.RIGHT);
        inc.add(chfOpen);
        JLabel eur = new JLabel("EUR");
        eur.setHorizontalAlignment(SwingConstants.RIGHT);
        inc.add(eur);
        JLabel eurOpen = new JLabel("davon offen");
        eurOpen.setHorizontalAlignment(SwingConstants.RIGHT);
        inc.add(eurOpen);
        sum = new JLabel("(EUR * " + CONSTANTS.exchangeRate + " + CHF)");
        sum.setHorizontalAlignment(SwingConstants.RIGHT);
        inc.add(sum);
        JLabel sumOpen = new JLabel("davon offen");
        sumOpen.setHorizontalAlignment(SwingConstants.RIGHT);
        inc.add(sumOpen);

        JPanel bottom = new JPanel();
        bottom.setLayout(new GridLayout(2,7));
        JButton exportPDF = new JButton("Ansicht als PDF speichern");
        JButton setExchangeRate = new JButton("Wechselkurs anpassen");
        AnalysisView parent = this;

        setExchangeRate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String rate = JOptionPane.showInputDialog(parent, "Wechselkurs: EUR * X = CHF\nWert für X:",
                        CONSTANTS.exchangeRate);
                if(rate != null && !rate.isEmpty()) {
                    try {
                        CONSTANTS.exchangeRate = Double.parseDouble(rate);
                        updateView();
                    } catch (NumberFormatException x) {
                        //dont change the exchange rate
                        JOptionPane.showMessageDialog(parent, "Fehlerhafte Eingabe.", "Info: ",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
        exportPDF.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File f = chooseSaveLocation();
                if (f != null) {
                    if (!f.getAbsolutePath().toLowerCase().endsWith(".pdf")) {
                        String filePath = f.getAbsolutePath() + ".pdf";
                        f = new File(filePath);
                    }
                    String col1 = "";
                    switch (selection.getSelectedIndex()) {
                        case 0 -> // Klassen
                                col1 = "Klasse";
                        case 1 -> // Schüler
                                col1 = "Schüler";
                        case 2 -> // Sponsoren
                                col1 = "Sponsor";
                    }
                    DecimalFormat df = new DecimalFormat("0.00");
                    String header = col1 + "\tCHF" + "\tEUR" + "\tSumme CHF (EUR * " + CONSTANTS.exchangeRate + " + CHF)";
                    float[] format = {1f, 0.5f, 0.5f, 0.5f};
                    String[] text = {};
                    switch (selection.getSelectedIndex()) {
                        case 0 -> // Klassen
                                text = model.getClassesListSortedByTotalIncome().stream().map(
                                        c -> c.getNameCellRender() + "\t" + df.format(c.getTotal(Currency.CHF)) + "\t"
                                                + df.format(c.getTotal(Currency.EUR)) + "\t" + df.format(c.getTotalCombinedCHF())
                                ).toArray(String[]::new);
                        case 1 -> // Schüler
                                text = model.getStudentListSortedByTotalIncome().stream().map(
                                        c -> c.getNameCellRender() + "\t" + df.format(c.getTotal(Currency.CHF)) + "\t"
                                                + df.format(c.getTotal(Currency.EUR)) + "\t" + df.format(c.getTotalCombinedCHF())
                                ).toArray(String[]::new);
                        case 2 -> // Sponsoren
                                text = model.getSponsorListSortedByTotalIncome().stream().map(
                                        c -> c.getNameCellRender() + "\t" + df.format(c.getTotal(Currency.CHF)) + "\t"
                                                + df.format(c.getTotal(Currency.EUR)) + "\t" + df.format(c.getTotalCombinedCHF())
                                ).toArray(String[]::new);
                    }
                    String total = "Grand Total EUR: " + totalEUR.getText() + "   Grand Total CHF: " + totalCHF.getText();
                    PDFCreator.createList(header, text, format, f, total);
                    JOptionPane.showMessageDialog(parent, "Liste wurde erfolgreich erstellt.",
                            "Info: ", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        totalEUR = new JLabel("0");
        openEUR = new JLabel("0");
        gotEUR = new JLabel("0");

        totalCHF = new JLabel("0");
        openCHF = new JLabel("0");
        gotCHF = new JLabel("0");

        bottom.add(new JLabel("Grand Total EUR: "));
        bottom.add(totalEUR);
        bottom.add(new JLabel("Eingenommen EUR: "));
        bottom.add(gotEUR);
        bottom.add(new JLabel("Ausstehend EUR: "));
        bottom.add(openEUR);
        bottom.add(exportPDF);

        bottom.add(new JLabel("Grand Total CHF: "));
        bottom.add(totalCHF);
        bottom.add(new JLabel("Eingenommen CHF: "));
        bottom.add(gotCHF);
        bottom.add(new JLabel("Ausstehend CHF: "));
        bottom.add(openCHF);
        bottom.add(setExchangeRate);
        this.add(bottom, BorderLayout.SOUTH);

        //inc.add(exportPDF);
        list = new JList<>();
        list.setCellRenderer(new AnalysisCellRender(this));
        JScrollPane scrollPaneCenter = new JScrollPane();
        scrollPaneCenter.setViewportView(list);
        center.add(inc, BorderLayout.NORTH);
        center.add(scrollPaneCenter, BorderLayout.CENTER);
        this.add(center, BorderLayout.CENTER);
    }

    public void refresh() {
        double totEUR = model.calculateGrandTotal(Currency.EUR);
        double opEUR = model.calculateOpenTotal(Currency.EUR);

        double totCHF = model.calculateGrandTotal(Currency.CHF);
        double opCHF = model.calculateOpenTotal(Currency.CHF);

        totalEUR.setText(Double.toString(totEUR));
        totalCHF.setText(Double.toString(totCHF));
        openEUR.setText(Double.toString(opEUR));
        gotEUR.setText(Double.toString(totEUR - opEUR));
        openCHF.setText(Double.toString(opCHF));
        gotCHF.setText(Double.toString(totCHF - opCHF));
        updateView();
    }

    public void showClassHighScores() {
        StudentClass[] res = model.getClassesListSortedByTotalIncome().stream().toList().toArray(new StudentClass[0]);
        list.setListData(res);
    }

    public void showStudentHighScores() {
        Student[] res = model.getStudentListSortedByTotalIncome().stream().toList().toArray(new Student[0]);
        list.setListData(res);
    }

    public void showSponsorHighScores() {
        Sponsor[] res = model.getSponsorListSortedByTotalIncome().stream().toList().toArray(new Sponsor[0]);
        list.setListData(res);
    }

    private void updateView(){
        sum.setText("(EUR * " + CONSTANTS.exchangeRate + " + CHF)");
        switch (selection.getSelectedIndex()) {
            case 0 -> // Klassen
                    showClassHighScores();
            case 1 -> // Schüler
                    showStudentHighScores();
            case 2 -> // Sponsoren
                    showSponsorHighScores();
        }
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
