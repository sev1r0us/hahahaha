/** A found item scored against a lost report. */
public class MatchResult implements Comparable<MatchResult> {
    private final Item item;
    private final int score;
    private final String reasons;

    public MatchResult(Item item, int score, String reasons) {
        this.item = item;
        this.score = score;
        this.reasons = reasons;
    }
    public Item getItem() { return item; }
    public int getScore() { return score; }
    public String getReasons() { return reasons; }

    @Override
    public int compareTo(MatchResult other) { return Integer.compare(other.score, this.score); }
}
