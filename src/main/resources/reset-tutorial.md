# MapMorph Map Reset Tutorial

This tutorial explains how to set up a map that can be reset to its original state using MapMorph.

## Basic Concepts

MapMorph allows you to reset maps in several ways:

1. **WorldEdit Schematics**: Store the original state as a schematic and paste it when needed
2. **World Backup/Restore**: Keep a backup copy of the world folder and restore it
3. **Region-based Reset**: Only reset specific WorldGuard regions

## Method 1: Using WorldEdit Schematics (Recommended)

This method uses WorldEdit schematics to store and restore map states.

### Step 1: Build Your Map

First, build your map exactly as you want it in its "reset" state.

### Step 2: Create a Schematic

1. Select the entire area of your map using WorldEdit
   ```
   //pos1 [corner 1]
   //pos2 [corner 2]
   ```

2. Save the selection as a schematic
   ```
   //schem save [map_name]
   ```

3. The schematic will be saved to the WorldEdit schematics folder

### Step 3: Configure the Map in MapMorph

1. Create a new map in MapMorph:
   ```
   /mapmorph create [map_name]
   ```

2. Add the schematic to the map:
   ```
   /mapmorph addschematic [map_name] [schematic_name] [world] [x] [y] [z]
   ```
   Where x, y, z are the coordinates where the schematic should be pasted.

3. Save your configuration:
   ```
   /mapmorph save
   ```

### Step 4: Reset the Map

To reset the map, use:
```
/mapmorph reset [map_name]
```

This will paste the schematic at the configured location, effectively resetting the map.

## Method 2: World Backup/Restore

For larger maps or complete world resets.

### Step 1: Create a Backup

1. Stop your server
2. Make a copy of your world folder
3. Restart your server

### Step 2: Configure MapMorph

1. Create a map entry:
   ```
   /mapmorph create [map_name]
   ```

2. Configure the backup location in maps.yml:
   ```yaml
   [map_name]:
     backup:
       world: "world"
       source: "path/to/backup"
       method: "copy"  # Can be "copy" or "regenerate"
   ```

3. Save your configuration

### Step 3: Reset the Map

To reset the map:
```
/mapmorph reset [map_name]
```

This will restore the world from the backup.

## Method 3: Region-based Reset (With WorldGuard)

For resetting only specific regions within a world.

### Step 1: Define WorldGuard Regions

1. Create WorldGuard regions for the areas you want to reset:
   ```
   //region define [region_name]
   ```

2. Associate these regions with your map:
   ```
   /mapmorph addregion [map_name] [region_name]
   ```

### Step 2: Create Schematics for Each Region

1. Select your region:
   ```
   //sel
   ```

2. Save as a schematic:
   ```
   //schem save [region_name]
   ```

### Step 3: Configure Region Resets

In your maps.yml, configure each region with its schematic:

```yaml
[map_name]:
  regions:
    - [region_name1]
    - [region_name2]
  schematics:
    [region_name1]:
      file: "[region_name1].schem"
      world: "world"
      x: 0
      y: 64
      z: 0
    [region_name2]:
      file: "[region_name2].schem"
      world: "world"
      x: 100
      y: 64
      z: 100
```

### Step 4: Reset the Map

To reset specific regions:
```
/mapmorph resetregion [map_name] [region_name]
```

Or reset all regions:
```
/mapmorph reset [map_name]
```

## Best Practices

1. **Test your resets** in a development environment before using in production
2. **Schedule regular resets** for multiplayer maps to prevent griefing
3. **Create multiple spawn points** so players don't spawn on top of each other
4. **Use WorldGuard flags** to protect your maps from modification

## Advanced Tips

- **Automated Resets**: Use a scheduler plugin to automatically reset maps
- **Game-Based Resets**: Trigger resets at the end of each game round
- **Player Limits**: Set player limits per map to prevent overcrowding
- **Reset Notifications**: Notify players before a reset occurs

## Troubleshooting

- **Missing Blocks**: Your schematic selection may be incomplete
- **Entities Not Resetting**: Ensure entity pasting is enabled
- **Performance Issues**: Large schematics may cause lag - consider optimizing
- **Reset Failures**: Check console for error messages

For more help, join our Discord server or create an issue on GitHub.
