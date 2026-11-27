# Roadmap

What is left to do, in the order it is planned: every step builds on the previous ones and makes a visible difference on
its own. What the engine can do already is in the [readme](readme.md). Paths of shaders are relative to
`engine/src/main/resources/shaders/`.

## Animation

- [ ] **1. Animation.** Objects move only from code: there are no animations in the resources. Keyframe
  animation of the transforms of the hierarchy nodes (position, rotation, scale with interpolation) is the first
  step, and the boxes of the nodes must follow them, or the culling would cut a moving object; skeletal animation
  (skinned meshes with joints and weights, played and blended in the vertex shader) needs a format with skeletons, so
  it comes with a glTF loader, which also brings PBR materials and scenes in one file.

## Later

- [ ] **2. Temporal antialiasing.** The image with the post effects shimmers on thin things (leaves, wires) in motion,
  which FXAA only softens. TAA keeps the history of the frames and needs the motion vectors of what moves: the camera,
  the objects and the skinned meshes, so it comes after the animation.
- [ ] **3. Volumetric clouds.** The clouds are painted on the 8 bit skybox: they don't move, they cast no shadows on the
  land and no light shafts, and the sun can't be hidden by one. Clouds made of a noise (a march through a layer of
  the sky, or a few layers of cards), lit by the sun and the sky, would drift, shadow the land through the shadow maps
  and the light shafts, and change the light of the scene as they pass.
- [ ] **4. Ambient occlusion of the indirect light only.** The ambient occlusion multiplies the finished image, so it
  darkens the direct light of the sun in a corner as well, where the shadow maps decide what is lit. The shaders of
  the surfaces should read it and apply it to the light of the sky and to the image based lighting only. For that
  the occlusion must be known before the surfaces are drawn: a depth pass over the objects and the terrain (with the
  wind, the holes of the cutouts and the instancing), the occlusion from its depth, and then the pass of the colors with
  the depth test that accepts what the depth pass wrote (the vertices must give the same depths in both).

## Editor

- [ ] **5. Studio.** The editor of scenes (`studio/`) edits a scene in its viewport and inspector: it chooses, creates, copies,
  deletes, moves, turns and scales things, edits the environment and the files of the things and of their materials (a light also with handles for its range and cone), brings
  in and renames files with the references updated, follows the changes of the files on the disk, with undo and redo, and saves the files. What is left, in the order of the work:
  1. Groups of things (the engine needs scene nodes with a parent first).
  2. The editors of the terrain, the water, the emitters and the sounds (the tools of the old `editor` become panels); playing a scene
     in the editor and building the archive.
  3. Smaller things: a silhouette instead of the frame of the chosen thing, choosing by pixels (an id buffer), panels as strips of
     buttons at the edges, and the old `editor` goes when its tools are in the studio.
