import utest._

import scala.meta._
import scala.meta.io._
import scala.meta.internal.semanticdb._

import Main._

object ApiDiffTests extends TestSuite{
  val tests = Tests{
    "diff" - {
      val oldApi = run(Classpath(AbsolutePath(fix.BuildInfo.oldApiClasspath.toPath)))
      val newApi = run(Classpath(AbsolutePath(fix.BuildInfo.newApiClasspath.toPath)))

      val obtained = diff(oldApi, newApi)
      val expected = "p.A#f3()."

      assert(obtained == expected)
    }
  }
}
