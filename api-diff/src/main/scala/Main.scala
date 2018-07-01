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

    def removeParallelCollections(api: List[SymbolInformation]): List[SymbolInformation] =
      api.filter(!_.symbol.startsWith("scala.collection.parallel."))

    val scala213 = removeParallelCollections(run(scala("2.13.0-M4")))
    val scala212 = removeParallelCollections(run(scala("2.12.6")))
    val out = diff(scala212, scala213)

    Files.write(Paths.get("out"), out.getBytes())
  }

  val reporter = scala.meta.cli
      .Reporter()
      .withOut(System.out)
      .withErr(System.out)

  def run(sclasspath: Classpath): List[SymbolInformation] = {
    val settings = metacp.Settings()
      .withClasspath(sclasspath)
      .withPar(false)

    val dbFiles = scala.meta.cli.Metacp.process(settings, reporter).get.entries.head.toNIO

    val docsB = List.newBuilder[TextDocument]
    Locator(dbFiles)((path, docs) =>
      docsB ++= docs.documents
    )
    val out = docsB.result()
    out.flatMap(_.symbols)
  }

  def diff(oldApi: List[SymbolInformation], newApi: List[SymbolInformation]): String = {
    def symbolsMap(api: List[SymbolInformation]): Map[String, SymbolInformation] =
      api.groupBy(_.symbol).mapValues(_.head).toMap

    val oldSymbols = symbolsMap(oldApi) 
    val newSymbols = symbolsMap(newApi)

    def topLevel(si: SymbolInformation): Boolean = {
      import SymbolInformation.Kind._
      
      si.kind match {
        case OBJECT | PACKAGE | PACKAGE_OBJECT | CLASS | TRAIT | INTERFACE => true
        case _ => false
      }
    }

    def deprecated(s: SymbolInformation): Boolean = 
      s.annotations.exists(_.tpe match {
        case TypeRef(_ , s, _) => s == "scala.deprecated#"
        case _ => false
      })

    val deprecatedSymbols = 
      oldApi
        .filter(si => topLevel(si) && deprecated(si))
        .map(_.symbol)


    val removed = (oldSymbols.keySet -- newSymbols.keySet).toList.map(s => oldSymbols(s))

    removed
      .filter(si => !deprecated(si) && 
                    !deprecatedSymbols.exists(ds => si.symbol.startsWith(ds)))
      .map(_.symbol)
      .sorted
      .mkString(System.lineSeparator)
  }
}
