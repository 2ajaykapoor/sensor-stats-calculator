import cats.effect.ExitCode
import monix.execution.Scheduler.Implicits.global
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class StartSpec extends AnyFlatSpec with Matchers {

  "Start.run" should "return ExitCode.Error when not provided exactly one argument" in {
    val args = List("arg1", "arg2")
    val result = Start.run(args).runSyncUnsafe()
    result shouldBe ExitCode.Error
  }

  it should "return ExitCode.Success when provided valid argument" in {
    val leaderFile_1 = "leader-1.csv"
    val canonicalPath: String = new java.io.File(".").getCanonicalPath
    val currentDirectory = s"$canonicalPath/src/test/scala"
    val args = List(currentDirectory)
    val result = Start.run(args).runSyncUnsafe()
    result shouldBe ExitCode.Success
  }
}
