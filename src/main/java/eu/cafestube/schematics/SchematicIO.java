package eu.cafestube.schematics;

import eu.cafestube.schematics.data.DataConverter;
import eu.cafestube.schematics.data.NoOpDataConverter;
import eu.cafestube.schematics.schematic.Schematic;
import eu.cafestube.schematics.version.SchematicVersion;
import eu.cafestube.schematics.version.V1SchematicVersion;
import eu.cafestube.schematics.version.V2SchematicVersion;
import eu.cafestube.schematics.version.V3SchematicVersion;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        return parseSchematic(file, NoOpDataConverter.INSTANCE);
    }

    public static Schematic parseSchematic(File file, boolean compressed) throws IOException {
        return parseSchematic(file, compressed, NoOpDataConverter.INSTANCE);
    }

    public static Schematic parseSchematic(InputStream in, boolean compressed) throws IOException {
        return parseSchematic(in, compressed, NoOpDataConverter.INSTANCE);
    }

    public static Schematic parseSchematic(InputStream inputStream) throws IOException {
        return parseSchematic(inputStream, NoOpDataConverter.INSTANCE);
    }

    public static Schematic parseSchematic(File file, DataConverter dataConverter) throws IOException {
        return parseSchematic(file, true, dataConverter);
    }

    public static Schematic parseSchematic(File file, boolean compressed, DataConverter dataConverter) throws IOException {
        return parseSchematic(new FileInputStream(file), compressed, dataConverter);
    }

    public static Schematic parseSchematic(InputStream in, boolean compressed, DataConverter dataConverter) throws IOException {
        try (in) {
            CompoundBinaryTag tag = BinaryTagIO.unlimitedReader().read(in, compressed ? BinaryTagIO.Compression.GZIP : BinaryTagIO.Compression.NONE);
            return parseSchematic(tag, dataConverter);
        }
    }

    public static Schematic parseSchematic(InputStream inputStream, DataConverter dataConverter) throws IOException {
        return parseSchematic(inputStream, true, dataConverter);
    }

    public static Schematic parseSchematic(CompoundBinaryTag compound) {
        return parseSchematic(compound, NoOpDataConverter.INSTANCE);
    }

    public static Schematic parseSchematic(@NotNull CompoundBinaryTag compound, @NotNull DataConverter dataConverter) {
        if(compound.get("Schematic") != null) {
            return parseSchematic(compound.getCompound("Schematic"), dataConverter);
        }

        int version = compound.getInt("Version");

        SchematicVersion schematicVersion = versions.get(version);
        if(schematicVersion == null) {
            throw new IllegalArgumentException("Unsupported schematic version: " + version);
        }

        return schematicVersion.deserialize(compound, dataConverter);
    }

}
