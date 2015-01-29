/**
 * Created by Adam on 2015-01-29.
 */
object Messages {
  case class Start(url: String, depth: Int)
  case class Seek(url: String, depth: Int)
  case class Result(map: Map[String, Int])
}
