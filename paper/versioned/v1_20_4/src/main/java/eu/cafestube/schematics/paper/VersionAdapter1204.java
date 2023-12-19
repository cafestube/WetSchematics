package eu.cafestube.schematics.paper;

import com.github.steveice10.opennbt.tag.builtin.*;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class VersionAdapter1204 implements VersionAdapter {

    @Override
    public Entity spawnEntity(Location location, int dataVersion, NamespacedKey type, CompoundTag nbt) {
        EntityType entityType = Registry.ENTITY_TYPE.get(type);
        if(entityType == null) throw new IllegalArgumentException("Unknown entity type: " + type);
        Entity entity = location.getWorld().spawnEntity(location, entityType);

        net.minecraft.nbt.CompoundTag tag = convertNBTtoMC(nbt);

        if(dataVersion != -1 && dataVersion < CraftMagicNumbers.INSTANCE.getDataVersion()) {
            tag = (net.minecraft.nbt.CompoundTag) DataFixers.getDataFixer().update(References.ENTITY,
                    new Dynamic(NbtOps.INSTANCE, tag), dataVersion, CraftMagicNumbers.INSTANCE.getDataVersion()).getValue();
        }

        net.minecraft.world.entity.Entity mcEntity = ((CraftEntity) entity).getHandle();
        mcEntity.load(tag);

        return entity;
    }

    private static final int UPDATE = 1;
    private static final int NOTIFY = 2;

    @Override
    public void placeBlockFast(World world, int x, int y, int z, BlockData blockData, boolean updateEntityAI, boolean updateLighting) {
        ServerLevel craftWorld = ((CraftWorld) world).getHandle();
        BlockPos blockPos = new BlockPos(x, y, z);
        LevelChunk chunk = craftWorld.getChunkAt(blockPos);

        BlockState expectedBlockState = ((CraftBlockData) blockData).getState();
        BlockState oldState = chunk.getBlockState(blockPos);
        BlockState newState = chunk.setBlockState(blockPos, expectedBlockState, false, true);

        if(newState != null) {
            if(updateLighting) {
                craftWorld.getChunkSource().getLightEngine().checkBlock(blockPos);
            }

            if(chunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING)) {
                if(updateEntityAI) {
                    craftWorld.sendBlockUpdated(blockPos, oldState, newState, UPDATE | NOTIFY);
                } else {
                    craftWorld.getChunkSource().blockChanged(blockPos);
                }
            }

        }

    }

    @Override
    public void setTileEntity(Location location, int dataVersion, NamespacedKey type, CompoundTag nbt) {
        CraftWorld craftWorld = (CraftWorld) location.getWorld();

        BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        net.minecraft.nbt.CompoundTag tag = convertNBTtoMC(nbt);
        tag.putString("id", type.toString());

        if(dataVersion != -1 && dataVersion < CraftMagicNumbers.INSTANCE.getDataVersion()) {
            tag = (net.minecraft.nbt.CompoundTag) DataFixers.getDataFixer().update(References.BLOCK_ENTITY,
                    new Dynamic(NbtOps.INSTANCE, tag), dataVersion, CraftMagicNumbers.INSTANCE.getDataVersion()).getValue();
        }


        BlockEntity blockEntity = BlockEntity.loadStatic(blockPos, craftWorld.getHandle().getBlockState(blockPos), tag);
        if(blockEntity == null) return;

        craftWorld.getHandle().setBlockEntity(blockEntity);
    }

    private net.minecraft.nbt.CompoundTag convertNBTtoMC(CompoundTag nbt) {
        net.minecraft.nbt.CompoundTag mcNBT = new net.minecraft.nbt.CompoundTag();

        for(String key : nbt.getValue().keySet()) {
            net.minecraft.nbt.Tag tag = convertTag(nbt.get(key));
            if(tag == null) continue;

            mcNBT.put(key, tag);
        }

        return mcNBT;
    }

    private net.minecraft.nbt.Tag convertTag(Tag tag) {
        if(tag instanceof CompoundTag compoundTag) {
            return convertNBTtoMC(compoundTag);
        } else if(tag instanceof IntTag intTag) {
            return net.minecraft.nbt.IntTag.valueOf(intTag.getValue());
        } else if(tag instanceof FloatTag floatTag) {
            return net.minecraft.nbt.FloatTag.valueOf(floatTag.getValue());
        } else if(tag instanceof DoubleTag doubleTag) {
            return net.minecraft.nbt.DoubleTag.valueOf(doubleTag.getValue());
        } else if(tag instanceof LongTag longTag) {
            return net.minecraft.nbt.LongTag.valueOf(longTag.getValue());
        } else if(tag instanceof ShortTag shortTag) {
            return net.minecraft.nbt.ShortTag.valueOf(shortTag.getValue());
        } else if(tag instanceof StringTag stringTag) {
            return net.minecraft.nbt.StringTag.valueOf(stringTag.getValue());
        } else if(tag instanceof ByteArrayTag byteArrayTag) {
            return new net.minecraft.nbt.ByteArrayTag(byteArrayTag.getValue());
        } else if(tag instanceof IntArrayTag intArrayTag) {
            return new net.minecraft.nbt.IntArrayTag(intArrayTag.getValue());
        } else if(tag instanceof LongArrayTag longArrayTag) {
            return new net.minecraft.nbt.LongArrayTag(longArrayTag.getValue());
        } else if(tag instanceof ListTag listTag) {
            net.minecraft.nbt.ListTag mcListTag = new net.minecraft.nbt.ListTag();
            for(Tag listTagElement : listTag.getValue()) {
                mcListTag.add(convertTag(listTagElement));
            }
            return mcListTag;
        } else if(tag instanceof ByteTag byteTag) {
            return net.minecraft.nbt.ByteTag.valueOf(byteTag.getValue());
        }
        return null;
    }

}
