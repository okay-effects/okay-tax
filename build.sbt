/** okay-tax: take-home pay and taxes in Poland, computed from the statutes themselves (specs/tax.md). Scala 3; the
 * engine on the JVM and in the browser, the page on okay-ui. A calculator, not tax advice */
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

ThisBuild / organization := "io.github.okay-effects"
ThisBuild / scalaVersion := "3.3.8"
ThisBuild / version := "0.1.0"
ThisBuild / scalacOptions ++= Seq("-deprecation", "-feature", "-Wunused:all", "-Werror")
ThisBuild / homepage := Some(url("https://github.com/okay-effects/okay-tax"))
ThisBuild / licenses := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / versionScheme := Some("early-semver")
/** RELEASES, ON GITHUB PAGES: `sbt publish` writes okay-tax-core (JVM and Scala.js) as a Maven repository into
 * `target/pages-maven/`; it goes to the gh-pages branch under `maven/` (RELEASING.md) */
val pagesRepository = settingKey[File]("the Maven repository a release is written to, for GitHub Pages")
ThisBuild / pagesRepository := (ThisBuild / baseDirectory).value / "target" / "pages-maven"
ThisBuild / publishTo := Some(MavenCache("pages", (ThisBuild / pagesRepository).value))
ThisBuild / Compile / packageDoc / publishArtifact := false
ThisBuild / resolvers ++= Seq("okay-scala" at "https://okay-effects.github.io/okay-scala/maven",
  "okay-simple" at "https://okay-effects.github.io/okay-simple/maven",
  "okay-faces" at "https://okay-effects.github.io/okay-faces/maven",
  "okay-http" at "https://okay-effects.github.io/okay-http/maven",
  "okay-ui" at "https://okay-effects.github.io/okay-ui/maven")

val munit = Def.setting("org.scalameta" %%% "munit" % "1.2.3" % Test)

/** THE ENGINE: the theory of a year as data (every value with its quote from the statute), the calculations, the
 * laws (the books' worked examples). No dependency: any page or program may take it */
lazy val core = crossProject(JVMPlatform, JSPlatform).crossType(CrossType.Pure).in(file("core"))
  .settings(name := "okay-tax-core", libraryDependencies += munit.value,
    Test / fork := false)
  .jvmSettings(Test / baseDirectory := (ThisBuild / baseDirectory).value)

/** THE PAGE: the calculator on okay-ui's screens, in the browser */
lazy val app = project.in(file("app")).enablePlugins(ScalaJSPlugin).dependsOn(core.js)
  .settings(name := "okay-tax-app", publish / skip := true,
    libraryDependencies ++= Seq("io.github.okay-effects" %%% "okay-ui" % "0.1.0", munit.value),
    scalaJSUseMainModuleInitializer := true, Compile / mainClass := Some("okay.tax.app.InTheBrowser"))

/** `sbt web`: the page in app/target/web — index.html and main.js (optimised) */
val web = taskKey[File]("the page in app/target/web")
/** `sbt publishSite`: the page built from a clean tree and pushed to this repository's gh-pages branch */
val publishSite = taskKey[Unit]("build the page and push it to gh-pages")

lazy val root = (project in file(".")).aggregate(core.jvm, core.js, app)
  .settings(publish / skip := true,
    web := {
      val linked = (app / Compile / fullLinkJS).value.data.publicModules.head.jsFileName
      val from = (app / Compile / fullLinkJS / scalaJSLinkerOutputDirectory).value / linked
      val out = (app / target).value / "web"
      IO.delete(out)
      IO.copyDirectory((ThisBuild / baseDirectory).value / "app" / "web", out)
      // the script named by its content: a page reloaded after a publish never runs an old one
      val hash = Hash.toHex(Hash(from)).take(12)
      IO.copyFile(from, out / s"main-$hash.js")
      IO.write(out / "index.html", IO.read(out / "index.html").replace("main.js", s"main-$hash.js"))
      out
    },
    publishSite := {
      import scala.sys.process._
      val base = (ThisBuild / baseDirectory).value
      if (Process(Seq("git", "status", "--porcelain"), base).!!.trim.nonEmpty)
        sys.error("publishSite: commit first — the page is built from what is committed")
      val page = web.value
      val sha = Process(Seq("git", "rev-parse", "--short", "HEAD"), base).!!.trim
      val repo = target.value / "gh-pages"
      def git(args: String*): Unit = if (Process("git" +: args, repo).! != 0) sys.error(s"git ${args.mkString(" ")}")
      IO.delete(repo)
      IO.createDirectory(repo)
      val origin = Process(Seq("git", "remote", "get-url", "origin"), base).!!.trim
      if (Process(Seq("git", "clone", "-q", "--depth", "1", "--branch", "gh-pages", origin, repo.getPath)).! != 0) {
        IO.delete(repo); IO.createDirectory(repo)
        git("init", "-q"); git("checkout", "-q", "--orphan", "gh-pages"); git("remote", "add", "origin", origin)
      }
      IO.listFiles(repo).filterNot(_.getName == ".git").filterNot(_.getName == "maven").foreach(IO.delete)
      IO.copyDirectory(page, repo)
      IO.touch(repo / ".nojekyll")
      git("add", "-A")
      if (Process(Seq("git", "diff", "--cached", "--quiet"), repo).! != 0) {
        git("commit", "-q", "-m", s"the page at $sha"); git("push", "-q", "origin", "gh-pages")
      }
      streams.value.log.info("published: https://okay-effects.github.io/okay-tax/")
    })
