import akka.actor.{Props, ActorSystem}

object MainSeeker {
import Seeker._
import Messages._

  def main(args: Array[String]): Unit = {
    val sys = ActorSystem("system")
    val masterSeeker = sys.actorOf(Props[MasterSeeker], "MasterSeeker")

    masterSeeker ! Start("https://inf.ug.edu.pl/", 2)
  }
}
