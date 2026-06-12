package com.saltsplit.ks;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public final class ArtistSeparatorRulesTest {
    @Test
    public void splitArtistsUsesLiteralCustomSeparators() {
        assertEquals(
                Arrays.asList("A", "B", "C"),
                ArtistSeparatorRules.splitArtists(
                        "A feat. B / C",
                        ArtistSeparatorRules.parseSeparators("feat.\n/")));
    }

    @Test
    public void splitArtistsTrimsEmptyPartsAndDuplicates() {
        assertEquals(
                Arrays.asList("A", "B"),
                ArtistSeparatorRules.splitArtists(
                        " A / B / A / ",
                        ArtistSeparatorRules.parseSeparators("/")));
    }

    @Test
    public void emptySeparatorsDoNotSplitArtists() {
        assertEquals(
                Arrays.asList("A/B"),
                ArtistSeparatorRules.splitArtists(
                        "A/B",
                        ArtistSeparatorRules.parseSeparators("\n  \n")));
    }

    @Test
    public void missingSeparatorsDoNotSplitArtists() {
        assertEquals(
                Arrays.asList("Leo/need"),
                ArtistSeparatorRules.splitArtists(
                        "Leo/need",
                        ArtistSeparatorRules.parseSeparators(null)));
    }

    @Test
    public void spacedSlashDoesNotSplitBandNamesWithBareSlash() {
        assertEquals(
                Arrays.asList("A", "B"),
                ArtistSeparatorRules.splitArtists(
                        "A / B",
                        ArtistSeparatorRules.parseSeparators(" / ")));
        assertEquals(
                Arrays.asList("Leo/need"),
                ArtistSeparatorRules.splitArtists(
                        "Leo/need",
                        ArtistSeparatorRules.parseSeparators(" / ")));
    }

    @Test
    public void excludedArtistNamesAreKeptAsProtectedTokens() {
        assertEquals(
                Arrays.asList("Leo/need"),
                ArtistSeparatorRules.splitArtists(
                        "Leo/need",
                        ArtistSeparatorRules.parseSeparators("/"),
                        ArtistSeparatorRules.parseExcludedArtists("Leo/need")));
    }

    @Test
    public void excludedArtistNamesCanAppearInsideSplitArtistFields() {
        assertEquals(
                Arrays.asList("Leo/need", "初音ミク", "40mP", "164"),
                ArtistSeparatorRules.splitArtists(
                        "Leo/need/初音ミク/40mP/164",
                        ArtistSeparatorRules.parseSeparators("/"),
                        ArtistSeparatorRules.parseExcludedArtists("Leo/need")));
    }

    @Test
    public void excludedArtistNamesUseSeparatorBoundaries() {
        assertEquals(
                Arrays.asList("Leo", "need"),
                ArtistSeparatorRules.splitArtists(
                        "Leo/need",
                        ArtistSeparatorRules.parseSeparators("/"),
                        ArtistSeparatorRules.parseExcludedArtists("need")));
    }

    @Test
    public void longestExcludedArtistNameWinsAtSamePosition() {
        assertEquals(
                Arrays.asList("Leo/need", "初音ミク"),
                ArtistSeparatorRules.splitArtists(
                        "Leo/need/初音ミク",
                        ArtistSeparatorRules.parseSeparators("/"),
                        ArtistSeparatorRules.parseExcludedArtists("Leo\nLeo/need")));
    }
}
