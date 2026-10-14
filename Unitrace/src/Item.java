import java.time.LocalDate;
import java.util.List;

/** Abstract parent of every found item. Subclasses supply their own claim verification. */
public abstract class Item implements Claimable {
    private final String itemId;
    private final ItemCategory category;
    private final String itemName;
    private final String locationFound;
    private final LocalDate dateLogged;
    private final String loggedBy;
    private ItemStatus status = ItemStatus.UNCLAIMED;

    protected Item(String itemId, ItemCategory category, String itemName,
                   String locationFound, LocalDate dateLogged, String loggedBy) {
        this.itemId = itemId;
        this.category = category;
        this.itemName = itemName;
        this.locationFound = locationFound;
        this.dateLogged = dateLogged;
        this.loggedBy = loggedBy;
    }

    public String getItemId() { return itemId; }
    public ItemCategory getCategory() { return category; }
    public String getItemName() { return itemName; }
    public String getLocationFound() { return locationFound; }
    public LocalDate getDateLogged() { return dateLogged; }
    public String getLoggedBy() { return loggedBy; }
    public ItemStatus getStatus() { return status; }

    /** Name shown to users; subclasses may add detail (e.g. "Laptop (Acer)"). */
    public String getDisplayName() { return itemName; }

    /** Public (non-secret) extra words used for searching and matching. */
    public abstract String publicDetails();
    /** Lines shown to students: private values are masked. */
    protected abstract List<String> maskedLines();
    /** Lines shown to admins/guards: everything. */
    protected abstract List<String> fullLines();

    public String searchText() {
        return itemName + " " + category.getLabel() + " " + locationFound + " " + publicDetails();
    }

    private String render(List<String> extra) {
        StringBuilder sb = new StringBuilder();
        sb.append(TextUtil.field("Item ID", itemId)).append('\n');
        sb.append(TextUtil.field("Type", category.getLabel())).append('\n');
        sb.append(TextUtil.field("Item", getDisplayName())).append('\n');
        sb.append(TextUtil.field("Found at", locationFound)).append('\n');
        sb.append(TextUtil.field("Logged", dateLogged.toString())).append('\n');
        for (String l : extra) sb.append(l).append('\n');
        sb.append(TextUtil.field("Status", TextUtil.color(statusColor(), status.name())));
        return sb.toString();
    }

    private String statusColor() {
        return status == ItemStatus.UNCLAIMED ? TextUtil.GREEN
                : status == ItemStatus.CLAIMED ? TextUtil.GRAY : TextUtil.YELLOW;
    }

    /** Student view (private details hidden). */
    public String displayDetails() { return render(maskedLines()); }
    /** Admin/guard view (everything visible). */
    public String displayFullDetails() { return render(fullLines()) + "\n" + TextUtil.field("By", loggedBy); }

    /** Changes status only through the allowed lifecycle transitions. */
    public void updateStatus(ItemStatus next) throws InvalidStatusTransitionException {
        if (!status.canChangeTo(next)) {
            throw new InvalidStatusTransitionException(
                    "Cannot change " + itemId + " from " + status + " to " + next + ".");
        }
        status = next;
    }

    @Override
    public abstract boolean verifyClaim(String proof);
}
