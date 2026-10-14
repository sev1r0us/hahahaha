import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/** Reads and validates console input. Invalid input is re-asked instead of crashing. */
public class InputHelper {
    private final Scanner in = new Scanner(System.in);

    public String readLine(String prompt) {
        System.out.print("  " + prompt);
        if (!in.hasNextLine()) throw new IllegalStateException("Input closed");
        return in.nextLine().trim();
    }

    public String readRequired(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (!s.isEmpty()) return s;
            TextUtil.error("This field cannot be empty.");
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            try {
                int n = Integer.parseInt(readLine(prompt));
                if (n >= min && n <= max) return n;
                TextUtil.error("Please enter a number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                TextUtil.error("Invalid input. Please enter a number.");
            }
        }
    }

    /** Date as YYYY-MM-DD; blank means today. */
    public LocalDate readDate(String prompt) {
        while (true) {
            String s = readLine(prompt + " (YYYY-MM-DD, blank = today): ");
            if (s.isEmpty()) return LocalDate.now();
            try {
                LocalDate d = LocalDate.parse(s);
                if (d.isAfter(LocalDate.now())) { TextUtil.error("Date cannot be in the future."); continue; }
                return d;
            } catch (DateTimeParseException e) {
                TextUtil.error("Invalid date format.");
            }
        }
    }

    /** Student numbers look like 25-12345. */
    public String readStudentNumber(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (s.matches("\\d{2}-\\d{5}")) return s;
            TextUtil.error("Invalid student number. Use the format 25-12345.");
        }
    }

    public ItemCategory readCategory() {
        ItemCategory[] cats = ItemCategory.values();
        for (int i = 0; i < cats.length; i++) System.out.println("  [" + (i + 1) + "] " + cats[i].getLabel());
        return cats[readInt("Category: ", 1, cats.length) - 1];
    }

    public boolean readYesNo(String prompt) {
        while (true) {
            String s = readLine(prompt + " (y/n): ").toLowerCase();
            if (s.equals("y") || s.equals("yes")) return true;
            if (s.equals("n") || s.equals("no")) return false;
            TextUtil.error("Please answer y or n.");
        }
    }

    public void pause() { readLine("Press Enter to return..."); }
}
