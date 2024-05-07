package ch.steinerschule_stgallen.model;

import ch.steinerschule_stgallen.util.Currency;
import ch.steinerschule_stgallen.util.CONSTANTS;
import ch.steinerschule_stgallen.util.ListCellRenderCompatible;
import com.fasterxml.jackson.annotation.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.LinkedList;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Sponsor implements ListCellRenderCompatible {

    private final int id;
    private String title;
    private String name;
    private String surname;
    private String email;
    private String address;
    private String city;
    private String plz;
    private String land;
    private boolean emailSent;

    private boolean emailPay;

    private boolean reminded;
    private boolean payed;
    private boolean createdPDF;

    private boolean sentThankYou;

    @JsonIgnore
    private LinkedList<Sponsorship> sponsorships;

    public Sponsor(String title,
                   String name,
                   String surname,
                   String email,
                   String address,
                   String city,
                   String plz,
                   String land,
                   Boolean emailPay) {
        this.id = Main.getNextId();
        this.title = title;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.address = address;
        this.city = city;
        this.plz = plz;
        this.land = land;
        this.emailPay = emailPay;
        sponsorships = new LinkedList<>();
        emailSent = false;
        createdPDF = false;
        payed = false;
        reminded = false;
        sentThankYou = false;
    }
    @JsonCreator
    public Sponsor(@JsonProperty("id")int id,
                   @JsonProperty("title")String title,
                   @JsonProperty("name")String name,
                   @JsonProperty("surname")String surname,
                   @JsonProperty("email")String email,
                   @JsonProperty("address")String address,
                   @JsonProperty("city")String city,
                   @JsonProperty("plz")String plz,
                   @JsonProperty("land")String land,
                   @JsonProperty("emailSent")boolean emailSent,
                   @JsonProperty("reminded")boolean reminded,
                   @JsonProperty("sentThankYou")boolean sentThankYou,
                   @JsonProperty("createdPDF")boolean createdPDF,
                   @JsonProperty("payed")boolean payed,
                   @JsonProperty("emailPay")boolean emailPay) {
        this.id = id;
        Main.updateMinId(id);
        this.title = title;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.address = address;
        this.city = city;
        this.plz = plz;
        this.land = land;
        this.emailSent = emailSent;
        this.reminded = reminded;
        this.sentThankYou = sentThankYou;
        this.createdPDF = createdPDF;
        this.payed = payed;
        this.emailPay = emailPay;

        sponsorships = new LinkedList<>();
        LoadedStructure.idToSponsor.put(id, this);
    }

    public int getId() {
        return id;
    }

    @JsonIgnore
    public String getBillName() {
        return surname + "_" + name + "_" + id;
    }

    public void addSponsorship(Sponsorship sponsorship) {
        sponsorships.add(sponsorship);
    }
    public void reRegisterSponsorship(Sponsorship sponsorship) {
        sponsorships.add(sponsorship);
        sponsorship.reRegisterSponsor(this);
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void deleteSponsorship(Sponsorship sponsorship) {
        sponsorships.remove(sponsorship);
    }

    public String getEmail() {
        return email;
    }

    public LinkedList<Sponsorship> getSponsorships() {
        return sponsorships;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setPlz(String plz) {
        this.plz = plz;
    }

    public void delete() {
        sponsorships.forEach(sponsorship -> sponsorship.getStudent().deleteSponsorship(sponsorship));
    }

    @JsonIgnore
    public double getTotal(Currency curr) {
        return sponsorships.stream().filter(sponsorship -> sponsorship.getCurrency() == curr)
                .mapToDouble(Sponsorship::getTotal).sum();
    }

    @JsonIgnore
    @Override
    public double getTotalCombinedCHF() {
        return getTotal(Currency.CHF) + getTotal(Currency.EUR) * CONSTANTS.exchangeRate;
    }

    @JsonIgnore
    @Override
    public double getTotalCombinedCHFOpen() {
        return getOpenTotal(Currency.CHF) + getOpenTotal(Currency.EUR) * CONSTANTS.exchangeRate;
    }

    @JsonIgnore
    @Override
    public double getOpenTotal(Currency cur) {
        return sponsorships.stream().filter(sponsorship -> sponsorship.getCurrency() == cur && !sponsorship.getSponsor().isPayed())
                .mapToDouble(Sponsorship::getTotal).sum();
    }

    @JsonIgnore
    @Override
    public String getNameCellRender() {
        return surname + " " + name;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getPlz() {
        return plz;
    }

    public String getLand() {
        return land;
    }

    public void setLand(String land) {
        this.land = land;
    }

    public boolean isEmailSent() {
        return emailSent;
    }

    public void setEmailSent(boolean emailSent) {
        this.emailSent = emailSent;
    }

    public boolean isPayed() {
        return payed;
    }

    public void setPayed(boolean payed) {
        this.payed = payed;
    }

    public boolean isCreatedPDF() {
        return createdPDF;
    }

    public void setCreatedPDF(boolean createdPDF) {
        this.createdPDF = createdPDF;
    }

    public boolean isReminded() {
        return reminded;
    }

    public void setReminded(boolean reminded) {
        this.reminded = reminded;
    }

    @Override
    public String toString() {
        return title + " " + surname + " " + name + " " + email;
    }

    public static String listStringHeader() {
        return "Titel\tNachname\tVorname\tE-Mail\tDankesschreiben erhalten\tE-Mail Rechnung erhalten\tE-Mail Erinnerung erhalten\tRechnung bezahlt\tNr.";
    }
    public static float[] listWidths() {
        return new float[]{0.3f, 0.5f, 0.5f, 1f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f};
    }
    public String listString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.title);
        sb.append("\t");
        sb.append(this.surname);
        sb.append("\t");
        sb.append(this.name);
        sb.append("\t");
        sb.append(this.email);
        String thanked = this.isSentThankYou() ? "Ja" : "Nein";
        String send = this.emailSent ? "Ja" : "Nein";
        String reminded = this.reminded ? "Ja" : "Nein";
        String payed = this.payed ? "Ja" : "Nein";
        sb.append("\t");
        sb.append(thanked);
        sb.append("\t");
        sb.append(send);
        sb.append("\t");
        sb.append(reminded);
        sb.append("\t");
        sb.append(payed);
        sb.append("\t");
        sb.append(id);
        return sb.toString();
    }

    @JsonIgnore
    public boolean hasValidMail() {
        //potentially regular expression check
        return this.email != null && !this.email.isEmpty();
    }

    public void setSendThankYou(boolean b) {
        this.sentThankYou = b;
    }

    public boolean isSentThankYou() {
        return sentThankYou;
    }

    public boolean isEmailPay() {
        return emailPay;
    }

    public void setEmailPay(boolean emailPay) {
        this.emailPay = emailPay;
    }
}
