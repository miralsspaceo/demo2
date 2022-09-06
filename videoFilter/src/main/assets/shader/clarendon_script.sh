#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform int orientation;
uniform samplerExternalOES sTexture;
varying vec2 vTextureCoord;
float scale;

void main(void)
{
    if (orientation != -1) {
    	scale = 2.0f * 0.6f - 1.0f;
    	vec4 color = texture2D(sTexture, vTextureCoord);
    	vec3 new_color = color.rgb;
    	new_color.r = color.r - color.r * ( 1.0 - color.r) * scale;
    	new_color.b = color.b + color.b * ( 1.0 - color.b) * scale;
    	new_color.g = color.g + color.g * ( 1.0 - color.g) * scale;
    	float max_value = max(new_color.r, max(new_color.g, new_color.b));
    	if (max_value > 1.0) {
        new_color /= max_value;
      }
      gl_FragColor = vec4(new_color, color.a);
    }
}