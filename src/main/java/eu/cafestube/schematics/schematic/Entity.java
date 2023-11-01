package eu.cafestube.schematics.schematic;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import eu.cafestube.schematics.math.Pos;

public record Entity(
        Pos pos,
        String id,
        CompoundTag extra
) {}