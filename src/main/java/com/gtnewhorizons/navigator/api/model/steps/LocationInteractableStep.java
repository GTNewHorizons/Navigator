package com.gtnewhorizons.navigator.api.model.steps;

import java.util.List;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.util.Util;

public interface LocationInteractableStep extends RenderStep {

    void getTooltip(List<String> list);

    void onActionKeyPressed();

    default boolean onKeyPressed(int keyCode) {
        if (Util.isKeyPressed(NavigatorApi.ACTION_KEY)) {
            onActionKeyPressed();
            return true;
        }
        return false;
    }
}
