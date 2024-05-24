package eu.cafestube.schematics.version;

import eu.cafestube.schematics.math.BlockPos;
import eu.cafestube.schematics.schematic.BlockEntity;
import eu.cafestube.schematics.schematic.Schematic;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class V1SchematicVersion implements SchematicVersion {



    @Override
    public Schematic deserialize(CompoundBinaryTag compound) {
        int width = compound.getShort("Width", (short) 0) & 0xFFFF;
        int height = compound.getShort("Height", (short) 0) & 0xFFFF;
        int length = compound.getShort("Length", (short) 0) & 0xFFFF;

        CompoundBinaryTag metadata = compound.getCompound("Metadata");

        BlockPos offset = BlockPos.ZERO;
        if(compound.get("Offset") == null) {
            int[] offsetArray = compound.getIntArray("Offset", new int[]{0, 0, 0});
            offset = BlockPos.fromArray(offsetArray);
        }

        Map<Integer, String> blockPalette = parseBlockPalette(compound);

        byte[] blockData = compound.getByteArray("BlockData");

        ListBinaryTag blockEntitiesTag = compound.getList("TileEntities");
        List<BlockEntity> blockEntities = parseBlockEntities(blockEntitiesTag);
        Map<BlockPos, BlockEntity> blockEntityMap = blockEntities.stream().collect(Collectors.toMap(BlockEntity::pos, blockEntity -> blockEntity));

        return new Schematic(Schematic.NO_DATA_VERSION, width, height, length, metadata, offset, blockPalette, blockData,
                blockEntityMap, new ArrayList<>(), null);
    }

    public static Map<Integer, String> parseBlockPalette(CompoundBinaryTag compound) {
        int paletteMax = compound.getInt("PaletteMax", 0);
        CompoundBinaryTag paletteTag = compound.getCompound("Palette");

        if(paletteTag.size() != paletteMax) {
            throw new IllegalArgumentException("Palette is missing or does not expect the palette max");
        }

        return StreamSupport.stream(paletteTag.spliterator(), false)
                .collect(Collectors.toMap(entry -> ((IntBinaryTag) entry.getValue()).value(), Map.Entry::getKey));
    }

    public static void writeBlockPalette(Map<Integer, String> palette, CompoundBinaryTag.Builder compound) {
        int paletteMax = palette.keySet().stream().max(Integer::compare).orElse(0) + 1;

        compound.putInt("PaletteMax", paletteMax);

        CompoundBinaryTag.Builder paletteTag = CompoundBinaryTag.builder();
        palette.forEach((id, name) -> paletteTag.put(name, IntBinaryTag.intBinaryTag(id)));
        compound.put("Palette", paletteTag.build());
    }

    private List<BlockEntity> parseBlockEntities(ListBinaryTag blockEntitiesTag) {
        if(blockEntitiesTag == null) {
            return new ArrayList<>();
        }
        return blockEntitiesTag.stream().map(tag -> parseBlockEntity((CompoundBinaryTag) tag)).collect(Collectors.toList());
    }

    private BlockEntity parseBlockEntity(CompoundBinaryTag blockEntityTag) {
        BlockPos pos = BlockPos.fromArray(blockEntityTag.getIntArray("Pos"));
        String id = blockEntityTag.getString("Id");
        int contentVersion = blockEntityTag.getInt("ContentVersion", 0);

        CompoundBinaryTag extra = blockEntityTag.remove("Pos")
                .remove("Id")
                .remove("ContentVersion");

        return new BlockEntity(contentVersion, pos, id, extra);
    }

    @Override
    public CompoundBinaryTag serialize(Schematic schematic) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
