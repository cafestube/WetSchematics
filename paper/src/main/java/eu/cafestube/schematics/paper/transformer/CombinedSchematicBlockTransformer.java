package eu.cafestube.schematics.paper.transformer;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CombinedSchematicBlockTransformer implements SchematicBlockTransformer {

    private final List<SchematicBlockTransformer> blockTransformer;

    public CombinedSchematicBlockTransformer(List<SchematicBlockTransformer> blockTransformer) {
        this.blockTransformer = blockTransformer;
    }

    @Override
    public @Nullable BlockData transform(Block block, BlockData newData) {
        for (SchematicBlockTransformer transformer : this.blockTransformer) {
            newData = transformer.transform(block, newData);
            if (newData == null) {
                return null;
            }
        }
        return newData;
    }

}
