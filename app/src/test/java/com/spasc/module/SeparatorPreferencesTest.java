package com.spasc.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public final class SeparatorPreferencesTest {
    @Test
    public void applyRequestVersionInvalidatesArtistCacheSignature() {
        String firstApply = ArtistCacheSignature.build("/", "Leo/need", 1L);
        String secondApply = ArtistCacheSignature.build("/", "Leo/need", 2L);

        assertNotEquals(firstApply, secondApply);
    }

    @Test
    public void sameApplyRequestVersionKeepsArtistCacheSignatureStable() {
        assertEquals(
                ArtistCacheSignature.build("/", "Leo/need", 1L),
                ArtistCacheSignature.build("/", "Leo/need", 1L));
    }
}
