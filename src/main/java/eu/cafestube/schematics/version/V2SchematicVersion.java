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

import java.util.*;
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


        BiomeData biomeData = null;

        if(compound.contains("BiomeData")) {
            int biomePaletteMax = NBTUtil.getIntOrDefault(compound, "BiomePaletteMax", 0);
            CompoundTag biomePaletteTag = compound.get("BiomePalette");
            if(biomePaletteTag == null || biomePaletteTag.values().size() != biomePaletteMax) {
                throw new IllegalArgumentException("Biome palette is missing or does not expect the palette max");
            }
            Map<Integer, String> biomePalette = biomePaletteTag.getValue().entrySet().stream()
                    .collect(Collectors.toMap(stringObjectEntry -> ((IntTag) stringObjectEntry.getValue()).getValue(), Map.Entry::getKey));

            byte[] biomeDataBytes = compound.<ByteArrayTag>get("BiomeData").getValue();

            biomeData = new BiomeData(biomePalette, biomeDataBytes, BiomeDataType.TWO_DIMENSIONAL);
        }


        return new Schematic(dataVersion, width, height, length, metadata, offset, blockPalette, blockData,
                blockEntityMap, entities, biomeData);
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

    public static ListTag serializeEntities(List<Entity> entities) {
        ListTag entitiesTag = new ListTag("Entities", CompoundTag.class);
        for (Entity entity : entities) {
            entitiesTag.add(serializeEntity(entity));
        }
        return entitiesTag;
    }

    private static Tag serializeEntity(Entity entity) {
        Map<String, Tag> extraData = new HashMap<>(entity.extra().getValue());
        CompoundTag entityTag = new CompoundTag("Entity", extraData);
        entityTag.put(new StringTag("Id", entity.id()));
        entityTag.put(new ListTag("Pos", List.of(new DoubleTag("x", entity.pos().x()),
                new DoubleTag("y", entity.pos().y()), new DoubleTag("z", entity.pos().z()))));
        return new CompoundTag("Entity", extraData);
    }


    public static Map<BlockPos, BlockEntity> parseBlockEntities(int dataVersion, CompoundTag compound) {
        ListTag blockEntitiesTag = compound.get("BlockEntities");
        if(blockEntitiesTag == null) {
            return new HashMap<>();
        }
        List<BlockEntity> blockEntities = parseBlockEntities(dataVersion, blockEntitiesTag);
        return blockEntities.stream().collect(Collectors.toMap(BlockEntity::pos, blockEntity -> blockEntity));
    }

    public static ListTag serializeBlockEntities(Map<BlockPos, BlockEntity> entities) {
        ListTag blockEntities = new ListTag("BlockEntities", CompoundTag.class);
        for (BlockEntity value : entities.values()) {
            blockEntities.add(serializeBlockEntity(value));
        }
        return blockEntities;
    }

    public static CompoundTag serializeBlockEntity(BlockEntity blockEntity) {
        Map<String, Tag> extraData = new HashMap<>(blockEntity.extra().getValue());

        CompoundTag entity = new CompoundTag("BlockEntity", extraData);
        entity.put(new IntArrayTag("Pos", blockEntity.pos().toArray()));
        entity.put(new StringTag("Id", blockEntity.id()));
        return entity;
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
        CompoundTag compound = new CompoundTag("Schematic");
        compound.put(new IntTag("Version", getVersion()));
        compound.put(new IntTag("DataVersion", schematic.dataVersion()));

        compound.put(new ShortTag("Width", (short) schematic.width()));
        compound.put(new ShortTag("Height", (short) schematic.height()));
        compound.put(new ShortTag("Length", (short) schematic.length()));

        compound.put(schematic.metadata());

        compound.put(new IntArrayTag("Offset", schematic.offset().toArray()));

        compound.put(new ByteArrayTag("BlockData", schematic.blockData()));

        V1SchematicVersion.writeBlockPalette(schematic.blockStatePalette(), compound);

        compound.put(serializeBlockEntities(schematic.blockEntities()));
        compound.put(serializeEntities(schematic.entities()));

        if(schematic.biomeData() != null) {
            compound.put(new IntTag("BiomePaletteMax", schematic.biomeData().biomePalette().keySet().stream().max(Integer::compare).orElse(0)));

            CompoundTag biomePaletteTag = new CompoundTag("BiomePalette");
            schematic.biomeData().biomePalette().forEach((id, name) -> biomePaletteTag.put(new IntTag(name, id)));
            compound.put(biomePaletteTag);

            if(schematic.biomeData().type() == BiomeDataType.TWO_DIMENSIONAL) {
                compound.put(new ByteArrayTag("BiomeData", schematic.biomeData().biomes()));
            } else {
                //Convert from 3d to 2d by using the biome at y 0
                byte[] convertedData = new byte[schematic.width() * schematic.length()];

                for(int x = 0; x < schematic.width(); x++) {
                    for(int z = 0; z < schematic.length(); z++) {
                        convertedData[x + z * schematic.width()] = schematic.getBiomePaletteIndex(x, z);
                    }
                }

                compound.put(new ByteArrayTag("BiomeData", convertedData));
            }
        }

        return compound;
    }

    @Override
    public int getVersion() {
        return 2;
    }
}
