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

uniform mat4 uMVPMatrix;
uniform mat4 uSTMatrix;
attribute vec4 aPosition;
attribute vec4 aTextureCoord;
varying vec2 vTextureCoord;
void main() {
      gl_Position = uMVPMatrix * aPosition;
          vTextureCoord = (uSTMatrix * aTextureCoord).xy;
          }
          ", "
#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 vTextureCoord;\nuniform samplerExternalOES sTexture;\nvoid main() {\n    gl_FragColor = texture2D(sTexture, vTextureCoord);\n}\n");
  170          C2406a aVar2 = new C2406a(this.f3281i);
  171          this.f3280h = aVar2;
  172:         aVar2.m4629a(3553, "uniform mat4 uMVPMatrix;\nuniform mat4 uSTMatrix;\nattribute vec4 aPosition;\nattribute vec4 aTextureCoord;\nvarying vec2 vTextureCoord;\nvoid main() {\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n}\n", "precision mediump float;\nvarying vec2 vTextureCoord;\nuniform sampler2D sTexture;\nvoid main() {\n    gl_FragColor = texture2D(sTexture, vTextureCoord);\n}\n