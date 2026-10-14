import java.time.LocalDate;
import java.util.List;

public class Electronics extends Item {
    private final String brand;
    private final String serialNumber;          // private proof
    private final String lockScreenDescription; // private proof

    public Electronics(String itemId, String itemName, String locationFound, LocalDate dateLogged,
                       String loggedBy, String brand, String serialNumber, String lockScreenDescription) {
        super(itemId, ItemCategory.ELECTRONICS, itemName, locationFound, dateLogged, loggedBy);
        this.brand = brand;
        this.serialNumber = serialNumber;
        this.lockScreenDescription = lockScreenDescription;
    }

    public String getBrand() { return brand; }
    public String getSerialNumber() { return serialNumber; }

    @Override public String getDisplayName() { return getItemName() + " (" + brand + ")"; }
    @Override public String publicDetails() { return brand; }

    @Override protected List<String> maskedLines() {
        return List.of(TextUtil.field("Serial", TextUtil.mask()),
                       TextUtil.field("Lock scr", TextUtil.mask()));
    }
    @Override protected List<String> fullLines() {
        return List.of(TextUtil.field("Serial", serialNumber),
                       TextUtil.field("Lock scr", lockScreenDescription));
    }

    /** Accepts either the serial number or the lock screen description. */
    @Override public boolean verifyClaim(String proof) {
        String serial = TextUtil.alnum(proof);
        if (!serial.isEmpty() && serial.equals(TextUtil.alnum(serialNumber))) return true;
        return TextUtil.looselyMatches(proof, lockScreenDescription);
    }
    @Override public String getProofPrompt() { return "Enter the serial number or lock screen description."; }
}
