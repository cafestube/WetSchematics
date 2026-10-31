package eu.cafestube.schematics.paper;

import ca.spottedleaf.dataconverter.minecraft.MCDataConverter;
import ca.spottedleaf.dataconverter.minecraft.datatypes.MCTypeRegistry;
import ca.spottedleaf.moonrise.common.PlatformHooks;
import com.mojang.serialization.Dynamic;
import eu.cafestube.schematics.data.DataConverter;
import eu.cafestube.schematics.schematic.BlockEntity;
import eu.cafestube.schematics.schematic.Entity;
import eu.cafestube.schematics.schematic.biome.BiomeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.state.StateHolder;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class DataConverter1213 implements DataConverter {

    private final int dataVersion = Bukkit.getUnsafe().getDataVersion();
    private final VersionAdapter1213 versionAdapter;

    public DataConverter1213(VersionAdapter1213 versionAdapter) {
        this.versionAdapter = versionAdapter;
    }

    @Override
    public int getFinalDataVersion(int source) {
        if(source > dataVersion) return source;
        return dataVersion;
    }

    private CompoundTag convertToVanilla(String blockState) {
        int propIdx = blockState.indexOf('[');
        CompoundTag out = new CompoundTag();

        if (propIdx < 0) {
            out.putString(StateHolder.NAME_TAG, blockState);
            return out;
        }

        out.putString(StateHolder.NAME_TAG, blockState.substring(0, propIdx));
        CompoundTag properties = new CompoundTag();
        String props = blockState.substring(propIdx + 1, blockState.length() - 1);
        String[] propArr = props.split(",");
        for (String pair : propArr) {
            final String[] split = pair.split("=");
            properties.putString(split[0], split[1]);
        }
        out.put(StateHolder.PROPERTIES_TAG, properties);
        return out;
    }

    private String convertBackToSpongeRepresentation(CompoundTag tag) {
        if (tag.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        out.append(tag.getString(StateHolder.NAME_TAG));
        if (tag.contains(StateHolder.PROPERTIES_TAG)) {
            CompoundTag properties = tag.getCompound(StateHolder.PROPERTIES_TAG);
            out.append('[');
            boolean first = true;
            for (String key : properties.getAllKeys()) {
                if(!first) out.append(",");
                out.append(key).append("=").append(properties.getString(key).replace("\"", ""));
                first = false;
            }
            out.append(']');
        }
        return out.toString();
    }

    @Override
    public String upgradeBlockState(String state, int from) {
        if(from >= dataVersion) return state;

        CompoundTag tag = convertToVanilla(state);
        Dynamic<Tag> dynamic = new Dynamic<>(NbtOps.INSTANCE, tag);
        CompoundTag result = (CompoundTag) MinecraftServer.getServer().getFixerUpper()
                .update(References.BLOCK_STATE, dynamic, from, dataVersion).getValue();
        return convertBackToSpongeRepresentation(result);
    }

    @Override
    public BlockEntity upgradeBlockEntity(BlockEntity from) {
        if(from.contentVersion() >= dataVersion) return from;
        CompoundTag nbt = versionAdapter.concatTileEntityToNBT(from.id(), from.extra());

        CompoundTag result = MCDataConverter.convertTag(MCTypeRegistry.TILE_ENTITY, nbt, from.contentVersion(), dataVersion);
        String id = result.getString("id");
        if (id.isEmpty()) {
            throw new NoSuchElementException("Missing 'id' tag");
        }
        result.remove("id");

        return new BlockEntity(
                dataVersion,
                from.pos(),
                id,
                VersionAdapter1213.convertTag(result)
        );
    }

    @Override
    public Entity upgradeEntity(Entity entity, int from) {
        if(from >= dataVersion) return entity;

        CompoundTag nbt = versionAdapter.concatEntityToNBT(entity.id(), entity.extra());

        CompoundTag result = PlatformHooks.get().convertNBT(References.ENTITY,
                MinecraftServer.getServer().getFixerUpper(), nbt, from, dataVersion);

        String id = result.getString("id");
        if (id.isEmpty()) {
            throw new NoSuchElementException("Missing 'id' tag");
        }
        result.remove("id");

        return new Entity(
                entity.pos(),
                id,
                VersionAdapter1213.convertTag(result)
        );
    }

    @Override
    public BiomeData upgradeBiomeData(BiomeData biomeData, int from) {
        if(biomeData == null) return null;
        if(from >= dataVersion) return biomeData;

        Map<Integer, String> biomes = new HashMap<>(biomeData.biomePalette().size());

        for (Map.Entry<Integer, String> integerStringEntry : biomeData.biomePalette().entrySet()) {
            biomes.put(integerStringEntry.getKey(), upgradeBiome(integerStringEntry.getValue(), from));
        }

        return new BiomeData(
                biomes,
                biomeData.biomes(),
                biomeData.type()
        );
    }

    @Override
    public String upgradeBiome(String key, int from) {
        if(from >= dataVersion) return key;

        return MinecraftServer.getServer().getFixerUpper()
                .update(References.BIOME, new Dynamic<>(NbtOps.INSTANCE, StringTag.valueOf(key)), from, this.dataVersion)
                .asString().result().orElse(key);
    }
}
