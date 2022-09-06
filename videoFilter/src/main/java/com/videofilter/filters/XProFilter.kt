package com.videofilter.filters

import com.videofilter.OpenGlUtils.DEFAULT_VERTEX_SHADER
import com.videofilter.ScriptCode

class XProFilter :
    GlFilter(DEFAULT_VERTEX_SHADER, CODE.script) {

    companion object {
        private const val CODE_HEADER = (
                "#extension GL_OES_EGL_image_external : require\n"
                        + "precision mediump float;\n"
                        + "varying vec2 vTextureCoord;\n"
                        + "uniform int orientation;\n"
                        + "uniform samplerExternalOES sTexture;\n"
                        + "uniform lowp vec2 vignetteCenter;\n"
                        + "uniform highp float vignetteStart;\n"
                        + "uniform highp float vignetteEnd;\n"
                        + "float scale;\n"
                        + " float contrast;\n"
                        + "void main() {\n"
                        + " if (orientation != -1) {\n")
        private const val SCRIPT = ("  contrast =" + 0.80f + ";\n"
                + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
                + "  color.r = color.r - color.r * ( 1.0 - color.r - color.r) * (2.0f * 0.6f - 1.0f);\n"
                + "  color.g = color.g - color.g * ( 1.0 - color.g - color.g - color.g) * (2.0f * 0.6f - 1.0f) * 0.75;\n"
                + "  color.b = color.b + color.b * ( 1.0 - color.b) * (2.0f * 0.6f - 1.0f);\n"
                + "  color -= 0.1;\n" + "  color *= contrast;\n"
                + "  color += 0.1;\n" + "  gl_FragColor = color;\n")

        //        const val SCRIPT =
//            ("scale = " + (2.0f * 0.58f - 1.0f) + ";\n"+
//                    "vec4 color = texture2D(sTexture, vTextureCoord);\n" +
//                    "vec3 new_color = color.rgb;\n" +
//                    "new_color.r = color.r + color.r * ( 1.0 + color.r) * scale;\n" +
//                    "new_color.b = color.b - color.b * ( 1.0 - color.b) * scale;\n" +
//                    "if (scale > 0.0) {\n" +
//                    "new_color.g = color.g + color.g * ( 1.0 + color.g + color.g + color.g) * scale * 0.53;\n" +
//                    "}\n" +
//                    "float max_value = max(new_color.r, max(new_color.g, new_color.b));\n" +
//                    "if (max_value > 1.0){\n" +
//                    "new_color /= max_value;\n" +
//                            "}\n" +
//                    "lowp vec3 textureColor = new_color.rgb;\n" +
//                    "lowp float d = distance(vTextureCoord, vec2(0.5, 0.5));\n" +
//                    "lowp float percent = smoothstep(0.2, 0.85, d);\n" +
//                    "gl_FragColor = vec4(mix(textureColor.x, 0.0, percent), mix(textureColor.y, 0.0, percent), mix(textureColor.z, 0.0, percent), 1.0);\n")
        private val CODE = ScriptCode(CODE_HEADER)
            .addContentScript(SCRIPT)
    }
}