package eu.cafestube.schematics.version;

import eu.cafestube.schematics.math.BlockPos;
import eu.cafestube.schematics.math.Pos;
import eu.cafestube.schematics.schematic.BlockEntity;
import eu.cafestube.schematics.schematic.Entity;
import eu.cafestube.schematics.schematic.Schematic;
import eu.cafestube.schematics.schematic.biome.BiomeData;
import eu.cafestube.schematics.schematic.biome.BiomeDataType;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class V3SchematicVersion implements SchematicVersion {

    @Override
    public Schematic deserialize(CompoundBinaryTag compound) {
        int dataVersion = compound.getInt("DataVersion", 0);

        int width = compound.getShort("Width", (short) 0) & 0xFFFF;
        int height = compound.getShort("Height", (short) 0) & 0xFFFF;
        int length = compound.getShort("Length", (short) 0) & 0xFFFF;
        CompoundBinaryTag metadata = compound.getCompound("Metadata");

        BlockPos offset = BlockPos.ZERO;
        if(compound.get("Offset") != null) {
            int[] offsetArray = compound.getIntArray("Offset");
            offset = BlockPos.fromArray(offsetArray);
        }


        CompoundBinaryTag blocksTag = compound.getCompound("Blocks");

        CompoundBinaryTag paletteTag = blocksTag.getCompound("Palette");
        Map<Integer, String> blockPalette = StreamSupport.stream(paletteTag.spliterator(), true)
                .collect(Collectors.toMap(stringObjectEntry -> ((IntBinaryTag) stringObjectEntry.getValue()).value(), Map.Entry::getKey));
        byte[] blockData = blocksTag.getByteArray("Data");

        Map<BlockPos, BlockEntity> blockEntityMap = parseBlockEntities(dataVersion, blocksTag);


        BiomeData biomeData = null;
        if(compound.get("Biomes") != null) {
            CompoundBinaryTag biomesTag = compound.getCompound("Biomes");
            CompoundBinaryTag biomePaletteTag = biomesTag.getCompound("Palette");
            Map<Integer, String> biomePalette = StreamSupport.stream(biomePaletteTag.spliterator(), true)
                    .collect(Collectors.toMap(stringObjectEntry -> ((IntBinaryTag) stringObjectEntry.getValue()).value(), Map.Entry::getKey));
            byte[] biomeDataBytes = biomesTag.getByteArray("Data");

            biomeData = new BiomeData(biomePalette, biomeDataBytes, BiomeDataType.THREE_DIMENSIONAL);
        }


        ListBinaryTag entitiesTag = compound.getList("Entities");
        List<Entity> entities = parseEntities(entitiesTag);


        return new Schematic(dataVersion, width, height, length, metadata, offset, blockPalette,
                V1SchematicVersion.readBlockData(blockData, width, height, length), blockEntityMap, entities, biomeData);
    }

    public static List<Entity> parseEntities(ListBinaryTag entitiesTag) {
        if(entitiesTag == null) {
            return new ArrayList<>();
        }
        return entitiesTag.stream().map(tag -> parseEntity((CompoundBinaryTag) tag)).collect(Collectors.toList());
    }

    public static Entity parseEntity(CompoundBinaryTag tag) {
        return new Entity(Pos.fromDoubleList(tag.getList("Pos")), tag.getString("Id"), tag.getCompound("Data"));
    }

    public static Map<BlockPos, BlockEntity> parseBlockEntities(int dataVersion, CompoundBinaryTag compound) {
        ListBinaryTag blockEntitiesTag = compound.getList("BlockEntities");
        List<BlockEntity> blockEntities = parseBlockEntities(dataVersion, blockEntitiesTag);
        return blockEntities.stream().collect(Collectors.toMap(BlockEntity::pos, blockEntity -> blockEntity));
    }

    private static List<BlockEntity> parseBlockEntities(int dataVersion, ListBinaryTag blockEntitiesTag) {
        return blockEntitiesTag.stream().map(tag -> parseBlockEntity(dataVersion, (CompoundBinaryTag) tag)).collect(Collectors.toList());
    }

    private static BlockEntity parseBlockEntity(int dataVersion, CompoundBinaryTag blockEntityTag) {
        return new BlockEntity(dataVersion, BlockPos.fromArray(blockEntityTag.getIntArray("Pos")),
                blockEntityTag.getString("Id"), blockEntityTag.getCompound("Data"));
    }


    @Override
    public CompoundBinaryTag serialize(Schematic schematic) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public int getVersion() {
        return 2;
    }
}
