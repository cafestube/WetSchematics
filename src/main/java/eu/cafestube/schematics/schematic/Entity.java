package eu.cafestube.schematics.schematic;

import eu.cafestube.schematics.math.Pos;
import me.nullicorn.nedit.type.NBTCompound;

public record Entity(
        Pos pos,
        String id,
        NBTCompound extra
) {}