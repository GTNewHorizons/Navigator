# Navigator

Navigator is an api mod that allows for other mods to add integration for
* [JourneyMap](https://www.curseforge.com/minecraft/mc-mods/journeymap)
* [Xaeros World](https://www.curseforge.com/minecraft/mc-mods/xaeros-world-map) & [Minimap](https://www.curseforge.com/minecraft/mc-mods/xaeros-minimap)
* [VoxelMap](https://www.curseforge.com/minecraft/mc-mods/voxelmap) (Limited support)

## Dependencies
* [GTNHLib](https://www.curseforge.com/minecraft/mc-mods/gtnhlib)


## Documentation
### Example Layer Implementation
Navigator provides a debug layer which can also be used as a template for your own layers.
* [JourneyMap Dirty Chunk Layer](https://github.com/GTNewHorizons/Navigator/tree/master/src/main/java/com/gtnewhorizons/navigator/impl/journeymap)
* [Xaeros Dirty Chunk Layer ](https://github.com/GTNewHorizons/Navigator/tree/master/src/main/java/com/gtnewhorizons/navigator/impl/xaero)
* [The ButtonManger, LayerManager and the ILocationProvider](https://github.com/GTNewHorizons/Navigator/tree/master/src/main/java/com/gtnewhorizons/navigator/impl) are always shared between the two mods.

### Example JourneyMap Layer

Navigator provides an API for custom and interactive layers.
This API will keep all maps as optional mod at runtime and not crash you game if it is missing.

Start by extending [`ButtonManager`](https://github.com/GTNewHorizons/Navigator/blob/master/src/main/java/com/gtnewhorizons/navigator/api/model/buttons/ButtonManager.java) to create your own logical button.
```java
class MyButtonManager extends ButtonManager {

  public static final MyButtonManager INSTANCE = new MyButtonManager();

  @Override
  public ResourceLocation getIcon(SupportedMods mod, String theme) {
    return new ResourceLocation(YOUR_MOD_ID, "textures/icons/example.png");
  }

  @Override
  public String getButtonText() {
    return "My Button";
  }
}
```

If you start the game now, you will see a new button in the menu!

First, you will implement [`ILocationProvider`](https://github.com/GTNewHorizons/Navigator/blob/master/src/main/java/com/gtnewhorizons/navigator/api/model/locations/ILocationProvider.java). This class is a container and will provide all information required to display your item on screen. It won't do any rendering.

```java
class MyLocation implements ILocationProvider {

    public int getDimensionId() {
        return 0; // overworld
    }

    public int getBlockX() {
        return 0;
    }

    public int getBlockY() {
        return 65;
    }

    public int getBlockZ() {
        return 0;
    }

    public String getLabel() {
        return "Hello Minecraft";
    }
}
```

Next up, you'll extend [`LayerManager`](https://github.com/GTNewHorizons/Navigator/blob/master/src/main/java/com/gtnewhorizons/navigator/api/model/layers/LayerManager.java) and implement the abstract function to generate a cached list of your [`ILocationProvider`](https://github.com/GTNewHorizons/Navigator/blob/master/src/main/java/com/gtnewhorizons/navigator/api/model/locations/ILocationProvider.java) implementation. You should only add whatever items are visible to this list. There are more methods to override, that will assist you with that. Take a look!

```java
class MyLayerManager extends LayerManager {

    public static final MyLayerManager INSTANCE = new MyLayerManager();

    public MyLayerManager() {
        super(buttonManager);
    }

    protected List<? extends ILocationProvider> generateVisibleElements(int minBlockX, int minBlockZ, int maxBlockX, int maxBlockZ) {
        return Collections.singletonList(new MyLocation);
    }
}
```

Congratulations, you have finished the logical implementation of your custom map layer! Now it is time for the visual integration. This example is provided for JourneyMap, but you might as well take a look at the other possibilities. Since you already implemented the button as a first step, you will need to follow it up with an implementation of `JMRenderStep`. This class will receive an instance of `MyLocation` and perform the actual rendering.

```java
class MyDrawStep implements JMRenderStep {

    private final MyLocation myLocation;

    public MyDrawStep(MyLocation myLocation) {
        this.myLocation = myLocation;
    }

    @Override
    public void draw(double draggedPixelX, double draggedPixelY, GridRenderer gridRenderer, float drawScale, double fontScale, double rotation) {
        final double blockSize = Math.pow(2, gridRenderer.getZoom());
        final Point2D.Double blockAsPixel = gridRenderer.getBlockPixelInGrid(myLocation.getBlockX(), myLocation.getBlockZ());
        final Point2D.Double pixel = new Point2D.Double(blockAsPixel.getX() + draggedPixelX, blockAsPixel.getY() + draggedPixelY);

        DrawUtil.drawLabel(myLocation.getText(), pixel.getX(), pixel.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, 0, 180, 0x00FFFFFF, 255, fontScale, false, rotation);
    }
}
```

Continue with your own implementation of [`JMLayerRenderer`](https://github.com/GTNewHorizons/Navigator/blob/master/src/main/java/com/gtnewhorizons/navigator/api/journeymap/render/JMLayerRenderer.java). This class will cache all `Renderstep`s and provide it to JourneyMap whenever it is time to render.

```java
class MyLayerRenderer extends JMLayerRenderer {

  public static final MyLayerRenderer INSTANCE = new MyLayerRenderer();

    public MyLayerRenderer() {
        // You may skip MyLayerManager and use an existing ButtonManager like "OreVeinLayerManager.instance"
        // Your custom layer will toggle with whatever button you specify here
        super(MyLayerManager.instance);
    }

    @Override
    public List<? extends RenderStep> generateRenderSteps(List<? extends ILocationProvider> visibleElements) {
        final List<MyDrawStep> drawSteps = new ArrayList<>();
        visibleElements.stream()
                .map(element -> (MyLocation) element)
                .forEach(location -> drawSteps.add(new MyDrawStep(location)));
        return drawSteps;
    }

}
```

Finally, you will need to register your new layer with Navigator. This is done through the [`NavigatorAPI`](https://github.com/GTNewHorizons/Navigator/blob/master/src/main/java/com/gtnewhorizons/navigator/api/NavigatorApi.java) class during any one of the init phases.

```java
NavigatorApi.registerButtonManger(MyButtonManager.INSTANCE);
NavigatorApi.registerLayerManager(MyLayerManager.INSTANCE);
NavigatorApi.registerLayerRenderer(MyLayerRenderer.INSTANCE);
```
