/**
 * Created by Adam on 2015-01-28.
 */
import javax.xml.parsers.SAXParser

import org.xml.sax.InputSource

import scala.xml.parsing.NoBindingFactoryAdapter

class LinkParser extends NoBindingFactoryAdapter {

  override def loadXML(source: InputSource, _p: SAXParser) = {
    loadXML(source)
  }

  // zamiast parsera REGEX

  def loadXML(source : InputSource) = {
    import nu.validator.htmlparser.common.XmlViolationPolicy
    import nu.validator.htmlparser.sax.HtmlParser
    import nu.validator.htmlparser.{common, sax}

    val reader = new HtmlParser
    reader.setXmlPolicy(XmlViolationPolicy.ALLOW)
    reader.setContentHandler(this)
    reader.parse(source)


    rootElem
  }

  def getLinks(): List[String] = {
    val aElems = rootElem \\ "a"

    val links = aElems.toList
      .flatMap(node => node.attribute("href"))
      .filter(node => !node.isEmpty)
      .map(node => node.toString())

    links
  }
}
