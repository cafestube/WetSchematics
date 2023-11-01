# WetSchematics

Sponge Schematic Parser. Visit https://github.com/SpongePowered/Schematic-Specification/ for more information about the format.
This parser can read schematics and write schematics in all versions of the format. It can also be used to convert between versions.

## Paper Support
With the paper module the parser can be used to place schematics in paper(possibly spigot as well) worlds. 
Entities and Tile Entities are only supported on 1.20.1 and 1.20.2 as of right now.

This can be a great alternative if you don't want to require worldedit for your plugin. 
However, it is not as fast as WorldEdit, and it is not recommended to use this for large schematics.