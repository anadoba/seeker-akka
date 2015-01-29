import java.io.IOException
import java.net.URL

import akka.actor.{PoisonPill, Actor}
import akka.actor.Actor.Receive
import org.xml.sax.InputSource

import scala.collection.immutable.ListMap


/**
 * Created by Adam on 2015-01-23.
 */
object Seeker {
  sealed abstract class SeekerMsg
  case class Seek(url: String, depth: Int)
  case class Result(map: Map[Int, String])

  def getLinksFromPage(url: String): List[String] = {
    val parser = new LinkParser
    try  {
      parser.loadXML(new InputSource(url))
      val links = parser.getLinks()
        .filter(link => link.startsWith("http"))
        .map(link => link.replace("https", "http"))

      return links
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

    return ListMap(map.toSeq.sortWith(_._1 > _._1):_*)
  }

  def mergeServerMaps(map1: Map[Int, String], map2: Map[Int, String]): Map[Int, String] = {
    val tempMap1 = map1.map(t => (t._2, t._1))
    val tempMap2 = map2.map(t => (t._2, t._1))

    (tempMap1 ++ tempMap2).map {
      case(server, count) => server -> (count + tempMap1.getOrElse(server, 0))
    }.map(t => (t._2, t._1))
  }
}

class Seeker extends Actor {
  import Seeker._

  var servers: Map[Int, String] = Map[Int, String]()

  override def receive = init

  def init: Receive = {
    case Seek(url, depth) =>
      if (depth == 0)
        sender() ! Result(Map[Int, String]())
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
      context.parent ! Result(mergeServerMaps(s, servers))
  }

  def initializedToSeek(url: String, depth: Int): Receive = {
    case _ => ;
  }

  def seekingFinished(): Receive = {
    case _ => ;
  }
}
