package ch.steinerschule_stgallen.controller;

import ch.steinerschule_stgallen.util.Action;
import ch.steinerschule_stgallen.util.ListType;

public interface ListController {
    void buttonPressed(ListType type, Action action);
    void listItemSelected(ListType type, int index);
    void listSearchChanged(ListType type, String query);
    boolean saveAs();
    void load();
}

