-- Writes CHUNK begin identifier
function TagBegin f tagID =
(
  WriteLong f tagID
  WriteLong f 0
  local ps=ftell f
  return ps
) -- End of 'TagBegin' function

-- Writes CHUNK end identifier 
function TagEnd f tagValue =
(
  local size=ftell f
  size-=tagValue
  fseek f (tagValue-4) #seek_set
  WriteLong f size
  fseek f 0 #seek_end
 ) -- End if 'TagEnd' function

-- Writes boolean value to file
function WriteBool f value =
(
  if value then 
    WriteByte f 1 
  else 
    WriteByte f 0
) -- End of 'WriteBool' function

-- Writes 4x3 matrix to file
function WriteMatrix f M =
(
  local j
  for j=1 to 4 do
  (
    WriteFloat f M[j][1]
    WriteFloat f M[j][2]
    WriteFloat f M[j][3]
  )
) -- End of 'WriteMatrix' function

-- Writes color to file
function WriteColor f color a =
(
  WriteByte f color.blue
  WriteByte f color.green
  WriteByte f color.red
  WriteByte f (a*255/100)
) -- End of 'WriteColor' function

-- Determine object's index
function IndexOfObject obj =
(
  local i
  for i=1 to objects.count do
  (
    if obj == objects[i] then return (i-1)
  )
  return -1
) -- End of 'IndexOfObject' function

-- Determine materials' index
function IndexOfMaterial mat =
(
  local i
  for i=1 to sceneMaterials.count do
  (
    if mat == sceneMaterials[i] then return (i-1)
  )
  return -1
) -- End of 'IndexOfMaterial' function

-- Writes map to file
function WriteMap f map factor EnableFlag kind =
(
  local materialWrited = false
  if map!=undefined and factor>0 and EnableFlag then
  (
    if classof map == Bitmaptexture then
    (
      WriteByte f 1
      WriteLong f map.fileName.count
      WriteString f map.fileName
      
      materialWrited = true
    )
  )
  
  if (materialWrited == false) then
    WriteByte f 0
) -- End of 'WriteMap' function 

-- Writes standard material information to file
function WriteMaterial f matIndex =
(
  local mat=sceneMaterials[matIndex]
  
  if classof mat == Standardmaterial then
  (
    -- Write material index
    WriteLong f (matIndex-1)

    -- Write material colors
    WriteColor f mat.ambient mat.opacity
    WriteColor f mat.diffuse mat.opacity
    WriteColor f mat.specular mat.opacity
  
    -- Write self-illumination parameters 
    if mat.useSelfIllumColor then
      WriteColor f mat.selfIllumColor mat.opacity
    else
    (
      WriteByte f (mat.diffuse.blue*mat.selfIllumAmount/100)
      WriteByte f (mat.diffuse.green*mat.selfIllumAmount/100)
      WriteByte f (mat.diffuse.red*mat.selfIllumAmount/100)
      WriteByte f (mat.opacity*255/100)
    )
  
    -- Specular & glossiness levels
    WriteFloat f mat.SpecularLevel
    WriteFloat f mat.glossiness

    -- Maps
    WriteMap f mat.diffuseMap mat.diffuseMapAmount mat.diffuseMapEnable 2
    WriteMap f mat.opacityMap mat.opacityMapAmount mat.opacityMapEnable 7
    WriteMap f mat.bumpMap mat.bumpMapAmount mat.bumpMapEnable 9
    WriteMap f mat.reflectionMap mat.reflectionMapAmount mat.reflectionMapEnable 10
  )
) -- End of 'WriteStandardMaterial' function

-- Writes timestamp to file 
function WriteTime f tm =
(
  WriteLong f (tm as float / 4.8+0.5)
) -- End of 'WriteTime' function

-- Writes animation speed to file
function WriteSpeed f speed =
(
  WriteFloat f (speed /100.0)
) -- End of 'WriteSpeed' function

-- Writes moving (position) animation to file
function WritePosAnimation f obj =
(
  if obj.pos.isAnimated==false then return 0

  local tag=TagBegin f 0x00100000
  local i

  local n=obj.pos.controller.keys.count
  WriteLong f n

  for i=1 to n do
  (
    local tm=obj.pos.controller.keys[i].time
    WriteTime f tm
    
    local p
    if obj.parent != undefined then
    (
      p=at time tm (obj.pos * (inverse obj.parent.transform))
    )
    else
      p=at time tm obj.pos			
    WriteFloat f p[1]
    WriteFloat f p[2]
    WriteFloat f p[3]
  )
  TagEnd f tag
) -- End of 'WritePosAnimation' function 

-- Writes rotation animation to file 
function WriteRotationAnimation f obj =
(
  if obj.rotation.isAnimated==false then return 0

  local tag=TagBegin f 0x00100001
  local i
  local axis
  local angle
  local Q

  local n=obj.rotation.controller.keys.count
  WriteLong f n

  for i=1 to n do
  (
    local tm=obj.rotation.controller.keys[i].time
    WriteTime f tm
    if obj.parent != undefined then
    (
      Q=at time tm ((obj.transform * 
                    (inverse obj.parent.transform)) as quat)
    )
    else
    (
      Q = at time tm (obj.transform as quat)
    )
    WriteFloat f Q.x
    WriteFloat f Q.y
    WriteFloat f Q.z
    WriteFloat f Q.w
  )
  TagEnd f tag
) -- End of 'WriteRotationAnimation' function 

-- Writes scaling animation to file
function WriteScaleAnimation f obj =
(
  if obj.scale.isAnimated==false then return 0

  local tag=TagBegin f 0x00100002
  local i
  local scale
  local scale0
  local isScale0 = false

  local n=obj.scale.controller.keys.count
  WriteLong f n

  for i=1 to n do
  (
    local tm=obj.scale.controller.keys[i].time
    WriteTime f tm
    if obj.parent != underfined then
    (
      scale=at time tm ((obj.transform * 
                        (inverse obj.parent.transform)).scale)
    )
    else
    (
      scale = at time tm obj.scale
    )
    if isScale0 then
    (
      scale.x/=scale0.x
      scale.y/=scale0.y
      scale.z/=scale0.z
    )
    else
    (
      scale0 = scale
      scale.x = 1
      scale.y = 1
      scale.z = 1
      isScale0 = true
    )

    WriteFloat f scale[1]
    WriteFloat f scale[2]
    WriteFloat f scale[3]
  )
  TagEnd f tag
) -- End of 'WriteScaleAnimation' function 

-- Writes node to file 
function WriteNode f obj objType =
(
  local objIndex=IndexOfObject obj
  local tag=TagBegin f 0x00000030
  WriteLong f objType
  local prnIndex
  local trgIndex

  -- Determine parent index
  if obj.parent != undefined then
    prnIndex = IndexOfObject obj.parent
  else
    prnIndex=-1
  
  if obj.target != undefined then
    trgIndex = IndexOfObject obj.target
  else
    trgIndex=-1

  -- Writing object name
  WriteString f obj.name
  WriteLong f objIndex
  WriteLong f prnIndex
  WriteLong f trgIndex

  local LocalMatrix 
  LocalMatrix=copy obj.transform
  LocalMatrix.row1=normalize LocalMatrix.row1
  LocalMatrix.row2=normalize LocalMatrix.row2
  LocalMatrix.row3=normalize LocalMatrix.row3
    
  if obj.parent != undefined then
  (
    local parentMatrix = copy obj.parent.transform
    parentMatrix.row1=normalize parentMatrix.row1
    parentMatrix.row2=normalize parentMatrix.row2
    parentMatrix.row3=normalize parentMatrix.row3
    LocalMatrix = LocalMatrix * (inverse parentMatrix)
  )

  if objType==1 or objType==2 then
  (
    LocalMatrix.row3=-LocalMatrix.row3
)

  WriteMatrix f LocalMatrix
  TagEnd f tag

  WritePosAnimation f obj
  WriteRotationAnimation f obj
  WriteScaleAnimation f obj
) -- End of 'WriteNode' function 

-- Bone weight structure
struct TBoneWeight(bone,weight)

-- Index structure 
struct TIndexItem (index,next,smooth,normal,ncount,u,v,MatID)

-- Progress counter
progressCount=0

-- Gets morphing node by name
function GetMorphNode morphNodeName =
(
  local nm=""
  local i
  for i=3 to morphNodeName.count do nm+=morphNodeName[i]
  return GetNodeByName nm
) -- End of 'GetMorphNode' function

-- Writes all materials to file
function WriteMaterials f =
(
  -- Count the number of materials
  local materialsNumber = 0
  for i=1 to sceneMaterials.count do 
    if classof sceneMaterials[i] == Standardmaterial then
      materialsNumber = materialsNumber + 1
  
  -- Write the number of materials
  WriteLong f materialsNumber
    
  -- Write materials
  for i=1 to sceneMaterials.count do 
    WriteMaterial f i
) -- End of 'WriteMaterials' function

-- Calculate face center
function VEOGetFaceCenter m i = 
(
  face = (GetFace m i) - [1, 1, 1]
  v1 = (GetVert m (face.x as Integer))
  v2 = (GetVert m (face.y as Integer))
  v3 = (GetVert m (face.z as Integer))
  
  point3 center (v1.x + v2.x + v3.x) / 3.0 (v1.y + v2.y + v3.y) / 3.0 (v1.z + v2.z + v3.z) / 3.0
  
  return center
) -- End of 'GetFaceCenter' function

-- Writes all scene objects to file
function VEOWriteData f =
(
  local i

  -- Writing materials
  WriteMaterials f
  
  -- All acceptable meshes
  local obj = #()  
  for currentObj in objects do  
    if ((classOf currentObj) != BoneGeometry) and ((classOf currentObj) != Biped_Object) and (canConvertTo currentObj TriMeshGeometry) then
      append obj currentObj
    
  -- Writing number of meshes
  local objCount = obj.count    
  WriteLong f objCount
  
  -- First & last animation keys    
  start = animationRange.start.frame as integer
  end = animationRange.end.frame as integer
    
  -- Writing number of animation keys
  WriteLong f (end-start+1)
  WriteLong f frameRate
  
  -- Determine masses center
  massesCenter = [0.0, 0.0, 0.0]
  local totalVertices = 0  
  for currentObj in obj do
  (
    mesh = convertToMesh currentObj	
    V_Count = mesh.numVerts   -- Number of vertices	
   
    for i = 1 to V_Count do
    (
      p = (GetVert mesh i)	  
	  massesCenter = massesCenter + p
    )	
	
	totalVertices = totalVertices + V_Count
  )
  
  -- Getting center
  massesCenter = massesCenter / totalVertices
  
  -- Determine maximal distance from masses center
  local maxDistance = 0.0
  for currentObj in obj do
  (
    mesh = convertToMesh currentObj	
    V_Count = mesh.numVerts   -- Number of vertices	
   
    for i = 1 to V_Count do
    (
      p = (GetVert mesh i)	  
	  
	  local dist = distance massesCenter p
	  if (dist > maxDistance) then
	  (
	    maxDistance = dist
	  )
    )	
  )  
    
  -- Writing meshes            
  for currentObj in obj do 
  (   
    mesh = convertToMesh currentObj
    
    -- Mesh header
    V_Count = mesh.numVerts   -- Number of vertices
    F_Count = mesh.numFaces   -- Number of faces
    T_Count = mesh.numTVerts  -- Number of texture vertices
 
    -- Writing mesh header
    WriteLong f V_Count
    WriteLong f F_Count
    WriteLong f T_Count
    
    local materialIndex = IndexOfMaterial currentObj.material        
    WriteLong f materialIndex

    -- Writing geometry
    
    -- Set current animation key to start
    at time start
      
    -- Vertices
    for i = 1 to V_Count do
    (
      p = (GetVert mesh i)
      WriteFloat f ((p.x - massesCenter.x) / maxDistance)
      WriteFloat f ((p.z - massesCenter.z) / maxDistance)
      WriteFloat f ((p.y - massesCenter.y) / maxDistance)
      
      n = GetNormal mesh i
      WriteFloat f n.x
      WriteFloat f n.z
      WriteFloat f n.y
    )
    
    -- Texture coordinates
    for i = 1 to T_Count do
    (
      t = (GetTVert mesh i) 
      WriteFloat f t.x
      WriteFloat f t.y
    )     
      
    -- Faces & texture faces
    for i = 1 to F_Count do
    (
      -- Obtain vertices indexes
      face = (GetFace mesh i) - [1, 1, 1]
      WriteLong f (face.x as Integer)
      WriteLong f (face.y as Integer)
      WriteLong f (face.z as Integer)
      
      -- Obtain texture vertices indexes
      tface = (GetTVFace mesh i) - [1, 1, 1]
      WriteLong f (tface.x as Integer)
      WriteLong f (tface.y as Integer)
      WriteLong f (tface.z as Integer)   

      -- Obtain face normal
      faceNormal = GetFaceNormal mesh i
      WriteFloat f (faceNormal.x as Float)		
      WriteFloat f (faceNormal.z as Float)		
      WriteFloat f (faceNormal.y as Float)		
      
      -- Obtain face center
      v1 = (GetVert mesh (face.x as Integer + 1))
      v2 = (GetVert mesh (face.y as Integer + 1))
      v3 = (GetVert mesh (face.z as Integer + 1))
      faceCenter = [(v1.x + v2.x + v3.x) / 3.0, (v1.y + v2.y + v3.y) / 3.0, (v1.z + v2.z + v3.z) / 3.0]
     
      -- Write face center      
      WriteFloat f (faceCenter.x as Float)		
      WriteFloat f (faceCenter.z as Float)		
      WriteFloat f (faceCenter.y as Float)	      
    )
        
    -- Animation    
    at time start 
    m0 = (inverse mesh.objectTransform)
    
    -- Writing frames
    for frame = start to end do 
      at time frame
      (
        -- Rotation matrix from initial position
        m = m0 * mesh.objectTransform
        
        -- Position
        p = m.Position
        
        -- Quaternion
        r = m.Rotation
        
        -- Writing
        WriteFloat f p.x
        WriteFloat f p.y
        WriteFloat f p.z
        WriteFloat f r.x
        WriteFloat f r.y
        WriteFloat f r.z
        WriteFloat f r.w
      )
  )            
) -- End of 'VEOWriteData' function

-- Writes file header
function WriteHeader f = 
(
  local engineVersion = 1
  WriteByte f 86             -- Engine: V (Venta Engine)
  WriteByte f 4              -- Format: object
  WriteLong f engineVersion  -- Engine version: 1  
) -- End of 'WriteHeader' function

-- Main exporting function
function VEOExport =
(
  -- Determine output file name
  local fname=getSaveFileName caption: "Export into VEO" types: "VEO-format (*.veo)|*.veo"
  if fname != undefined then
  (
    -- Move slider to the beginning
    sliderTime = 0
     
    if viewport.getType() != #view_camera then
    (
      local n=viewport.numViews
      local i
      for i = 1 to n do
      (
        viewport.activeViewport=i
        if viewport.getType() == #view_camera then exit
      )
    )
    
    -- Open file to write
    escapeEnable = true
    f=fopen fname "wb"
    
    -- Writing header
    WriteHeader f

    -- Writing scene
    VEOWriteData f
    fclose f    
    
    -- That's it
    MessageBox "Scene was exported successfully" title: "Scene export into VEO" beep:false
  )
) -- End of 'VEOExport' function

-- Export macros
MacroScript VEOExportMacro category:"VEO Exporter" tooltip:"Scene export into VEO" buttontext:"Export into VEO"
(
  VEOExport()
) -- End of 'VEOExportMacro' function

-- Registering macros at main menu
if menuMan.registerMenuContext 0x1ee76006 then 
(
  local mainMenu = menuMan.GetMainMenuBar()
  local fileMenuItem = mainMenu.GetItem 1
  local fileMenu = fileMenuItem.GetSubMenu()
  local newMenuItem = menuMan.createActionItem "VEOExportMacro" "VEO Exporter" 
  fileMenu.AddItem newMenuItem 1
  menuMan.updateMenuBar()
) -- Macros registered