package eu.cafestube.schematics.version;

import com.github.steveice10.opennbt.tag.builtin.*;
import eu.cafestube.schematics.math.BlockPos;
import eu.cafestube.schematics.math.Pos;
import eu.cafestube.schematics.schematic.BlockEntity;
import eu.cafestube.schematics.schematic.Entity;
import eu.cafestube.schematics.schematic.Schematic;
import eu.cafestube.schematics.schematic.biome.BiomeData;
import eu.cafestube.schematics.schematic.biome.BiomeDataType;
import eu.cafestube.schematics.util.NBTUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class V2SchematicVersion implements SchematicVersion {

    @Override
    public Schematic deserialize(CompoundTag compound) {
        int dataVersion = NBTUtil.getIntOrDefault(compound, "DataVersion", 0);

        int width = NBTUtil.getShortOrDefault(compound, "Width", (short) 0) & 0xFFFF;
        int height = NBTUtil.getShortOrDefault(compound, "Height", (short) 0) & 0xFFFF;
        int length = NBTUtil.getShortOrDefault(compound, "Length", (short) 0) & 0xFFFF;
        CompoundTag metadata = new CompoundTag("Metadata");
        if(compound.contains("Metadata")) {
            metadata = compound.get("Metadata");
        }

        BlockPos offset = BlockPos.ZERO;
        if(compound.contains("Offset")) {
            int[] offsetArray = compound.<IntArrayTag>get("Offset").getValue();
            offset = BlockPos.fromArray(offsetArray);
        }

        Map<Integer, String> blockPalette = V1SchematicVersion.parseBlockPalette(compound);
        byte[] blockData = compound.<ByteArrayTag>get("BlockData").getValue();

        Map<BlockPos, BlockEntity> blockEntityMap = parseBlockEntities(dataVersion, compound);

        ListTag entitiesTag = compound.get("Entities");
        List<Entity> entities = parseEntities(entitiesTag);

        int biomePaletteMax = NBTUtil.getIntOrDefault(compound, "BiomePaletteMax", 0);
        CompoundTag biomePaletteTag = compound.get("BiomePalette");
        if(biomePaletteTag == null || biomePaletteTag.values().size() != biomePaletteMax) {
            throw new IllegalArgumentException("Biome palette is missing or does not expect the palette max");
        }
        Map<Integer, String> biomePalette = biomePaletteTag.getValue().entrySet().stream()
                .collect(Collectors.toMap(stringObjectEntry -> ((IntTag) stringObjectEntry.getValue()).getValue(), Map.Entry::getKey));

        byte[] biomeData = compound.<ByteArrayTag>get("BiomeData").getValue();

        return new Schematic(dataVersion, width, height, length, metadata, offset, blockPalette, blockData,
                blockEntityMap, entities, new BiomeData(biomePalette, biomeData, BiomeDataType.TWO_DIMENSIONAL));
    }

    public static Map<BlockPos, BlockEntity> parseBlockEntities(int dataVersion, CompoundTag compound) {
        ListTag blockEntitiesTag = compound.get("BlockEntities");
        if(blockEntitiesTag == null) {
            return new HashMap<>();
        }
        List<BlockEntity> blockEntities = parseBlockEntities(dataVersion, blockEntitiesTag);
        return blockEntities.stream().collect(Collectors.toMap(BlockEntity::pos, blockEntity -> blockEntity));
    }

    public static List<Entity> parseEntities(ListTag entitiesTag) {
        if(entitiesTag == null) {
            return new ArrayList<>();
        }
        return entitiesTag.getValue().stream().map(tag -> parseEntity((CompoundTag) tag)).collect(Collectors.toList());
    }

    public static Entity parseEntity(CompoundTag tag) {
        CompoundTag extra = tag.clone();
        extra.remove("Id");
        extra.remove("Pos");

        return new Entity(Pos.fromDoubleList(tag.get("Pos")), tag.<StringTag>get("Id").getValue(), extra);
    }

    private static List<BlockEntity> parseBlockEntities(int dataVersion, ListTag blockEntitiesTag) {
        return blockEntitiesTag.getValue().stream().map(tag -> parseBlockEntity(dataVersion, (CompoundTag) tag)).collect(Collectors.toList());
    }

    private static BlockEntity parseBlockEntity(int dataVersion, CompoundTag blockEntityTag) {
        CompoundTag extra = blockEntityTag.clone();
        extra.remove("Id");
        extra.remove("Pos");
        return new BlockEntity(dataVersion, BlockPos.fromArray(blockEntityTag.<IntArrayTag>get("Pos").getValue()),
                blockEntityTag.<StringTag>get("Id").getValue(), extra);
    }

    @Override
    public CompoundTag serialize(Schematic schematic) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public int getVersion() {
        return 2;
    }
}
