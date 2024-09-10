package eu.cafestube.schematics.paper;

import eu.cafestube.schematics.math.BlockPos;
import eu.cafestube.schematics.paper.transformer.SchematicBlockTransformer;
import eu.cafestube.schematics.paper.transformer.SchematicEntityTransformer;
import eu.cafestube.schematics.schematic.BlockEntity;
import eu.cafestube.schematics.schematic.Entity;
import eu.cafestube.schematics.schematic.Schematic;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import javax.xml.validation.Schema;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PaperSchematics {

    private static final VersionAdapter VERSION_ADAPTER = VersionAdapter.create();

    private boolean hasSentVersionAdapterError = false;
    private final JavaPlugin plugin;

    public PaperSchematics(JavaPlugin plugin) {
        this.plugin = plugin;
        if(VERSION_ADAPTER == null) {
            plugin.getLogger().log(Level.SEVERE, "Failed to find version adapter. Some features and performance might be limited.");
        }
    }

    public CompletableFuture<Void> loadRequiredChunks(Location location, Schematic schematic, boolean ignoreOffset) {
        Location origin = ignoreOffset ? location : location.clone()
                .add(new Vector(schematic.offset().x(), schematic.offset().y(), schematic.offset().z()));
        Location other = origin.clone().add(schematic.width(), schematic.height(), schematic.length());

        int originChunkX = origin.getBlockX() >> 4;
        int originChunkZ = origin.getBlockZ() >> 4;

        int otherChunkX = other.getBlockX() >> 4;
        int otherChunkZ = other.getBlockZ() >> 4;

        List<CompletableFuture<Chunk>> futures = new java.util.ArrayList<>();

        for (int xx = originChunkX; xx <= otherChunkX; xx++) {
            for (int zz = originChunkZ; zz <= otherChunkZ; zz++) {
                futures.add(location.getWorld().getChunkAtAsync(xx, zz));
            }
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
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
        placeSchematic(location, schematic, ignoreOffset, tileEntities, entities, biomes, placeAir, updateEntityAI, updateLighting, false, null, null);
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
            boolean updateLighting,
            boolean keepEntityUUIDs,
            @Nullable SchematicBlockTransformer blockTransformer,
            @Nullable SchematicEntityTransformer entityTransformer
    ) {
        if(biomes && schematic.biomeData() == null)
            biomes = false;

        Location origin = ignoreOffset ? location : location.clone()
                .add(new Vector(schematic.offset().x(), schematic.offset().y(), schematic.offset().z()));

        Location negOrigin = !ignoreOffset ? location : location.clone()
                .subtract(new Vector(schematic.offset().x(), schematic.offset().y(), schematic.offset().z()));

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

                    if(blockData == null) {
                        plugin.getLogger().severe("Failed to find block data in palette at " + x + ", " + y + ", " + z + " with state id "
                                + schematic.getPaletteStateId(x, y, z));
                        continue;
                    }

                    if(blockData.equals("minecraft:air") && !placeAir)
                        continue;

                    BlockData data = Bukkit.createBlockData(blockData);
                    Material initialType = data.getMaterial();
                    if(blockTransformer != null) {
                        data = blockTransformer.transform(world.getBlockAt(worldX, worldY, worldZ), data);
                        if(data == null)
                            continue;
                    }

                    placeBlockFast(world, worldX, worldY, worldZ, data, updateEntityAI, updateLighting);

                    if(tileEntities && data.getMaterial().equals(initialType)) {
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
                double worldX = negOrigin.getBlockX() + entity.pos().x();
                double worldY = negOrigin.getBlockY() + entity.pos().y();
                double worldZ = negOrigin.getBlockZ() + entity.pos().z();

                CompoundBinaryTag extra = entity.extra()
                        .putString("id", entity.id());

                if(!keepEntityUUIDs) {
                    extra = extra.remove("UUID");
                }

                ListBinaryTag tag = extra.getList("Rotation");
                float yaw = tag.getFloat(0);
                float pitch = tag.getFloat(1);

                if(entityTransformer != null) {
                    extra = entityTransformer.transform(new Location(world, worldX, worldY, worldZ, yaw, pitch), NamespacedKey.fromString(entity.id()), extra);
                    if(extra == null)
                        continue;
                }


                placeEntity(new Location(world, worldX, worldY, worldZ, yaw, pitch), schematic.dataVersion(), extra, entityTransformer);
            }
        }
    }

    public void placeEntity(Location location, int dataVersion, CompoundBinaryTag nbt, @Nullable SchematicEntityTransformer entityTransformer) {
        org.bukkit.entity.Entity entity;
        if(VERSION_ADAPTER == null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                BinaryTagIO.writer().write(nbt.putInt("DataVersion", dataVersion), outputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //noinspection deprecation
            entity = Bukkit.getUnsafe().deserializeEntity(outputStream.toByteArray(), location.getWorld());
        } else {
            entity = VERSION_ADAPTER.deserializeEntity(nbt, dataVersion, location.getWorld());
        }

        if(entityTransformer != null) {
            entityTransformer.transformPreSpawn(location, entity);
        }

        entity.spawnAt(location, CreatureSpawnEvent.SpawnReason.CUSTOM);


        if(entityTransformer != null) {
            entityTransformer.transformAfterSpawn(entity);
        }
    }

    public void placeBlockFast(World world, int x, int y, int z, BlockData blockData, boolean updateEntityAI, boolean updateLighting) {
        if(VERSION_ADAPTER == null) {
            world.getBlockAt(x, y, z).setBlockData(blockData, false);
            return;
        }
        VERSION_ADAPTER.placeBlockFast(world, x, y, z, blockData, updateEntityAI, updateLighting);
    }

    public void placeBlockEntity(Location location, int dataVersion, NamespacedKey type, CompoundBinaryTag nbt) {
        if(VERSION_ADAPTER == null) {
            if(!hasSentVersionAdapterError) {
                plugin.getLogger().log(Level.SEVERE, "Failed to find version adapter. Ignoring block entity.");
                hasSentVersionAdapterError = true;
            }
            return;
        }
        VERSION_ADAPTER.setTileEntity(location, dataVersion, type, nbt);
    }

}
