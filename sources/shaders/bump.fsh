varying vec3 N;
varying vec3 L;
varying vec3 v;
varying float pattern;

//uniform vec4 color0; // Diffuse Color: 0.8, 0.0, 0.0, 1.0
//uniform vec4 color1; // Ambient Color
//uniform vec4 color2; // Specular Color: 0.8, 0.0, 0.0, 1.0

uniform vec4 veCameraPosition; // Eye Position

#define shininess 10.0

void main (void)
{
  vec4 color0 = vec4(0.8, 0.0, 0.0, 1.0);
  vec4 color1 = vec4(0.6, 0.1, 0.8, 1.0);
  vec4 color2 = vec4(0.8, 0.0, 0.0, 1.0);

  vec3 E = normalize(veCameraPosition.xyz - v);
  vec3 R = normalize(2.0 * dot(N,L) * N-L); // R = normalize(-reflect(L,N));

  float spec = pow(max(dot(R,E),0.0), shininess);
  float spec2= pattern*spec;

  float diffuse = max(dot(N,L),0.0);
  diffuse = smoothstep(diffuse, 0.0, 0.5);

  gl_FragColor = color0*diffuse + color1 + color2*(spec+spec2);
}