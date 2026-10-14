public class InvalidClaimException extends Exception {
    private final int failedAttempts;
    public InvalidClaimException(String message) { this(message, 0); }
    public InvalidClaimException(String message, int failedAttempts) {
        super(message);
        this.failedAttempts = failedAttempts;
    }
    public int getFailedAttempts() { return failedAttempts; }
}
