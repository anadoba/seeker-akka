import akka.actor.Actor
import akka.actor.Actor.Receive

object MasterSeeker {
  sealed abstract class MasterSeekerMsg
}

class MasterSeeker extends Actor {

  override def receive: Receive = {
    case _ => ;
  }
}
