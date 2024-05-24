package eu.cafestube.schematics.schematic;

import eu.cafestube.schematics.math.Pos;
import net.kyori.adventure.nbt.CompoundBinaryTag;

public record Entity(
        Pos pos,
        String id,
        CompoundBinaryTag extra
) {}