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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class V3SchematicVersion implements SchematicVersion {

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


        CompoundTag blocksTag = compound.get("Blocks");

        CompoundTag paletteTag = blocksTag.get("Palette");
        Map<Integer, String> blockPalette = paletteTag.getValue().entrySet().stream()
                .collect(Collectors.toMap(stringObjectEntry -> ((IntTag) stringObjectEntry.getValue()).getValue(), Map.Entry::getKey));
        byte[] blockData = blocksTag.<ByteArrayTag>get("Data").getValue();


        Map<BlockPos, BlockEntity> blockEntityMap = V2SchematicVersion.parseBlockEntities(dataVersion, blocksTag);

        CompoundTag biomesTag = compound.get("Biomes");
        CompoundTag biomePaletteTag = biomesTag.get("Palette");
        Map<Integer, String> biomePalette = biomePaletteTag.getValue().entrySet().stream()
                .collect(Collectors.toMap(stringObjectEntry -> ((IntTag) stringObjectEntry.getValue()).getValue(), Map.Entry::getKey));
        byte[] biomeData = biomesTag.<ByteArrayTag>get("Data").getValue();


        ListTag entitiesTag = compound.get("Entities");
        List<Entity> entities = V2SchematicVersion.parseEntities(entitiesTag);


        return new Schematic(dataVersion, width, height, length, metadata, offset, blockPalette, blockData,
                blockEntityMap, entities, new BiomeData(biomePalette, biomeData, BiomeDataType.THREE_DIMENSIONAL));
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
