package eu.cafestube.schematics.paper.transformer;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SchematicEntityTransformer {

    @Nullable
    CompoundBinaryTag transform(Location location, NamespacedKey type, CompoundBinaryTag nbt);

    static SchematicEntityTransformer combined(SchematicEntityTransformer... transformers) {
        return new CombinedSchematicEntityTransformer(List.of(transformers));
    }

}
