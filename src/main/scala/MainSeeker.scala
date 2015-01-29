import akka.actor.{Props, ActorSystem}

object MainSeeker {
import Seeker._

  def main(args: Array[String]): Unit = {
    val sys = ActorSystem("system")
    val boss = sys.actorOf(Props[Seeker], "MainSeeker")

    boss ! Seek("http://www.wp.pl", 1)
  }
}
