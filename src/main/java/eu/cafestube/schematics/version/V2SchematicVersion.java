package eu.cafestube.schematics.version;

import eu.cafestube.schematics.math.BlockPos;
import eu.cafestube.schematics.math.Pos;
import eu.cafestube.schematics.schematic.BlockEntity;
import eu.cafestube.schematics.schematic.Entity;
import eu.cafestube.schematics.schematic.Schematic;
import eu.cafestube.schematics.schematic.biome.BiomeData;
import eu.cafestube.schematics.schematic.biome.BiomeDataType;
import eu.cafestube.schematics.util.NBTUtil;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.nedit.type.NBTList;
import me.nullicorn.nedit.type.TagType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class V2SchematicVersion implements SchematicVersion {

    @Override
    public Schematic deserialize(NBTCompound compound) {
        int dataVersion = compound.getInt("DataVersion", 0);

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

        Map<Integer, String> blockPalette = V1SchematicVersion.parseBlockPalette(compound);
        byte[] blockData = compound.getByteArray("BlockData");

        Map<BlockPos, BlockEntity> blockEntityMap = parseBlockEntities(dataVersion, compound);

        NBTList entitiesTag = compound.getList("Entities");
        List<Entity> entities = parseEntities(entitiesTag);

        int biomePaletteMax = compound.getInt("BiomePaletteMax", 0);
        NBTCompound biomePaletteTag = compound.getCompound("BiomePalette");
        if(biomePaletteTag == null || biomePaletteTag.values().size() != biomePaletteMax) {
            throw new IllegalArgumentException("Biome palette is missing or does not expect the palette max");
        }
        Map<Integer, String> biomePalette = biomePaletteTag.entrySet().stream()
                .collect(Collectors.toMap(stringObjectEntry -> (int) stringObjectEntry.getValue(), Map.Entry::getKey));

        byte[] biomeData = compound.getByteArray("BiomeData");

        return new Schematic(dataVersion, width, height, length, metadata, offset, blockPalette, blockData,
                blockEntityMap, entities, new BiomeData(biomePalette, biomeData, BiomeDataType.TWO_DIMENSIONAL));
    }

    public static Map<BlockPos, BlockEntity> parseBlockEntities(int dataVersion, NBTCompound compound) {
        NBTList blockEntitiesTag = compound.getList("BlockEntities");
        if(blockEntitiesTag == null) {
            blockEntitiesTag = new NBTList(TagType.COMPOUND);
        }
        List<BlockEntity> blockEntities = parseBlockEntities(dataVersion, blockEntitiesTag);
        return blockEntities.stream().collect(Collectors.toMap(BlockEntity::pos, blockEntity -> blockEntity));
    }

    public static List<Entity> parseEntities(NBTList entitiesTag) {
        if(entitiesTag == null) {
            return new ArrayList<>();
        }
        return entitiesTag.stream().map(tag -> parseEntity((NBTCompound) tag)).collect(Collectors.toList());
    }

    public static Entity parseEntity(NBTCompound tag) {
        NBTCompound extra = NBTUtil.clone(tag);
        extra.remove("Id");
        extra.remove("Pos");

        return new Entity(Pos.fromDoubleList(tag.getList("Pos")), tag.getString("Id"), extra);
    }

    private static List<BlockEntity> parseBlockEntities(int dataVersion, NBTList blockEntitiesTag) {
        return blockEntitiesTag.stream().map(tag -> parseBlockEntity(dataVersion, (NBTCompound) tag)).collect(Collectors.toList());
    }

    private static BlockEntity parseBlockEntity(int dataVersion, NBTCompound blockEntityTag) {
        NBTCompound extra = NBTUtil.clone(blockEntityTag);
        extra.remove("Id");
        extra.remove("Pos");
        return new BlockEntity(dataVersion, BlockPos.fromArray(blockEntityTag.getIntArray("Pos")),
                blockEntityTag.getString("Id"), extra);
    }

    @Override
    public NBTCompound serialize(Schematic schematic) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public int getVersion() {
        return 2;
    }
}
