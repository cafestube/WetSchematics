package eu.cafestube.schematics.schematic;

import eu.cafestube.schematics.math.BlockPos;
import eu.cafestube.schematics.schematic.biome.BiomeData;
import eu.cafestube.schematics.schematic.biome.BiomeDataType;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record Schematic(
        int dataVersion,
        int width,
        int height,
        int length,
        CompoundBinaryTag metadata,
        BlockPos offset,

        Map<Integer, String> blockStatePalette,
        byte[] blockData,

        Map<BlockPos, BlockEntity> blockEntities,
        List<Entity> entities,

        @Nullable
        BiomeData biomeData
) {

    public static final int NO_DATA_VERSION = -1;

    @Override
    public String toString() {
        return "Schematic{" +
                "dataVersion=" + dataVersion +
                ", width=" + width +
                ", height=" + height +
                ", length=" + length +
                ", metadata=" + metadata +
                ", offset=" + offset +
                ", blockStatePalette=" + blockStatePalette +
                ", blockData=" + Arrays.toString(blockData) +
                ", blockEntities=" + blockEntities +
                ", entities=" + entities +
                ", biomeData=" + biomeData +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schematic schematic = (Schematic) o;
        return dataVersion == schematic.dataVersion && width == schematic.width && height == schematic.height
                && length == schematic.length && Objects.equals(metadata, schematic.metadata) && Objects.equals(offset, schematic.offset)
                && Objects.equals(blockStatePalette, schematic.blockStatePalette)
                && Arrays.equals(blockData, schematic.blockData) && Objects.equals(blockEntities, schematic.blockEntities)
                && Objects.equals(entities, schematic.entities) && Objects.equals(biomeData, schematic.biomeData);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(dataVersion, width, height, length, metadata, offset, blockStatePalette, blockEntities, entities, biomeData);
        result = 31 * result + Arrays.hashCode(blockData);
        return result;
    }

    public String getBlockData(int x, int y, int z) {
        return blockStatePalette.get(getPaletteStateId(x, y, z));
    }

    public int getPaletteStateId(int x, int y, int z) {
        int index = (x + z * width + y * width * length);
        return blockData[index];
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
