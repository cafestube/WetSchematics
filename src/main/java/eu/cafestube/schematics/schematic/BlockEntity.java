package eu.cafestube.schematics.schematic;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import eu.cafestube.schematics.math.BlockPos;

public record BlockEntity(
        int contentVersion,

        BlockPos pos,
        String id,
        CompoundTag extra
) {}
