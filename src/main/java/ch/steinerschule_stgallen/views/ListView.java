package ch.steinerschule_stgallen.views;

import ch.steinerschule_stgallen.controller.ListController;
import ch.steinerschule_stgallen.util.Action;
import ch.steinerschule_stgallen.util.ListType;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

public class ListView extends JPanel {

    private final JLabel heading;
    private final JButton addButton;
    private final JButton editButton;
    private final JButton removeButton;
    private final JList<String> list;

    public ListView(String headingText, String buttonText, ListController controller, ListType type) {
        super();
        this.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setLayout(new GridLayout(2,1));

        JPanel inception = new JPanel();
        inception.setLayout(new FlowLayout());
        heading = new JLabel(headingText);
        JTextField search = new JTextField("");
        search.setColumns(15);
        inception.add(new JLabel("Suchen: "));
        inception.add(search);
        top.add(heading);
        top.add(inception);
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                controller.listSearchChanged(type, search.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                controller.listSearchChanged(type, search.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
        this.add(top, BorderLayout.NORTH);

        list = new JList<>();
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                    controller.listItemSelected(type, list.getSelectedIndex());
                    editButton.setEnabled(list.getSelectedIndex() != -1);
                    removeButton.setEnabled(list.getSelectedIndex() != -1);
            }
        });

        addButton = new JButton(buttonText + " Hinzufügen");
        addButton.setEnabled(false);
        editButton = new JButton(buttonText + " Bearbeiten");
        editButton.setEnabled(false);
        removeButton = new JButton(buttonText + " Löschen");
        removeButton.setEnabled(false);

        JPanel bottom = new JPanel();
        //bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        //bottom.setLayout(new FlowLayout());
        bottom.setLayout(new GridLayout(3,1));
        bottom.add(addButton);
        bottom.add(editButton);
        bottom.add(removeButton);
        this.add(bottom, BorderLayout.SOUTH);

        addButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                controller.buttonPressed(type, Action.ADD);
            }
        });
        editButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                controller.buttonPressed(type, Action.EDIT);
            }
        });
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                controller.buttonPressed(type, Action.DELETE);
            }
        });

        JScrollPane scrollPaneCenter = new JScrollPane();
        scrollPaneCenter.setViewportView(list);
        this.add(scrollPaneCenter, BorderLayout.CENTER);
    }

    public void setListContent(String[] content) {
        list.setListData(content);
        if(content.length == 0) {
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
        }
    }

    public void setHeading(String heading) {
        this.heading.setText(heading);
    }

    public void setSelectedIndex(int index) {
        list.setSelectedIndex(index);
    }

    public void setAddButtonEnabled(boolean enabled) {
        this.addButton.setEnabled(enabled);
    }
    public void setEditButtonEnabled(boolean enabled) {
        this.editButton.setEnabled(enabled);
    }
    public void setRemoveButtonEnabled(boolean enabled) {
        this.removeButton.setEnabled(enabled);
    }
}
