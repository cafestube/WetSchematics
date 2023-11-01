package eu.cafestube.schematics.version;

import eu.cafestube.schematics.math.BlockPos;
import eu.cafestube.schematics.math.Pos;
import eu.cafestube.schematics.schematic.BlockEntity;
import eu.cafestube.schematics.schematic.Entity;
import eu.cafestube.schematics.schematic.Schematic;
import eu.cafestube.schematics.schematic.biome.BiomeData;
import eu.cafestube.schematics.schematic.biome.BiomeDataType;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.nedit.type.NBTList;
import me.nullicorn.nedit.type.TagType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class V3SchematicVersion implements SchematicVersion {

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


        NBTCompound blocksTag = compound.getCompound("Blocks");

        NBTCompound paletteTag = blocksTag.getCompound("Palette");
        Map<Integer, String> blockPalette = paletteTag.entrySet().stream().collect(Collectors.toMap(stringObjectEntry -> (int) stringObjectEntry.getValue(), Map.Entry::getKey));
        byte[] blockData = blocksTag.getByteArray("Data");


        Map<BlockPos, BlockEntity> blockEntityMap = V2SchematicVersion.parseBlockEntities(dataVersion, blocksTag);

        NBTCompound biomesTag = compound.getCompound("Biomes");
        NBTCompound biomePaletteTag = biomesTag.getCompound("Palette");
        Map<Integer, String> biomePalette = biomePaletteTag.entrySet().stream()
                .collect(Collectors.toMap(stringObjectEntry -> (int) stringObjectEntry.getValue(), Map.Entry::getKey));
        byte[] biomeData = biomesTag.getByteArray("Data");


        NBTList entitiesTag = compound.getList("Entities");
        List<Entity> entities = V2SchematicVersion.parseEntities(entitiesTag);


        return new Schematic(dataVersion, width, height, length, metadata, offset, blockPalette, blockData,
                blockEntityMap, entities, new BiomeData(biomePalette, biomeData, BiomeDataType.THREE_DIMENSIONAL));
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
