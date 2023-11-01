package eu.cafestube.schematics.version;

import eu.cafestube.schematics.math.BlockPos;
import eu.cafestube.schematics.schematic.BlockEntity;
import eu.cafestube.schematics.schematic.Schematic;
import eu.cafestube.schematics.util.NBTUtil;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.nedit.type.NBTList;
import me.nullicorn.nedit.type.TagType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class V1SchematicVersion implements SchematicVersion {



    @Override
    public Schematic deserialize(NBTCompound compound) {
        int width = compound.getShort("Width", (short) 0) & 0xFFFF;
        int height = compound.getShort("Height", (short) 0) & 0xFFFF;
        int length = compound.getShort("Length", (short) 0) & 0xFFFF;
        NBTCompound metadata = new NBTCompound();
        if(compound.containsKey("Metadata")) {
            metadata = compound.getCompound("Metadata");
        }

        BlockPos offset = BlockPos.ZERO;
        if(compound.containsKey("Offset")) {
            int[] offsetArray = compound.getIntArray("Offset");
            offset = BlockPos.fromArray(offsetArray);
        }

        Map<Integer, String> blockPalette = parseBlockPalette(compound);

        byte[] blockData = compound.getByteArray("BlockData");

        NBTList blockEntitiesTag = compound.getList("TileEntities");
        if(blockEntitiesTag == null) {
            blockEntitiesTag = new NBTList(TagType.COMPOUND);
        }
        List<BlockEntity> blockEntities = parseBlockEntities(blockEntitiesTag);
        Map<BlockPos, BlockEntity> blockEntityMap = blockEntities.stream().collect(Collectors.toMap(BlockEntity::pos, blockEntity -> blockEntity));

        return new Schematic(Schematic.NO_DATA_VERSION, width, height, length, metadata, offset, blockPalette, blockData,
                blockEntityMap, new ArrayList<>(), null);
    }

    public static Map<Integer, String> parseBlockPalette(NBTCompound compound) {
        int paletteMax = compound.getInt("PaletteMax", 0);
        NBTCompound paletteTag = compound.getCompound("Palette");

        if(paletteTag == null || paletteTag.values().size() != paletteMax) {
            throw new IllegalArgumentException("Palette is missing or does not expect the palette max");
        }

        return paletteTag.entrySet().stream().collect(Collectors.toMap(stringObjectEntry -> (int) stringObjectEntry.getValue(), Map.Entry::getKey));
    }

    private List<BlockEntity> parseBlockEntities(NBTList blockEntitiesTag) {
        return blockEntitiesTag.stream().map(tag -> parseBlockEntity((NBTCompound) tag)).collect(Collectors.toList());
    }

    private BlockEntity parseBlockEntity(NBTCompound blockEntityTag) {
        BlockPos pos = BlockPos.fromArray(blockEntityTag.getIntArray("Pos"));
        String id = blockEntityTag.getString("Id");
        int contentVersion = blockEntityTag.getInt("ContentVersion", 0);

        NBTCompound extra = NBTUtil.clone(blockEntityTag);
        extra.remove("Pos");
        extra.remove("Id");
        extra.remove("ContentVersion");

        return new BlockEntity(contentVersion, pos, id, extra);
    }

    @Override
    public NBTCompound serialize(Schematic schematic) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
