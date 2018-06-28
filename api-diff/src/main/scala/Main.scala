import scala.meta._
import scala.meta.io._
import scala.meta.internal.semanticdb._

import java.nio.file.Path

object Main {
  def main(args: Array[String]): Unit = {
    val coursier = new Coursier()
    def scala(version: String): Classpath = {
      Classpath(AbsolutePath(coursier.fetchJars("org.scala-lang", "scala-library", version).head.toPath))
    }

    val scala213 = scala("2.13.0-M4")
    val scala212 = scala("2.12.6")

    Locator(run(scala212))((path, doc) => println(path))
  }

  def run(sclasspath: Classpath): Path = {
    val settings = metacp.Settings()
      .withClasspath(sclasspath)
      .withPar(false)
    val reporter = scala.meta.cli
      .Reporter()
      .withOut(System.out)
      .withErr(System.out)
    scala.meta.cli.Metacp.process(settings, reporter).get.entries.head.toNIO
  }
}
