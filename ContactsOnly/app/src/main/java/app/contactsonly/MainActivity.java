package app.contactsonly;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.role.RoleManager;
import android.content.ActivityNotFoundException;
import android.content.SharedPreferences;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.DateFormat;
import java.util.Date;

public final class MainActivity extends Activity {
    private AppStore store;
    private RoleManager roles;
    private Switch blockingSwitch;
    private TextView statusTitle;
    private TextView statusDetail;
    private TextView diagnostics;
    private Button setupButton;
    private Button pauseButton;
    private Button clearButton;
    private LinearLayout historyList;
    private boolean rendering;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            renderStatus();
            handler.postDelayed(this, 1000);
        }
    };
    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceListener =
            (preferences, key) -> { renderStatus(); renderHistory(); };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        View root = findViewById(R.id.root);
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                Insets bars = insets.getInsets(WindowInsets.Type.systemBars()
                        | WindowInsets.Type.displayCutout());
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            });
            root.requestApplyInsets();
        } else {
            root.setFitsSystemWindows(true);
        }

        store = new AppStore(this);
        roles = getSystemService(RoleManager.class);
        blockingSwitch = findViewById(R.id.blocking_switch);
        statusTitle = findViewById(R.id.status_title);
        statusDetail = findViewById(R.id.status_detail);
        diagnostics = findViewById(R.id.diagnostics);
        setupButton = findViewById(R.id.setup_button);
        pauseButton = findViewById(R.id.pause_button);
        clearButton = findViewById(R.id.clear_button);
        historyList = findViewById(R.id.history_list);

        blockingSwitch.setOnCheckedChangeListener((button, checked) -> {
            if (rendering) return;
            store.setEnabled(checked && roleHeld());
            renderStatus();
        });
        setupButton.setOnClickListener(view -> requestScreeningRole());
        pauseButton.setOnClickListener(view -> {
            if (store.pauseUntil() > System.currentTimeMillis()) store.resume();
            else store.pause();
            renderStatus();
        });
        clearButton.setOnClickListener(view -> {
            store.clearHistory();
            renderHistory();
        });
        findViewById(R.id.test_button).setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle(R.string.test_title)
                .setMessage(R.string.test_body)
                .setPositiveButton(R.string.close, null)
                .show());
    }

    @Override protected void onResume() {
        super.onResume();
        // If the user selects another screening app, clearly reset our switch.
        if (!roleHeld() && store.enabled()) store.setEnabled(false);
        getSharedPreferences(AppStore.PREFS, MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(preferenceListener);
        renderStatus();
        renderHistory();
        handler.postDelayed(tick, 1000);
    }

    @Override protected void onPause() {
        handler.removeCallbacks(tick);
        getSharedPreferences(AppStore.PREFS, MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(preferenceListener);
        super.onPause();
    }

    private boolean roleAvailable() {
        return roles != null && roles.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING);
    }

    private boolean roleHeld() {
        return roles != null && roles.isRoleHeld(RoleManager.ROLE_CALL_SCREENING);
    }

    @SuppressWarnings("deprecation")
    private void requestScreeningRole() {
        if (!roleAvailable() || roleHeld()) return;
        try {
            startActivityForResult(roles.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING), 1);
        } catch (ActivityNotFoundException | SecurityException exception) {
            Toast.makeText(this, R.string.setup_error, Toast.LENGTH_LONG).show();
        }
    }

    private void renderStatus() {
        boolean available = roleAvailable();
        boolean held = roleHeld();
        boolean enabled = store.enabled();
        long now = System.currentTimeMillis();
        long pauseUntil = store.pauseUntil();
        boolean paused = enabled && pauseUntil > now;

        rendering = true;
        blockingSwitch.setChecked(held && enabled);
        blockingSwitch.setEnabled(available && held);
        rendering = false;
        setupButton.setEnabled(available && !held);
        setupButton.setText(!available ? R.string.setup_unavailable
                : held ? R.string.setup_done : R.string.setup_button);
        pauseButton.setEnabled(available && held && enabled);
        pauseButton.setText(paused ? R.string.resume : R.string.pause);

        if (!available) {
            statusTitle.setText(R.string.status_unavailable);
            statusDetail.setText(R.string.detail_unavailable);
        } else if (!held) {
            statusTitle.setText(R.string.status_setup);
            statusDetail.setText(R.string.detail_setup);
        } else if (!enabled) {
            statusTitle.setText(R.string.status_off);
            statusDetail.setText(R.string.detail_off);
        } else if (paused) {
            long minutes = Math.max(1, (pauseUntil - now + 59_999) / 60_000);
            statusTitle.setText(getString(R.string.status_paused, minutes));
            String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(pauseUntil));
            statusDetail.setText(getString(R.string.detail_paused, time));
        } else {
            statusTitle.setText(R.string.status_on);
            statusDetail.setText(R.string.detail_on);
        }
        diagnostics.setText(getString(R.string.diagnostics, Build.MANUFACTURER, Build.MODEL,
                Build.VERSION.RELEASE, getString(available ? R.string.yes : R.string.no),
                getString(held ? R.string.yes : R.string.no)));
    }

    private void renderHistory() {
        JSONArray events = store.history();
        historyList.removeAllViews();
        clearButton.setVisibility(events.length() > 0 ? View.VISIBLE : View.GONE);
        if (events.length() == 0) {
            TextView empty = new TextView(this);
            empty.setText(R.string.history_empty);
            empty.setTextSize(15);
            empty.setTextColor(getColor(R.color.muted));
            empty.setPadding(0, dp(8), 0, dp(8));
            historyList.addView(empty);
            return;
        }
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.optJSONObject(i);
            if (event == null) continue;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, dp(12), 0, dp(12));
            TextView number = new TextView(this);
            number.setText(event.optString("number", getString(R.string.number_unavailable)));
            number.setTextDirection(View.TEXT_DIRECTION_LTR);
            number.setTextIsSelectable(true);
            number.setTextSize(17);
            number.setTextColor(getColor(R.color.ink));
            row.addView(number);
            TextView detail = new TextView(this);
            String result = getString(event.optBoolean("rejected")
                    ? R.string.event_rejected : R.string.event_allowed);
            detail.setText(getString(R.string.event_detail, result,
                    format.format(new Date(event.optLong("time")))));
            detail.setTextSize(13);
            detail.setTextColor(getColor(R.color.muted));
            detail.setPadding(0, dp(4), 0, 0);
            row.addView(detail);
            historyList.addView(row);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
