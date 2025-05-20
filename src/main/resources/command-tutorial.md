# MapMorph Command Usage Tutorial

## Basic Commands

### Help
Shows all available commands:
```
/mapmorph help
```

### List Maps
Lists all available maps:
```
/mapmorph list
```

### Map Information
View information about a specific map:
```
/mapmorph info <map_name>
```

## Map Management

### Create a Map
Create a new map:
```
/mapmorph create <map_name>
```

### Delete a Map
Delete an existing map:
```
/mapmorph delete <map_name>
```

### Load a Map
Load a specific map:
```
/mapmorph load <map_name>
```

### Rollback to Previous Map
Return to the previously active map:
```
/mapmorph rollback
```

## Spawn Management

### Add a Spawn Point
Add a spawn point at your current location:
```
/mapmorph addspawn <map_name>
```

### Remove a Spawn Point
Remove a spawn point by index:
```
/mapmorph removespawn <map_name> <index>
```

## Region Management

### Add a WorldGuard Region
Associate a WorldGuard region with a map:
```
/mapmorph addregion <map_name> <region_id>
```

### Remove a WorldGuard Region
Remove a WorldGuard region association:
```
/mapmorph removeregion <map_name> <region_id>
```

## Miscellaneous Commands

### Teleport
Teleport a player to a map's spawn point:
```
/mapmorph teleport <map_name> [player_name]
```
Shorthand: `/mapmorph tp <map_name> [player_name]`

### Save Changes
Save the current map configuration:
```
/mapmorph save
```

### Reload Configuration
Reload the plugin configuration:
```
/mapmorph reload
```

## Permissions

- `mapmorph.admin` - Access to all MapMorph commands
- `mapmorph.create` - Permission to create maps
- `mapmorph.delete` - Permission to delete maps
- `mapmorph.load` - Permission to load maps
- `mapmorph.addspawn` - Permission to add spawn points
- `mapmorph.teleport` - Permission to teleport players
- `mapmorph.reload` - Permission to reload configuration

## Examples

### Creating and Setting Up a New Map

1. Create the map:
   ```
   /mapmorph create lobby
   ```

2. Add spawn points:
   ```
   /mapmorph addspawn lobby
   ```

3. Add WorldGuard regions:
   ```
   /mapmorph addregion lobby lobby_spawn
   /mapmorph addregion lobby lobby_main
   ```

4. Save your changes:
   ```
   /mapmorph save
   ```

5. Load the map:
   ```
   /mapmorph load lobby
   ```

### Managing Players

- Teleport yourself to a map:
  ```
  /mapmorph tp arena
  ```

- Teleport another player:
  ```
  /mapmorph teleport lobby PlayerName
  ```

## Advanced Usage

### Map Rotation

MapMorph supports different map rotation modes:
- Sequential
- Random
- Voting

Configure the rotation mode in the config.yml file under the `rotation.mode` setting.

### Event Hooks

Maps can trigger events that other plugins can listen to. The main event is `MapChangeEvent` which is fired whenever a map is loaded or changed.
