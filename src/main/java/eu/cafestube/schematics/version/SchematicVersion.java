package eu.cafestube.schematics.version;

import eu.cafestube.schematics.schematic.Schematic;
import me.nullicorn.nedit.type.NBTCompound;

public interface SchematicVersion {

    Schematic deserialize(NBTCompound compound);

    NBTCompound serialize(Schematic schematic);

    int getVersion();

}
