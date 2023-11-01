package eu.cafestube.schematics.schematic;

import eu.cafestube.schematics.math.BlockPos;
import me.nullicorn.nedit.type.NBTCompound;

public record BlockEntity(
        int contentVersion,

        BlockPos pos,
        String id,
        NBTCompound extra
) {}
