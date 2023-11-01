package eu.cafestube.schematics.schematic.biome;

import java.util.Map;

public record BiomeData(
        Map<Integer, String> biomePalette,
        byte[] biomes,
        BiomeDataType type
) {}
