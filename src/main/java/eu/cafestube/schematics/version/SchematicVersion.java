package eu.cafestube.schematics.version;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import eu.cafestube.schematics.schematic.Schematic;

public interface SchematicVersion {

    Schematic deserialize(CompoundTag compound);

    CompoundTag serialize(Schematic schematic);

    int getVersion();

}
