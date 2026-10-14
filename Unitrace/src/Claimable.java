/** Anything that can be claimed by proving ownership. */
public interface Claimable {
    boolean verifyClaim(String proof);
    String getProofPrompt();
}
