import java.util.List;

/** Menus and screens for both roles. */
public class ConsoleUI {
    private static final String ADMIN_PIN = "1234"; // demo PIN
    private final ItemRegistry registry;
    private final InputHelper input = new InputHelper();
    private final AdminGuard admin = new AdminGuard("ADM-001", "Admin/Guard", ADMIN_PIN);

    public ConsoleUI(ItemRegistry registry) { this.registry = registry; }

    // ================= Main menu =================
    public void run() {
        while (true) {
            System.out.println();
            System.out.println(TextUtil.doubleLine());
            System.out.println(TextUtil.color(TextUtil.BLUE, TextUtil.center("U N I T R A C E", 39)));
            System.out.println("  Campus Lost and Found System");
            System.out.println(TextUtil.doubleLine());
            System.out.println("  [1] Owner\n  [2] Admin / Guard (PIN)\n  [3] Exit");
            System.out.println(TextUtil.line());
            int choice = input.readInt("Enter choice: ", 1, 3);
            switch (choice) {
                case 1: ownerLogin(); break;
                case 2: adminLogin(); break;
                default: System.out.println("  Goodbye!"); return;
            }
        }
    }

    private void ownerLogin() {
        String number = input.readStudentNumber("Enter owner number: ");
        Student student = registry.findOrCreateStudent(number);
        TextUtil.ok("Welcome, " + student.getName() + "!");
        boolean running = true;
        while (running) {
            System.out.println("\n  OWNER MENU");
            System.out.println("  [1] Search Found Items\n  [2] Report Lost Item\n  [3] My Reports and Possible Matches");
            System.out.println("  [4] Submit Claim Request\n  [5] My Claims\n  [6] Log Out");
            int c = input.readInt("Choice: ", 1, 6);
            switch (c) {
                case 1: searchItems(false); break;
                case 2: reportLost(student); break;
                case 3: myReports(student); break;
                case 4: submitClaim(student); break;
                case 5: listClaims(registry.getClaimsBy(student)); break;
                default: running = false;
            }
        }
    }

    private void adminLogin() {
        for (int tries = 1; tries <= 3; tries++) {
            if (admin.authenticate(input.readLine("Enter admin PIN: "))) { adminMenu(); return; }
            TextUtil.error("Wrong PIN (" + tries + " of 3).");
        }
        TextUtil.warn("Too many wrong PINs. Returning to main menu.");
    }

    // ================= Student actions =================
    private void searchItems(boolean asAdmin) {
        System.out.println("\n  " + (asAdmin ? "ADMIN MENU" : "OWNER MENU") + " > Search Found Items");
        String keyword = input.readLine("Keyword (blank = all): ");
        showItems(registry.searchItems(keyword, asAdmin), asAdmin);
        input.pause();
    }

    private void showItems(List<Item> found, boolean full) {
        System.out.println(TextUtil.line());
        for (Item item : found) {
            System.out.println(full ? item.displayFullDetails() : item.displayDetails());
            System.out.println(TextUtil.line());
        }
        System.out.println("  " + found.size() + (found.size() == 1 ? " item found." : " items found."));
        if (!full && !found.isEmpty()) TextUtil.info("Private details are hidden. Submit a claim with proof to verify ownership.");
    }

    private void reportLost(Student student) {
        System.out.println("\n  OWNER MENU > Report Lost Item");
        ItemCategory cat = input.readCategory();
        String name = input.readRequired("Item name: ");
        String details = input.readRequired("Details (color, brand, marks): ");
        String where = input.readRequired("Where did you lose it? ");
        LostReport r = new LostReport(registry.nextReportId(), student, cat, name, details, where,
                input.readDate("Date lost"));
        registry.addReport(r);
        TextUtil.ok("Report " + r.getReportId() + " filed.");
        showMatches(r);
        input.pause();
    }

    private void showMatches(LostReport r) {
        List<MatchResult> matches = registry.findMatches(r);
        if (matches.isEmpty()) { TextUtil.info("No possible matches yet."); return; }
        System.out.println("  Possible matches (best first):");
        for (MatchResult m : matches) {
            System.out.println(TextUtil.line());
            System.out.println("  Match score: " + m.getScore() + "/100 (" + m.getReasons() + ")");
            System.out.println(m.getItem().displayDetails());
        }
    }

    private void myReports(Student student) {
        System.out.println("\n  OWNER MENU > My Reports and Possible Matches");
        List<LostReport> mine = registry.getReportsBy(student);
        if (mine.isEmpty()) TextUtil.info("You have no lost reports.");
        for (LostReport r : mine) {
            System.out.println(TextUtil.line());
            System.out.println(r.displayDetails());
            showMatches(r);
        }
        input.pause();
    }

    private void submitClaim(Student student) {
        System.out.println("\n  OWNER MENU > Submit Claim Request");
        String itemId = input.readRequired("Item ID : ");
        Item item;
        try {
            item = registry.findItem(itemId);
        } catch (ItemNotFoundException e) {
            TextUtil.error(e.getMessage());
            return;
        }
        System.out.println("  Prompt : " + item.getProofPrompt());
        while (true) {
            String proof = input.readLine("Proof (blank to cancel): ");
            if (proof.isEmpty()) return;
            try {
                ClaimRequest claim = registry.submitClaim(itemId, student, proof);
                if (claim.getStatus() == ClaimStatus.LOCKED) {
                    TextUtil.error("Proof does not match.");
                    System.out.println("    Failed attempts: " + ItemRegistry.MAX_FAILED_ATTEMPTS + " of " + ItemRegistry.MAX_FAILED_ATTEMPTS);
                    TextUtil.warn("Claim LOCKED. Sent to admin for manual review.");
                } else {
                    TextUtil.ok("Proof accepted. Claim " + claim.getClaimId() + " is waiting for admin verification.");
                }
                System.out.println("  Item status: " + TextUtil.color(TextUtil.YELLOW, claim.getItem().getStatus().name()));
                return;
            } catch (InvalidClaimException e) {
                TextUtil.error(e.getMessage());
                if (e.getFailedAttempts() > 0) {
                    System.out.println("    Failed attempts: " + e.getFailedAttempts() + " of " + ItemRegistry.MAX_FAILED_ATTEMPTS);
                } else return;
            } catch (ItemNotFoundException | InvalidStatusTransitionException e) {
                TextUtil.error(e.getMessage());
                return;
            }
        }
    }

    private void listClaims(List<ClaimRequest> claims) {
        System.out.println(String.format("\n  %-8s %-8s %-18s %-12s %s", "Claim", "Item", "Name", "Claimant", "Status"));
        System.out.println(TextUtil.line());
        if (claims.isEmpty()) TextUtil.info("No claims yet.");
        for (ClaimRequest c : claims) System.out.println(c.summary());
        input.pause();
    }

    // ================= Admin actions =================
    private void adminMenu() {
        TextUtil.ok("Access granted.");
        boolean running = true;
        while (running) {
            System.out.println("\n  ADMIN MENU");
            System.out.println("  [1] Log Found Item\n  [2] View All Items\n  [3] Search Items\n  [4] View Lost Reports & Matches");
            System.out.println("  [5] Process & Verify Claims\n  [6] Release Item / Print Receipt\n  [7] View All Claims\n  [8] Log Out");
            switch (input.readInt("Choice: ", 1, 8)) {
                case 1: logFoundItem(); break;
                case 2: showItems(registry.getAllItems(), true); input.pause(); break;
                case 3: searchItems(true); break;
                case 4: viewReports(); break;
                case 5: processClaims(); break;
                case 6: releaseItem(); break;
                case 7: listClaims(registry.getAllClaims()); break;
                default: running = false;
            }
        }
    }

    private void logFoundItem() {
        System.out.println("\n  ADMIN MENU > Log Found Item");
        ItemCategory cat = input.readCategory();
        String name = cat == ItemCategory.STUDENT_ID ? "Student ID" : input.readRequired("Item name: ");
        String where = input.readRequired("Location found: ");
        java.time.LocalDate date = input.readDate("Date found");
        String id = registry.nextItemId();
        String by = admin.getRole();
        Item item;
        switch (cat) {
            case STUDENT_ID:
                item = new StudentIDCard(id, name, where, date, by,
                        input.readStudentNumber("Student number on ID: "), input.readRequired("Course/Department: "));
                break;
            case ELECTRONICS:
                item = new Electronics(id, name, where, date, by, input.readRequired("Brand: "),
                        input.readRequired("Serial number: "), input.readRequired("Lock screen description: "));
                break;
            default:
                item = new PersonalBelonging(id, name, where, date, by, input.readRequired("Color: "),
                        input.readRequired("Distinct feature (hidden from students): "));
        }
        registry.registerItem(item);
        TextUtil.ok("Item " + id + " logged.");
        input.pause();
    }

    private void viewReports() {
        System.out.println("\n  ADMIN MENU > Lost Reports & Matches");
        if (registry.getAllReports().isEmpty()) TextUtil.info("No lost reports yet.");
        for (LostReport r : registry.getAllReports()) {
            System.out.println(TextUtil.line());
            System.out.println(r.displayDetails());
            List<MatchResult> matches = registry.findMatches(r);
            if (matches.isEmpty()) TextUtil.info("No possible matches.");
            for (MatchResult m : matches) {
                System.out.println("  -> " + m.getScore() + "/100  " + m.getItem().getItemId() + " "
                        + m.getItem().getDisplayName() + " (" + m.getReasons() + ")");
            }
        }
        input.pause();
    }

    private void processClaims() {
        System.out.println("\n  ADMIN MENU > Process & Verify Claims");
        boolean any = false;
        for (ClaimRequest c : registry.getAllClaims()) {
            if (c.getStatus() == ClaimStatus.PENDING_VERIFICATION || c.getStatus() == ClaimStatus.LOCKED) {
                System.out.println(c.summary());
                any = true;
            }
        }
        if (!any) { TextUtil.info("No claims waiting for review."); input.pause(); return; }
        try {
            ClaimRequest claim = registry.findClaim(input.readRequired("Claim ID: "));
            System.out.println(TextUtil.line());
            System.out.println(claim.getItem().displayFullDetails());
            System.out.println(TextUtil.field("Proof", claim.getProof()));
            System.out.println(TextUtil.field("Claimant", claim.getClaimant().getStudentNumber()));
            System.out.println(TextUtil.line());
            boolean approve = input.readYesNo("Approve this claim?");
            String remarks = input.readLine("Remarks (optional): ");
            if (claim.getStatus() == ClaimStatus.LOCKED) registry.manualReview(claim.getClaimId(), approve, remarks);
            else registry.processClaim(claim.getClaimId(), approve, remarks);
            TextUtil.ok(approve ? "Claim approved. Item is READY_FOR_PICKUP." : "Claim rejected. Item is UNCLAIMED again.");
        } catch (InvalidClaimException | InvalidStatusTransitionException e) {
            TextUtil.error(e.getMessage());
        }
        input.pause();
    }

    private void releaseItem() {
        System.out.println("\n  ADMIN MENU > Release Item / Print Receipt");
        boolean any = false;
        for (ClaimRequest c : registry.getAllClaims()) {
            if (c.getStatus() == ClaimStatus.READY_FOR_PICKUP) { System.out.println(c.summary()); any = true; }
        }
        if (!any) { TextUtil.info("No items are ready for pickup."); input.pause(); return; }
        try {
            ClaimRequest claim = registry.releaseItem(input.readRequired("Claim ID: "),
                    input.readLine("Student number on claimant's school ID: "));
            TextUtil.ok("Check claimant's school ID... verified");
            System.out.println(registry.displayReceipt(claim, admin.getRole()));
            TextUtil.ok("Item released successfully.");
        } catch (InvalidClaimException | InvalidStatusTransitionException e) {
            TextUtil.error(e.getMessage());
        }
        input.pause();
    }
}
