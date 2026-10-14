/** Top-level claim operations, hiding the internal logic of the registry. */
public interface ClaimProcessor {
    ClaimRequest submitClaim(String itemId, Student claimant, String proof)
            throws ItemNotFoundException, InvalidClaimException, InvalidStatusTransitionException;

    void processClaim(String claimId, boolean approve, String remarks)
            throws InvalidClaimException, InvalidStatusTransitionException;
}
