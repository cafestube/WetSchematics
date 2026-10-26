package eu.cafestube.schematics.data;

import eu.cafestube.schematics.math.BlockPos;
import eu.cafestube.schematics.schematic.BlockEntity;
import eu.cafestube.schematics.schematic.Entity;
import eu.cafestube.schematics.schematic.biome.BiomeData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DataConverter {

    default Map<Integer, String> upgradeBlockPalette(Map<Integer, String> palette, int from) {
        if(from >= getFinalDataVersion(from)) return palette;

        Map<Integer, String> out = new HashMap<>(palette.size());

        for (Map.Entry<Integer, String> entry : palette.entrySet()) {
            out.put(entry.getKey(), upgradeBlockState(entry.getValue(), from));
        }
        return out;
    }

    String upgradeBlockState(String src, int from);

    BlockEntity upgradeBlockEntity(BlockEntity from);

    Entity upgradeEntity(Entity entity, int from);

    @Nullable BiomeData upgradeBiomeData(@Nullable BiomeData biomeData, int dataVersion);

    String upgradeBiome(String key, int dataVersion);

    int getFinalDataVersion(int source);

    default Map<BlockPos, BlockEntity> upgradeBlockEntities(Map<BlockPos, BlockEntity> entities) {
        Map<BlockPos, BlockEntity> out = new HashMap<>(entities.size());

        for (Map.Entry<BlockPos, BlockEntity> blockPosBlockEntityEntry : entities.entrySet()) {
            out.put(blockPosBlockEntityEntry.getKey(), upgradeBlockEntity(blockPosBlockEntityEntry.getValue()));
        }

        return out;
    }

    default List<Entity> upgradeEntities(List<Entity> entities, int from) {
        if(from >= getFinalDataVersion(from)) return entities;

        List<Entity> out = new ArrayList<>(entities.size());

        for (Entity entity : entities) {
            out.add(upgradeEntity(entity, from));
        }

        return out;
    }
}
