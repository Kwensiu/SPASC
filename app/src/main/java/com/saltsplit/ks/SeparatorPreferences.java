package com.saltsplit.ks;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;

import de.robv.android.xposed.XSharedPreferences;

final class SeparatorPreferences {
    private static final String PREFS_NAME = "artist_separator";
    private static final String KEY_SEPARATORS = "separators";
    private static final String KEY_EXCLUDED_ARTISTS = "excluded_artists";
    static final String KEY_APPLY_REQUEST_VERSION = "apply_request_version";
    private static final String MODULE_PACKAGE = "com.saltsplit.ks";
    static final String CONFIG_AUTHORITY = MODULE_PACKAGE + ".config";
    static final Uri CONFIG_URI = Uri.parse("content://" + CONFIG_AUTHORITY + "/artist_separator");

    private SeparatorPreferences() {
    }

    static SharedPreferences appPreferences(Context context) {
        try {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_READABLE);
        } catch (SecurityException ignored) {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    static String getSeparatorText(Context context) {
        return appPreferences(context)
                .getString(KEY_SEPARATORS, ArtistSeparatorRules.DEFAULT_SEPARATORS_TEXT);
    }

    static String getExcludedArtistsText(Context context) {
        return appPreferences(context).getString(KEY_EXCLUDED_ARTISTS, "");
    }

    static long getApplyRequest(Context context) {
        return appPreferences(context).getLong(KEY_APPLY_REQUEST_VERSION, 0L);
    }

    static boolean saveSettings(Context context, String separatorsText, String excludedArtistsText) {
        boolean saved = appPreferences(context)
                .edit()
                .putString(KEY_SEPARATORS, separatorsText)
                .putString(KEY_EXCLUDED_ARTISTS, excludedArtistsText)
                .commit();
        publishPreferenceFile(context);
        return saved;
    }

    static boolean saveSettingsAndRequestApply(
            Context context,
            String separatorsText,
            String excludedArtistsText) {
        long applyRequestVersion = getApplyRequest(context) + 1L;
        boolean saved = appPreferences(context)
                .edit()
                .putString(KEY_SEPARATORS, separatorsText)
                .putString(KEY_EXCLUDED_ARTISTS, excludedArtistsText)
                .putLong(KEY_APPLY_REQUEST_VERSION, applyRequestVersion)
                .commit();
        publishPreferenceFile(context);
        return saved;
    }

    static ArtistSeparatorConfig loadForXposed() {
        return loadForXposed(null);
    }

    static ArtistSeparatorConfig loadForXposed(Context context) {
        ArtistSeparatorConfig providerConfig = loadFromProvider(context);
        if (providerConfig != null) {
            return providerConfig;
        }
        XSharedPreferences preferences = new XSharedPreferences(MODULE_PACKAGE, PREFS_NAME);
        preferences.reload();
        boolean readable = preferences.getFile().canRead();
        boolean hasSeparatorSetting = preferences.contains(KEY_SEPARATORS);
        boolean hasExcludedArtistsSetting = preferences.contains(KEY_EXCLUDED_ARTISTS);
        String separatorsText = preferences.getString(
                KEY_SEPARATORS,
                ArtistSeparatorRules.DEFAULT_SEPARATORS_TEXT);
        String excludedArtistsText = preferences.getString(KEY_EXCLUDED_ARTISTS, "");
        long applyRequestVersion = preferences.getLong(KEY_APPLY_REQUEST_VERSION, 0L);
        return new ArtistSeparatorConfig(
                ArtistSeparatorRules.parseSeparators(separatorsText),
                ArtistSeparatorRules.parseExcludedArtists(excludedArtistsText),
                ArtistCacheSignature.build(
                        separatorsText,
                        excludedArtistsText,
                        applyRequestVersion),
                readable,
                hasSeparatorSetting,
                hasExcludedArtistsSetting,
                "xshared");
    }

    private static void publishPreferenceFile(Context context) {
        File dataDir = new File(context.getApplicationInfo().dataDir);
        File prefsFile = new File(
                context.getApplicationInfo().dataDir + "/shared_prefs/" + PREFS_NAME + ".xml");
        File prefsDir = prefsFile.getParentFile();
        dataDir.setExecutable(true, false);
        dataDir.setReadable(true, false);
        if (prefsDir != null) {
            prefsDir.setExecutable(true, false);
            prefsDir.setReadable(true, false);
        }
        prefsFile.setReadable(true, false);
    }

    private static ArtistSeparatorConfig loadFromProvider(Context context) {
        if (context == null) {
            return null;
        }
        try (Cursor cursor = context.getContentResolver().query(
                CONFIG_URI,
                null,
                null,
                null,
                null)) {
            if (cursor == null || !cursor.moveToFirst()) {
                return null;
            }
            String separatorsText = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SEPARATORS));
            String excludedArtistsText = cursor.getString(
                    cursor.getColumnIndexOrThrow(KEY_EXCLUDED_ARTISTS));
            long applyRequestVersion = cursor.getLong(
                    cursor.getColumnIndexOrThrow(KEY_APPLY_REQUEST_VERSION));
            return new ArtistSeparatorConfig(
                    ArtistSeparatorRules.parseSeparators(separatorsText),
                    ArtistSeparatorRules.parseExcludedArtists(excludedArtistsText),
                    ArtistCacheSignature.build(
                            separatorsText,
                            excludedArtistsText,
                            applyRequestVersion),
                    true,
                    true,
                    true,
                    "provider");
        } catch (Throwable ignored) {
            return null;
        }
    }
}
