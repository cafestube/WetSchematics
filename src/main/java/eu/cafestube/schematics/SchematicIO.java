package eu.cafestube.schematics;

import com.github.steveice10.opennbt.NBTIO;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import eu.cafestube.schematics.schematic.Schematic;
import eu.cafestube.schematics.version.SchematicVersion;
import eu.cafestube.schematics.version.V1SchematicVersion;
import eu.cafestube.schematics.version.V2SchematicVersion;
import eu.cafestube.schematics.version.V3SchematicVersion;

import java.io.*;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SchematicIO {

    private static final Map<Integer, SchematicVersion> versions = Map.of(
            1, new V1SchematicVersion(),
            2, new V2SchematicVersion(),
            3, new V3SchematicVersion()
    );

    public static void writeSchematic(OutputStream stream, int version, Schematic schematic) throws IOException {
        writeSchematic(stream, version, schematic, true, false);
    }


    public static void writeSchematic(OutputStream stream, int version, Schematic schematic, boolean compressed, boolean littleEndian) throws IOException {
        SchematicVersion schematicVersion = versions.get(version);

        try {
            if(schematicVersion == null) {
                throw new IllegalArgumentException("Unsupported schematic version: " + version);
            }
            CompoundTag serialize = schematicVersion.serialize(schematic);
            serialize.put(new IntTag("Version", version));

            if(compressed) {
                stream = new GZIPOutputStream(stream);
            }

            NBTIO.writeTag(stream, serialize, littleEndian);
        } finally {
            stream.close();
        }
    }

    public static Schematic parseSchematic(File file) throws IOException {
        return parseSchematic(NBTIO.readFile(file));
    }

    public static Schematic parseSchematic(File file, boolean compressed, boolean littleEndian) throws IOException {
        return parseSchematic(NBTIO.readFile(file, compressed, littleEndian));
    }

    public static Schematic parseSchematic(InputStream in, boolean compressed, boolean littleEndian) throws IOException {
        try {
            if (compressed) {
                in = new GZIPInputStream(in);
            }

            Tag tag = NBTIO.readTag(in, littleEndian);
            if (!(tag instanceof CompoundTag)) {
                throw new IOException("Root tag is not a CompoundTag!");
            }

            return parseSchematic((CompoundTag) tag);
        } finally {
            in.close();
        }
    }

    public static Schematic parseSchematic(InputStream inputStream) throws IOException {
        return parseSchematic(inputStream, true, false);
    }

    public static Schematic parseSchematic(CompoundTag compound) {
        int version = compound.<IntTag>get("Version").getValue();

        SchematicVersion schematicVersion = versions.get(version);
        if(schematicVersion == null) {
            throw new IllegalArgumentException("Unsupported schematic version: " + version);
        }

        return schematicVersion.deserialize(compound);
    }

}
