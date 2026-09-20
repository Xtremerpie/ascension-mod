package com.xtremerpie.ascension.structures;

public record StructurePlacementResult(boolean success, String message, int blocksPlaced) {
    public static StructurePlacementResult failed(String message) {
        return new StructurePlacementResult(false, message, 0);
    }

    public static StructurePlacementResult success(int blocksPlaced) {
        return new StructurePlacementResult(true, "Structure built.", blocksPlaced);
    }
}
