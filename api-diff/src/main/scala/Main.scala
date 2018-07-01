import scala.meta._
import scala.meta.io._
import scala.meta.internal.semanticdb._
import scala.meta.internal.metap.DocumentPrinter

import java.nio.file.{Paths, Files}

object Main {
  def main(args: Array[String]): Unit = {
    val coursier = new Coursier()
    def scala(version: String): Classpath =
      Classpath(AbsolutePath(coursier.fetchJars("org.scala-lang", "scala-library", version).head.toPath))

    val scala213 = run(scala("2.13.0-M4"))
    val scala212 = run(scala("2.12.6"))
    val out = diff(scala212, scala213)

    Files.write(Paths.get("out"), out.getBytes())
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

  def diff(oldApi: List[TextDocument], newApi: List[TextDocument]): String = {
    def symbolsMap(api: List[TextDocument]): Map[String, SymbolInformation] =
      api.flatMap(_.symbols).groupBy(_.symbol).mapValues(_.head).toMap


    val oldSymbols = symbolsMap(oldApi) 
    val newSymbols = symbolsMap(newApi)
    
    def notDeprecated(s: SymbolInformation): Boolean = 
      s.annotations.forall(_.tpe match {
        case TypeRef(_ , s, _) => s != "scala.deprecated#"
        case _ => true
      })

    val removed = (oldSymbols.keySet -- newSymbols.keySet).toList.map(s => oldSymbols(s))

    removed.filter(notDeprecated).map(_.symbol).sorted.mkString(System.lineSeparator)
  }
}
