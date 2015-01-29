import akka.actor.{Props, Actor}
import akka.actor.Actor.Receive

object MasterSeeker {
  sealed abstract class MasterSeekerMsg
  case class Start(url: String, depth: Int) extends MasterSeekerMsg
}

class MasterSeeker extends Actor {
  import Seeker._
  import MasterSeeker._

  override def receive: Receive = {
    case Start(url, depth) =>
      val seeker = context.actorOf(Props[Seeker])
      seeker ! Seek(url, depth)

    case Result(s) =>
      for (a <- s) println(a)
  }
}
