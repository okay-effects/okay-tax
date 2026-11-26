package okay.tax.app

import okay.business.faces.Skin
import okay.business.laminar.Laminar
import okay.business.model.Ui
import okay.scala3.*
import okay.scala3.std.*
import org.scalajs.dom
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

/** the calculator in the page: ?lang=pl|en|ru|uk, else the browser's language, else Polish */
object InTheBrowser:
  type Browser[X] = (Async + Ui)[X]
  val skin: Skin = Skin.calm(accent = "#1f5fa8", deep = "#123a66")

  def main(args: Array[String]): Unit =
    val asked = """lang=([a-z]{2})""".r.findFirstMatchIn(dom.window.location.search).map(_.group(1))
    val words = Words.of(asked.getOrElse(dom.window.navigator.language))
    val root = Option(dom.document.getElementById("app")).getOrElse(dom.document.body)
    val inLang = skin.copy(look = skin.look.copy(words = okay.faces.Words.of(words.lang)))
    val running = Async.start(Laminar.run(root, inLang)(App.main[Browser](words)))
    running.future.failed.foreach(e => dom.console.error(e.toString))
