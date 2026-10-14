import java.util.HashSet;
import java.util.Set;

/** Small helpers for text matching and console formatting. */
public final class TextUtil {
    private TextUtil() {}

    public static final boolean COLOR = System.getenv("NO_COLOR") == null;
    public static final String RESET = "\u001B[0m", RED = "\u001B[91m", GREEN = "\u001B[32m",
            YELLOW = "\u001B[33m", BLUE = "\u001B[94m", GRAY = "\u001B[90m";
    private static final Set<String> STOP_WORDS =
            Set.of("the", "and", "for", "with", "near", "from", "lost", "found", "was", "has", "its", "inside");

    public static String color(String code, String text) { return COLOR ? code + text + RESET : text; }

    /** Lowercase, strip punctuation, collapse spaces. */
    public static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase().replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    /** Lowercase letters and digits only (for IDs and serial numbers). */
    public static String alnum(String s) {
        return s == null ? "" : s.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    public static Set<String> keywords(String s) {
        Set<String> out = new HashSet<>();
        for (String t : normalize(s).split(" ")) {
            if (t.length() >= 3 && !STOP_WORDS.contains(t)) out.add(t);
        }
        return out;
    }

    /** Forgiving text comparison: equal, or one contains the other (min. 4 characters). */
    public static boolean looselyMatches(String a, String b) {
        String x = normalize(a), y = normalize(b);
        if (x.isEmpty() || y.isEmpty()) return false;
        if (x.length() < 4 || y.length() < 4) return x.equals(y);
        return x.contains(y) || y.contains(x);
    }

    public static String mask() { return color(YELLOW, "********"); }
    public static String field(String label, String value) { return field(label, value, 8); }
    public static String field(String label, String value, int width) {
        return String.format("  %-" + width + "s: %s", label, value);
    }
    public static String line() { return "  " + "-".repeat(37); }
    public static String doubleLine() { return "=".repeat(39); }

    public static String center(String s, int width) {
        int pad = Math.max(0, (width - s.length()) / 2);
        return " ".repeat(pad) + s;
    }

    public static void error(String msg) { System.out.println("  " + color(RED, "[X] " + msg)); }
    public static void warn(String msg)  { System.out.println("  " + color(YELLOW, "[!] " + msg)); }
    public static void ok(String msg)    { System.out.println("  " + color(GREEN, msg)); }
    public static void info(String msg)  { System.out.println("  " + color(GRAY, msg)); }
}
