package com.r4sh33d.musicslam.utils;

import android.content.Context;
import android.support.v8.renderscript.RenderScript;

public class RenderScriptHelper {
    private static RenderScript sRenderScript;

    public static RenderScript getRenderScript(Context context) {
        if (sRenderScript == null) {
            sRenderScript = RenderScript.create(context);
        }
        return sRenderScript;
    }

    public void destroy() {
        if (sRenderScript != null) {
            sRenderScript.destroy();
        }
        sRenderScript = null;
    }
}
