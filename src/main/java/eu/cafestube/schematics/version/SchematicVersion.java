package eu.cafestube.schematics.version;

import eu.cafestube.schematics.data.DataConverter;
import eu.cafestube.schematics.schematic.Schematic;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jetbrains.annotations.NotNull;

public interface SchematicVersion {

    Schematic deserialize(CompoundBinaryTag compound, @NotNull DataConverter dataConverter);

    CompoundBinaryTag serialize(Schematic schematic);

    int getVersion();

}
