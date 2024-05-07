package ch.steinerschule_stgallen.model;

import ch.steinerschule_stgallen.util.Currency;
import ch.steinerschule_stgallen.util.CONSTANTS;

import ch.steinerschule_stgallen.util.ListCellRenderCompatible;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;

public class StudentClass implements ListCellRenderCompatible {
    private final LinkedList<Student> students;
    private String name;

    public StudentClass(String name) {
        this.students = new LinkedList<>();
        this.name = name;
    }

    @JsonCreator
    public StudentClass(@JsonProperty("name")String name,
                        @JsonProperty("students")LinkedList<Student> students) {
        this.students = students;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public LinkedList<Student> getStudents() {
        return students;
    }

    @Override
    public String toString(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public double getTotal(Currency curr) {
        return students.stream().mapToDouble(student -> student.getTotal(curr)).sum();
    }

    /**
     *
     * @return the combined total of Franks and Euro in Franks. Using exchange rate from CONSTANTS
     */
    @JsonIgnore
    public double getTotalCombinedCHF() {
        return students.stream().mapToDouble(student -> student.getTotal(Currency.CHF) +
                (CONSTANTS.exchangeRate * student.getTotal(Currency.EUR))).sum();
    }

    @JsonIgnore
    @Override
    public double getTotalCombinedCHFOpen() {
        return students.stream().mapToDouble(student -> student.getOpenTotal(Currency.CHF) +
                (CONSTANTS.exchangeRate * student.getOpenTotal(Currency.EUR))).sum();
    }

    @JsonIgnore
    @Override
    public double getOpenTotal(Currency cur) {
        return students.stream().mapToDouble(student -> student.getOpenTotal(cur)).sum();
    }

    @JsonIgnore
    @Override
    public String getNameCellRender() {
        return name;
    }

    @JsonIgnore
    public String getHighScoreString(Currency curr) {
        return "Die Klasse: " + name + " hat Einnahmen in der Höhe von "
                + getTotal(curr) + " " + curr + " erzielt";
    }
}
