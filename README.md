# Navigator

Navigator is an api mod that allows for other mods to add integration for
* [JourneyMap](https://www.curseforge.com/minecraft/mc-mods/journeymap)
* [Xaeros World](https://www.curseforge.com/minecraft/mc-mods/xaeros-world-map) & [Minimap](https://www.curseforge.com/minecraft/mc-mods/xaeros-minimap)
* [VoxelMap](https://www.curseforge.com/minecraft/mc-mods/voxelmap) (Limited support)

Map mods remain optional at runtime. A consumer owns its data and domain actions; Navigator owns viewport caching,
layer buttons, rendering dispatch, search dispatch, and map-specific integration.

## Documentation

- [API guide](docs/API.md) - architecture, complete examples, caching, interaction, waypoints, JourneyMap 6 native
  overlays, search, and compatibility guidance
- [`NavigatorApi`](src/main/java/com/gtnewhorizons/navigator/api/NavigatorApi.java) - registration entry point

The preferred implementation path is map-neutral:

1. Implement `ILocationProvider` for the data shown on the map.
2. Extend `ButtonManager` and `LayerManager` (or `InteractableLayerManager`).
3. Extend `UniversalRenderStep` for rendering.
4. Use `UniversalLocationInteractableStep` for non-waypoint interaction, or `UniversalInteractableStep` when
   double-click should toggle a waypoint.
5. Return a `UniversalLayerRenderer` / `UniversalInteractableRenderer` from the manager and register it client-side.

```java
NavigatorApi.registerLayerManager(MyLayerManager.INSTANCE);
```

The JourneyMap-specific renderer and render-step APIs are deprecated because they support only JourneyMap 5. Use the
universal APIs for JourneyMap 5, JourneyMap 6, and Xaero; map-specific APIs remain escape hatches only where the
universal model cannot express required behavior.

## Dependency

Navigator depends on [GTNHLib](https://github.com/GTNewHorizons/GTNHLib). Consumer mods should compile against
Navigator while keeping supported map mods optional unless they deliberately use a map-specific API.
