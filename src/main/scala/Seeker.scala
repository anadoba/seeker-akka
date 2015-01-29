import java.io.IOException
import java.net.URL

import akka.actor._
import akka.actor.Actor.Receive
import akka.util.Timeout
import org.xml.sax.InputSource

import scala.util.control.NonFatal
import scala.util.{Success, Failure}
import scala.collection.immutable.ListMap
import akka.pattern.ask

import scala.concurrent.duration._

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
    val map = servers.groupBy(server => server).map(temp => (temp._1, temp._2.length))

    ListMap(map.toSeq.sortWith(_._2 > _._2):_*)
  }

  def mergeServerMaps(map1: Map[String, Int], map2: Map[String, Int]): Map[String, Int] = {
    val mergedKeyMaps = map1.keySet ++ map2.keySet
    ListMap(mergedKeyMaps.map(key =>
      (key, map1.getOrElse(key, 0) + map2.getOrElse(key, 0))
    ).toMap.toSeq.sortWith(_._2 > _._2):_*)
  }
}


class Seeker extends Actor {
  import Seeker._
  import Messages._

  var mainServersMap = Map[String, Int]()

  var answersCounter = 0

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
          seeker ! Seek(link, depth -1)
        }
        context.become(workFinished(serversMap, links.length))

      }
    case _ => ;
  }

  def workFinished(sMap: Map[String, Int], answersToReceive: Int): Receive = {
    case Result(s) =>
      if (answersCounter == 0)
        mainServersMap = sMap

      mainServersMap = mergeServerMaps(sMap, s)
      answersCounter = answersCounter + 1
      //println(self.path.name + " " + answersCounter)

      if (answersCounter == answersToReceive) {
        println("XXXXXXXXXXXXXXXXXXXXX")
        context.parent ! Result(mainServersMap)
        println(mainServersMap)
      }
  }
}
