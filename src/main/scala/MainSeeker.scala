import akka.actor.{Props, ActorSystem}

object MainSeeker {
import Seeker._
import MasterSeeker._

  def main(args: Array[String]): Unit = {
    val sys = ActorSystem("system")
    val boss = sys.actorOf(Props[MasterSeeker], "MasterSeeker")

    boss ! Start("https://inf.ug.edu.pl/", 2)
  }
}
