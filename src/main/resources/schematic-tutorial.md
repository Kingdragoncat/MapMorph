# MapMorph Schematic & Configuration Tutorial

## Introduction

MapMorph allows you to manage and load WorldEdit schematics for different maps. This guide will help you understand how to:
- Create and manage schematics
- Configure maps with schematics
- Set up automatic map rotation
- Configure essential plugin settings

## Schematic Management

### Creating Schematics

1. **Build your map** in a world
2. **Select the region** with WorldEdit
   ```
   //wand               (Get the selection wand)
   //pos1               (Set first corner)
   //pos2               (Set second corner)
   ```
3. **Save the schematic**
   ```
   //schem save mymap   (Save selection as "mymap")
   ```
4. **Place your schematic** in the right location:
   - Default: `plugins/WorldEdit/schematics/`
   - FAWE: `plugins/FastAsyncWorldEdit/schematics/`

### Map Configuration with Schematics

Each map in MapMorph can be associated with a schematic:

1. **Create a map in MapMorph**
   ```
   /mapmorph create mymap
   ```

2. **Associate the schematic** by editing `plugins/MapMorph/config.yml`:
   ```yaml
   maps:
     mymap:
       schematic: "mymap"       # Name of the schematic file without extension
       paste-location:          # Where to paste the schematic
         world: "world"
         x: 0
         y: 64
         z: 0
       paste-options:           # Optional paste configuration
         air: true              # Paste air blocks
         biomes: false          # Update biomes
         entities: true         # Include entities 
   ```

3. **Save the configuration**
   ```
   /mapmorph save
   ```

## Using FAWE for Better Performance

FastAsyncWorldEdit (FAWE) is highly recommended for large schematics:

1. **Install FAWE** on your server
2. **Configure MapMorph** to use FAWE in `config.yml`:
   ```yaml
   general:
     use-fawe: true   # Enable FAWE integration if available
   ```

3. **Paste options specifically for FAWE**:
   ```yaml
   maps:
     mymap:
       # ...existing configuration...
       paste-options:
         air: true
         biomes: false
         entities: true
         fast-mode: true      # FAWE-specific for faster operations
         reuse-session: true  # FAWE-specific for better performance
   ```

## Advanced Schematic Configuration

### Rotations and Transformations

You can configure how schematics are pasted:

```yaml
maps:
  mymap:
    # ...existing configuration...
    paste-options:
      rotation: 90            # Rotate by 0, 90, 180, or 270 degrees
      flip-direction: "none"  # Can be "none", "x", "y", or "z"
      align-to-ground: true   # Adjust Y position to ground level
```

### Multi-Schematic Maps

For complex maps, you can use multiple schematics:

```yaml
maps:
  complex_map:
    schematics:
      main:
        file: "main_area"
        location:
          world: "world"
          x: 0
          y: 64
          z: 0
      addon:
        file: "addon_structure"
        location:
          world: "world"
          x: 100
          y: 64
          z: 100
```

## Map Rotation Configuration

Configure automatic map rotation:

```yaml
rotation:
  mode: "sequential"  # Options: sequential, random, voting
  interval: 30        # Minutes between auto-rotations (0 to disable)
  maps:               # Maps to include in rotation
    - "map1"
    - "map2"
    - "map3"
  exclude:            # Maps to exclude from rotation
    - "admin_map"
    - "test_map"
```

## WorldGuard Integration

Link WorldGuard regions to maps for permissions and protection:

```yaml
maps:
  lobby:
    # ...existing configuration...
    regions:
      - "lobby_main"
      - "lobby_spawn"
    region-options:
      create-regions: true      # Create regions if they don't exist
      copy-flags: true          # Copy region flags when creating
      default-flags:
        pvp: "deny"             # Set default flags for created regions
        build: "deny"
```

## Backup and Restore

Enable automatic backups before pasting schematics:

```yaml
general:
  backups:
    enabled: true
    max-backups: 5           # How many backups to keep per map
    include-entities: true   # Include entities in backups
    backup-before-load: true # Create backup before loading a new map
```

## Performance Tips

1. **Use FAWE** for large schematics
2. **Optimize paste options**:
   ```yaml
   paste-options:
     entities: false        # Disable if not needed
     fast-mode: true        # Use FAWE fast mode
     lighting-updates: false # Disable lighting updates during paste
   ```

3. **Configure chunk loading**:
   ```yaml
   general:
     chunk-loading:
       radius: 8            # Radius of chunks to load around paste location
       eager-loading: true  # Pre-load chunks before pasting
   ```

## Troubleshooting

### Common Issues

1. **Schematic fails to load**
   - Verify the schematic exists in the correct directory
   - Check file permissions
   - Ensure the schematic name in config matches the file name (without extension)

2. **Paste operation is slow**
   - Enable FAWE integration
   - Reduce the size of your schematic
   - Use paste options to optimize (fast-mode, disable entities)

3. **WorldGuard regions not working**
   - Verify WorldGuard is properly installed
   - Check region names in configuration match those in WorldGuard
   - Ensure proper permissions are set

## Example Complete Configuration

```yaml
general:
  prefix: "&8[&bMapMorph&8] &r"
  use-fawe: true
  debug-mode: false
  backups:
    enabled: true
    max-backups: 3

rotation:
  mode: "sequential"
  interval: 45
  maps:
    - "lobby"
    - "arena"
    - "parkour"

maps:
  lobby:
    schematic: "lobby_main"
    paste-location:
      world: "world"
      x: 0
      y: 64
      z: 0
    paste-options:
      air: true
      entities: true
      fast-mode: true
    regions:
      - "lobby_main"
      - "lobby_spawn"
    spawns:
      1:
        world: "world"
        x: 10
        y: 65
        z: 10
        yaw: 90
        pitch: 0

  arena:
    schematic: "battle_arena"
    paste-location:
      world: "world"
      x: 500
      y: 64
      z: 500
    paste-options:
      rotation: 90
      entities: false
    regions:
      - "arena_battle"
      - "arena_spawn"
    spawns:
      1:
        world: "world"
        x: 510
        y: 65
        z: 510
        yaw: 0
        pitch: 0
```
