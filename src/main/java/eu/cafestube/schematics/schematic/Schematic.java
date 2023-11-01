package eu.cafestube.schematics.schematic;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import eu.cafestube.schematics.math.BlockPos;
import eu.cafestube.schematics.schematic.biome.BiomeData;
import eu.cafestube.schematics.schematic.biome.BiomeDataType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record Schematic(
        int dataVersion,
        int width,
        int height,
        int length,
        CompoundTag metadata,
        BlockPos offset,

        Map<Integer, String> blockStatePalette,
        byte[] blockData,

        Map<BlockPos, BlockEntity> blockEntities,
        List<Entity> entities,

        @Nullable
        BiomeData biomeData
) {

    public static final int NO_DATA_VERSION = -1;

    public String getBlockData(int x, int y, int z) {
        int index = (x + z * width + y * width * length);
        int stateId = blockData[index];
        return blockStatePalette.get(stateId);
    }

    public @Nullable String getBiomeId(int x, int z) {
        return getBiomeId(x, 0, z);
    }

    public @Nullable String getBiomeId(int x, int y, int z) {
        if(biomeData == null) {
            return null;
        }

        Byte biomePaletteIndex = getBiomePaletteIndex(x, y, z);
        if(biomePaletteIndex == null) {
            return null;
        }
        return biomeData.biomePalette().get(biomePaletteIndex.intValue());
    }

    public Byte getBiomePaletteIndex(int x, int z) {
        return getBiomePaletteIndex(x, 0, z);
    }

    public Byte getBiomePaletteIndex(int x, int y, int z) {
        if(biomeData == null) {
            return null;
        }

        if(biomeData.type() == BiomeDataType.TWO_DIMENSIONAL) {
            return biomeData.biomes()[x + z * width];
        } else {
            return biomeData.biomes()[x + z * width + y * width * length];
        }
    }



}
