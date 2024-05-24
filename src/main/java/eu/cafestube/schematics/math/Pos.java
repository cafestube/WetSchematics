package eu.cafestube.schematics.math;


import net.kyori.adventure.nbt.ListBinaryTag;

public record Pos(double x, double y, double z) {

    public static final Pos ZERO = new Pos(0, 0, 0);

    public static Pos fromDoubleList(ListBinaryTag list) {
        return new Pos(list.getDouble(0), list.getDouble(1), list.getDouble(2));
    }


}
