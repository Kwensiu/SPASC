package com.spasc.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ArtistSeparatorRules {
    static final String DEFAULT_SEPARATORS_TEXT = "";

    private ArtistSeparatorRules() {
    }

    static List<String> parseSeparators(String text) {
        if (text == null) {
            return Collections.emptyList();
        }
        ArrayList<String> separators = new ArrayList<>();
        String[] lines = text.replace("\r\n", "\n").replace('\r', '\n').split("\n");
        for (String line : lines) {
            if (!line.trim().isEmpty() && !separators.contains(line)) {
                separators.add(line);
            }
        }
        return separators;
    }

    static List<String> parseExcludedArtists(String text) {
        if (text == null) {
            return Collections.emptyList();
        }
        ArrayList<String> artists = new ArrayList<>();
        String[] lines = text.replace("\r\n", "\n").replace('\r', '\n').split("\n");
        for (String line : lines) {
            String artist = line.trim();
            if (!artist.isEmpty() && !artists.contains(artist)) {
                artists.add(artist);
            }
        }
        return artists;
    }

    static List<String> splitArtists(String artist, List<String> separators) {
        return splitArtists(artist, separators, Collections.emptyList());
    }

    static List<String> splitArtists(
            String artist,
            List<String> separators,
            List<String> excludedArtists) {
        if (artist == null) {
            return Collections.emptyList();
        }
        if (separators != null && separators.isEmpty()) {
            return normalizeSingleArtist(artist);
        }
        List<String> activeSeparators = separators == null ? Collections.emptyList() : separators;
        ArrayList<ArtistPart> artists = protectExcludedArtists(
                artist,
                activeSeparators,
                excludedArtists);
        for (String separator : activeSeparators) {
            if (separator == null || separator.isEmpty()) {
                continue;
            }
            ArrayList<ArtistPart> next = new ArrayList<>();
            for (ArtistPart item : artists) {
                if (item.protectedArtist) {
                    next.add(item);
                } else {
                    addSplitParts(next, item.text, separator);
                }
            }
            artists = next;
        }
        ArrayList<String> normalized = new ArrayList<>();
        for (ArtistPart item : artists) {
            String trimmed = item.text.trim();
            if (!trimmed.isEmpty() && !normalized.contains(trimmed)) {
                normalized.add(trimmed);
            }
        }
        return normalized;
    }

    private static ArrayList<ArtistPart> protectExcludedArtists(
            String artist,
            List<String> separators,
            List<String> excludedArtists) {
        ArrayList<ArtistPart> parts = new ArrayList<>();
        if (excludedArtists == null || excludedArtists.isEmpty()) {
            parts.add(new ArtistPart(artist, false));
            return parts;
        }
        int index = 0;
        while (index < artist.length()) {
            ProtectedMatch match = findNextProtectedArtist(
                    artist,
                    index,
                    separators,
                    excludedArtists);
            if (match == null) {
                parts.add(new ArtistPart(artist.substring(index), false));
                break;
            }
            if (match.start > index) {
                parts.add(new ArtistPart(artist.substring(index, match.start), false));
            }
            parts.add(new ArtistPart(match.artist, true));
            index = match.start + match.artist.length();
        }
        return parts;
    }

    private static List<String> normalizeSingleArtist(String artist) {
        String trimmed = artist.trim();
        if (trimmed.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(trimmed);
    }

    private static void addSplitParts(List<ArtistPart> target, String source, String separator) {
        int start = 0;
        int index;
        while ((index = source.indexOf(separator, start)) >= 0) {
            target.add(new ArtistPart(source.substring(start, index), false));
            start = index + separator.length();
        }
        target.add(new ArtistPart(source.substring(start), false));
    }

    private static ProtectedMatch findNextProtectedArtist(
            String source,
            int start,
            List<String> separators,
            List<String> excludedArtists) {
        ProtectedMatch best = null;
        for (String excludedArtist : excludedArtists) {
            if (excludedArtist == null || excludedArtist.isEmpty()) {
                continue;
            }
            int index = source.indexOf(excludedArtist, start);
            while (index >= 0) {
                int end = index + excludedArtist.length();
                if (isArtistBoundary(source, index, end, separators)
                        && (best == null
                        || index < best.start
                        || (index == best.start
                        && excludedArtist.length() > best.artist.length()))) {
                    best = new ProtectedMatch(index, excludedArtist);
                    break;
                }
                index = source.indexOf(excludedArtist, index + 1);
            }
        }
        return best;
    }

    private static boolean isArtistBoundary(
            String source,
            int start,
            int end,
            List<String> separators) {
        return isStartBoundary(source, start, separators) && isEndBoundary(source, end, separators);
    }

    private static boolean isStartBoundary(String source, int start, List<String> separators) {
        if (source.substring(0, start).trim().isEmpty()) {
            return true;
        }
        for (String separator : separators) {
            if (separator != null
                    && !separator.isEmpty()
                    && start >= separator.length()
                    && source.substring(start - separator.length(), start).equals(separator)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEndBoundary(String source, int end, List<String> separators) {
        if (source.substring(end).trim().isEmpty()) {
            return true;
        }
        for (String separator : separators) {
            if (separator != null
                    && !separator.isEmpty()
                    && end + separator.length() <= source.length()
                    && source.substring(end, end + separator.length()).equals(separator)) {
                return true;
            }
        }
        return false;
    }

    private static final class ArtistPart {
        final String text;
        final boolean protectedArtist;

        ArtistPart(String text, boolean protectedArtist) {
            this.text = text;
            this.protectedArtist = protectedArtist;
        }
    }

    private static final class ProtectedMatch {
        final int start;
        final String artist;

        ProtectedMatch(int start, String artist) {
            this.start = start;
            this.artist = artist;
        }
    }
}
