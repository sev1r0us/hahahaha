public class AdminGuard extends User {
    private final String pin;

    public AdminGuard(String userId, String name, String pin) {
        super(userId, name);
        this.pin = pin;
    }
    public boolean authenticate(String attempt) { return pin.equals(attempt); }
    @Override public String getRole() { return "Admin/Guard"; }
}
