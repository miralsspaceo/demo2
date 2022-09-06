#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform int orientation;
uniform samplerExternalOES sTexture;
varying vec2 vTextureCoord;

void main(void)
{
    if (orientation != -1) {
    	vec4 color = texture2D(sTexture, vTextureCoord);
    	float colorR = (color.r + color.g + color.b) / 3.0;
    	float colorG = (color.r + color.g + color.b) / 3.0;
    	float colorB = (color.r + color.g + color.b) / 3.0;
      gl_FragColor = vec4(colorR, colorG, colorB, color.a);
    }
}