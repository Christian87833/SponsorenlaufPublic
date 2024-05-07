package ch.steinerschule_stgallen.model;

import ch.steinerschule_stgallen.util.SponsorshipType;
import ch.steinerschule_stgallen.util.Currency;
import ch.steinerschule_stgallen.views.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Holds all the data. Holds functions to add data, remove data, access data, filter data
 */
public class Model {
    @JsonIgnore
    public StudentClass selectedClass;
    @JsonIgnore
    public Student selectedStudent;
    @JsonIgnore
    public Sponsor selectedSponsor;
    @JsonIgnore
    public Sponsorship selectedSponsorship;
    @JsonIgnore
    View view;

    private LinkedList<StudentClass> allClasses;
    @JsonIgnore
    private LinkedList<Student> studentsOfCurrentClass;
    @JsonIgnore
    private LinkedList<Sponsorship> sponsorshipsOfSelectedStudent;

    private LinkedList<Sponsor> allSponsors;

    @JsonIgnore
    private LinkedList<StudentClass> searchRestrictedClasses;
    @JsonIgnore
    private LinkedList<Student> searchRestrictedStudentsOfCurrentClass;
    @JsonIgnore
    private LinkedList<Sponsorship> searchRestrictedSponsorsOfCurrentStudent;
    @JsonIgnore
    private LinkedList<Sponsor> searchRestrictedSponsors;

    @JsonIgnore
    private String classQuery;
    @JsonIgnore
    private String studentQuery;
    @JsonIgnore
    private String sponsorshipQuery;
    @JsonIgnore
    private String sponsorQuery;

    @JsonIgnore
    private File currentFile;

    public Model() {
        selectedClass = null;
        selectedStudent = null;
        selectedSponsor = null;
        allClasses = new LinkedList<>();
        studentsOfCurrentClass = new LinkedList<>();
        sponsorshipsOfSelectedStudent = new LinkedList<>();
        allSponsors = new LinkedList<>();
    }
    public StudentClass getSelectedClass() {
        return selectedClass;
    }

    public void addClass(String name){
        StudentClass newClass = new StudentClass(name);
        allClasses.add(newClass);
    }

    public void addStudent(String name, String surname, String tel, int lapCount, int lNumber){
        Student student = new Student(name, surname, tel, lapCount, lNumber);
        studentsOfCurrentClass.add(student);
    }

    public void registerView(View view) {
        this.view = view;
    }

    public void classSelected(int index) {
        if(index != -1) {
            selectedClass = searchRestrictedClasses.get(index);
            studentsOfCurrentClass = selectedClass.getStudents();
        } else {
            selectedClass = null;
            studentsOfCurrentClass = null;
        }
    }

    public void studentSelected(int index) {
        if(index != -1) {
            selectedStudent = searchRestrictedStudentsOfCurrentClass.get(index);
            sponsorshipsOfSelectedStudent = selectedStudent.getSponsorships();
        } else {
            selectedStudent = null;
            sponsorshipsOfSelectedStudent = null;
        }
    }

    public void sponsorSelected(int index) {
        if(index != -1) {
            selectedSponsor = searchRestrictedSponsors.get(index);
        } else {
            selectedSponsor = null;
        }
    }

    public void sponsorshipSelected(int index){
        if(index != -1) {
            selectedSponsorship = searchRestrictedSponsorsOfCurrentStudent.get(index);
        } else {
            selectedSponsorship = null;
        }
    }

    public void addSponsorship(double amount, SponsorshipType type, Currency curr){
        Sponsorship ss = new Sponsorship(selectedSponsor, selectedStudent, type, amount, curr);
        selectedStudent.addSponsorship(ss);
        selectedSponsor.addSponsorship(ss);
    }

    public void addSponsor(String title, String name, String surname, String email, String address, String city, String plz, String land, Boolean emailBill){
        Optional<Sponsor> potentialMatch;
        if(email != null && !email.isEmpty()) {
            potentialMatch = allSponsors.stream().filter(sponsor -> sponsor.getEmail().equals(email) ||
                    (sponsor.getName().equals(name) && sponsor.getSurname().equals(surname))).findFirst();
        } else {
            potentialMatch = allSponsors.stream().filter(sponsor -> sponsor.getName().equals(name)
                    && sponsor.getSurname().equals(surname)).findFirst();
        }
        if(potentialMatch.isPresent()) {
            if (view.confirmWindow("Es exestiert bereits ein Sponsor mit folgendem Namen und Email:\n"
                    + potentialMatch.get().toString() + "\n Wollen Sie trozdem einen neuen Sponsor anlegen?")) {
                Sponsor sponsor = new Sponsor(title, name, surname, email.trim(), address, city, plz, land, emailBill);
                this.allSponsors.add(sponsor);
            }
        } else {
            Sponsor sponsor = new Sponsor(title, name, surname, email.trim(), address, city, plz, land, emailBill);
            this.allSponsors.add(sponsor);
        }
    }

    public void setClassQuery(String classQuery) {
        this.classQuery = classQuery;
    }

    public void setStudentQuery(String studentQuery) {
        this.studentQuery = studentQuery;
    }

    public void setSponsorshipQuery(String sponsorshipQuery) {
        this.sponsorshipQuery = sponsorshipQuery;
    }

    public void setSponsorQuery(String sponsorQuery) {
        this.sponsorQuery = sponsorQuery;
    }

    @JsonIgnore
    public LinkedList<StudentClass> getSearchRestrictedClasses() {
        allClasses.sort(new Comparator<StudentClass>() {
            @Override
            public int compare(StudentClass o1, StudentClass o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        if(classQuery == null || classQuery.isEmpty()) {
            searchRestrictedClasses = allClasses;
        } else {
            searchRestrictedClasses = allClasses.stream().filter(studentClass ->
                            studentClass.toString().toLowerCase().contains(classQuery.toLowerCase()))
                    .collect(Collectors.toCollection(LinkedList::new));
        }
        return searchRestrictedClasses;
    }

    @JsonIgnore
    public LinkedList<Student> getSearchRestrictedStudentsOfCurrentClass() {
        studentsOfCurrentClass.sort(new Comparator<Student>() {
            @Override
            public int compare(Student o1, Student o2) {
                return o1.getSurname().compareTo(o2.getSurname());
            }
        });
        if(studentQuery == null || studentQuery.isEmpty()) {
            searchRestrictedStudentsOfCurrentClass = studentsOfCurrentClass;
        } else {
            searchRestrictedStudentsOfCurrentClass = studentsOfCurrentClass.stream().filter(student ->
                            student.toString().toLowerCase().contains(studentQuery.toLowerCase()))
                    .collect(Collectors.toCollection(LinkedList::new));
        }
        return searchRestrictedStudentsOfCurrentClass;
    }

    @JsonIgnore
    public LinkedList<Sponsorship> getSearchRestrictedSponsorshipsOfCurrentStudent() {
        if(sponsorshipQuery == null || sponsorshipQuery.isEmpty()) {
            searchRestrictedSponsorsOfCurrentStudent = sponsorshipsOfSelectedStudent;
        } else {
            searchRestrictedSponsorsOfCurrentStudent = sponsorshipsOfSelectedStudent.stream().filter(sponsorship ->
                            sponsorship.toString().toLowerCase().contains(sponsorshipQuery.toLowerCase()))
                    .collect(Collectors.toCollection(LinkedList::new));
        }
        return searchRestrictedSponsorsOfCurrentStudent;
    }

    @JsonIgnore
    public LinkedList<Sponsor> getSearchRestrictedSponsors() {
        allSponsors.sort(new Comparator<Sponsor>() {
            @Override
            public int compare(Sponsor o1, Sponsor o2) {
                return o1.getSurname().compareTo(o2.getSurname());
            }
        });
        if(sponsorQuery == null || sponsorQuery.isEmpty()) {
            searchRestrictedSponsors = allSponsors;
        } else {
            searchRestrictedSponsors = allSponsors.stream().filter(sponsor ->
                            sponsor.toString().toLowerCase().contains(sponsorQuery.toLowerCase()))
                    .collect(Collectors.toCollection(LinkedList::new));
        }
        return searchRestrictedSponsors;
    }

    public void updateStudent(String name, String surname, String tel, int lapCount, int lNumber) {
        selectedStudent.setName(name);
        selectedStudent.setSurname(surname);
        selectedStudent.setTelNumber(tel);
        selectedStudent.setLapCount(lapCount);
        selectedStudent.setLnumber(lNumber);
    }

    public void updateClass(String name) {
        selectedClass.setName(name);
    }

    public void updateSponsor(String title, String name, String surname, String email,
                              String address, String city, String plz, String land, Boolean emailBill) {
        selectedSponsor.setTitle(title);
        selectedSponsor.setName(name);
        selectedSponsor.setSurname(surname);
        selectedSponsor.setEmail(email.trim());
        selectedSponsor.setAddress(address);
        selectedSponsor.setPlz(plz);
        selectedSponsor.setCity(city);
        selectedSponsor.setLand(land);
        selectedSponsor.setEmailPay(emailBill);
    }

    public void updateSponsorship(double amount, SponsorshipType type, Currency curr) {
        selectedSponsorship.setAmount(amount);
        selectedSponsorship.setType(type);
        selectedSponsorship.setCurrency(curr);
    }

    public void deleteSelectedSponsorship() {
        selectedSponsorship.delete();
        sponsorshipsOfSelectedStudent.remove(selectedSponsorship);
    }

    public void deleteSelectedStudent() {
        selectedStudent.delete();
        studentsOfCurrentClass.remove(selectedStudent);
    }

    public void deleteSelectedSponsor() {
        selectedSponsor.delete();
        allSponsors.remove(selectedSponsor);
    }

    public void deleteSelectedClass() {
        studentsOfCurrentClass.forEach(Student::delete);
        allClasses.remove(selectedClass);
    }


    // save and load capabilities:
    public void testSave(File saveTo) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(saveTo, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void testLoad(File loadFrom) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            LoadedStructure ls;
            ls = mapper.readValue(
                    loadFrom, LoadedStructure.class);
            ls.connectSponsorships();
            this.allSponsors = ls.getAllSponsors();
            this.allClasses = ls.getAllClasses();
            currentFile = loadFrom;
        } catch (IOException e) {
            view.displayInfo("Die gewählte Datei ist nicht kompatiebel");
            System.out.println(e.toString());
        }


    }


    //These two getters are important for the save feature of jackson.
    //DO NOT DELETE, DO NOT ANNOTATE
    public LinkedList<StudentClass> getAllClasses() {
        return allClasses;
    }

    //DO NOT DELETE, DO NOT ANNOTATE
    public LinkedList<Sponsor> getAllSponsors() {
        return allSponsors;
    }

    @JsonIgnore
    public LinkedList<Sponsorship> getSponsorshipsOfSelectedStudent() {
        return sponsorshipsOfSelectedStudent;
    }



    public double calculateGrandTotal(Currency curr) {
        return allClasses.stream()
                .flatMap(studentClass -> studentClass.getStudents().stream())
                .mapToDouble(student -> student.getTotal(curr))
                .sum();
    }

    public double calculateOpenTotal(Currency curr) {
        return allClasses.stream()
                .flatMap(studentClass -> studentClass.getStudents().stream())
                .mapToDouble(student -> student.getOpenTotal(curr))
                .sum();
    }

    @JsonIgnore
    public LinkedList<StudentClass> getClassesListSortedByTotalIncome() {
        return allClasses.stream().sorted((o1, o2) -> Double.compare(o2.getTotalCombinedCHF(),
                o1.getTotalCombinedCHF())).collect(Collectors.toCollection(LinkedList::new));
    }

    @JsonIgnore
    public LinkedList<Student> getStudentListSortedByTotalIncome() {
        return allClasses.stream().flatMap(studentClass -> studentClass.getStudents().stream())
                .sorted((o1, o2) -> Double.compare(o2.getTotalCombinedCHF(), o1.getTotalCombinedCHF()))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @JsonIgnore
    public LinkedList<Sponsor> getSponsorListSortedByTotalIncome() {
        return allSponsors.stream().sorted((o1, o2) ->
                Double.compare(o2.getTotalCombinedCHF(), o1.getTotalCombinedCHF()))
                .collect(Collectors.toCollection(LinkedList::new));
    }


    @JsonIgnore
    public List<Sponsor> getSponsorsByTrueIndex(boolean[] trueIndexes) {
        return IntStream.range(0, allSponsors.size())
                .filter(i -> trueIndexes[i])
                .mapToObj(allSponsors::get)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public boolean[] getArrayOfSponsorsWithEmail() {
        boolean[] ret = new boolean[allSponsors.size()];
        Arrays.fill(ret, false);
        for(int i = 0; i < allSponsors.size(); i++){
            if(allSponsors.get(i).getEmail() != null && !allSponsors.get(i).getEmail().trim().isEmpty())
                ret[i] = true;
        }
        return ret;
    }

    @JsonIgnore
    public File getCurrentFile() {
        return currentFile;
    }

    @JsonIgnore
    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
    }

    @JsonIgnore
    public boolean doesLNumExist(int lNum) {
        for(StudentClass sc : allClasses) {
            for(Student s : sc.getStudents()) {
                if(s.getLNumber() == lNum) {
                    return true;
                }
            }
        }
        return false;
    }

    @JsonIgnore
    public int allLaps() {
        int res = 0;
        for(StudentClass sc : allClasses) {
            for(Student s : sc.getStudents()) {
                res += s.getLapCount();
            }
        }
        return res;
    }

    @JsonIgnore
    public Sponsor getSponsorById(int targetId) {
        return allSponsors.stream()
                .filter(sponsor -> sponsor.getId() == targetId)
                .findFirst()
                .orElse(null);
    }

    @JsonIgnore
    public StudentClass getClassOfStudent(Student student) {
        return allClasses.stream()
                .filter(studentClass -> studentClass.getStudents().contains(student))
                .findFirst().orElse(null);
    }
}

