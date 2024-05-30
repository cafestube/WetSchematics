package eu.cafestube.schematics.paper;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;

import java.lang.reflect.InvocationTargetException;

public interface VersionAdapter {

    Entity deserializeEntity(CompoundBinaryTag nbt, int dataVersion, World world);

    void setTileEntity(Location location, int dataVersion, NamespacedKey type, CompoundBinaryTag nbt);

    void placeBlockFast(World world, int x, int y, int z, BlockData blockData, boolean updateEntityAI, boolean updateLighting);


    static VersionAdapter create() {
        //Paper announced that they will be deprecating the old CraftBukkit package names
        //in the future but getMinecraftVersion does not exist in spigot

        String packageName = VersionAdapter.class.getPackageName();

        String versionAdapterName = null;
        if (hasClass("com.destroystokyo.paper.PaperConfig") || hasClass("io.papermc.paper.configuration.Configuration")) {
            versionAdapterName = "VersionAdapter" + Bukkit.getMinecraftVersion().replace(".", "");
        } else {
            versionAdapterName = "VersionAdapter" + Bukkit.getBukkitVersion().split("-")[0].replace(".", "");
        }

        try {
            return (VersionAdapter) Class.forName(packageName + "." + versionAdapterName).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException |
                 InvocationTargetException | NoSuchMethodException e) {
            return null;
        }
    }

    private static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
