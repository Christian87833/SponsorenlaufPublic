package ch.steinerschule_stgallen.views;

import ch.steinerschule_stgallen.model.Sponsor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class LoadingBar extends JDialog {

    private final JProgressBar progressBar;
    private final JButton close;
    private final Frame parentFrame;
    private final JDialog dialog;
    private final JList<String> notSentList;
    private final JPanel cards;
    private final JLabel progressText;

    private final int maxValueProgressBar;


    public LoadingBar (Frame parentFrame, int maxValueProgressBar) {
        super(parentFrame, "Sende Emails...", true);
        this.maxValueProgressBar = maxValueProgressBar;
        this.parentFrame = parentFrame;

        progressBar = new JProgressBar(0, 100);
        close = new JButton("Schließen");
        close.setEnabled(false);
        dialog = this;
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        JPanel notSent = new JPanel(new BorderLayout());
        JScrollPane listPane = new JScrollPane();
        notSentList = new JList<>();
        listPane.setViewportView(notSentList);
        notSent.add(new JLabel("Email senden fehlgeschlagen für folgende Sponsoren:"),
                BorderLayout.NORTH);
        notSent.add(listPane, BorderLayout.CENTER);

        JPanel progressUpdates = new JPanel(new GridLayout(0,1));
        progressUpdates.add(new JLabel("Die Emails werden versendet, bitte warten."));
        progressUpdates.add(progressBar);
        progressText = new JLabel("0");
        progressUpdates.add(progressText);
        JPanel allGood = new JPanel(new GridLayout(0,1));
        allGood.add(new JLabel("Alle Emails erfolgreich versendet."));

        cards = new JPanel(new CardLayout());
        cards.add(progressUpdates, "progress");
        cards.add(notSent, "notSent");
        cards.add(allGood, "allGood");

        dialog.setLayout(new BorderLayout());
        dialog.add(cards, BorderLayout.CENTER);

        dialog.add(close, BorderLayout.SOUTH);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setResizable(false);
    }

    public void doneLoading(List<Sponsor> failedToSend) {
        progressBar.setValue(100);
        parentFrame.setEnabled(true);
        close.setEnabled(true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        if(!failedToSend.isEmpty()) {
            String[] res = failedToSend.stream().map(sponsor -> {
                        return sponsor.getTitle() + " " + sponsor.getSurname() + " mit Email: " + sponsor.getEmail();
                    })
                    .toList().toArray(new String[0]);
            notSentList.setListData(res);
            CardLayout cl = (CardLayout)(cards.getLayout());
            cl.show(cards, "notSent");
        } else {
            CardLayout cl = (CardLayout)(cards.getLayout());
            cl.show(cards, "allGood");
        }
    }

    public void updateProgress(int progress) {
        progressBar.setValue((int) (((double) progress / maxValueProgressBar) * 100));
        progressText.setText(progress + " von " + maxValueProgressBar + " Emails versendet.");
    }
}
