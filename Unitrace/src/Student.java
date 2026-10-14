public class Student extends User {
    private final String studentNumber;
    private String contact;

    public Student(String studentNumber, String name, String contact) {
        super(studentNumber, name);
        this.studentNumber = studentNumber;
        this.contact = contact;
    }
    public String getStudentNumber() { return studentNumber; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    @Override public String getRole() { return "Student"; }
}
