public enum ItemCategory {
    STUDENT_ID("Student ID"), ELECTRONICS("Electronics"), PERSONAL_BELONGING("Personal Belonging");

    private final String label;
    ItemCategory(String label) { this.label = label; }
    public String getLabel() { return label; }
}
