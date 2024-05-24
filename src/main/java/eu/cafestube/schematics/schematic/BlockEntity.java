package eu.cafestube.schematics.schematic;

import eu.cafestube.schematics.math.BlockPos;
import net.kyori.adventure.nbt.CompoundBinaryTag;

public record BlockEntity(
        int contentVersion,

        BlockPos pos,
        String id,
        CompoundBinaryTag extra
) {}
