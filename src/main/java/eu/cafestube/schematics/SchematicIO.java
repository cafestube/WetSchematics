package eu.cafestube.schematics;

import eu.cafestube.schematics.schematic.Schematic;
import eu.cafestube.schematics.version.SchematicVersion;
import eu.cafestube.schematics.version.V1SchematicVersion;
import eu.cafestube.schematics.version.V2SchematicVersion;
import eu.cafestube.schematics.version.V3SchematicVersion;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.io.*;
import java.util.Map;

public class SchematicIO {

    private static final Map<Integer, SchematicVersion> versions = Map.of(
            1, new V1SchematicVersion(),
            2, new V2SchematicVersion(),
            3, new V3SchematicVersion()
    );

    public static void writeSchematic(OutputStream stream, int version, Schematic schematic) throws IOException {
        writeSchematic(stream, version, schematic, true);
    }


    public static void writeSchematic(OutputStream stream, int version, Schematic schematic, boolean compressed) throws IOException {
        SchematicVersion schematicVersion = versions.get(version);

        try (stream) {
            if (schematicVersion == null) {
                throw new IllegalArgumentException("Unsupported schematic version: " + version);
            }
            CompoundBinaryTag serialize = schematicVersion.serialize(schematic);
            serialize.putInt("Version", version);

            BinaryTagIO.writer().write(serialize, stream, compressed ? BinaryTagIO.Compression.GZIP : BinaryTagIO.Compression.NONE);
        }
    }

    public static Schematic parseSchematic(File file) throws IOException {
        return parseSchematic(file, true);
    }

    public static Schematic parseSchematic(File file, boolean compressed) throws IOException {
        return parseSchematic(new FileInputStream(file), compressed);
    }

    public static Schematic parseSchematic(InputStream in, boolean compressed) throws IOException {
        try (in) {
            CompoundBinaryTag tag = BinaryTagIO.reader().read(in, compressed ? BinaryTagIO.Compression.GZIP : BinaryTagIO.Compression.NONE);
            return parseSchematic(tag);
        }
    }

    public static Schematic parseSchematic(InputStream inputStream) throws IOException {
        return parseSchematic(inputStream, true);
    }

    public static Schematic parseSchematic(CompoundBinaryTag compound) {
        int version = compound.getInt("Version");

        SchematicVersion schematicVersion = versions.get(version);
        if(schematicVersion == null) {
            throw new IllegalArgumentException("Unsupported schematic version: " + version);
        }

        return schematicVersion.deserialize(compound);
    }

}
