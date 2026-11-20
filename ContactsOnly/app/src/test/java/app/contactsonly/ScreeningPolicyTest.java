package app.contactsonly;

import org.junit.Test;
import static app.contactsonly.ScreeningPolicy.Decision.*;
import static org.junit.Assert.assertEquals;

public class ScreeningPolicyTest {
    private static final long NOW = 1_700_000_000_000L;

    @Test public void enabledUnsavedCallerIsRejected() {
        assertEquals(REJECT, ScreeningPolicy.decide(true, true, true, 0, NOW));
    }
    @Test public void offAlwaysAllows() {
        assertEquals(ALLOW, ScreeningPolicy.decide(true, true, false, 0, NOW));
    }
    @Test public void pauseAllowsUntilDeadline() {
        assertEquals(ALLOW, ScreeningPolicy.decide(true, true, true, NOW + 1, NOW));
    }
    @Test public void pauseExpiresExactlyAtDeadline() {
        assertEquals(REJECT, ScreeningPolicy.decide(true, true, true, NOW, NOW));
    }
    @Test public void expiredPauseBlocksAgain() {
        assertEquals(REJECT, ScreeningPolicy.decide(true, true, true, NOW - 1, NOW));
    }
    @Test public void outgoingCallsAreNeverScreened() {
        assertEquals(IGNORE_OUTGOING, ScreeningPolicy.decide(false, true, true, 0, NOW));
    }
    @Test public void missingNumberFailsOpen() {
        assertEquals(ALLOW, ScreeningPolicy.decide(true, false, true, 0, NOW));
    }
}
