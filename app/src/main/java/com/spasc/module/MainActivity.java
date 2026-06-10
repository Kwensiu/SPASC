package com.spasc.module;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private static final String TAG = "SPCAD";
    private static final String SOURCE_CODE_URL = "https://github.com/Kwensiu/SPASC";

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
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_CODE_URL)));
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
}
