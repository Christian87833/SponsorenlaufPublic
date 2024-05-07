package ch.steinerschule_stgallen.model;

import ch.steinerschule_stgallen.util.CustomUncaughtExceptionHandler;
import ch.steinerschule_stgallen.views.View;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        System.out.println("hello");
        CustomUncaughtExceptionHandler exceptionHandler = new CustomUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
        System.out.println("still there?");

        try {
            // Set System L&F
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException |
               IllegalAccessException e) {
            System.out.println(e);
        }
        System.out.println("and now?");

        Model model = new Model();
        View v = new View(model);
        exceptionHandler.registerView(v);
    }

    /**
     * Id Handling for Saving in JSON format, also used for Bill Number.
     */
    private static int nextId = 0;
    public static int getNextId() {
        nextId ++;
        return nextId;
    }

    public static void updateMinId(int id) {
        if (id > nextId)
            nextId = id;
    }
}