package ch.steinerschule_stgallen.views;

import ch.steinerschule_stgallen.model.Sponsor;
import ch.steinerschule_stgallen.model.StudentClass;
import ch.steinerschule_stgallen.util.Currency;
import ch.steinerschule_stgallen.util.ListCellRenderCompatible;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * custom list cell render for display in analysis view
 */
public class AnalysisCellRender implements ListCellRenderer<ListCellRenderCompatible> {

    private final DecimalFormat df = new DecimalFormat("0.00");
    AnalysisView creator;
    public AnalysisCellRender(AnalysisView creator) {
        this.creator = creator;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ListCellRenderCompatible> list, ListCellRenderCompatible value, int index, boolean isSelected, boolean cellHasFocus) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1,7));
        panel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel name = new JLabel(value.getNameCellRender());
        name.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(name);
        JLabel chf = new JLabel(df.format(value.getTotal(Currency.CHF)));
        chf.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(chf);
        JLabel chfOpen = new JLabel(df.format(value.getOpenTotal(Currency.CHF)));
        chfOpen.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(chfOpen);
        JLabel eur = new JLabel(df.format(value.getTotal(Currency.EUR)));
        eur.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(eur);
        JLabel eurOpen = new JLabel(df.format(value.getOpenTotal(Currency.EUR)));
        eurOpen.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(eurOpen);
        JLabel total = new JLabel(df.format(value.getTotalCombinedCHF()));
        total.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(total);
        JLabel totalOpen = new JLabel(df.format(value.getTotalCombinedCHFOpen()));
        totalOpen.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(totalOpen);

        return panel;
    }
}
