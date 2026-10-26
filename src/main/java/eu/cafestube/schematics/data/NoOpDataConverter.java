package eu.cafestube.schematics.data;

import eu.cafestube.schematics.schematic.BlockEntity;
import eu.cafestube.schematics.schematic.Entity;
import eu.cafestube.schematics.schematic.biome.BiomeData;

import java.util.Map;

public class NoOpDataConverter implements DataConverter {
    public static NoOpDataConverter INSTANCE = new NoOpDataConverter();

    @Override
    public Map<Integer, String> upgradeBlockPalette(Map<Integer, String> palette, int from) {
        return palette;
    }

    @Override
    public String upgradeBlockState(String src, int from) {
        return src;
    }

    @Override
    public BlockEntity upgradeBlockEntity(BlockEntity from) {
        return from;
    }

    @Override
    public Entity upgradeEntity(Entity entity, int from) {
        return entity;
    }

    @Override
    public BiomeData upgradeBiomeData(BiomeData biomeData, int dataVersion) {
        return biomeData;
    }

    @Override
    public String upgradeBiome(String key, int dataVersion) {
        return key;
    }

    @Override
    public int getFinalDataVersion(int source) {
        return source;
    }
}
