package eu.cafestube.schematics.paper.transformer;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SchematicBlockTransformer {

    @Nullable
    BlockData transform(Block block, BlockData newData);

    static SchematicBlockTransformer combined(SchematicBlockTransformer... transformers) {
        return new CombinedSchematicBlockTransformer(List.of(transformers));
    }

}
