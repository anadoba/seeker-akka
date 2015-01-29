import java.io.IOException
import java.net.URL

import akka.actor.{PoisonPill, Actor}
import akka.actor.Actor.Receive
import org.xml.sax.InputSource

/**
 * Created by Adam on 2015-01-23.
 */
object Seeker {
  sealed abstract class SeekerMsg
  case class Seek(url: String, depth: Int)
  case class Result(servers: List[String])

  def getLinksFromPage(url: String): List[String] = {
    val parser = new LinkParser
    try  {
      parser.loadXML(new InputSource(url))
      val links = parser.getLinks()
        .filter(link => link.startsWith("http"))
        .map(link => link.replace("https", "http"))

      links
    } catch {
      case e: IOException =>
        return List[String]()
    }
  }

  def getServersFromLinks(links: List[String]): List[String] = {
    for (link <- links) yield new URL(link).getHost
  }

  def countServers(servers: List[String]): Map[Int, String] = {
    val map = servers.groupBy(server => server).map(temp => (temp._2.length, temp._1))
    map.toSeq.sortWith(_._1 > _._1).toMap
  }
}

class Seeker extends Actor {
  import Seeker._

  var servers: List[String] = List[String]()

  override def receive = init

  def init: Receive = {
    case Seek(url, depth) =>
      if (depth == 0)
        sender() ! Result(List[String]())
      else {
        val links = getLinksFromPage(url)
        //for (link <- links) println(link)
        val servers = getServersFromLinks(links)
        val map = countServers(servers)
        for (m <- map) println(m)
        self ! PoisonPill
        sender() ! PoisonPill
      }

    case Result(s) =>
      context.parent ! Result(servers ++ s)
  }

  def initializedToSeek(url: String, depth: Int): Receive = {
    case _ => ;
  }

  def seekingFinished(): Receive = {
    case _ => ;
  }
}
