package sim

import java.text.DecimalFormat
import java.util._

// personal sandbox for learning scala

object Sandbox extends App {
  println("Enter something")
  val i: String = scala.io.StdIn.readLine()
  println("That something is: " + i)
  println("Now enter a double")
  try{
    val j = scala.io.StdIn.readLine().toDouble
    val k = 2.0 + j
    println("You entered " + j + " but now you get " + k)
  }
  catch {
    case e: Exception => println("You failed")
  }


}
