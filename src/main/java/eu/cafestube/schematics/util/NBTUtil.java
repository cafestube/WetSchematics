package eu.cafestube.schematics.util;

import me.nullicorn.nedit.type.NBTCompound;

public class NBTUtil {

    public static NBTCompound clone(NBTCompound compound) {
        NBTCompound compoundClone = new NBTCompound();

        for(String key : compound.keySet()) {
            Object o = compound.get(key);
            if(o instanceof NBTCompound) {
                o = clone((NBTCompound) o);
            }
            System.out.println(o);
            compoundClone.put(key, o);
        }

        return compoundClone;
    }

}
