import java.time.LocalDate;

/** A student's report of a missing item, used for matching. */
public class LostReport {
    private final String reportId;
    private final Student reporter;
    private final ItemCategory category;
    private final String itemName;
    private final String details;
    private final String locationLost;
    private final LocalDate dateLost;

    public LostReport(String reportId, Student reporter, ItemCategory category, String itemName,
                      String details, String locationLost, LocalDate dateLost) {
        this.reportId = reportId;
        this.reporter = reporter;
        this.category = category;
        this.itemName = itemName;
        this.details = details;
        this.locationLost = locationLost;
        this.dateLost = dateLost;
    }
    public String getReportId() { return reportId; }
    public Student getReporter() { return reporter; }
    public ItemCategory getCategory() { return category; }
    public String getItemName() { return itemName; }
    public String getDetails() { return details; }
    public String getLocationLost() { return locationLost; }
    public LocalDate getDateLost() { return dateLost; }

    public String displayDetails() {
        return TextUtil.field("Report", reportId) + "\n"
             + TextUtil.field("Owner", reporter.getStudentNumber()) + "\n"
             + TextUtil.field("Type", category.getLabel()) + "\n"
             + TextUtil.field("Item", itemName) + "\n"
             + TextUtil.field("Details", details) + "\n"
             + TextUtil.field("Lost at", locationLost) + "\n"
             + TextUtil.field("Date", dateLost.toString());
    }
}
