# Navigator API guide

This guide documents Navigator's supported extension points. It focuses on the universal API, which keeps a consumer
independent of the installed map mod, and then covers the map-specific escape hatches.

## Mental model

A layer has four parts:

| Part | Responsibility |
| --- | --- |
| `ButtonManager` | One shared toggle and its map-specific icon. |
| `LayerManager` | Finds, caches, updates, filters, and removes logical locations. |
| `ILocationProvider` | Identifies one logical element and supplies its dimension and world position. |
| `LayerRenderer` + `RenderStep` | Converts cached locations into visible map output. |

Navigator either calls the manager for every chunk covered by the largest active fullscreen/minimap viewport or asks
for one collection covering that viewport. The manager caches returned locations per dimension. Each renderer then
caches one render step per location identity and receives only the currently visible set.

Register each manager once, on the client, during an initialization phase:

```java
NavigatorApi.registerLayerManager(MyLayerManager.INSTANCE);
```

## Minimal universal layer

### Location

`ILocationProvider` is intentionally small:

```java
public final class MyLocation implements ILocationProvider {

    private final int dimension;
    private final int blockX;
    private final int blockZ;

    public MyLocation(int dimension, int blockX, int blockZ) {
        this.dimension = dimension;
        this.blockX = blockX;
        this.blockZ = blockZ;
    }

    @Override
    public int getDimensionId() {
        return dimension;
    }

    @Override
    public double getBlockX() {
        return blockX;
    }

    @Override
    public double getBlockZ() {
        return blockZ;
    }
}
```

The default identity, `toLong()`, is the packed chunk position. This is correct for chunk-grid data and for one point
per chunk. Override it with a stable layer-unique key when using multiple points per chunk or another logical identity.

Coordinates may be fractional. Negative coordinates are converted with floor-compatible chunk semantics.

### Button

```java
public final class MyButtonManager extends ButtonManager {

    public static final MyButtonManager INSTANCE = new MyButtonManager();

    private MyButtonManager() {}

    @Override
    public ResourceLocation getIcon(SupportedMods mod, String theme) {
        return new ResourceLocation("mymod", "textures/gui/map_layer.png");
    }

    @Override
    public String getButtonText() {
        return "Show my layer";
    }
}
```

Activating a Navigator button deactivates other distinct Navigator buttons. `setOnToggle` is for consumer-side work;
the manager installs its own notification callback automatically.

### Render step

```java
public final class MyRenderStep extends UniversalRenderStep<MyLocation> {

    public MyRenderStep(MyLocation location) {
        super(location);
        setSize(16); // world blocks
    }

    @Override
    public void draw(double x, double y, float drawScale, double zoom) {
        DrawUtils.drawHollowRect(x, y, getAdjustedWidth(), getAdjustedHeight(), 0x00FF00, 180);
    }
}
```

`x` and `y` are map-space coordinates for the location. Use `getAdjustedWidth()` / `getAdjustedHeight()` when the
size should remain consistent across integrations. `getZoomStep()` provides Navigator's normalized, discrete zoom
step for visibility decisions.

### Manager and renderer

```java
public final class MyLayerManager extends LayerManager {

    public static final MyLayerManager INSTANCE = new MyLayerManager();

    private MyLayerManager() {
        super(MyButtonManager.INSTANCE);
    }

    @Override
    protected LayerRenderer addLayerRenderer(LayerManager manager, SupportedMods mod) {
        return new UniversalLayerRenderer(manager)
            .withRenderStep(location -> new MyRenderStep((MyLocation) location));
    }

    @Override
    protected ILocationProvider generateLocation(int chunkX, int chunkZ, int dimension) {
        MyData data = MyDataStore.find(chunkX, chunkZ, dimension);
        return data == null ? null : new MyLocation(dimension, data.blockX, data.blockZ);
    }
}
```

`addLayerRenderer` is called once for each installed and enabled map integration. Return `null` for an integration the
layer does not support. A universal renderer can be returned for every integration.

For multiple independently interactive points in one chunk, override the collection hook instead of
`generateLocation`:

```java
@Override
protected Collection<? extends ILocationProvider> generateVisibleLocations(
        int minBlockX, int minBlockZ, int maxBlockX, int maxBlockZ, int dimension) {
    return MyDataStore.findAll(minBlockX, minBlockZ, maxBlockX, maxBlockZ, dimension);
}
```

Return an empty collection when the viewport contains nothing. The default `null` return keeps chunk discovery active.
Every returned location must override `toLong()` when more than one element may occupy a chunk. Navigator retains the
first object for each identity; update mutable cached fields through `updateElement` or invalidate the location when
its source data changes.

## Cache and refresh lifecycle

### Discovery

- `generateLocation(chunkX, chunkZ, dimension)` is called when a chunk key is absent from the current dimension cache.
- Returning `null` means there is no element for that chunk.
- `generateVisibleLocations(...)` replaces chunk lookup for collection-backed layers and may return multiple stable
  identities from one chunk. Returning `null` selects chunk lookup; an empty collection means no visible elements.
- `getElementSize()` expands viewport discovery by that many chunks on every side. Override it for elements whose
  visual bounds extend beyond their identifying chunk.
- `onUpdatePre` and `onUpdatePost` receive inclusive chunk bounds around a recache. They are appropriate for batched
  network requests and validation.
- `updateElement(location)` runs for every cached visible location during recache. Mutate the existing object there;
  do not replace it merely to update fields.

### Invalidating data

Call `forceRefresh()` after data changes outside `updateElement`. It schedules a redraw and increments the refresh
version used by the JourneyMap 6 native-overlay synchronizer.

Use the narrowest cache operation that matches the change:

| Operation | Effect |
| --- | --- |
| `removeLocation(...)` | Queues one location and its render step for removal. |
| `clearCurrentCache()` | Clears the current dimension on the next recache. |
| `clearFullCache()` | Clears every dimension and every renderer cache. |
| `addExtraLocation(location)` | Inserts an already-created location into the current dimension cache. |

`getVisibleLocations()` is the current viewport set. `getCachedLocations()` includes locations outside the viewport in
the current dimension. Treat both collections as manager-owned; mutating them directly couples code to cache internals.

When a layer is disabled, the default `onLayerToggled(false)` clears all caches. Override it only when consumer state
also needs cleanup, and call `super`.

## Search

Enable Navigator's map search field in the manager constructor and respond to normalized text changes:

```java
private MyLayerManager() {
    super(MyButtonManager.INSTANCE);
    setHasSearchField(true);
}

@Override
public void onSearch(@NotNull String searchString) {
    for (MyLocation location : locations) {
        location.setVisible(searchString.isEmpty() || location.getName().toLowerCase().contains(searchString));
    }
    forceRefresh();
}
```

Search policy belongs to the consumer because Navigator does not know which fields are meaningful. The JourneyMap 6
search widget currently has a known focus conflict when switching directly to Minecraft chat; see the compatibility
roadmap.

## Interaction without waypoints

Interaction and waypoint capability are separate. A clickable element that is not a waypoint implements only
`ILocationProvider`, and its step extends `UniversalLocationInteractableStep`:

```java
public final class ClaimRenderStep extends UniversalLocationInteractableStep<ClaimLocation> {

    public ClaimRenderStep(ClaimLocation location) {
        super(location);
    }

    @Override
    public void draw(double x, double y, float drawScale, double zoom) {
        DrawUtils.drawRect(x, y, getAdjustedWidth(), getAdjustedHeight(), location.getColor(), 135);
    }

    @Override
    public void getTooltip(List<String> tooltip) {
        tooltip.add(location.getOwnerName());
    }

    @Override
    public void onActionKeyPressed() {
        location.unclaim();
    }
}
```

Configure it with `UniversalInteractableRenderer`:

```java
UniversalInteractableRenderer renderer = new UniversalInteractableRenderer(manager)
    .withClickAction(click -> {
        if (!click.isDoubleClick()) return false;

        LocationInteractableStep step = click.getLocationRenderStep();
        if (step != null && step.getLocation() instanceof ClaimLocation claim) {
            claim.toggleLoaded();
            return true;
        }

        claimChunk(click.getChunkX(), click.getChunkZ());
        return true;
    });
renderer.withRenderStep(location -> new ClaimRenderStep((ClaimLocation) location));
return renderer;
```

Return `true` only when the callback consumed the event. Consumed clicks and action keys trigger a manager refresh.
`ClickPos#getLocationRenderStep()` is the location-neutral accessor. `getRenderStep()` is retained for binary and
source compatibility with older waypoint-only consumers and returns `null` for a plain-location step.

Override `isMouseOver` when the default rectangular bounds do not match the shape. The bounds are derived from the
step's current map-space position, size, and offset.

## Waypoint-capable interaction

Use `IWaypointAndLocationProvider` only when the element should participate in Navigator's active waypoint:

```java
public final class OreLocation implements IWaypointAndLocationProvider {

    private boolean active;

    @Override
    public Waypoint toWaypoint() {
        return new Waypoint(blockX, blockY, blockZ, dimension, name, color);
    }

    @Override
    public boolean isActiveAsWaypoint() {
        return active;
    }

    @Override
    public void onWaypointUpdated(Waypoint waypoint) {
        active = waypoint.blockX == blockX && waypoint.blockZ == blockZ && waypoint.dimensionId == dimension;
    }

    @Override
    public void onWaypointCleared() {
        active = false;
    }
}
```

Its render step extends `UniversalInteractableStep<OreLocation>`. `UniversalInteractableRenderer` then supplies the
default behavior: double-clicking the hovered step sets or clears the active waypoint. A custom click callback runs
first and may consume the click instead.

An `InteractableLayerManager` may return map-specific `WaypointManager` instances from `addWaypointManager`. It only
sends waypoint callbacks to visible locations that actually implement `IWaypointAndLocationProvider`; plain clickable
locations are ignored safely.

Do not implement the waypoint interface merely to reuse double-click handling, and never return `null` from
`toWaypoint()` as a substitute for a domain action. Use the location-neutral interaction path above.

## JourneyMap 6 point markers

`MapMarker` is Navigator's map-neutral description of a native JourneyMap 6 `MarkerOverlay`. It supports a buffered
image or Minecraft `ResourceLocation`, independent texture/display sizes, label styling, tooltip text, a label zoom
threshold, and fullscreen-only labels.

```java
UniversalInteractableRenderer renderer = new UniversalInteractableRenderer(manager);
renderer.withRenderStep(location -> new OreRenderStep((OreLocation) location));
renderer.withMapMarker(location -> new MapMarker(ORE_ICON, 16, 16)
    .setDisplaySize(12, 12)
    .setLabel(((OreLocation) location).getName())
    .setLabelMinZoom(3)
    .setLabelOnMinimap(false));
return renderer;
```

Important lifecycle rules:

- `withRenderStep(...)` is still required. Visible render steps are the source of marker locations and interaction.
- Navigator creates the `MapImage`, centers its anchors, assigns the location dimension, enables fullscreen and
  minimap contexts, and owns show/remove/refresh lifecycle.
- If `MapMarker#setTooltip` is not called, Navigator obtains tooltip lines from an interactable render step.
- Marker clicks and action keys are forwarded to `UniversalInteractableRenderer`.
- `setLabelMinZoom` uses Navigator's normalized zoom steps, not JourneyMap's internal zoom value.
- `setLabelOnMinimap(false)` hides only the text; the icon remains visible in both contexts.

On JourneyMap 5 and Xaero, the universal render step remains the visual implementation. `MapMarker` itself is ignored.

## Raw JourneyMap 6 overlays

Use `withJourneyMapV6Overlays` for native areas or images that `MapMarker` cannot describe:

```java
if (Util.isJourneyMapV6Installed()) {
    renderer.withJourneyMapV6Overlays(location -> createClaimPolygons((ClaimLocation) location), true);
}
```

The factory returns JourneyMap 6 `Displayable` objects as `Collection<?>`, keeping JM6 types out of Navigator's common
API signature. The consumer remains responsible for each displayable's geometry, dimension, active UI contexts,
display order, image properties, and stable identifiers.

Navigator owns display lifecycle and refresh. For JourneyMap `Overlay` instances, Navigator also installs its
interaction listener when all of the following are true:

- the renderer is a `UniversalInteractableRenderer`;
- the render step is a `UniversalLocationInteractableStep`;
- the overlay does not already have a listener.

If the consumer supplies a listener, Navigator leaves it untouched. Set `replaceRenderSteps` to `true` when native
overlays fully replace the universal step on the JourneyMap 6 fullscreen map. Minimap rendering is native-overlay only;
JourneyMap 5 and Xaero continue using the universal render step.

Native overlays are retained while their location remains visible. `forceRefresh()` recreates overlays for visible
locations and removes overlays that left the refreshed visible set. Avoid calling it every tick unless domain data
actually changed.

Because this path uses JourneyMap 6 classes in consumer code, isolate the factory behind a JM6 runtime check and keep
those classes out of code that must load under JourneyMap 5.

## Rendering details

`UniversalRenderStep` exposes:

| Method | Meaning |
| --- | --- |
| `preRender(...)` | Update size, offset, scale, or other derived state before each draw. |
| `setSize(...)` | Logical width/height in world blocks. Defaults to one chunk. |
| `setOffset(...)` | Map-space offset added to the calculated position. |
| `getAdjustedWidth/Height()` | Size converted for the active integration. |
| `getFontScale()` | Map-provided label scale. |
| `setMinScale(...)` | Xaero-only lower scaling bound. |
| `isMinimap()` | Whether no GUI screen is currently open. |
| `getZoomStep()` | Normalized zoom step suitable for visibility thresholds. |

Rendering runs inside a pushed OpenGL matrix with Navigator's basic blend state configured. A render step must still
restore any additional GL state it changes.

Render priorities are ascending. Use `withRenderPriority` on a universal renderer or override `getRenderPriority` on a
custom renderer.

## Map-specific APIs

The preferred path is universal. The following APIs remain for compatibility or behavior that cannot be expressed by
a universal step:

- `JMRenderStep`, `JMInteractableStep`, `JMLayerRenderer`, and `JMInteractableLayerRenderer` target the JourneyMap 5
  draw-step API. They directly reference JourneyMap client classes.
- `XaeroRenderStep`, `XaeroInteractableStep`, `XaeroLayerRenderer`, and `XaeroInteractableLayerRenderer` target Xaero's
  renderer API directly.
- `JMWaypointManager` selects the JourneyMap 5 implementation or delegates to JourneyMap 6 internally.
- `XaeroWaypointManager` publishes a Navigator-owned custom waypoint and disables it outside its dimension.
- `VoxelMapWaypointManager` only provides waypoint creation helpers; VoxelMap has no Navigator layer renderer.

Do not load a map-specific implementation when its map mod is absent. Select it in `addLayerRenderer` or
`addWaypointManager` using the supplied `SupportedMods` value.

## JourneyMap version detection

Use `JourneyMapVersion.get()` or the convenience methods in `Util`:

```java
if (Util.isJourneyMapV6Installed()) {
    // Configure JM6-native overlays.
}
```

Detection is cached for the process lifetime. JM6 is identified by the public v2 client API class. Avoid parsing the
mod version string.

## Compatibility checklist

Before shipping a consumer change, test:

- JourneyMap 5 fullscreen rendering and interaction;
- JourneyMap 6 fullscreen and minimap contexts;
- Xaero World Map and Minimap rendering;
- layer toggle off/on and returning to the menu;
- dimension changes and negative coordinates;
- tooltip enter/leave behavior and empty-space clicks;
- search refresh, if enabled;
- action key and double-click behavior;
- compatibility with existing consumer jars when changing public fields or method descriptors.

Navigator intentionally retains some legacy API shapes because 1.7.10 integrations and third-party mixins may access
them by exact JVM descriptor. Prefer additive APIs over changing existing public/protected types.
