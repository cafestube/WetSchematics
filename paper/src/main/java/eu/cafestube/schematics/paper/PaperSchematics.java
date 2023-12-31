package eu.cafestube.schematics.paper;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import eu.cafestube.schematics.math.BlockPos;
import eu.cafestube.schematics.schematic.BlockEntity;
import eu.cafestube.schematics.schematic.Entity;
import eu.cafestube.schematics.schematic.Schematic;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.logging.Level;

public class PaperSchematics {

    private static final VersionAdapter VERSION_ADAPTER = VersionAdapter.create();

    private boolean hasSentVersionAdapterError = false;
    private final JavaPlugin plugin;

    public PaperSchematics(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void placeSchematic(Location location, Schematic schematic) {
        placeSchematic(location, schematic, false, true, true, true, false, false, true);
    }

    public void placeSchematic(
            Location location,
            Schematic schematic,
            boolean ignoreOffset,
            boolean tileEntities,
            boolean entities,
            boolean biomes
    ) {
        placeSchematic(location, schematic, ignoreOffset, tileEntities, entities, biomes, false, false, true);
    }

    public void placeSchematic(
            Location location,
            Schematic schematic,
            boolean ignoreOffset,
            boolean tileEntities,
            boolean entities,
            boolean biomes,
            boolean placeAir,
            boolean updateEntityAI,
            boolean updateLighting
    ) {
        if(biomes && schematic.biomeData() == null)
            biomes = false;

        Location origin = ignoreOffset ? location : location.clone()
                .add(new Vector(schematic.offset().x(), schematic.offset().y(), schematic.offset().z()));

        World world = origin.getWorld();

        for (int x = 0; x < schematic.width(); x++) {
            for (int y = 0; y < schematic.height(); y++) {
                for (int z = 0; z < schematic.length(); z++) {
                    String blockData = schematic.getBlockData(x, y, z);

                    int worldX = origin.getBlockX() + x;
                    int worldY = origin.getBlockY() + y;
                    int worldZ = origin.getBlockZ() + z;

                    if(biomes) {
                        String biomeId = schematic.getBiomeId(x, y, z);
                        if(biomeId != null) {
                            Biome biome = Registry.BIOME.get(Objects.requireNonNull(NamespacedKey.fromString(biomeId)));
                            if(biome != null) {
                                world.setBiome(worldX, worldY, worldZ, biome);
                            } else {
                                plugin.getLogger().warning("Failed to find biome with id " + biomeId);
                            }
                        }
                    }

                    if(blockData.equals("minecraft:air") && !placeAir)
                        continue;

                    placeBlockFast(world, worldX, worldY, worldZ, Bukkit.createBlockData(blockData), updateEntityAI, updateLighting);

                    if(tileEntities) {
                        BlockEntity blockEntity = schematic.blockEntities().get(new BlockPos(x, y, z));

                        if(blockEntity != null) {
                            placeBlockEntity(new Location(world, worldX, worldY, worldZ), schematic.dataVersion(),
                                    NamespacedKey.fromString(blockEntity.id()), blockEntity.extra());
                        }
                    }
                }

            }
        }

        if(entities) {
            for (Entity entity : schematic.entities()) {
                double worldX = origin.getBlockX() + entity.pos().x();
                double worldY = origin.getBlockY() + entity.pos().y();
                double worldZ = origin.getBlockZ() + entity.pos().z();

                placeEntity(new Location(world, worldX, worldY, worldZ), schematic.dataVersion(), NamespacedKey.fromString(entity.id()), entity.extra());
            }
        }
    }

    public void placeEntity(Location location, int dataVersion, NamespacedKey type, CompoundTag nbt) {
        if(VERSION_ADAPTER == null && !hasSentVersionAdapterError) {
            plugin.getLogger().log(Level.SEVERE, "Failed to find version adapter. Ignoring block entity.");
            hasSentVersionAdapterError = true;
            return;
        }
        VERSION_ADAPTER.spawnEntity(location, dataVersion, type, nbt);
    }

    public void placeBlockFast(World world, int x, int y, int z, BlockData blockData, boolean updateEntityAI, boolean updateLighting) {
        if(VERSION_ADAPTER == null) {
            world.getBlockAt(x, y, z).setBlockData(blockData, false);
            return;
        }
        VERSION_ADAPTER.placeBlockFast(world, x, y, z, blockData, updateEntityAI, updateLighting);
    }

    public void placeBlockEntity(Location location, int dataVersion, NamespacedKey type, CompoundTag nbt) {
        if(VERSION_ADAPTER == null && !hasSentVersionAdapterError) {
            plugin.getLogger().log(Level.SEVERE, "Failed to find version adapter. Ignoring block entity.");
            hasSentVersionAdapterError = true;
            return;
        }
        VERSION_ADAPTER.setTileEntity(location, dataVersion, type, nbt);
    }

}
