package eu.cafestube.schematics.version;

import eu.cafestube.schematics.schematic.Schematic;
import net.kyori.adventure.nbt.CompoundBinaryTag;

public interface SchematicVersion {

    Schematic deserialize(CompoundBinaryTag compound);

    CompoundBinaryTag serialize(Schematic schematic);

    int getVersion();

}
