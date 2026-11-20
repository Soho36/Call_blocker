package app.contactsonly;

/** Pure decision logic. Android filters saved contacts before invoking our service. */
public final class ScreeningPolicy {
    public enum Decision { IGNORE_OUTGOING, ALLOW, REJECT }

    private ScreeningPolicy() { }

    public static Decision decide(boolean incoming, boolean hasTelephoneNumber,
                                  boolean enabled, long pauseUntil, long now) {
        if (!incoming) return Decision.IGNORE_OUTGOING;
        // Missing call details fail open instead of rejecting an unidentified call.
        if (!hasTelephoneNumber || !enabled || pauseUntil > now) return Decision.ALLOW;
        return Decision.REJECT;
    }
}
