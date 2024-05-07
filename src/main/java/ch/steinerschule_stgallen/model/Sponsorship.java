package ch.steinerschule_stgallen.model;

import ch.steinerschule_stgallen.model.Sponsor;
import ch.steinerschule_stgallen.util.Currency;
import ch.steinerschule_stgallen.util.SponsorshipType;
import com.fasterxml.jackson.annotation.*;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Sponsorship {
    private final int id;

    private Sponsor sponsor;
    private Student student;

    private final int sponsorId;
    private final int studentId;
    private SponsorshipType type;
    private Currency currency;

    private double amount;

    public Sponsorship(Sponsor sponsor,
                       Student student,
                       SponsorshipType type,
                       double amount,
                       Currency currency) {
        this.sponsor = sponsor;
        this.student = student;
        this.sponsorId = sponsor.getId();
        this.studentId = student.getId();
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.id = Main.getNextId();
    }

    @JsonCreator
    public Sponsorship(@JsonProperty("id") int id,
                       @JsonProperty("sponsorId")int sponsorId,
                       @JsonProperty("studentId")int studentId,
                       @JsonProperty("type") SponsorshipType type,
                       @JsonProperty("amount")double amount,
                       @JsonProperty("currency") Currency currency) {
        this.sponsorId = sponsorId;
        this.studentId = studentId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.id = id;
        Main.updateMinId(id);
    }

    public void reRegisterStudent(Student student) {
        this.student = student;
    }
    public void reRegisterSponsor(Sponsor sponsor) {
        this.sponsor = sponsor;
    }

    public int getId() {
        return id;
    }


    @JsonIgnore
    public Sponsor getSponsor() {
        return sponsor;
    }

    @JsonIgnore
    public Student getStudent() {
        return student;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getSponsorId() {
        return sponsorId;
    }
    public SponsorshipType getType() {
        return type;
    }

    public Currency getCurrency() {
        return currency;
    }

    public double getAmount() {
        return amount;
    }

    public void setType(SponsorshipType type) {
        this.type = type;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void delete() {
        sponsor.deleteSponsorship(this);
        student.deleteSponsorship(this);
    }

    /**
     *
     * @return the total yield of sponsorship depending on lap-count of student
     */
    @JsonIgnore
    public double getTotal(){
        if (type == SponsorshipType.ONCE_OFF)
            return amount;
        else
            return student.getLapCount() * amount;
    }


    @Override
    public String toString(){
        String sType = type == SponsorshipType.PER_LAP ? "pro Runde " : "einmalig ";
        return getSponsor().getTitle() + " " + getSponsor().getSurname() + " (" + getSponsor().getName() + ") spendet " + sType + amount + " " + currency.toString();
    }

    @JsonIgnore
    public String getDetailedString(Model model){
        String sType = type == SponsorshipType.PER_LAP ? "pro Runde " : "einmalig ";
        return getSponsor().getTitle() + " " + getSponsor().getSurname() + " spendet " + sType + currency.toString() + " "
                + amount + " an " + getStudent().toString() + " (" + model.getClassOfStudent(getStudent()).getName()  + ")";
    }

}
