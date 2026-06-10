package com.spasc.module;

import java.util.List;

final class ArtistSeparatorConfig {
    final List<String> separators;
    final List<String> excludedArtists;
    final String signature;
    final boolean readable;
    final boolean hasSeparatorSetting;
    final boolean hasExcludedArtistsSetting;
    final String source;

    ArtistSeparatorConfig(
            List<String> separators,
            List<String> excludedArtists,
            String signature,
            boolean readable,
            boolean hasSeparatorSetting,
            boolean hasExcludedArtistsSetting,
            String source) {
        this.separators = separators;
        this.excludedArtists = excludedArtists;
        this.signature = signature;
        this.readable = readable;
        this.hasSeparatorSetting = hasSeparatorSetting;
        this.hasExcludedArtistsSetting = hasExcludedArtistsSetting;
        this.source = source;
    }
}
