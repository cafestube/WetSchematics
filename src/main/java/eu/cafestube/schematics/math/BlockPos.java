package eu.cafestube.schematics.math;

public record BlockPos(int x, int y, int z) {

    public static final BlockPos ZERO = new BlockPos(0, 0, 0);

    public static BlockPos fromArray(int[] array) {
        return new BlockPos(array[0], array[1], array[2]);
    }

}
