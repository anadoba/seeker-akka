import akka.actor.Actor

object MasterSeeker {
  sealed abstract class MasterSeekerMsg
}

class MasterSeeker extends Actor {

  def init: Receive = {
    case _ => ;
  }

  override def receive = init
}
