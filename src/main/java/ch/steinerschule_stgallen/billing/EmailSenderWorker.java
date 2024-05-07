package ch.steinerschule_stgallen.billing;

import ch.steinerschule_stgallen.model.Model;
import ch.steinerschule_stgallen.model.Sponsor;
import ch.steinerschule_stgallen.util.EmailType;
import ch.steinerschule_stgallen.views.LoadingBar;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Send emails to list of Sponsors in a separate thread. Publish progress for use in status bar
 */
public class EmailSenderWorker extends SwingWorker<List<Sponsor>, Integer> {
    private final List<Sponsor> sponsors;
    private final LoadingBar progressBar;

    private final EmailType emailType;

    private final Model model;


    /**
     *
     * @param model needed for reference to pass on to other class
     * @param sponsors to receive emails
     * @param progressBar to publish progress to
     * @param emailType decides what template to use
     */
    public EmailSenderWorker(Model model, List<Sponsor> sponsors, LoadingBar progressBar, EmailType emailType) {
        this.sponsors = sponsors;
        this.progressBar = progressBar;
        this.emailType = emailType;
        this.model = model;
    }

    /**
     * Sends Emails to sponsors
     * @return list of sponsors for which email sending failed
     */
    @Override
    protected List<Sponsor> doInBackground() {
        System.out.println("and now now?");

        EmailService es;
        LinkedList<Sponsor> failedToSend = new LinkedList<>();

        try {
            es = new EmailService();
            int totalEmails = sponsors.size();
            int progress;
            for (int i = 0; i < totalEmails; i++) {
                Sponsor s = sponsors.get(i);
                if(s.getEmail().isEmpty()) {
                    failedToSend.add(s);
                }
                if(es.sendEmail(s, emailType, model)){
                    if(emailType == EmailType.BILL) {
                        s.setEmailSent(true);
                    } else if (emailType == EmailType.REMINDER) {
                        s.setReminded(true);
                    } else {
                        //Dankesschreiben
                        s.setSendThankYou(true);
                    }
                } else {
                    failedToSend.add(s);
                }
                // Update the progress
                progress = i;
                publish(progress);
            }
        }
        catch (NoKeysException e) {
            int totalEmails = sponsors.size();
            int progress;
            for (int i = 0; i < totalEmails; i++) {
                failedToSend.add(sponsors.get(i));
                // Update the progress
                progress = i;
                publish(progress);
            }
        }
            return failedToSend;
    }

    /**
     * Updates progress bar
     * @param chunks intermediate results to process
     *
     */
    @Override
    protected void process(List<Integer> chunks) {
        // Update the progress bar with the latest progress value
        int progress = chunks.get(chunks.size() - 1);
        progressBar.updateProgress(progress);
    }

    /**
     * Calls the progress bar method to display the list of sponsors for which the email could not be sent
     */
    @Override
    protected void done() {
        // The email sending process is complete
        try {
            progressBar.doneLoading(get()); //get() returns the list of failed emails from worker thread
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

    }
}
