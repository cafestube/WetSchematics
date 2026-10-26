package eu.cafestube.schematics.paper;

import ca.spottedleaf.moonrise.common.PlatformHooks;
import com.mojang.serialization.Dynamic;
import eu.cafestube.schematics.data.DataConverter;
import eu.cafestube.schematics.schematic.BlockEntity;
import eu.cafestube.schematics.schematic.Entity;
import eu.cafestube.schematics.schematic.biome.BiomeData;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.state.StateHolder;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DataConverter263 implements DataConverter {

    private static final int BLOCK_STATE_FIELD_RENAME_VERSION = 5006;
    private final int dataVersion = Bukkit.getUnsafe().getDataVersion();
    private final VersionAdapter263 versionAdapter;

    public DataConverter263(VersionAdapter263 versionAdapter) {
        this.versionAdapter = versionAdapter;
    }

    @Override
    public int getFinalDataVersion(int source) {
        if(source > dataVersion) return source;
        return dataVersion;
    }


    private CompoundTag convertToVanilla(String blockState, int from) {
        boolean legacyFieldNames = from < BLOCK_STATE_FIELD_RENAME_VERSION;
        String idKey = legacyFieldNames ? NbtUtils.LEGACY_BLOCK_STATE_ID_TAG : StateHolder.ID_TAG;
        String propertiesKey = legacyFieldNames ? NbtUtils.LEGACY_BLOCKSTATE_PROPERTY_TAG : StateHolder.PROPERTIES_TAG;

        int propIdx = blockState.indexOf('[');
        CompoundTag out = new CompoundTag();

        if (propIdx < 0) {
            out.putString(idKey, blockState);
            return out;
        }


        out.putString(idKey, blockState.substring(0, propIdx));
        CompoundTag properties = new CompoundTag();
        String props = blockState.substring(propIdx + 1, blockState.length() - 1);
        String[] propArr = props.split(",");
        for (String pair : propArr) {
            final String[] split = pair.split("=");
            properties.putString(split[0], split[1]);
        }
        out.put(propertiesKey, properties);
        return out;
    }

    private String convertBackToSpongeRepresentation(CompoundTag tag) {
        if (tag.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        out.append(tag.getString(StateHolder.ID_TAG).orElseThrow());

        Optional<CompoundTag> propertiesOpt = tag.getCompound(StateHolder.PROPERTIES_TAG);
        if(propertiesOpt.isPresent()) {
            CompoundTag properties = propertiesOpt.get();

            out.append('[');
            boolean first = true;
            for (String key : properties.keySet()) {
                if(!first) out.append(",");
                out.append(key).append("=").append(properties.getString(key).get().replace("\"", ""));
                first = false;
            }
            out.append(']');
        }
        return out.toString();
    }


    @Override
    public String upgradeBlockState(String state, int from) {
        if(from >= dataVersion) return state;

        CompoundTag tag = convertToVanilla(state, from);
        CompoundTag result = PlatformHooks.get().convertNBT(References.BLOCK_STATE,
                MinecraftServer.getServer().getFixerUpper(), tag, from, dataVersion);
        return convertBackToSpongeRepresentation(result);
    }

    @Override
    public BlockEntity upgradeBlockEntity(BlockEntity from) {
        if(from.contentVersion() >= dataVersion) return from;
        CompoundTag nbt = versionAdapter.concatTileEntityToNBT(from.id(), from.extra());

        CompoundTag result = PlatformHooks.get().convertNBT(References.BLOCK_ENTITY,
                MinecraftServer.getServer().getFixerUpper(), nbt, from.contentVersion(), dataVersion);

        String id = result.getString("id").get();
        result.remove("id");

        return new BlockEntity(
                dataVersion,
                from.pos(),
                id,
                VersionAdapter263.convertTag(result)
        );
    }

    @Override
    public Entity upgradeEntity(Entity entity, int from) {
        if(from >= dataVersion) return entity;

        CompoundTag nbt = versionAdapter.concatEntityToNBT(entity.id(), entity.extra());

        CompoundTag result = PlatformHooks.get().convertNBT(References.ENTITY,
                MinecraftServer.getServer().getFixerUpper(), nbt, from, dataVersion);

        String id = result.getString("id").get();
        result.remove("id");

        return new Entity(
                entity.pos(),
                id,
                VersionAdapter263.convertTag(result)
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
