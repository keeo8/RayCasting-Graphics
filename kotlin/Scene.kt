import org.w3c.dom.HTMLCanvasElement
import org.khronos.webgl.WebGLRenderingContext as GL
import org.khronos.webgl.Float32Array
import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.*
import kotlin.js.Date
import kotlin.math.* 

class Scene (
  val gl : WebGL2RenderingContext) : UniformProvider("scene") {

  val vsQuad = Shader(gl, GL.VERTEX_SHADER, "quad-vs.glsl")
  val fstrace = Shader(gl, GL.FRAGMENT_SHADER, "trace-fs.glsl")
  val traceProgram = Program(gl, vsQuad, fstrace)
  val skyCubeTexture = TextureCube(gl,
      "media/skycube/posx.jpg", "media/skycube/negx.jpg",
      "media/skycube/posy.jpg", "media/skycube/negy.jpg",
      "media/skycube/posz.jpg", "media/skycube/negz.jpg"
    )
  val sandTexture = Texture2D(gl, "media/textures/sand1.jfif")
  val waterTexture = Texture2D(gl, "media/textures/water123.jpg")
  val traceMaterial = Material(traceProgram).apply{
    this["envTexture"]?.set( skyCubeTexture )
    this["sandTexture"]?.set( sandTexture )
    this["waterTexture"]?.set( waterTexture )
  }
  val quadGeometry = TexturedQuadGeometry(gl)
  val traceMesh = Mesh(traceMaterial, quadGeometry)

  val lights = Array<Light>(8) { Light(it) }
  init{

    lights[0].position.set(1f, 5f, -2f, 0f).normalize()
    lights[0].powerDensity.set(1.1f, 1.1f, 1.1f)
  }

  val umbrellaPos = Vec3(2f, 4f, 2f)
  val umbrellaTilt = Vec3(.25f, 1f, -.15f) 
  val roll = 2.0f 

  val quadrics = Array<Quadric>(6) { Quadric(it) }
  init{


    //umbrella stick
    quadrics[0].surface.set(
      1f, 0f, 0f, 0f,
      0f, 0f, 0f, 0f,
      0f, 0f, 1f, 0f,
      0f, 0f, 0f, -0.01f
    )

    quadrics[0].surface.transform(Mat4().set().translate(umbrellaPos).rotate(roll, umbrellaTilt))
    quadrics[0].clipper.set(Quadric.unitSlab)
    quadrics[0].clipper.transform(Mat4().set().scale(0.1f, 3f, 0.1f).translate(umbrellaPos).rotate(roll, umbrellaTilt))
    quadrics[0].shininess.set(50f)


    ///umbrella head
    quadrics[2].surface.set(Quadric.unitSphere)
    quadrics[2].clipper.set(Quadric.unitSlab)
    quadrics[2].surface.transform(Mat4().set().scale(3f, 1f, 3f).translate(0f, 2.01f, 0f).translate(umbrellaPos).rotate(roll, umbrellaTilt))
    quadrics[2].clipper.transform(Mat4().set().translate(0f, 3.11f, 0f).translate(umbrellaPos).rotate(roll, umbrellaTilt))
    quadrics[2].shininess.set(40f)


    //beach ball
    quadrics[3].surface.set(Quadric.unitSphere)
    quadrics[3].clipper.set(Quadric.noClip)
    quadrics[3].surface.transform(Mat4().set().translate(-17f, -3f, 0f).translate())
    quadrics[3].shininess.set(20f)


    //sand
    quadrics[4].surface.set(
      .01f, 0f, 0f, 0f,
      0f, 0f, 0f, 1f,
      0f, 0f, .01f, 0f,
      0f, 0f, 0f, -1f
    )
    quadrics[4].clipper.set(
      0.0f, 0.0f, 0.0f, 0.0f,
      0.0f, 1.0f, 0.0f, 0.0f,
      0.0f, 0.0f, 0.0f, 0.0f,
      0.0f, 0.0f, 0.0f, -4.0f)
    quadrics[4].clipper.transform(Mat4().set().translate(0f, -1f, 0f))
    quadrics[4].shininess.set(5f)


    //ocean
    quadrics[5].surface.set(Quadric.unitSlab)
    quadrics[5].surface.transform(Mat4().set().translate(0f, -3f, 0f))
    quadrics[5].shininess.set(10f)

}



  val timeAtFirstFrame = Date().getTime()
  var timeAtLastFrame =  timeAtFirstFrame

  val camera = PerspectiveCamera()
  init {
    camera.position.set(0f, 4f, 10f)
  }

  init{
    gl.enable(GL.DEPTH_TEST)
    addComponentsAndGatherUniforms(*Program.all)
  }

  fun resize(gl : WebGL2RenderingContext, canvas : HTMLCanvasElement) {
    gl.viewport(0, 0, canvas.width, canvas.height)
    camera.setAspectRatio(canvas.width.toFloat() / canvas.height.toFloat())
  }

  fun update(gl : WebGL2RenderingContext, keysPressed : Set<String>) {

    val timeAtThisFrame = Date().getTime() 
    val dt = (timeAtThisFrame - timeAtLastFrame).toFloat() / 1000.0f
    val t  = (timeAtThisFrame - timeAtFirstFrame).toFloat() / 1000.0f    
    timeAtLastFrame = timeAtThisFrame

    val tLocation = gl.getUniformLocation(traceProgram.glProgram, "t")
    gl.uniform1f(tLocation, t)

    timeAtLastFrame = timeAtThisFrame

    val waterDisplacement = sin(t)
    quadrics[5].surface.set(
      0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, waterDisplacement,
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, -1.0f
      )
    quadrics[5].surface.transform(Mat4().set().translate(0f, -3f, 0f))
    quadrics[5].clipper.set(
      0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, waterDisplacement,
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, -1.0f
      )
    quadrics[5].clipper.transform(Mat4().set().translate(0f, -2f, 0f))

    val ballDisplacement = sin(t)
    quadrics[3].surface.set(
       1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, ballDisplacement,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, -1.0f
      )
    //quadrics[3].surface.transform(Mat4().set().translate(0f, -3f, 0f))

    quadrics[3].surface.transform(Mat4().set().translate(-17f, -1f, 0f).translate(-ballDisplacement, 0f, 0f))

    camera.move(dt, keysPressed)

    // clear the screen
    gl.clearColor(0.3f, 0.0f, 0.3f, 1.0f)
    gl.clearDepth(1.0f)
    gl.clear(GL.COLOR_BUFFER_BIT or GL.DEPTH_BUFFER_BIT)

    traceMesh.draw(camera, *quadrics, *lights)

  }
}
