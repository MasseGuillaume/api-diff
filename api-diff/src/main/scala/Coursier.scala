import coursier._
import coursier.MavenRepository
import coursier.util.Gather
import coursier.util.Task

import java.io.File
import java.io.OutputStreamWriter

import scala.concurrent.ExecutionContext.Implicits.global

class Coursier() {
  def fetchJars(org: String, artifact: String, version: String): List[File] = this.synchronized {
    val toResolve = Resolution(Set(Dependency(Module(org, artifact), version)))
    val MavenCentral: MavenRepository = MavenRepository("https://repo1.maven.org/maven2")
    val repositories: List[Repository] = List(Cache.ivy2Local, MavenCentral)
    val term = new TermDisplay(new OutputStreamWriter(System.err), true)
    term.init()
    val fetch = Fetch.from(repositories, Cache.fetch[Task](logger = Some(term)))
    val resolution = toResolve.process.run(fetch).unsafeRun()
    val errors = resolution.errors
    if (errors.nonEmpty) {
      sys.error(errors.mkString(System.lineSeparator))
    }
    val localArtifacts = Gather[Task].gather(
      resolution.artifacts.map(artifact => Cache.file[Task](artifact).run)
    ).unsafeRun()

    val jars = localArtifacts.flatMap {
      case Left(e) =>
        throw new IllegalArgumentException(e.describe)
      case Right(jar) if jar.getName.endsWith(".jar") =>
        jar :: Nil
      case _ =>
        Nil
    }
    term.stop()
    jars.toList
  }
}
