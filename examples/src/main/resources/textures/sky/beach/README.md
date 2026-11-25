# Sky of the beach

The faces of the cubemap (`px`, `nx`, `py`, `ny`, `pz`, `nz`) are made from the panorama
[Umhlanga Sunrise](https://polyhaven.com/a/umhlanga_sunrise) by Greg Zaal, from Poly Haven, [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/).
The tone mapped jpg of 8192 by 4096 pixels is cut into faces of 1536 pixels with `tools/equirect_to_cubemap.py`, and the sun of its
radiance file is found by the same script: it is over the horizon at the direction (0.588, 0.034, -0.808).
