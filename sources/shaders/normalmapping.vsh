//uniform float lightRadius;

varying vec3 lightDir;
varying vec3 viewDir;

attribute vec4 veTangent;
attribute vec3 veBinormal;

void main()
{
  float lightRadius = 10.0;
  gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
  gl_TexCoord[0] = gl_MultiTexCoord0;

  vec3 vertexPos = vec3(gl_ModelViewMatrix * gl_Vertex);
    
  vec3 n = normalize(gl_NormalMatrix * gl_Normal);
  vec3 t = normalize(gl_NormalMatrix * veTangent.xyz);
  vec3 b = normalize(veBinormal);
    
  mat3 tbnMatrix = mat3(t.x, b.x, n.x,
                        t.y, b.y, n.y,
                        t.z, b.z, n.z);

  lightDir = (gl_LightSource[0].position.xyz - vertexPos) / lightRadius;
  lightDir = tbnMatrix * lightDir;

  viewDir = -vertexPos;
  viewDir = tbnMatrix * viewDir;
}