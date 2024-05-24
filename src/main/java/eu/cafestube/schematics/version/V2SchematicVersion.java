package eu.cafestube.schematics.version;

import eu.cafestube.schematics.math.BlockPos;
import eu.cafestube.schematics.math.Pos;
import eu.cafestube.schematics.schematic.BlockEntity;
import eu.cafestube.schematics.schematic.Entity;
import eu.cafestube.schematics.schematic.Schematic;
import eu.cafestube.schematics.schematic.biome.BiomeData;
import eu.cafestube.schematics.schematic.biome.BiomeDataType;
import net.kyori.adventure.nbt.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class V2SchematicVersion implements SchematicVersion {

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

        Map<Integer, String> blockPalette = V1SchematicVersion.parseBlockPalette(compound);
        byte[] blockData = compound.getByteArray("BlockData");

        Map<BlockPos, BlockEntity> blockEntityMap = parseBlockEntities(dataVersion, compound);

        ListBinaryTag entitiesTag = compound.getList("Entities");
        List<Entity> entities = parseEntities(entitiesTag);


        BiomeData biomeData = null;

        if(compound.get("BiomeData") != null) {
            int biomePaletteMax = compound.getInt("BiomePaletteMax", 0);
            CompoundBinaryTag biomePaletteTag = compound.getCompound("BiomePalette");
            if(biomePaletteTag.size() != biomePaletteMax) {
                throw new IllegalArgumentException("Biome palette is missing or does not expect the palette max");
            }
            Map<Integer, String> biomePalette = StreamSupport.stream(biomePaletteTag.spliterator(), true)
                    .collect(Collectors.toMap(stringObjectEntry -> ((IntBinaryTag) stringObjectEntry.getValue()).value(), Map.Entry::getKey));

            byte[] biomeDataBytes = compound.getByteArray("BiomeData");

            biomeData = new BiomeData(biomePalette, biomeDataBytes, BiomeDataType.TWO_DIMENSIONAL);
        }


        return new Schematic(dataVersion, width, height, length, metadata, offset, blockPalette, blockData,
                blockEntityMap, entities, biomeData);
    }


    public static List<Entity> parseEntities(ListBinaryTag entitiesTag) {
        if(entitiesTag == null) {
            return new ArrayList<>();
        }
        return entitiesTag.stream().map(tag -> parseEntity((CompoundBinaryTag) tag)).collect(Collectors.toList());
    }

    public static Entity parseEntity(CompoundBinaryTag tag) {
        CompoundBinaryTag extra = tag.remove("Id")
                .remove("Pos");

        return new Entity(Pos.fromDoubleList(tag.getList("Pos")), tag.getString("Id"), extra);
    }

    public static ListBinaryTag serializeEntities(List<Entity> entities) {
        return ListBinaryTag.from(entities.stream().map(V2SchematicVersion::serializeEntity).toList());
    }

    private static CompoundBinaryTag serializeEntity(Entity entity) {
        return entity.extra()
                .putString("Id", entity.id())
                .put("Pos", ListBinaryTag.from(List.of(DoubleBinaryTag.doubleBinaryTag(entity.pos().x()),
                    DoubleBinaryTag.doubleBinaryTag(entity.pos().y()), DoubleBinaryTag.doubleBinaryTag(entity.pos().z()))));
    }


    public static Map<BlockPos, BlockEntity> parseBlockEntities(int dataVersion, CompoundBinaryTag compound) {
        ListBinaryTag blockEntitiesTag = compound.getList("BlockEntities");
        List<BlockEntity> blockEntities = parseBlockEntities(dataVersion, blockEntitiesTag);
        return blockEntities.stream().collect(Collectors.toMap(BlockEntity::pos, blockEntity -> blockEntity));
    }

    public static ListBinaryTag serializeBlockEntities(Map<BlockPos, BlockEntity> entities) {
        return ListBinaryTag.from(entities.values().stream().map(V2SchematicVersion::serializeBlockEntity).toList());
    }

    public static CompoundBinaryTag serializeBlockEntity(BlockEntity blockEntity) {
        return blockEntity.extra().putIntArray("Pos", blockEntity.pos().toArray()).putString("Id", blockEntity.id());
    }

    private static List<BlockEntity> parseBlockEntities(int dataVersion, ListBinaryTag blockEntitiesTag) {
        return blockEntitiesTag.stream().map(tag -> parseBlockEntity(dataVersion, (CompoundBinaryTag) tag)).collect(Collectors.toList());
    }

    private static BlockEntity parseBlockEntity(int dataVersion, CompoundBinaryTag blockEntityTag) {
        CompoundBinaryTag extra = blockEntityTag.remove("Id").remove("Pos");
        return new BlockEntity(dataVersion, BlockPos.fromArray(blockEntityTag.getIntArray("Pos")),
                blockEntityTag.getString("Id"), extra);
    }

    @Override
    public CompoundBinaryTag serialize(Schematic schematic) {
        CompoundBinaryTag.Builder compound = CompoundBinaryTag.builder();
        compound.putInt("Version", getVersion());
        compound.putInt("DataVersion", schematic.dataVersion());

        compound.putShort("Width", (short) schematic.width());
        compound.putShort("Height", (short) schematic.height());
        compound.putShort("Length", (short) schematic.length());

        compound.put("Metadata", schematic.metadata());

        compound.putIntArray("Offset", schematic.offset().toArray());

        compound.putByteArray("BlockData", schematic.blockData());

        V1SchematicVersion.writeBlockPalette(schematic.blockStatePalette(), compound);

        compound.put("BlockEntities", serializeBlockEntities(schematic.blockEntities()));
        compound.put("Entities", serializeEntities(schematic.entities()));

        if(schematic.biomeData() != null) {
            compound.putInt("BiomePaletteMax", schematic.biomeData().biomePalette().keySet().stream().max(Integer::compare).orElse(0) + 1);

            CompoundBinaryTag.Builder biomePaletteTag = CompoundBinaryTag.builder();
            schematic.biomeData().biomePalette().forEach((id, name) -> biomePaletteTag.putInt(name, id));
            compound.put("BiomePalette", biomePaletteTag.build());

            if(schematic.biomeData().type() == BiomeDataType.TWO_DIMENSIONAL) {
                compound.putByteArray("BiomeData", schematic.biomeData().biomes());
            } else {
                //Convert from 3d to 2d by using the biome at y 0
                byte[] convertedData = new byte[schematic.width() * schematic.length()];

                for(int x = 0; x < schematic.width(); x++) {
                    for(int z = 0; z < schematic.length(); z++) {
                        convertedData[x + z * schematic.width()] = schematic.getBiomePaletteIndex(x, z);
                    }
                }

                compound.putByteArray("BiomeData", convertedData);
            }
        }

        return compound.build();
    }

    @Override
    public int getVersion() {
        return 2;
    }
}
