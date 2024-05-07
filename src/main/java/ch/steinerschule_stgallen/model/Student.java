package ch.steinerschule_stgallen.model;

import ch.steinerschule_stgallen.util.Currency;
import ch.steinerschule_stgallen.util.CONSTANTS;
import ch.steinerschule_stgallen.util.ListCellRenderCompatible;
import com.fasterxml.jackson.annotation.*;

import java.util.LinkedList;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Student implements ListCellRenderCompatible {

    private final int id;

    private String name;
    private String surname;
    private String telNumber;

    private int lnumber;

    private LinkedList<Sponsorship> sponsorships;

    private int lapCount;

    public Student(String name, String surname, String tel, int lapCount, int lnumber) {
        this.name = name;
        this.surname = surname;
        this.lapCount = lapCount;
        this.telNumber = tel;
        this.lnumber = lnumber;
        sponsorships = new LinkedList<>();
        this.id = Main.getNextId();
    }

    @JsonCreator
    public Student(@JsonProperty("id")int id,
                   @JsonProperty("name")String name,
                   @JsonProperty("surname")String surname,
                   @JsonProperty("telNumber")String telNumber,
                   @JsonProperty("lapCount")int lapCount,
                   @JsonProperty("lnumber")int lnumber,
                   @JsonProperty("sponsorships") LinkedList<Sponsorship> sponsorships) {
        this.id = id;
        Main.updateMinId(id);
        this.name = name;
        this.surname = surname;
        this.telNumber = telNumber;
        this.lapCount = lapCount;
        this.lnumber = lnumber;
        this.sponsorships = sponsorships == null ? new LinkedList<>() : sponsorships;
        LoadedStructure.idToStudent.put(id, this);
    }

    public LinkedList<Sponsorship> getSponsorships() {
        return sponsorships;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public int getLNumber() {
        return lnumber;
    }

    public int getLapCount() {
        return lapCount;
    }

    public String getTelNumber() {
        return telNumber;
    }

    public void setTelNumber(String telNumber) {
        this.telNumber = telNumber;
    }

    public void setLnumber(int lnumber) {
        this.lnumber = lnumber;
    }

    public void setLapCount(int lapCount) {
        this.lapCount = lapCount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void addSponsorship(Sponsorship sponsorship) {
        sponsorships.add(sponsorship);
    }
    public void reRegisterSponsorship(Sponsorship sponsorship) {
        sponsorship.reRegisterStudent(this);
    }

    public void deleteSponsorship(Sponsorship sponsorship) {
        sponsorships.remove(sponsorship);
    }

    public void delete() {
        sponsorships.forEach(sponsorship -> sponsorship.getSponsor().deleteSponsorship(sponsorship));
    }
    @JsonIgnore
    public double getTotal(Currency curr){
        return sponsorships.stream()
                .filter(sponsorship -> sponsorship.getCurrency() == curr)
                .mapToDouble(Sponsorship::getTotal)
                .sum();
    }

    @JsonIgnore
    public double getOpenTotal(Currency curr){
        return sponsorships.stream()
                .filter(sponsorship -> sponsorship.getCurrency() == curr && !sponsorship.getSponsor().isPayed())
                .mapToDouble(Sponsorship::getTotal)
                .sum();
    }

    @JsonIgnore
    @Override
    public double getTotalCombinedCHF() {
        return getTotal(Currency.CHF) + (getTotal(Currency.EUR) * CONSTANTS.exchangeRate);
    }

    @JsonIgnore
    @Override
    public double getTotalCombinedCHFOpen() {
        return getOpenTotal(Currency.CHF) + (getOpenTotal(Currency.EUR) * CONSTANTS.exchangeRate);
    }

    @JsonIgnore
    @Override
    public String getNameCellRender() {
        return name + " " + surname;
    }

    @Override
    public String toString(){

        return name + " " + surname + ", Start-Nr.: " + lnumber + ", Runden: " + lapCount;
    }


}
