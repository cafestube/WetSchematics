import eu.cafestube.schematics.SchematicIO;
import eu.cafestube.schematics.schematic.Schematic;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class SchematicReadTest {

    @Test
    public void TestRead() throws IOException {
        Schematic schematic = SchematicIO.parseSchematic(getClass().getResourceAsStream("schematictest.schem"));
    }

}
