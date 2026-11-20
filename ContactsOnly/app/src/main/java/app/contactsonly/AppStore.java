package app.contactsonly;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class AppStore {
    static final String PREFS = "contacts_only";
    private static final String HISTORY = "history";
    private static final int MAX_EVENTS = 30;
    private final SharedPreferences prefs;

    AppStore(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    boolean enabled() { return prefs.getBoolean("enabled", false); }
    long pauseUntil() { return prefs.getLong("pause_until", 0); }

    void setEnabled(boolean enabled) {
        prefs.edit().putBoolean("enabled", enabled).putLong("pause_until", 0).apply();
    }

    void pause() {
        prefs.edit().putLong("pause_until", System.currentTimeMillis() + 30 * 60 * 1000L).apply();
    }

    void resume() { prefs.edit().putLong("pause_until", 0).apply(); }

    JSONArray history() {
        try { return new JSONArray(prefs.getString(HISTORY, "[]")); }
        catch (JSONException ignored) { return new JSONArray(); }
    }

    void record(String number, boolean rejected, long time) {
        // Called after respondToCall so history never delays the screening decision.
        try {
            JSONArray previous = history();
            JSONArray next = new JSONArray();
            JSONObject event = new JSONObject();
            event.put("number", number);
            event.put("rejected", rejected);
            event.put("time", time);
            next.put(event);
            for (int i = 0; i < previous.length() && i < MAX_EVENTS - 1; i++) {
                next.put(previous.get(i));
            }
            prefs.edit().putString(HISTORY, next.toString()).apply();
        } catch (JSONException ignored) {
            // A history failure must not affect calls.
        }
    }

    void clearHistory() { prefs.edit().remove(HISTORY).apply(); }
}
