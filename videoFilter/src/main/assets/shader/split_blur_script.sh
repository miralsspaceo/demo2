#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform int orientation;
uniform samplerExternalOES sTexture;
varying vec2 vTextureCoord;

vec2 uv_offset = vec2(0.001388889, 0.000925926);

const float math_pi = 3.141592654;
const float math_e  = 2.718281828;

vec4 blurN(vec2 uv, int n, float sigma) {
    vec4 c = vec4(0.0);
    float weight_mul = -1.0 / (2.0 * sigma * sigma);
    float sum = 0.00000001;
    for (int i=-n; i<=n; i++) {
        for (int j=-n; j<=n; j++) {
            vec2 pos = clamp(uv + uv_offset * vec2(float(i), float(j)), 0.0, 1.0);
            vec4 pos_c = texture2D(sTexture, pos);
            float weight = pow(math_e, dot(pos, pos) * weight_mul);

            sum += weight;
            c += pos_c * weight;
        }
    }
    return c / sum;
}

vec4 black_white(vec2 uv) {
    vec4 color = texture2D(sTexture, vec2(uv.x, uv.y));
    float colorR = (color.r + color.g + color.b) / 3.0;
    float colorG = (color.r + color.g + color.b) / 3.0;
    float colorB = (color.r + color.g + color.b) / 3.0;
    return vec4(colorR, colorG, colorB, color.a);
}

vec4 black(vec2 uv) {
    return vec4(0.0, 0.0, 0.0, 0.0);
}

void main(void)
{
	vec2 uv = vTextureCoord.xy;
    if (orientation != -1) {
    	if (uv.y < 0.38889) {
    		gl_FragColor = black_white(vec2((uv.x+0.2185)*0.5625, (uv.y)*0.5625));
    	} else if (uv.y > 1.38889) {
    		gl_FragColor = blurN(vec2((uv.x+0.2185)*0.5625, (uv.y)*0.5625),5,2.0);
    	} else {
    		gl_FragColor = texture2D(sTexture, vec2(uv.x, uv.y-0.38889));
    	}
    }
}