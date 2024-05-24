package eu.cafestube.schematics.paper;

import com.mojang.serialization.Dynamic;
import net.kyori.adventure.nbt.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class VersionAdapter1201 implements VersionAdapter {

    @Override
    public Entity spawnEntity(Location location, int dataVersion, NamespacedKey type, CompoundBinaryTag nbt) {
        EntityType entityType = Registry.ENTITY_TYPE.get(type);
        if(entityType == null) throw new IllegalArgumentException("Unknown entity type: " + type);
        Entity entity = location.getWorld().spawnEntity(location, entityType);

        CompoundTag tag = convertNBTtoMC(nbt);

        if(dataVersion != -1 && dataVersion < CraftMagicNumbers.INSTANCE.getDataVersion()) {
            tag = (CompoundTag) DataFixers.getDataFixer().update(References.ENTITY,
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
    public void setTileEntity(Location location, int dataVersion, NamespacedKey type, CompoundBinaryTag nbt) {
        CraftWorld craftWorld = (CraftWorld) location.getWorld();

        BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        CompoundTag tag = convertNBTtoMC(nbt);
        tag.putString("id", type.toString());

        if(dataVersion != -1 && dataVersion < CraftMagicNumbers.INSTANCE.getDataVersion()) {
            tag = (CompoundTag) DataFixers.getDataFixer().update(References.BLOCK_ENTITY,
                    new Dynamic(NbtOps.INSTANCE, tag), dataVersion, CraftMagicNumbers.INSTANCE.getDataVersion()).getValue();
        }


        BlockEntity blockEntity = BlockEntity.loadStatic(blockPos, craftWorld.getHandle().getBlockState(blockPos), tag);
        if(blockEntity == null) return;

        craftWorld.getHandle().setBlockEntity(blockEntity);
    }

    private CompoundTag convertNBTtoMC(CompoundBinaryTag nbt) {
        CompoundTag mcNBT = new CompoundTag();
        nbt.forEach(stringEntry -> mcNBT.put(stringEntry.getKey(), convertTag(stringEntry.getValue())));
        return mcNBT;
    }

    private Tag convertTag(BinaryTag tag) {
        if(tag instanceof CompoundBinaryTag compoundTag) {
            return convertNBTtoMC(compoundTag);
        } else if(tag instanceof IntBinaryTag intTag) {
            return IntTag.valueOf(intTag.value());
        } else if(tag instanceof FloatBinaryTag floatTag) {
            return FloatTag.valueOf(floatTag.value());
        } else if(tag instanceof DoubleBinaryTag doubleTag) {
            return DoubleTag.valueOf(doubleTag.value());
        } else if(tag instanceof LongBinaryTag longTag) {
            return LongTag.valueOf(longTag.value());
        } else if(tag instanceof ShortBinaryTag shortTag) {
            return ShortTag.valueOf(shortTag.value());
        } else if(tag instanceof StringBinaryTag stringTag) {
            return StringTag.valueOf(stringTag.value());
        } else if(tag instanceof ByteArrayBinaryTag byteArrayTag) {
            return new ByteArrayTag(byteArrayTag.value());
        } else if(tag instanceof IntArrayBinaryTag intArrayTag) {
            return new IntArrayTag(intArrayTag.value());
        } else if(tag instanceof LongArrayBinaryTag longArrayTag) {
            return new LongArrayTag(longArrayTag.value());
        } else if(tag instanceof ListBinaryTag listTag) {
            ListTag mcListTag = new ListTag();
            listTag.forEach(binaryTag -> mcListTag.add(convertTag(binaryTag)));
            return mcListTag;
        } else if(tag instanceof ByteBinaryTag byteTag) {
            return ByteTag.valueOf(byteTag.value());
        }
        throw new IllegalArgumentException("Unknown tag type: " + tag.getClass());
    }

}
