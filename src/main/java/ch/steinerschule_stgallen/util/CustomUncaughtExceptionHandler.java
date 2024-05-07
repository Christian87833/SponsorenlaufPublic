package ch.steinerschule_stgallen.util;

import ch.steinerschule_stgallen.views.View;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger LOGGER = Logger.getLogger(CustomUncaughtExceptionHandler.class.getName());
    private static final String LOG_FILE = "log.txt";

    private View view;

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            // Write the exception details to the log file.
            writer.println("Uncaught exception in thread " + t.getName() + ":");
            e.printStackTrace(writer);
            if(view != null) {
                view.displayInfo("Ups, es ist leider etwas schief gelaufen. Der Fehler wurde in einem " +
                        "LOG_FILE dokumentiert. Bitte heben Sie diese Datei auf, und wenden Sie sich an den " +
                        "Produktentwickler. ");
            }
        } catch (IOException ioException) {
            // If an error occurs while writing to the log file, fallback to console output.
            LOGGER.log(Level.SEVERE, "Error writing to the log file.", ioException);
            System.err.println("Uncaught exception in thread " + t.getName() + ":");
            e.printStackTrace();
            if(view != null) {
                view.displayInfo("Ups, es ist leider etwas schief gelaufen: " + e.getMessage() + "\nBitte " +
                        "wenden Sie sich an den Produktentwickler.");
            }
        }
    }

    public void registerView(View view) {
        this.view = view;
    }
}

