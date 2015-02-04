
import java.io.IOException
import java.net.URL

import org.xml.sax.InputSource

import scala.util.Success

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import akka.pattern.ask
import spray.http.HttpResponse
import spray.http.HttpEntity
import spray.can.Http
import akka.io.IO

import scala.collection.immutable.ListMap
import scala.concurrent.Future

import akka.actor._

import scala.concurrent.{Await, Future}

import scala.io.Source

import spray.http._

/**
 * Created by Adam on 2015-01-23.
 */
object Seeker {
  implicit val system = ActorSystem()
  import system.dispatcher // execution context for futures
  import spray.http._
  import spray.client.pipelining._

  def getLinksFromPage(url: String): List[String] = {


    val parser = new LinkParser
    try  {
      parser.loadXML(new InputSource(url))
      val links = parser.getLinks()
        .filter(link => link.startsWith("http"))
        .map(link => link.replace("https", "http")).map(link => link.replace(" ", ""))

      links
    } catch {
      case e: IOException =>
        List[String]()
    }

    /*

    WERSJA OPARTA NA KLIENCIE SPRAY.io

    //val regex = "\\b(([\\w-]+://?|www[.])[^\\s()<>]+(?:\\([\\w\\d]+\\)|([^[:punct:]\\s]|/)))".r
    val regex = "<a href=\"([^\"]*)\"".r

    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    val responseFuture: Future[HttpResponse] = pipeline(Get(url))

    var plainHtml: String = ""

    responseFuture.onComplete{
      case Success(response) => plainHtml = response.entity.asString

      case _ => println("Stalo sie cos zlego")
    }

    val listOfLinks = regex.findAllIn(plainHtml)

    val result = listOfLinks.toList

    //for (link <- result) link.replace("<a href=\"", "")
    //for (link <- result) println(link)

    result

    */
  }

  def getServersFromLinks(links: List[String]): List[String] = {
    for (link <- links) yield new URL(link).getHost

    // uzyc spray, aby pobrac hosta
    //for (link <- links) yield Uri.apply(link).authority.host.toString
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
