import java.io.IOException
import java.net.URL

import akka.actor._
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

    ListMap(map.toSeq.sortWith(_._1 > _._1):_*)
  }

  def mergeServerMaps(map1: Map[Int, String], map2: Map[Int, String]): Map[Int, String] = {
    val tempMap1 = map1.map(t => (t._2, t._1))
    val tempMap2 = map2.map(t => (t._2, t._1))

    val merged = (tempMap1 ++ tempMap2).map {
      case(server, count) => server -> (count + tempMap1.getOrElse(server, 0))
    }.map(t => (t._2, t._1))

    ListMap(merged.toSeq.sortWith(_._1 > _._1):_*)
  }
}


class Seeker extends Actor {
  import Seeker._

  override def receive = init

  def init: Receive = {
    case Seek(url, depth) =>
      if (depth == 0)
        context.parent ! Result(Map[Int, String]())
      else {
        val links = getLinksFromPage(url)
        val servers = getServersFromLinks(links)
        val serversMap = countServers(servers)

        for (l <- links) println(l)
        //for (m <- serversMap) println(m)


        var i = 0

        for (link <- links) {
          Thread.sleep(100)
          i = i + 1
          val seeker = context.actorOf(Props[Seeker], "Seeker_" + url.hashCode + "_child:_" + i + "_link:_" + link.hashCode)
          seeker ! Seek (link, depth - 1)
        }

        //val seeker = context.actorOf(Props[Seeker], "Seeker_" + url.hashCode + "_child:_" + i + "_link:_" + links.head.hashCode)
        //seeker ! Seek (links.head, depth - 1)
        context.become(workFinished(serversMap))
      }
  }


  def workFinished(sMap: Map[Int, String]): Receive = {
    case Result(s) =>
      //context.parent ! Result(mergeServerMaps(s, serversMap))
      //if(context.parent != context.system.asInstanceOf[ExtendedActorSystem].guardian) {
        context.parent ! Result(mergeServerMaps(sMap, s))
      //}
      //else
        for (m <- sMap) println(m)
  }

}
