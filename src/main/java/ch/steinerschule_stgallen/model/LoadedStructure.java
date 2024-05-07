package ch.steinerschule_stgallen.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Set up data structure from read json file. It's about linking Students and Sponsors and Sponsorships by their ID.
 */
public class LoadedStructure {

    public static Map<Integer, Student> idToStudent = new HashMap<>();
    public static Map<Integer, Sponsor> idToSponsor = new HashMap<>();
    private final LinkedList<Sponsor> allSponsors;
    private final LinkedList<StudentClass> allClasses;

    @JsonCreator
    public LoadedStructure(@JsonProperty("allClasses") LinkedList<StudentClass>  allClasses,
                           @JsonProperty("allSponsors") LinkedList<Sponsor> allSponsors) {
        this.allClasses = allClasses;
        this.allSponsors = allSponsors;
    }

    public void connectSponsorships() {
        allClasses.forEach(studentClass ->
                studentClass.getStudents().forEach(student ->
                        student.getSponsorships().forEach(sponsorship -> {
            idToStudent.get(sponsorship.getStudentId()).reRegisterSponsorship(sponsorship);
            idToSponsor.get(sponsorship.getSponsorId()).reRegisterSponsorship(sponsorship);
})));
    }

    public LinkedList<Sponsor> getAllSponsors() {
        return allSponsors;
    }

    public LinkedList<StudentClass> getAllClasses() {
        return allClasses;
    }
}
