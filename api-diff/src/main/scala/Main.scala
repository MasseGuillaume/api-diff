import scala.meta._
import scala.meta.io._
import scala.meta.internal.semanticdb._
import scala.meta.internal.metap.DocumentPrinter


import java.nio.file.Path

object Main {
  def main(args: Array[String]): Unit = {
    val coursier = new Coursier()
    def scala(version: String): Classpath =
      Classpath(AbsolutePath(coursier.fetchJars("org.scala-lang", "scala-library", version).head.toPath))

    val scala213 = run(scala("2.13.0-M4"))
    // val scala212 = run(scala("2.12.6"))
    diff(Nil, scala213)
    // diff(scala212, scala213)
  }

  val reporter = scala.meta.cli
      .Reporter()
      .withOut(System.out)
      .withErr(System.out)

  def run(sclasspath: Classpath): List[TextDocument] = {
    val settings = metacp.Settings()
      .withClasspath(sclasspath)
      .withPar(false)

    val dbFiles = scala.meta.cli.Metacp.process(settings, reporter).get.entries.head.toNIO

    val docsB = List.newBuilder[TextDocument]
    Locator(dbFiles)((path, docs) =>
      docsB ++= docs.documents
    )
    docsB.result()
  }

  def diff(oldApi: List[TextDocument], newApi: List[TextDocument]): Unit = {
    val document = newApi.find(_.uri == "scala/collection/immutable/List.class").get
    val printer = new DocumentPrinter(metap.Settings(), reporter, document)
    printer.print()
  }
}
