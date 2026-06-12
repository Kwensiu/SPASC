package com.saltsplit.ks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowInsets;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class MainActivity extends Activity {
    private static final String TAG = "SaltSplit";
    private static final String SOURCE_CODE_URL = "https://github.com/Kwensiu/SaltSplit";
    private static final String UPDATE_MANIFEST_URL =
            "https://github.com/Kwensiu/SaltSplit/releases/latest/download/update.json";
    private static final int UPDATE_SCHEMA_VERSION = 1;

    private EditText separatorInput;
    private EditText excludedArtistsInput;
    private boolean inputsReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(20);
        root.setPadding(padding, padding, padding, padding);
        applyStatusBarPadding(root, padding);
        scrollView.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(this);
        title.setText(getString(R.string.app_name));
        title.setTextSize(24);
        title.setGravity(Gravity.START);
        root.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView status = new TextView(this);
        status.setText(getString(R.string.status_enabled));
        status.setPadding(0, dp(8), 0, dp(18));
        root.addView(status, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        addSectionTitle(root, R.string.separator_label);
        addBodyText(root, R.string.separator_summary, 0, dp(6));

        separatorInput = new EditText(this);
        separatorInput.setMinLines(3);
        separatorInput.setMaxLines(6);
        separatorInput.setGravity(Gravity.TOP | Gravity.START);
        separatorInput.setSingleLine(false);
        separatorInput.setHint(R.string.separator_hint);
        separatorInput.setText(SeparatorPreferences.getSeparatorText(this));
        root.addView(separatorInput, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        addSectionTitle(root, R.string.excluded_artists_label);
        addBodyText(root, R.string.excluded_artists_summary, 0, dp(6));

        excludedArtistsInput = new EditText(this);
        excludedArtistsInput.setMinLines(3);
        excludedArtistsInput.setMaxLines(6);
        excludedArtistsInput.setGravity(Gravity.TOP | Gravity.START);
        excludedArtistsInput.setSingleLine(false);
        excludedArtistsInput.setHint(R.string.excluded_artists_hint);
        excludedArtistsInput.setText(SeparatorPreferences.getExcludedArtistsText(this));
        root.addView(excludedArtistsInput, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        addBodyText(root, R.string.cache_notice, dp(12), 0);

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.END);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(12), 0, 0);

        Button sourceCode = new Button(this);
        sourceCode.setText(getString(R.string.source_code));
        sourceCode.setOnClickListener(view -> openSourceCode());
        actions.addView(sourceCode, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        Button save = new Button(this);
        save.setText(getString(R.string.separator_save));
        save.setOnClickListener(view -> saveSeparators());
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        saveParams.setMargins(dp(8), 0, 0, 0);
        actions.addView(save, saveParams);

        root.addView(actions, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        Button checkUpdates = new Button(this);
        checkUpdates.setText(getString(R.string.check_updates));
        checkUpdates.setOnClickListener(view -> checkForUpdates());
        LinearLayout.LayoutParams updateParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        updateParams.setMargins(0, dp(12), 0, 0);
        root.addView(checkUpdates, updateParams);

        setContentView(scrollView);
        inputsReady = true;
        TextWatcher autosave = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveSettings();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        separatorInput.addTextChangedListener(autosave);
        excludedArtistsInput.addTextChangedListener(autosave);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (separatorInput != null && excludedArtistsInput != null) {
            saveSettings();
        }
    }

    private void saveSeparators() {
        boolean saved = saveSettingsAndRequestApply();
        Toast.makeText(
                this,
                saved ? R.string.separator_saved : R.string.separator_save_failed,
                Toast.LENGTH_SHORT).show();
    }

    private void openSourceCode() {
        openExternalUrl(SOURCE_CODE_URL);
    }

    private void checkForUpdates() {
        Toast.makeText(this, R.string.checking_updates, Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                UpdateInfo updateInfo = fetchUpdateInfo();
                runOnUiThread(() -> showUpdateResult(updateInfo));
            } catch (Exception e) {
                Log.w(TAG, "Failed to check updates", e);
                runOnUiThread(() -> Toast.makeText(
                        this,
                        R.string.update_failed,
                        Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private UpdateInfo fetchUpdateInfo() throws Exception {
        HttpURLConnection connection =
                (HttpURLConnection) new URL(UPDATE_MANIFEST_URL).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("Accept", "application/json");
        try {
            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new IllegalStateException("HTTP " + code);
            }
            StringBuilder json = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
            }
            JSONObject object = new JSONObject(json.toString());
            return parseUpdateInfo(object);
        } finally {
            connection.disconnect();
        }
    }

    private UpdateInfo parseUpdateInfo(JSONObject object) {
        int schemaVersion = object.optInt("schemaVersion", 0);
        if (schemaVersion != UPDATE_SCHEMA_VERSION) {
            throw new IllegalArgumentException("Unsupported update schema: " + schemaVersion);
        }
        String packageName = object.optString("packageName", "");
        if (!BuildConfig.APPLICATION_ID.equals(packageName)) {
            throw new IllegalArgumentException("Unexpected update package: " + packageName);
        }
        String versionName = object.optString("versionName", "");
        int versionCode = object.optInt("versionCode", 0);
        String apkUrl = object.optString("apkUrl", "");
        String releaseUrl = object.optString("releaseUrl", "");
        if (versionName.isEmpty() || versionCode <= 0 || apkUrl.isEmpty() || releaseUrl.isEmpty()) {
            throw new IllegalArgumentException("Incomplete update manifest");
        }
        return new UpdateInfo(versionName, versionCode, apkUrl, releaseUrl);
    }

    private void showUpdateResult(UpdateInfo updateInfo) {
        if (updateInfo.versionCode <= BuildConfig.VERSION_CODE) {
            Toast.makeText(this, R.string.latest_version, Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.update_found, updateInfo.versionName));
        builder.setMessage(updateInfo.releaseUrl);
        builder.setPositiveButton(R.string.update_download, (dialog, which) -> {
            openExternalUrl(updateInfo.apkUrl);
        });
        builder.setNegativeButton(R.string.update_release_notes, (dialog, which) -> {
            openExternalUrl(updateInfo.releaseUrl);
        });
        builder.setNeutralButton(android.R.string.cancel, null);
        builder.show();
    }

    private void openExternalUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException | IllegalArgumentException e) {
            Log.w(TAG, "Failed to open external URL: " + url, e);
            Toast.makeText(this, R.string.open_url_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean saveSettings() {
        if (!inputsReady) {
            return false;
        }
        boolean saved = SeparatorPreferences.saveSettings(
                this,
                separatorInput.getText().toString(),
                excludedArtistsInput.getText().toString());
        Log.i(TAG, "Saved artist separator settings: saved="
                + saved
                + ", separatorsLength="
                + separatorInput.length()
                + ", excludedArtistsLength="
                + excludedArtistsInput.length());
        return saved;
    }

    private boolean saveSettingsAndRequestApply() {
        if (!inputsReady) {
            return false;
        }
        boolean saved = SeparatorPreferences.saveSettingsAndRequestApply(
                this,
                separatorInput.getText().toString(),
                excludedArtistsInput.getText().toString());
        Log.i(TAG, "Saved and applied artist separator settings: saved="
                + saved
                + ", separatorsLength="
                + separatorInput.length()
                + ", excludedArtistsLength="
                + excludedArtistsInput.length());
        return saved;
    }

    private void addSectionTitle(LinearLayout root, int stringRes) {
        TextView view = new TextView(this);
        view.setText(getString(stringRes));
        view.setTextSize(18);
        view.setGravity(Gravity.START);
        view.setPadding(0, dp(4), 0, dp(4));
        root.addView(view, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addBodyText(LinearLayout root, int stringRes, int topPadding, int bottomPadding) {
        TextView view = new TextView(this);
        view.setText(getString(stringRes));
        view.setTextSize(14);
        view.setPadding(0, topPadding, 0, bottomPadding);
        root.addView(view, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void applyStatusBarPadding(LinearLayout root, int basePadding) {
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            int statusBarHeight = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                statusBarHeight = insets.getInsets(WindowInsets.Type.statusBars()).top;
            } else {
                statusBarHeight = insets.getSystemWindowInsetTop();
            }
            view.setPadding(basePadding, basePadding + statusBarHeight, basePadding, basePadding);
            return insets;
        });
    }

    private static final class UpdateInfo {
        final String versionName;
        final int versionCode;
        final String apkUrl;
        final String releaseUrl;

        UpdateInfo(String versionName, int versionCode, String apkUrl, String releaseUrl) {
            this.versionName = versionName;
            this.versionCode = versionCode;
            this.apkUrl = apkUrl;
            this.releaseUrl = releaseUrl;
        }
    }
}
