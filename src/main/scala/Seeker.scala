import java.io.IOException
import java.net.URL

import akka.actor._

import org.xml.sax.InputSource

import scala.collection.immutable.ListMap

/**
 * Created by Adam on 2015-01-23.
 */
object Seeker {

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

  def countServers(servers: List[String]): Map[String, Int] = {
    //val map = servers.groupBy(server => server).map(temp => (temp._1, temp._2.length))
    val map = servers.groupBy(identity).map(temp => (temp._1, temp._2.length))

    ListMap(map.toSeq.sortWith(_._2 > _._2):_*)
  }

  def mergeServerMaps(map1: Map[String, Int], map2: Map[String, Int]): Map[String, Int] = {
    val merged = (map1.keys ++ map2.keys).map(key => key -> (map1.getOrElse(key, 0) + map2.getOrElse(key, 0))).toMap
    ListMap(merged.toSeq.sortWith(_._2 > _._2):_*)
  }
}


class Seeker extends Actor {
  import Seeker._
  import Messages._

  override def receive = init

  def init: Receive = {
    case Seek(url, depth) =>
      if (depth == 0) {
        sender() ! Result(Map[String, Int]())
      }
      else {
        val links = getLinksFromPage(url)
        val servers = getServersFromLinks(links)
        val serversMap = countServers(servers)

        for (link <- links) {
          val seeker = context.actorOf(Props[Seeker])
          seeker ! Seek(link, depth - 1)
        }

        if (links.length != 0)
          context.become(waitingForResults(serversMap, links.length - 1))
        else
          sender() ! Result(Map[String, Int]())
      }
  }

  def waitingForResults(sMap: Map[String, Int], answersToReceive: Int): Receive = {
    case Result(s) =>
      if (answersToReceive == 0) {
        context.parent ! Result(mergeServerMaps(sMap, s))
      }

      context.become(waitingForResults(mergeServerMaps(sMap, s), answersToReceive - 1))
  }
}
