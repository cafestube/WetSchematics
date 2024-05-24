package eu.cafestube.schematics.version;

import eu.cafestube.schematics.math.BlockPos;
import eu.cafestube.schematics.schematic.BlockEntity;
import eu.cafestube.schematics.schematic.Entity;
import eu.cafestube.schematics.schematic.Schematic;
import eu.cafestube.schematics.schematic.biome.BiomeData;
import eu.cafestube.schematics.schematic.biome.BiomeDataType;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;

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

        Map<BlockPos, BlockEntity> blockEntityMap = V2SchematicVersion.parseBlockEntities(dataVersion, blocksTag);

        CompoundBinaryTag biomesTag = compound.getCompound("Biomes");
        CompoundBinaryTag biomePaletteTag = biomesTag.getCompound("Palette");
        Map<Integer, String> biomePalette = StreamSupport.stream(biomePaletteTag.spliterator(), true)
                .collect(Collectors.toMap(stringObjectEntry -> ((IntBinaryTag) stringObjectEntry.getValue()).value(), Map.Entry::getKey));
        byte[] biomeData = biomesTag.getByteArray("Data");


        ListBinaryTag entitiesTag = compound.getList("Entities");
        List<Entity> entities = V2SchematicVersion.parseEntities(entitiesTag);


        return new Schematic(dataVersion, width, height, length, metadata, offset, blockPalette, blockData,
                blockEntityMap, entities, new BiomeData(biomePalette, biomeData, BiomeDataType.THREE_DIMENSIONAL));
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
