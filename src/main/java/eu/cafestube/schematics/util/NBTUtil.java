package eu.cafestube.schematics.util;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ShortTag;

public class NBTUtil {

    public static short getShortOrDefault(CompoundTag tag, String field, short defaultValue) {
        if(tag.contains(field)) {
            return tag.<ShortTag>get(field).getValue();
        } else {
            return defaultValue;
        }
    }

    public static int getIntOrDefault(CompoundTag tag, String field, int defaultValue) {
        if(tag.contains(field)) {
            return tag.<IntTag>get(field).getValue();
        } else {
            return defaultValue;
        }
    }

}
