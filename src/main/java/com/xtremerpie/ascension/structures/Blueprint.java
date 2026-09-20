package com.xtremerpie.ascension.structures;

import java.util.Map;

public record Blueprint(
        String id,
        String name,
        String description,
        int sizeX,
        int sizeY,
        int sizeZ,
        Map<String, Integer> materials // block id -> count, informational display only
) {
}
