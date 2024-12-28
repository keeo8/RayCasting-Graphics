import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.*

class Quadric(i : Int) : UniformProvider("""quadrics[${i}]""") {
  val surface by QuadraticMat4(unitSphere.clone())
  val clipper by QuadraticMat4(unitSlab.clone())  
  val shininess by Vec1(0f)  

  companion object {
    val unitSphere = 
      Mat4(
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, -1.0f
      )
    val unitSlab = 
      Mat4(
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, -1.0f
      ) 
    val plane = 
      Mat4(
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, -0.0f
      )            
    val noClip = 
    Mat4(
      0f, 0f, 0f, 0f,
      0f, 0f, 0f, 0f, 
      0f, 0f, 0f, 0f,
      0f, 0f, 0f, -1f)
  }

}