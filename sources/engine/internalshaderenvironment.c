#include "internalshadermanager.h"
#include "internalshaders.h"

/* Vertex shader */
VECHAR p_ShaderEnvironmentVertex[] = ""
"varying vec3 v;\r\n"
"varying vec3 l;\r\n"
"varying vec3 pos;\r\n"
"varying vec3 colorYxy;\r\n"
""
"uniform vec4 "VE_SHADERPARAM_CAMERAPOS";\r\n"
"uniform vec3 "VE_SHADERPARAM_SUNPOSITION";\r\n"
""
"const float turbidity = 0.5;\r\n"
""
"attribute vec4 "VE_SHADERPARAM_TANGENTS";\r\n"
"attribute vec3 "VE_SHADERPARAM_BINORMALS";\r\n"
""
""
"vec3 perezZenith(float t, float thetaSun)\r\n"
"{\r\n"
"  const float pi = 3.1415926;\r\n"
"  const vec4	cx1 = vec4 (0,        0.00209, -0.00375,  0.00165);\r\n"
"  const vec4	cx2 = vec4 (0.00394, -0.03202,  0.06377, -0.02903);\r\n"
"  const vec4	cx3 = vec4 (0.25886,  0.06052, -0.21196,  0.11693);\r\n"
"  const vec4	cy1 = vec4 (0.0,      0.00317, -0.00610,  0.00275);\r\n"
"  const vec4	cy2 = vec4 (0.00516, -0.04153,  0.08970, -0.04214);\r\n"
"  const vec4	cy3 = vec4 (0.26688,  0.06670, -0.26756,  0.15346);\r\n"
""
"  float t2  = t*t;\r\n"
"  float chi = (4.0 / 9.0 - t / 120.0 ) * (pi - 2.0 * thetaSun );\r\n"
"  vec4 theta = vec4(1, thetaSun, thetaSun*thetaSun, thetaSun*thetaSun*thetaSun);\r\n"
""
" float Y = (4.0453 * t - 4.9710) * tan ( chi ) - 0.2155 * t + 2.4192;\r\n"
" float x = t2 * dot(cx1, theta) + t * dot(cx2, theta) + dot(cx3, theta);\r\n"
" float y = t2 * dot(cy1, theta) + t * dot(cy2, theta) + dot(cy3, theta);\r\n"
""
"  return vec3(Y, x, y);\r\n"
"}\r\n"
""
""
"vec3 perezFunc(float t, float cosTheta, float cosGamma)\r\n"
"{\r\n"
"  float gamma = acos(cosGamma);\r\n"
"  float cosGammaSq = cosGamma * cosGamma;\r\n"
"  float aY =  0.17872 * t - 1.46303;\r\n"
"  float bY = -0.35540 * t + 0.42749;\r\n"
"  float cY = -0.02266 * t + 5.32505;\r\n"
"  float dY =  0.12064 * t - 2.57705;\r\n"
"  float eY = -0.06696 * t + 0.37027;\r\n"
"  float ax = -0.01925 * t - 0.25922;\r\n"
"  float bx = -0.06651 * t + 0.00081;\r\n"
"  float cx = -0.00041 * t + 0.21247;\r\n"
"  float dx = -0.06409 * t - 0.89887;\r\n"
"  float ex = -0.00325 * t + 0.04517;\r\n"
"  float ay = -0.01669 * t - 0.26078;\r\n"
"  float by = -0.09495 * t + 0.00921;\r\n"
"  float cy = -0.00792 * t + 0.21023;\r\n"
"  float dy = -0.04405 * t - 1.65369;\r\n"
"  float ey = -0.01092 * t + 0.05291;\r\n"
""
"  return vec3((1.0 + aY * exp(bY/cosTheta)) * (1.0 + cY * exp(dY * gamma) + eY*cosGammaSq),\r\n"
"              (1.0 + ax * exp(bx/cosTheta)) * (1.0 + cx * exp(dx * gamma) + ex*cosGammaSq),\r\n"
"              (1.0 + ay * exp(by/cosTheta)) * (1.0 + cy * exp(dy * gamma) + ey*cosGammaSq) );\r\n"
"}\r\n"
""
""
"vec3 perezSky(float t, float cosTheta, float cosGamma, float cosThetaSun)\r\n"
"{\r\n"
"  float thetaSun = acos(cosThetaSun);\r\n"
"  vec3 zenith    = perezZenith(t, thetaSun);\r\n"
"  vec3 clrYxy    = zenith * perezFunc(t, cosTheta, cosGamma) / perezFunc (t, 1.0, cosThetaSun);\r\n"
""
"  clrYxy[0] *= smoothstep(0.0, 0.1, cosThetaSun);\r\n"
""
"  return clrYxy;\r\n"
"}\r\n"
""
""
"varying vec3 sunDir;\r\n"
"varying float intensity;\r\n"
""
"void main()"
"{"
"  sunDir = normalize("VE_SHADERPARAM_SUNPOSITION" - gl_Vertex);\r\n"
"  intensity = clamp(dot(sunDir, gl_Normal), 0.1, 1.0);"
""
"  v = normalize((gl_Vertex-"VE_SHADERPARAM_CAMERAPOS".xyz).xyz);\r\n"
"  l = normalize(gl_LightSource[0].position.xyz);\r\n"
"  l = normalize(vec3(0.0, 0.0, 0.0));\r\n"
"  pos = 0.1 * gl_Vertex.xyz;\r\n"
"  colorYxy = perezSky(turbidity, max(v.z, 0.0) + 0.05, dot(l, v), l.z);\r\n"
""
"  gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\r\n"
"  gl_TexCoord[0] = gl_MultiTexCoord0;\r\n"
"}";

/* Fragment shader */
VECHAR p_ShaderEnvironmentFragment[] = ""
"varying vec3 v;\r\n"
"varying vec3 l;\r\n"
"varying vec3 pos;\r\n"
"varying vec3 colorYxy;\r\n"
""
""
"vec3 convertColor()\r\n"
"{\r\n"
"  vec3 clrYxy = vec3(colorYxy);\r\n"
"  clrYxy[0] = 1.0 - exp( -clrYxy[0] / 25.0);\r\n"
""
"  float ratio = clrYxy[0] / clrYxy[2];\r\n"
"  vec3 XYZ;\r\n"
""
"  XYZ.x = clrYxy[1] * ratio;\r\n"
"  XYZ.y = clrYxy[0];\r\n"
"  XYZ.z = ratio - XYZ.x - XYZ.y;\r\n"
""
"  const vec3 rCoeffs = vec3( 3.240479, -1.53715, -0.49853);\r\n"
"  const vec3 gCoeffs = vec3(-0.969256,  1.875991, 0.041556);\r\n"
"  const vec3 bCoeffs = vec3( 0.055684, -0.204043, 1.057311);\r\n"
""
"  return vec3(dot(rCoeffs, XYZ), dot(gCoeffs, XYZ), dot(bCoeffs, XYZ));\r\n"
"}\r\n"
""
""
"varying vec3 sunDir;\r\n"
"varying float intensity;\r\n"
""
"uniform vec4 "VE_SHADERPARAM_SKYCOLOR";\r\n"
""
"void main()"
"{"
"  vec4 skyColor = vec4(clamp(convertColor(), 0.0, 1.0), 1.0);\r\n"
//"  gl_FragColor = skyColor;\r\n"
"  gl_FragColor = intensity * "VE_SHADERPARAM_SKYCOLOR";\r\n"
"}";
