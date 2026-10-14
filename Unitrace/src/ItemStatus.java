/** Lifecycle states of a found item. */
public enum ItemStatus {
    UNCLAIMED, PENDING_VERIFICATION, READY_FOR_PICKUP, CLAIMED;

    /** Valid moves: UNCLAIMED -> PENDING -> READY -> CLAIMED, and PENDING -> UNCLAIMED (rejected claim). */
    public boolean canChangeTo(ItemStatus next) {
        switch (this) {
            case UNCLAIMED:            return next == PENDING_VERIFICATION;
            case PENDING_VERIFICATION: return next == READY_FOR_PICKUP || next == UNCLAIMED;
            case READY_FOR_PICKUP:     return next == CLAIMED;
            default:                   return false;
        }
    }
}
