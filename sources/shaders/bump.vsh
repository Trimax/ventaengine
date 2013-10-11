varying vec3 N;
varying vec3 L;
varying vec3 v;
varying float pattern;        
  
void main(void)
{
  vec3 lightPos = gl_LightSource[0].position.xyz;
  
  v = vec3(gl_ModelViewMatrix * gl_Vertex);
  L = normalize(lightPos - v);
  N = normalize(gl_NormalMatrix * gl_Normal);
   
  pattern=fract(4.0*(gl_Vertex.y+gl_Vertex.x+gl_Vertex.z));
  gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}
             