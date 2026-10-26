package eu.cafestube.schematics.paper;

import com.mojang.serialization.Dynamic;
import eu.cafestube.schematics.data.DataConverter;
import net.kyori.adventure.nbt.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Map;

public class VersionAdapter1213 implements VersionAdapter {

    private final DataConverter dataConverter = new DataConverter1213(this);

    @Override
    public Entity deserializeEntity(CompoundBinaryTag nbt, int dataVersion, World world) {
        net.minecraft.nbt.CompoundTag compound = convertNBTtoMC(nbt);

        compound = ca.spottedleaf.dataconverter.minecraft.MCDataConverter.convertTag(ca.spottedleaf.dataconverter.minecraft.datatypes.MCTypeRegistry.ENTITY,
                compound, dataVersion, Bukkit.getUnsafe().getDataVersion());

        return net.minecraft.world.entity.EntityType.create(compound, ((org.bukkit.craftbukkit.CraftWorld) world).getHandle(), net.minecraft.world.entity.EntitySpawnReason.LOAD)
                .orElseThrow(() -> new IllegalArgumentException("An ID was not found for the data. Did you downgrade?")).getBukkitEntity();
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
    public void flushChunks(World world, long[] chunks) {
        CraftWorld craftWorld = (CraftWorld) world;
        ServerLevel nmsWorld = craftWorld.getHandle();

        for (long chunk : chunks) {
            sendChunk(nmsWorld, new ChunkPos(chunk));
        }
    }

    @Override
    public DataConverter getDataConverter() {
        return dataConverter;
    }

    private void sendChunk(ServerLevel nmsWorld, ChunkPos coordIntPair) {
        ChunkHolder chunkHolder = nmsWorld.chunkSource.chunkMap.getVisibleChunkIfPresent(coordIntPair.toLong());
        if (chunkHolder == null) {
            return;
        }
        LevelChunk levelChunk = nmsWorld.getChunkSource().getChunkAtIfLoadedImmediately(coordIntPair.x, coordIntPair.z);
        if (levelChunk == null) {
            return;
        }
        ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(levelChunk,
                nmsWorld.getChunkSource().getLightEngine(), null, null, false);

        for (ServerPlayer player : nmsWorld.getChunkSource().chunkMap.getPlayers(coordIntPair, false)) {
            player.connection.send(packet);
        }
    }

    @Override
    public void setTileEntity(Location location, int dataVersion, NamespacedKey type, CompoundBinaryTag nbt) {
        CraftWorld craftWorld = (CraftWorld) location.getWorld();

        BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        net.minecraft.nbt.CompoundTag tag = concatTileEntityToNBT(type.asString(), nbt);

        if(dataVersion != -1 && dataVersion < Bukkit.getUnsafe().getDataVersion()) {
            tag = (net.minecraft.nbt.CompoundTag) DataFixers.getDataFixer().update(References.BLOCK_ENTITY,
                    new Dynamic(NbtOps.INSTANCE, tag), dataVersion, Bukkit.getUnsafe().getDataVersion()).getValue();
        }

        BlockEntity blockEntity = BlockEntity.loadStatic(blockPos, craftWorld.getHandle().getBlockState(blockPos), tag,
                MinecraftServer.getServer().registryAccess());
        if(blockEntity == null) return;

        craftWorld.getHandle().setBlockEntity(blockEntity);
    }

    public CompoundTag concatTileEntityToNBT(String type, CompoundBinaryTag extra) {
        net.minecraft.nbt.CompoundTag tag = convertNBTtoMC(extra);
        tag.putString("id", type);
        return tag;
    }

    public CompoundTag concatEntityToNBT(String id, CompoundBinaryTag extra) {
        net.minecraft.nbt.CompoundTag tag = convertNBTtoMC(extra);
        tag.putString("id", id);
        return tag;
    }

    public CompoundTag convertNBTtoMC(CompoundBinaryTag nbt) {
        CompoundTag mcNBT = new CompoundTag();
        nbt.forEach(stringEntry -> mcNBT.put(stringEntry.getKey(), convertTag(stringEntry.getValue())));
        return mcNBT;
    }

    public static <T extends BinaryTag> Tag convertTag(T tag) {
        return switch (tag.type().id()) {
            case Tag.TAG_END -> EndTag.INSTANCE;
            case Tag.TAG_BYTE -> ByteTag.valueOf(((ByteBinaryTag) tag).value());
            case Tag.TAG_SHORT -> ShortTag.valueOf(((ShortBinaryTag) tag).value());
            case Tag.TAG_INT -> IntTag.valueOf(((IntBinaryTag) tag).value());
            case Tag.TAG_LONG -> LongTag.valueOf(((LongBinaryTag) tag).value());
            case Tag.TAG_FLOAT -> FloatTag.valueOf(((FloatBinaryTag) tag).value());
            case Tag.TAG_DOUBLE -> DoubleTag.valueOf(((DoubleBinaryTag) tag).value());
            case Tag.TAG_BYTE_ARRAY -> new ByteArrayTag(((ByteArrayBinaryTag) tag).value());
            case Tag.TAG_STRING -> StringTag.valueOf(((StringBinaryTag) tag).value());
            case Tag.TAG_LIST -> {
                ListBinaryTag listTag = (ListBinaryTag) tag;
                int size = listTag.size();
                ListTag list = new ListTag();
                for (int i = 0; i < size; i++) {
                    list.add(convertTag(listTag.get(i)));
                }
                yield list;
            }
            case Tag.TAG_COMPOUND -> {
                CompoundBinaryTag compoundTag = (CompoundBinaryTag) tag;
                CompoundTag compound = new CompoundTag();
                for (Map.Entry<String, ? extends BinaryTag> entry : compoundTag) {
                    compound.put(entry.getKey(), convertTag(entry.getValue()));
                }
                yield compound;
            }
            case Tag.TAG_INT_ARRAY -> new IntArrayTag(((IntArrayBinaryTag) tag).value());
            case Tag.TAG_LONG_ARRAY -> new LongArrayTag(((LongArrayBinaryTag) tag).value());
            default -> throw new IllegalArgumentException("Invalid tag type " + tag.type().id());
        };
    }

    @SuppressWarnings("unchecked")
    public static <T extends BinaryTag> T convertTag(Tag base) {
        return switch (base.getId()) {
            case Tag.TAG_END -> (T) EndBinaryTag.endBinaryTag();
            case Tag.TAG_BYTE -> (T) ByteBinaryTag.byteBinaryTag(((ByteTag) base).getAsByte());
            case Tag.TAG_SHORT -> (T) ShortBinaryTag.shortBinaryTag(((ShortTag) base).getAsShort());
            case Tag.TAG_INT -> (T) IntBinaryTag.intBinaryTag(((IntTag) base).getAsInt());
            case Tag.TAG_LONG -> (T) LongBinaryTag.longBinaryTag(((LongTag) base).getAsLong());
            case Tag.TAG_FLOAT -> (T) FloatBinaryTag.floatBinaryTag(((FloatTag) base).getAsFloat());
            case Tag.TAG_DOUBLE -> (T) DoubleBinaryTag.doubleBinaryTag(((DoubleTag) base).getAsDouble());
            case Tag.TAG_BYTE_ARRAY -> (T) ByteArrayBinaryTag.byteArrayBinaryTag(((ByteArrayTag) base).getAsByteArray());
            case Tag.TAG_STRING -> (T) StringBinaryTag.stringBinaryTag(((StringTag) base).getAsString());
            case Tag.TAG_LIST -> {
                ListTag originalList = ((ListTag) base);
                if(originalList.isEmpty()) {
                    yield (T) ListBinaryTag.empty();
                }

                ListBinaryTag.Builder<BinaryTag> listBuilder = ListBinaryTag.builder();
                for (Tag entry : originalList) {
                    BinaryTag converted = convertTag(entry);
                    listBuilder.add(converted);
                }
                yield (T) listBuilder.build();
            }
            case Tag.TAG_COMPOUND -> {
                CompoundTag tag = (CompoundTag) base;
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                for (String key : tag.getAllKeys()) {
                    Tag value = tag.get(key);
                    if (value != null) {
                        builder.put(key, convertTag(value));
                    }
                }
                yield (T) builder.build();
            }
            case Tag.TAG_INT_ARRAY -> (T) IntArrayBinaryTag.intArrayBinaryTag(((IntArrayTag) base).getAsIntArray());
            case Tag.TAG_LONG_ARRAY -> (T) LongArrayBinaryTag.longArrayBinaryTag(((LongArrayTag) base).getAsLongArray());
            default -> throw new IllegalArgumentException("Invalid tag type " + base.getId());
        };
    }

}
