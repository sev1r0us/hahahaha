import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Stores all records in memory and runs logging, search, matching, claim verification and release. */
public class ItemRegistry implements ClaimProcessor {
    public static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int MIN_MATCH_SCORE = 55;

    private final ArrayList<Item> items = new ArrayList<>();
    private final ArrayList<LostReport> reports = new ArrayList<>();
    private final ArrayList<ClaimRequest> claims = new ArrayList<>();
    private final ArrayList<Student> students = new ArrayList<>();
    private final HashMap<String, Integer> failedAttempts = new HashMap<>(); // "studentNo|itemId" -> count
    private int itemCounter = 0, reportCounter = 0, claimCounter = 0, receiptCounter = 0;

    // ---------- IDs ----------
    public String nextItemId()   { return String.format("ITM-%03d", ++itemCounter); }
    public String nextReportId() { return String.format("RPT-%03d", ++reportCounter); }
    private String nextClaimId()   { return String.format("CLM-%03d", ++claimCounter); }
    private String nextReceiptNo() { return String.format("RCP-%04d", ++receiptCounter); }

    // ---------- Students ----------
    public Student findOrCreateStudent(String studentNumber) {
        for (Student s : students) {
            if (s.getStudentNumber().equals(studentNumber)) return s;
        }
        Student s = new Student(studentNumber, "Student", "");
        students.add(s);
        return s;
    }

    // ---------- Items ----------
    public void registerItem(Item item) { items.add(item); }

    public Item findItem(String itemId) throws ItemNotFoundException {
        for (Item i : items) {
            if (i.getItemId().equalsIgnoreCase(itemId.trim())) return i;
        }
        throw new ItemNotFoundException("No item found with ID '" + itemId + "'.");
    }

    public List<Item> getAllItems() { return Collections.unmodifiableList(items); }

    /** Every word in the keyword must appear in the item's public text. Blank keyword lists all. */
    public List<Item> searchItems(String keyword, boolean includeClaimed) {
        List<Item> out = new ArrayList<>();
        String[] words = TextUtil.normalize(keyword).split(" ");
        for (Item item : items) {
            if (!includeClaimed && item.getStatus() == ItemStatus.CLAIMED) continue;
            String text = TextUtil.normalize(item.searchText());
            boolean all = true;
            for (String w : words) {
                if (!w.isEmpty() && !text.contains(w)) { all = false; break; }
            }
            if (all) out.add(item);
        }
        return out;
    }

    // ---------- Lost reports and matching ----------
    public void addReport(LostReport report) { reports.add(report); }
    public List<LostReport> getAllReports() { return Collections.unmodifiableList(reports); }

    public List<LostReport> getReportsBy(Student student) {
        List<LostReport> out = new ArrayList<>();
        for (LostReport r : reports) if (r.getReporter() == student) out.add(r);
        return out;
    }

    /** Scores unclaimed items against a report (category 40, keywords 30, location 15, date 10). */
    public List<MatchResult> findMatches(LostReport report) {
        List<MatchResult> results = new ArrayList<>();
        Set<String> reportWords = TextUtil.keywords(report.getItemName() + " " + report.getDetails());
        Set<String> placeWords = TextUtil.keywords(report.getLocationLost());

        for (Item item : items) {
            if (item.getStatus() != ItemStatus.UNCLAIMED) continue;
            int score = 0;
            List<String> why = new ArrayList<>();

            if (item.getCategory() == report.getCategory()) { score += 40; why.add("category"); }

            if (!reportWords.isEmpty()) {
                Set<String> common = new HashSet<>(reportWords);
                common.retainAll(TextUtil.keywords(item.searchText()));
                int pts = 30 * common.size() / reportWords.size();
                if (pts > 0) { score += pts; why.add("keywords"); }
            }
            if (!placeWords.isEmpty()) {
                Set<String> common = new HashSet<>(placeWords);
                common.retainAll(TextUtil.keywords(item.getLocationFound()));
                int pts = 15 * common.size() / placeWords.size();
                if (pts > 0) { score += pts; why.add("location"); }
            }
            long days = ChronoUnit.DAYS.between(report.getDateLost(), item.getDateLogged());
            if (days >= 0 && days <= 3) { score += 10; why.add("date"); }
            else if (days > 3 && days <= 14) { score += 5; why.add("date"); }

            if (score >= MIN_MATCH_SCORE) results.add(new MatchResult(item, score, String.join(", ", why)));
        }
        Collections.sort(results); // best match first
        return results;
    }

    // ---------- Claims ----------
    public int getFailedAttempts(Student s, String itemId) {
        return failedAttempts.getOrDefault(s.getStudentNumber() + "|" + itemId, 0);
    }

    public List<ClaimRequest> getAllClaims() { return Collections.unmodifiableList(claims); }

    public List<ClaimRequest> getClaimsBy(Student student) {
        List<ClaimRequest> out = new ArrayList<>();
        for (ClaimRequest c : claims) if (c.getClaimant() == student) out.add(c);
        return out;
    }

    public ClaimRequest findClaim(String claimId) throws InvalidClaimException {
        for (ClaimRequest c : claims) {
            if (c.getClaimId().equalsIgnoreCase(claimId.trim())) return c;
        }
        throw new InvalidClaimException("No claim found with ID '" + claimId + "'.");
    }

    /**
     * Verifies the proof polymorphically via Item.verifyClaim().
     * Correct proof -> claim waits for admin verification. Wrong proof -> counts as a failed attempt;
     * the third failure locks the claim for manual admin review.
     */
    @Override
    public ClaimRequest submitClaim(String itemId, Student claimant, String proof)
            throws ItemNotFoundException, InvalidClaimException, InvalidStatusTransitionException {
        if (proof == null || proof.trim().isEmpty()) throw new InvalidClaimException("Proof cannot be empty.");
        Item item = findItem(itemId);
        if (item.getStatus() != ItemStatus.UNCLAIMED) {
            throw new InvalidClaimException(item.getItemId() + " is not open for claims (status: " + item.getStatus() + ").");
        }
        String key = claimant.getStudentNumber() + "|" + item.getItemId();

        if (item.verifyClaim(proof)) {
            ClaimRequest claim = new ClaimRequest(nextClaimId(), item, claimant, proof, ClaimStatus.PENDING_VERIFICATION);
            claims.add(claim);
            item.updateStatus(ItemStatus.PENDING_VERIFICATION);
            failedAttempts.remove(key);
            return claim;
        }

        int count = failedAttempts.merge(key, 1, Integer::sum);
        if (count >= MAX_FAILED_ATTEMPTS) {
            ClaimRequest claim = new ClaimRequest(nextClaimId(), item, claimant, proof, ClaimStatus.LOCKED);
            claim.markLocked();
            claims.add(claim);
            item.updateStatus(ItemStatus.PENDING_VERIFICATION);
            return claim;
        }
        throw new InvalidClaimException("Proof does not match.", count);
    }

    /** Admin decision on a claim whose proof was correct. */
    @Override
    public void processClaim(String claimId, boolean approve, String remarks)
            throws InvalidClaimException, InvalidStatusTransitionException {
        ClaimRequest claim = findClaim(claimId);
        if (claim.getStatus() != ClaimStatus.PENDING_VERIFICATION) {
            throw new InvalidClaimException(claimId + " is not awaiting verification (status: " + claim.getStatus() + ").");
        }
        decide(claim, approve, remarks);
    }

    /** Admin decision on a claim that was locked after 3 failed attempts. */
    public void manualReview(String claimId, boolean approve, String remarks)
            throws InvalidClaimException, InvalidStatusTransitionException {
        ClaimRequest claim = findClaim(claimId);
        if (claim.getStatus() != ClaimStatus.LOCKED) {
            throw new InvalidClaimException(claimId + " is not locked (status: " + claim.getStatus() + ").");
        }
        decide(claim, approve, remarks);
    }

    private void decide(ClaimRequest claim, boolean approve, String remarks) throws InvalidStatusTransitionException {
        if (approve) {
            claim.getItem().updateStatus(ItemStatus.READY_FOR_PICKUP);
            claim.markApproved(remarks);
        } else {
            claim.getItem().updateStatus(ItemStatus.UNCLAIMED); // rejected claim reopens the item
            claim.markRejected(remarks);
            failedAttempts.remove(claim.getClaimant().getStudentNumber() + "|" + claim.getItem().getItemId());
        }
    }

    /** Hands over a verified item once the claimant's school ID matches. */
    public ClaimRequest releaseItem(String claimId, String studentNumberOnId)
            throws InvalidClaimException, InvalidStatusTransitionException {
        ClaimRequest claim = findClaim(claimId);
        if (claim.getStatus() != ClaimStatus.READY_FOR_PICKUP) {
            throw new InvalidClaimException(claimId + " is not ready for pickup (status: " + claim.getStatus() + ").");
        }
        if (!TextUtil.alnum(studentNumberOnId).equals(TextUtil.alnum(claim.getClaimant().getStudentNumber()))) {
            throw new InvalidClaimException("School ID does not match the claimant. Item not released.");
        }
        claim.getItem().updateStatus(ItemStatus.CLAIMED);
        claim.markCompleted(nextReceiptNo());
        return claim;
    }

    public String displayReceipt(ClaimRequest claim, String releasedBy) {
        String bar = TextUtil.doubleLine();
        return bar + "\n" + TextUtil.color(TextUtil.BLUE, TextUtil.center("RELEASE RECEIPT", 39)) + "\n" + bar + "\n"
             + TextUtil.field("Receipt No", claim.getReceiptNo(), 11) + "\n"
             + TextUtil.field("Claim ID", claim.getClaimId(), 11) + "\n"
             + TextUtil.field("Item", claim.getItem().getItemId() + " " + claim.getItem().getDisplayName(), 11) + "\n"
             + TextUtil.field("Claimant", claim.getClaimant().getStudentNumber(), 11) + "\n"
             + TextUtil.field("Released by", releasedBy, 11) + "\n"
             + TextUtil.field("Date", LocalDate.now().toString(), 11) + "\n"
             + TextUtil.field("Status", TextUtil.color(TextUtil.GREEN, claim.getStatus().name()), 11) + "\n" + bar;
    }

    // ---------- Demo data ----------
    public void seedDemoData() {
        LocalDate today = LocalDate.now();
        registerItem(new StudentIDCard(nextItemId(), "Student ID", "Main Gate Guard Post", today.minusDays(3),
                "Guard", "25-00001", "BSIT"));
        registerItem(new PersonalBelonging(nextItemId(), "Wallet", "Canteen", today.minusDays(2),
                "Guard", "Brown", "Has a small keychain of a red dragon"));
        registerItem(new Electronics(nextItemId(), "Laptop", "Library, 2nd floor", today.minusDays(1),
                "Admin", "Acer", "ACR-9X2K-7731", "sunset beach"));
    }
}
