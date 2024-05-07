package ch.steinerschule_stgallen.views;

import ch.steinerschule_stgallen.model.Sponsor;
import ch.steinerschule_stgallen.views.SendBillsView;

import javax.swing.*;
import java.awt.*;

public class CheckboxListCellRenderer extends JCheckBox implements ListCellRenderer<Sponsor> {

    SendBillsView creator;
    public CheckboxListCellRenderer(SendBillsView creator) {
        this.creator = creator;
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Sponsor> list, Sponsor sponsor, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        setText(sponsor.toString());
        setSelected(creator.checkIndexSelected(index));
        setBackground(creator.checkIndexSelected(index) ? list.getSelectionBackground() : list.getBackground());
        setForeground(creator.checkIndexSelected(index) ? list.getSelectionForeground() : list.getForeground());

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(this, BorderLayout.CENTER);


        JPanel allStatus = new JPanel();
        allStatus.setLayout(new GridLayout(1,5));

        JLabel statusThanks = new JLabel();
        statusThanks.setText("<html><body width='100px'>Dank gesendet</body></html>");
        JLabel statusEMail = new JLabel();
        statusEMail.setText("<html><body width='100px'>Rechnung gesendet</body></html>");
        JLabel statusReminder = new JLabel();
        statusReminder.setText("<html><body width='100px'>Erinnerung gesendet</body></html>");
        JLabel statusPDF = new JLabel();
        statusPDF.setText("<html><body width='100px'>PDF erstellt</body></html>");
        JLabel statusPayed = new JLabel();
        statusPayed.setText("<html><body width='100px'>Bezahlt</body></html>");


        if (sponsor.isSentThankYou()) {
            statusThanks.setIcon(createCircleIcon(Color.GREEN));
        } else {
            statusThanks.setIcon(createCircleIcon(Color.GRAY));
        }
        if (sponsor.isEmailSent()) {
            statusEMail.setIcon(createCircleIcon(Color.GREEN));
        } else {
            statusEMail.setIcon(createCircleIcon(Color.GRAY));
        }
        if (sponsor.isReminded()){
            statusReminder.setIcon(createCircleIcon(Color.GREEN));
        } else {
            statusReminder.setIcon(createCircleIcon(Color.GRAY));
        }
        if (sponsor.isCreatedPDF()) {
            statusPDF.setIcon(createCircleIcon(Color.GREEN));
        } else {
            statusPDF.setIcon(createCircleIcon(Color.GRAY));
        }
        if (sponsor.isPayed()) {
            statusPayed.setIcon(createCircleIcon(Color.GREEN));
        } else {
            statusPayed.setIcon(createCircleIcon(Color.GRAY));
        }

        statusThanks.setBackground(creator.checkIndexSelected(index) ? list.getSelectionBackground() : list.getBackground());
        statusThanks.setOpaque(true);

        statusEMail.setBackground(creator.checkIndexSelected(index) ? list.getSelectionBackground() : list.getBackground());
        statusEMail.setOpaque(true);

        statusReminder.setBackground(creator.checkIndexSelected(index) ? list.getSelectionBackground() : list.getBackground());
        statusReminder.setOpaque(true);

        statusPDF.setBackground(creator.checkIndexSelected(index) ? list.getSelectionBackground() : list.getBackground());
        statusPDF.setOpaque(true);

        statusPayed.setBackground(creator.checkIndexSelected(index) ? list.getSelectionBackground() : list.getBackground());
        statusPayed.setOpaque(true);

        allStatus.add(statusThanks);
        allStatus.add(statusEMail);
        allStatus.add(statusReminder);
        allStatus.add(statusPDF);
        allStatus.add(statusPayed);


        panel.add(allStatus, BorderLayout.EAST);

        return panel;
    }

    private Icon createCircleIcon(Color color) {
        int size = 12; // Adjust the size as needed
        return new ColorCircleIcon(color, size);
    }

    private record ColorCircleIcon(Color color, int size) implements Icon {

        @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(color);
                g.fillOval(x, y, size - 1, size - 1);
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        }
}
