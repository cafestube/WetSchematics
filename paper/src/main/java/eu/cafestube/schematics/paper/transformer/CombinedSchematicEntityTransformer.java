package eu.cafestube.schematics.paper.transformer;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CombinedSchematicEntityTransformer implements SchematicEntityTransformer {

    private final List<SchematicEntityTransformer> entityTransformers;

    public CombinedSchematicEntityTransformer(List<SchematicEntityTransformer> entityTransformers) {
        this.entityTransformers = entityTransformers;
    }

    @Override
    public @Nullable CompoundBinaryTag transform(Location location, NamespacedKey type, CompoundBinaryTag nbt) {
        for (SchematicEntityTransformer transformer : entityTransformers) {
            nbt = transformer.transform(location, type, nbt);
            if (nbt == null) {
                return null;
            }
        }
        return nbt;
    }
}
