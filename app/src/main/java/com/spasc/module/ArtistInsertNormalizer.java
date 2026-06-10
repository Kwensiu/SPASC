package com.spasc.module;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import de.robv.android.xposed.XposedBridge;

final class ArtistInsertNormalizer {
    private ArtistInsertNormalizer() {
    }

    static Object normalize(
            Object value,
            ArtistSeparatorConfig config,
            ClassLoader classLoader,
            String artistClassName) {
        if (!(value instanceof List<?> source)
                || config.separators.isEmpty()
                || source.isEmpty()) {
            return value;
        }
        try {
            ArtistReflection reflection = new ArtistReflection(
                    Class.forName(artistClassName, false, classLoader));
            LinkedHashMap<String, ArtistRow> rows = new LinkedHashMap<>();
            boolean changed = false;
            for (Object artist : source) {
                String originalName = reflection.getName(artist);
                List<String> splitNames = ArtistSeparatorRules.splitArtists(
                        originalName,
                        config.separators,
                        config.excludedArtists);
                if (splitNames.size() != 1 || !splitNames.get(0).equals(originalName)) {
                    changed = true;
                }
                for (String splitName : splitNames) {
                    ArtistRow row = rows.get(splitName);
                    if (row == null) {
                        rows.put(splitName, ArtistRow.from(artist, splitName, reflection));
                    } else {
                        row.count += reflection.getCount(artist);
                    }
                }
            }
            if (!changed) {
                return value;
            }
            ArrayList<Object> normalized = new ArrayList<>(rows.size());
            for (ArtistRow row : rows.values()) {
                normalized.add(row.toArtist(reflection));
            }
            XposedBridge.log("SPCAD normalized Artist insert list: before="
                    + source.size()
                    + ", after="
                    + normalized.size());
            return normalized;
        } catch (Throwable throwable) {
            XposedBridge.log("SPCAD failed to normalize Artist insert list");
            XposedBridge.log(throwable);
            return value;
        }
    }

    private static final class ArtistReflection {
        private final Constructor<?> constructor;
        private final Method getName;
        private final Method isAlbumArtist;
        private final Method getCount;
        private final Method getCover;
        private final Method getCoverRealPath;
        private final Method getCoverModified;

        ArtistReflection(Class<?> artistClass) throws NoSuchMethodException {
            constructor = artistClass.getConstructor(
                    String.class,
                    String.class,
                    boolean.class,
                    int.class,
                    String.class,
                    String.class,
                    long.class);
            getName = artistClass.getMethod("getName");
            isAlbumArtist = artistClass.getMethod("isAlbumArtist");
            getCount = artistClass.getMethod("getCount");
            getCover = artistClass.getMethod("getCover");
            getCoverRealPath = artistClass.getMethod("getCoverRealPath");
            getCoverModified = artistClass.getMethod("getCoverModified");
        }

        String getName(Object artist) throws ReflectiveOperationException {
            return (String) getName.invoke(artist);
        }

        boolean isAlbumArtist(Object artist) throws ReflectiveOperationException {
            return (Boolean) isAlbumArtist.invoke(artist);
        }

        int getCount(Object artist) throws ReflectiveOperationException {
            return (Integer) getCount.invoke(artist);
        }

        String getCover(Object artist) throws ReflectiveOperationException {
            return (String) getCover.invoke(artist);
        }

        String getCoverRealPath(Object artist) throws ReflectiveOperationException {
            return (String) getCoverRealPath.invoke(artist);
        }

        long getCoverModified(Object artist) throws ReflectiveOperationException {
            return (Long) getCoverModified.invoke(artist);
        }

        Object newArtist(ArtistRow row) throws ReflectiveOperationException {
            return constructor.newInstance(
                    UUID.randomUUID().toString(),
                    row.name,
                    row.albumArtist,
                    row.count,
                    row.cover,
                    row.coverRealPath,
                    row.coverModified);
        }
    }

    private static final class ArtistRow {
        final String name;
        final boolean albumArtist;
        int count;
        final String cover;
        final String coverRealPath;
        final long coverModified;

        private ArtistRow(
                String name,
                boolean albumArtist,
                int count,
                String cover,
                String coverRealPath,
                long coverModified) {
            this.name = name;
            this.albumArtist = albumArtist;
            this.count = count;
            this.cover = cover;
            this.coverRealPath = coverRealPath;
            this.coverModified = coverModified;
        }

        static ArtistRow from(
                Object artist,
                String name,
                ArtistReflection reflection) throws ReflectiveOperationException {
            return new ArtistRow(
                    name,
                    reflection.isAlbumArtist(artist),
                    reflection.getCount(artist),
                    reflection.getCover(artist),
                    reflection.getCoverRealPath(artist),
                    reflection.getCoverModified(artist));
        }

        Object toArtist(ArtistReflection reflection) throws ReflectiveOperationException {
            return reflection.newArtist(this);
        }
    }
}
