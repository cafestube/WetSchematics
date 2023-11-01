package eu.cafestube.schematics.math;

import com.github.steveice10.opennbt.tag.builtin.DoubleTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;

public record Pos(double x, double y, double z) {

    public static final Pos ZERO = new Pos(0, 0, 0);

    public static Pos fromDoubleList(ListTag list) {
        return new Pos(list.<DoubleTag>get(0).getValue(), list.<DoubleTag>get(1).getValue(), list.<DoubleTag>get(2).getValue());
    }


}
