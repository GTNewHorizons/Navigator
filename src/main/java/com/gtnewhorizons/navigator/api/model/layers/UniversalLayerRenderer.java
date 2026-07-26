package com.gtnewhorizons.navigator.api.model.layers;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.model.markers.MapMarker;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;
import com.gtnewhorizons.navigator.api.model.steps.UniversalRenderStep;

/**
 * Map-neutral renderer configured with functions instead of one renderer subclass per map mod.
 * <p>
 * A universal render step is the lifecycle anchor for all output, including JourneyMap 6 native markers and raw
 * displayables. Configure {@link #withRenderStep(Function)} even when native output replaces fullscreen drawing.
 */
public class UniversalLayerRenderer extends LayerRenderer {

    private Function<ILocationProvider, UniversalRenderStep<?>> stepCreator;
    private Function<ILocationProvider, MapMarker> markerCreator;
    private Function<ILocationProvider, Collection<?>> journeyMapV6OverlayCreator;
    private boolean journeyMapV6OverlaysReplaceRenderSteps;
    private int renderPriority = 0;

    /** @param manager owning layer manager */
    public UniversalLayerRenderer(@Nonnull LayerManager manager) {
        super(manager, SupportedMods.NONE);
    }

    /**
     * Configures the render-step factory used for every cached visible location.
     *
     * @param supplier factory returning a map-neutral step
     * @return this renderer
     */
    public UniversalLayerRenderer withRenderStep(
        @Nonnull Function<ILocationProvider, UniversalRenderStep<?>> supplier) {
        this.stepCreator = supplier;
        return this;
    }

    /**
     * @param renderPriority ascending render/display order relative to other Navigator layers
     * @return this renderer
     */
    public UniversalLayerRenderer withRenderPriority(int renderPriority) {
        this.renderPriority = renderPriority;
        return this;
    }

    /**
     * Configures map-neutral JourneyMap 6 point markers.
     * <p>
     * Navigator owns marker context, dimension, interaction, and lifecycle. Other map integrations continue drawing
     * the configured universal render step.
     *
     * @param creator marker factory; returning {@code null} hides the marker for that location
     * @return this renderer
     */
    public UniversalLayerRenderer withMapMarker(@Nonnull Function<ILocationProvider, MapMarker> creator) {
        markerCreator = creator;
        return this;
    }

    /** @return whether a map-marker factory is configured */
    public boolean hasMapMarker() {
        return markerCreator != null;
    }

    /**
     * @param location source location
     * @return marker description, or {@code null}
     */
    public @Nullable MapMarker createMapMarker(ILocationProvider location) {
        return markerCreator == null ? null : markerCreator.apply(location);
    }

    /**
     * Adds native JourneyMap 6 displayables without making its optional API a runtime dependency of Navigator's
     * common API signature.
     * <p>
     * The consumer must configure each displayable's dimension, UI contexts, geometry, and display properties.
     */
    public UniversalLayerRenderer withJourneyMapV6Overlays(
        @Nonnull Function<ILocationProvider, Collection<?>> creator) {
        return withJourneyMapV6Overlays(creator, false);
    }

    /**
     * Adds native JourneyMap 6 displayables and optionally replaces universal fullscreen drawing on JourneyMap 6.
     *
     * @param creator            factory returning JourneyMap 6 {@code Displayable} instances as an untyped collection
     * @param replaceRenderSteps whether native overlays fully replace universal fullscreen steps on JourneyMap 6
     * @return this renderer
     */
    public UniversalLayerRenderer withJourneyMapV6Overlays(@Nonnull Function<ILocationProvider, Collection<?>> creator,
        boolean replaceRenderSteps) {
        journeyMapV6OverlayCreator = creator;
        journeyMapV6OverlaysReplaceRenderSteps = replaceRenderSteps;
        return this;
    }

    /** @return whether either a marker or raw JourneyMap 6 displayable factory is configured */
    public boolean hasJourneyMapV6Overlays() {
        return markerCreator != null || journeyMapV6OverlayCreator != null;
    }

    /**
     * @param location source location
     * @return raw JourneyMap 6 displayable candidates, or {@code null}
     */
    public @Nullable Collection<?> createJourneyMapV6Overlays(ILocationProvider location) {
        return journeyMapV6OverlayCreator == null ? null : journeyMapV6OverlayCreator.apply(location);
    }

    /** @return whether native JM6 displayables replace universal fullscreen rendering */
    public boolean journeyMapV6OverlaysReplaceRenderSteps() {
        return journeyMapV6OverlaysReplaceRenderSteps;
    }

    @Nullable
    @Override
    protected RenderStep generateRenderStep(ILocationProvider location) {
        if (stepCreator != null) {
            return stepCreator.apply(location);
        }

        return null;
    }

    @Override
    public int getRenderPriority() {
        return renderPriority;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<UniversalRenderStep<?>> getRenderSteps() {
        return (List<UniversalRenderStep<?>>) super.getRenderSteps();
    }
}
