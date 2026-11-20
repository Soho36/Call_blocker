package app.contactsonly;

import android.net.Uri;
import android.telecom.Call;
import android.telecom.CallScreeningService;

/** Android binds this service on demand; a permanently running background app is unnecessary. */
public final class ContactsScreeningService extends CallScreeningService {
    @Override
    public void onScreenCall(Call.Details details) {
        boolean incoming = details.getCallDirection() == Call.Details.DIRECTION_INCOMING;
        // Outgoing callbacks must never interfere with placing calls.
        if (!incoming) return;

        Uri handle = details.getHandle();
        String number = handle == null ? null : handle.getSchemeSpecificPart();
        boolean validNumber = handle != null && "tel".equals(handle.getScheme())
                && number != null && !number.trim().isEmpty();
        long now = System.currentTimeMillis();
        AppStore store = new AppStore(this);
        ScreeningPolicy.Decision decision = ScreeningPolicy.decide(
                true, validNumber, store.enabled(), store.pauseUntil(), now);
        boolean reject = decision == ScreeningPolicy.Decision.REJECT;

        // READ_CONTACTS is deliberately absent from our manifest. Android therefore
        // sends only non-contact numbers here. Adding that permission would invalidate
        // this design: an explicit contact lookup would then be required.
        respondToCall(details, new CallResponse.Builder()
                .setDisallowCall(reject)
                .setRejectCall(reject)
                .setSkipCallLog(false)
                .setSkipNotification(reject)
                .build());

        store.record(validNumber ? number : getString(R.string.number_unavailable), reject, now);
    }
}
