import java.time.LocalDate;
import java.util.List;

public class PersonalBelonging extends Item {
    private final String color;
    private final String distinctFeature; // private proof

    public PersonalBelonging(String itemId, String itemName, String locationFound, LocalDate dateLogged,
                             String loggedBy, String color, String distinctFeature) {
        super(itemId, ItemCategory.PERSONAL_BELONGING, itemName, locationFound, dateLogged, loggedBy);
        this.color = color;
        this.distinctFeature = distinctFeature;
    }

    public String getColor() { return color; }
    public String getDistinctFeature() { return distinctFeature; }

    @Override public String publicDetails() { return color; }

    @Override protected List<String> maskedLines() {
        return List.of(TextUtil.field("Color", color), TextUtil.field("Feature", TextUtil.mask()));
    }
    @Override protected List<String> fullLines() {
        return List.of(TextUtil.field("Color", color), TextUtil.field("Feature", distinctFeature));
    }

    @Override public boolean verifyClaim(String proof) { return TextUtil.looselyMatches(proof, distinctFeature); }
    @Override public String getProofPrompt() { return "Describe a distinct feature of the item (mark, sticker, contents)."; }
}
