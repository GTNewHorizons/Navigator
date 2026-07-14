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

public class UniversalLayerRenderer extends LayerRenderer {

    private Function<ILocationProvider, UniversalRenderStep<?>> stepCreator;
    private Function<ILocationProvider, MapMarker> markerCreator;
    private Function<ILocationProvider, Collection<?>> journeyMapV6OverlayCreator;
    private int renderPriority = 0;

    public UniversalLayerRenderer(@Nonnull LayerManager manager) {
        super(manager, SupportedMods.NONE);
    }

    public UniversalLayerRenderer withRenderStep(
        @Nonnull Function<ILocationProvider, UniversalRenderStep<?>> supplier) {
        this.stepCreator = supplier;
        return this;
    }

    public UniversalLayerRenderer withRenderPriority(int renderPriority) {
        this.renderPriority = renderPriority;
        return this;
    }

    public UniversalLayerRenderer withMapMarker(@Nonnull Function<ILocationProvider, MapMarker> creator) {
        markerCreator = creator;
        return this;
    }

    public boolean hasMapMarker() {
        return markerCreator != null;
    }

    public MapMarker createMapMarker(ILocationProvider location) {
        return markerCreator.apply(location);
    }

    /**
     * Adds native JourneyMap 6 displayables without making its optional API a runtime dependency of Navigator's API.
     */
    public UniversalLayerRenderer withJourneyMapV6Overlays(
        @Nonnull Function<ILocationProvider, Collection<?>> creator) {
        journeyMapV6OverlayCreator = creator;
        return this;
    }

    public boolean hasJourneyMapV6Overlays() {
        return markerCreator != null || journeyMapV6OverlayCreator != null;
    }

    public Collection<?> createJourneyMapV6Overlays(ILocationProvider location) {
        return journeyMapV6OverlayCreator.apply(location);
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
