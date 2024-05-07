package ch.steinerschule_stgallen.controller;

import ch.steinerschule_stgallen.model.Model;
import ch.steinerschule_stgallen.util.Action;
import ch.steinerschule_stgallen.util.ListType;
import ch.steinerschule_stgallen.views.View;

import java.io.File;

/**
 * ch.steinerschule_stgallen.controller.Controller from a ch.steinerschule_stgallen.model.Model, ch.steinerschule_stgallen.views.View, ch.steinerschule_stgallen.controller.Controller architecture.
 * Implementing callback functions for the view to register events.
 */
public class Controller implements ListController {

    Model model;
    View view;

    /**
     * ch.steinerschule_stgallen.controller.Controller for a model, view, controller architecture.
     * @param model of the architecture
     * @param view of the architecture
     */
    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
    }

    /**
     * Method to be invoked by action events in the view. Identifies the source of the event
     * and calls functions of the model and view to update them according to the action requested
     * @param type enum of what column from the view the event came from
     * @param action enum of what button from that column the event came from {add, edit, delete}
     */
    @Override
    public void buttonPressed(ListType type, Action action) {
        switch (type) {
            case CLASSES:
                switch (action) {
                    case ADD:
                        if(view.showNewClassDialog())
                            view.updateClassView();
                        break;
                    case EDIT:
                        if(view.showUpdateClassDialog())
                            view.updateClassView();
                        break;
                    case DELETE:
                        if(view.confirmWindow("Ausgewählte Gruppe einschließlich ihrer Schüler und Sponsorenschaften " +
                                "Löschen? ")) {
                            model.deleteSelectedClass();
                            view.updateClassView();
                            view.updateStudentsView();
                            view.updateSponsorshipsView();
                        }

                        break;
                }

                break;
            case STUDENTS:
                switch (action) {
                    case ADD:
                        if(model.selectedClass != null) {
                            if (view.showNewStudentDialog())
                                view.updateStudentsView();
                        }
                        else
                            view.displayInfo("Wählen Sie erst die Klasse zu der Sie einen Schüler hinzufügen möchten");
                        break;
                    case EDIT:
                        if(view.showUpdateStudentDialog()) {
                            view.updateStudentsView();
                        }
                        break;
                    case DELETE:
                        if(view.confirmWindow("Ausgewählten Schüler einschließlich seiner Sponsorenschaften Löschen? ")) {
                            model.deleteSelectedStudent();
                            view.updateStudentsView();
                            view.updateSponsorshipsView();
                        }
                        break;
                }
                break;
            case SPONSORSHIP:
                switch (action) {
                    case ADD:
                        if(model.selectedStudent != null && model.selectedSponsor != null) {
                            if (view.showNewSponsorshipDialog()) {
                                view.updateSponsorshipsView();
                            }
                        } else
                            view.displayInfo("Wählen Sie erst einen Schüler und einen Sposor, die" +
                                    "die eine Sponsorenschaft eingehen sollen.");
                        break;
                    case EDIT:
                        if (view.showUpdateSponsorshipDialog()) {
                            view.updateSponsorshipsView();
                        }
                        break;
                    case DELETE:
                        if(view.confirmWindow("Ausgewählte Sponsorenschaft Löschen?")) {
                            model.deleteSelectedSponsorship();
                            view.updateSponsorshipsView();
                        }
                        break;
                }
                break;
            case SPONSORS:
                switch (action) {
                    case ADD:
                        if(view.showNewSponsorDialog()) {
                            view.updateSponsorsView();
                        }
                        break;
                    case EDIT:
                        if(view.showUpdateSponsorDialog()) {
                            view.updateSponsorsView();
                            view.updateSponsorshipsView();
                        }
                        break;
                    case DELETE:
                        if(view.confirmWindow("Ausgewählten ch.steinerschule_stgallen.model.Sponsor einschließlich aller Sponsorenschaften Löschen?")) {
                            model.deleteSelectedSponsor();
                            view.updateSponsorsView();
                            view.updateSponsorshipsView();
                        }
                        break;
                }
        }
    }

    /**
     * Method to be invoked if the selection in any column changed.
     * Calls the according method in the model, and updates the view.
     * @param type to determine in what column the index changed
     * @param index is the new selected index
     */
    @Override
    public void listItemSelected(ListType type, int index) {
        switch (type) {
            case CLASSES -> {
                model.classSelected(index);
                view.updateStudentsView();
            }
            case STUDENTS -> {
                model.studentSelected(index);
                view.updateSponsorshipsView();
            }
            // the following do not require a view update, as their selection does
            // not influence what is displayed.
            case SPONSORS -> model.sponsorSelected(index);
            case SPONSORSHIP -> model.sponsorshipSelected(index);
        }
    }

    /**
     * Method to be invoked if the search terms in any column changed
     * @param type to identify from what column the search request originated
     * @param query the new search query from that column
     */
    @Override
    public void listSearchChanged(ListType type, String query) {
        switch (type) {
            case CLASSES -> {
                model.setClassQuery(query);
                view.updateClassView();
            }
            case STUDENTS -> {
                model.setStudentQuery(query);
                view.updateStudentsView();
            }
            case SPONSORS -> {
                model.setSponsorQuery(query);
                view.updateSponsorsView();
            }
            case SPONSORSHIP -> {
                model.setSponsorshipQuery(query);
                view.updateSponsorshipsView();
            }
        }
    }

    /**
     * To be called if the save button was pressed
     * Calls the view to open a dialog to choose the save location and
     * forwards the potential result to the model.
     */
    @Override
    public boolean saveAs() {
        File saveTo = view.chooseSaveLocation();
        if (saveTo != null) {
            if (!saveTo.getAbsolutePath().endsWith(".json")) {
                saveTo = new File(saveTo + ".json");
            }
            model.testSave(saveTo);
            return true;
        } else {
            return false;
        }
    }

    public void save() {
        if (model.getCurrentFile() == null) {
            saveAs();
        } else {
        File saveTo = model.getCurrentFile();
            if (!saveTo.getAbsolutePath().endsWith(".json")) {
                saveTo = new File(saveTo + ".json");
            }
            model.testSave(saveTo);
        }
    }

    /**
     * to be called if the load button was pressed
     * Calls the view to open a dialog to choose the file to load from and
     * forwards the potential selection to the model.
     */
    @Override
    public void load() {
        File loadFrom = view.chooseOpenLocation();
        if(loadFrom != null) {
            model.testLoad(loadFrom);
            view.refreshAll();

        }

    }
}
