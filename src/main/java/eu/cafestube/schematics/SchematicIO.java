package eu.cafestube.schematics;

import eu.cafestube.schematics.schematic.Schematic;
import eu.cafestube.schematics.version.SchematicVersion;
import eu.cafestube.schematics.version.V1SchematicVersion;
import eu.cafestube.schematics.version.V2SchematicVersion;
import eu.cafestube.schematics.version.V3SchematicVersion;
import me.nullicorn.nedit.NBTReader;
import me.nullicorn.nedit.type.NBTCompound;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class SchematicIO {

    private static final Map<Integer, SchematicVersion> versions = Map.of(
            1, new V1SchematicVersion(),
            2, new V2SchematicVersion(),
            3, new V3SchematicVersion()
    );

    public static Schematic parseSchematic(File file) throws IOException {
        return parseSchematic(new FileInputStream(file));
    }

    public static Schematic parseSchematic(InputStream inputStream) throws IOException {
        NBTCompound compound = NBTReader.read(inputStream);
        return parseSchematic(compound);
    }

    public static Schematic parseSchematic(NBTCompound compound) {
        int version = compound.getInt("Version", 0);

        SchematicVersion schematicVersion = versions.get(version);
        if(schematicVersion == null) {
            throw new IllegalArgumentException("Unsupported schematic version: " + version);
        }

        return schematicVersion.deserialize(compound);
    }

}
