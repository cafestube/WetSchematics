package eu.cafestube.schematics.schematic.biome;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public record BiomeData(
        Map<Integer, String> biomePalette,
        byte[] biomes,
        BiomeDataType type
) {

    @Override
    public String toString() {
        return "BiomeData{" +
                "biomePalette=" + biomePalette +
                ", biomes=" + Arrays.toString(biomes) +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BiomeData biomeData = (BiomeData) o;
        return Objects.equals(biomePalette, biomeData.biomePalette) && Arrays.equals(biomes, biomeData.biomes) && type == biomeData.type;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(biomePalette, type);
        result = 31 * result + Arrays.hashCode(biomes);
        return result;
    }
}
