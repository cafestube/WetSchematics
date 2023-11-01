import eu.cafestube.schematics.SchematicIO;
import eu.cafestube.schematics.schematic.Schematic;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SchematicTest {

    @Test
    public void TestRead() throws IOException {
        //Just hope for no exceptions
        SchematicIO.parseSchematic(getClass().getResourceAsStream("schematictest.schem"));
    }

    @Test
    public void TestWriteReadSimilarityV2() throws IOException {

        Schematic schematic = SchematicIO.parseSchematic(getClass().getResourceAsStream("schematictest.schem"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        SchematicIO.writeSchematic(outputStream, 2, schematic);

        Schematic schematic1 = SchematicIO.parseSchematic(new ByteArrayInputStream(outputStream.toByteArray()));

        assert schematic.equals(schematic1);
    }

    @Test
    public void TestBlockEntities() throws IOException {

        Schematic schematic = SchematicIO.parseSchematic(getClass().getResourceAsStream("schematictest.schem"));

        assert schematic.blockEntities().size() == 1;
        assert schematic.blockEntities().values().stream().findFirst().get().id().equals("minecraft:chest");

    }


}
