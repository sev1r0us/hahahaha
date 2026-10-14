/** One student's claim on one item, with the submitted proof. */
public class ClaimRequest {
    private final String claimId;
    private final Item item;
    private final Student claimant;
    private final String proof;
    private ClaimStatus status;
    private String remarks = "";
    private String receiptNo = "";

    public ClaimRequest(String claimId, Item item, Student claimant, String proof, ClaimStatus status) {
        this.claimId = claimId;
        this.item = item;
        this.claimant = claimant;
        this.proof = proof;
        this.status = status;
    }
    public String getClaimId() { return claimId; }
    public Item getItem() { return item; }
    public Student getClaimant() { return claimant; }
    public String getProof() { return proof; }
    public ClaimStatus getStatus() { return status; }
    public String getRemarks() { return remarks; }
    public String getReceiptNo() { return receiptNo; }

    public void markApproved(String remarks) { this.status = ClaimStatus.READY_FOR_PICKUP; this.remarks = remarks; }
    public void markRejected(String remarks) { this.status = ClaimStatus.REJECTED; this.remarks = remarks; }
    public void markLocked()                 { this.status = ClaimStatus.LOCKED; this.remarks = "3 failed verifications"; }
    public void markCompleted(String receiptNo) { this.status = ClaimStatus.CLAIMED; this.receiptNo = receiptNo; }

    public String summary() {
        return String.format("  %-8s %-8s %-18s %-12s %s", claimId, item.getItemId(),
                item.getDisplayName(), claimant.getStudentNumber(), status);
    }
}
