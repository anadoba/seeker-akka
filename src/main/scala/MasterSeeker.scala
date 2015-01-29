import akka.actor.{Props, Actor}
import akka.actor.Actor.Receive

object Messages {
  case class Start(url: String, depth: Int)
  case class Seek(url: String, depth: Int)
  case class Result(map: Map[String, Int])
}

class MasterSeeker extends Actor {
  import Messages._

  override def receive: Receive = {
    case Start(url, depth) =>
      val seeker = context.actorOf(Props[Seeker], "main")
      seeker ! Seek(url, depth)

    case Result(s) =>
      println("Wyniki:")
      for (a <- s) println(a)
      context.system.shutdown()
  }
}
