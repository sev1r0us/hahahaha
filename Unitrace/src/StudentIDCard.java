import java.time.LocalDate;
import java.util.List;

public class StudentIDCard extends Item {
    private final String studentNumber;   // private proof
    private final String courseDepartment;

    public StudentIDCard(String itemId, String itemName, String locationFound, LocalDate dateLogged,
                         String loggedBy, String studentNumber, String courseDepartment) {
        super(itemId, ItemCategory.STUDENT_ID, itemName, locationFound, dateLogged, loggedBy);
        this.studentNumber = studentNumber;
        this.courseDepartment = courseDepartment;
    }

    public String getStudentNumber() { return studentNumber; }
    public String getCourseDepartment() { return courseDepartment; }

    @Override public String publicDetails() { return courseDepartment; }

    @Override protected List<String> maskedLines() {
        return List.of(TextUtil.field("Course", courseDepartment),
                       TextUtil.field("Student#", TextUtil.mask()));
    }
    @Override protected List<String> fullLines() {
        return List.of(TextUtil.field("Course", courseDepartment),
                       TextUtil.field("Student#", studentNumber));
    }

    @Override public boolean verifyClaim(String proof) {
        return !TextUtil.alnum(proof).isEmpty() && TextUtil.alnum(proof).equals(TextUtil.alnum(studentNumber));
    }
    @Override public String getProofPrompt() { return "Enter the student number printed on the ID."; }
}
