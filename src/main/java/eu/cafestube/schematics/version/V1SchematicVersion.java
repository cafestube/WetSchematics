package eu.cafestube.schematics.version;

import com.github.steveice10.opennbt.tag.builtin.*;
import eu.cafestube.schematics.math.BlockPos;
import eu.cafestube.schematics.schematic.BlockEntity;
import eu.cafestube.schematics.schematic.Schematic;
import eu.cafestube.schematics.util.NBTUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class V1SchematicVersion implements SchematicVersion {



    @Override
    public Schematic deserialize(CompoundTag compound) {
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

        Map<Integer, String> blockPalette = parseBlockPalette(compound);

        byte[] blockData = compound.<ByteArrayTag>get("BlockData").getValue();

        ListTag blockEntitiesTag = compound.get("TileEntities");
        List<BlockEntity> blockEntities = parseBlockEntities(blockEntitiesTag);
        Map<BlockPos, BlockEntity> blockEntityMap = blockEntities.stream().collect(Collectors.toMap(BlockEntity::pos, blockEntity -> blockEntity));

        return new Schematic(Schematic.NO_DATA_VERSION, width, height, length, metadata, offset, blockPalette, blockData,
                blockEntityMap, new ArrayList<>(), null);
    }

    public static Map<Integer, String> parseBlockPalette(CompoundTag compound) {
        int paletteMax = NBTUtil.getIntOrDefault(compound, "PaletteMax", 0);
        CompoundTag paletteTag = compound.get("Palette");

        if(paletteTag == null || paletteTag.values().size() != paletteMax) {
            throw new IllegalArgumentException("Palette is missing or does not expect the palette max");
        }

        return paletteTag.getValue().entrySet().stream().collect(Collectors.toMap(stringObjectEntry -> ((IntTag) stringObjectEntry.getValue()).getValue(), Map.Entry::getKey));
    }

    public static void writeBlockPalette(Map<Integer, String> palette, CompoundTag compound) {
        int paletteMax = palette.keySet().stream().max(Integer::compare).orElse(0);

        compound.put(new IntTag("PaletteMax", paletteMax));
        CompoundTag paletteTag = new CompoundTag("Palette");
        palette.forEach((id, name) -> paletteTag.put(new IntTag(name, id)));
        compound.put(paletteTag);
    }

    private List<BlockEntity> parseBlockEntities(ListTag blockEntitiesTag) {
        if(blockEntitiesTag == null) {
            return new ArrayList<>();
        }
        return blockEntitiesTag.getValue().stream().map(tag -> parseBlockEntity((CompoundTag) tag)).collect(Collectors.toList());
    }

    private BlockEntity parseBlockEntity(CompoundTag blockEntityTag) {
        BlockPos pos = BlockPos.fromArray(blockEntityTag.<IntArrayTag>get("Pos").getValue());
        String id = blockEntityTag.<StringTag>get("Id").getValue();
        int contentVersion = NBTUtil.getIntOrDefault(blockEntityTag, "ContentVersion", 0);

        CompoundTag extra = blockEntityTag.clone();
        extra.remove("Pos");
        extra.remove("Id");
        extra.remove("ContentVersion");

        return new BlockEntity(contentVersion, pos, id, extra);
    }

    @Override
    public CompoundTag serialize(Schematic schematic) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
