package utils

import model.FileInfo
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File

class CsvUtilSpec extends AnyFlatSpec with Matchers {

  val canonicalPath: String = new java.io.File(".").getCanonicalPath
  val directoryPath = s"$canonicalPath/src/test/scala"
  val numberOfFilesExist: Int = new File(directoryPath).listFiles().count(f => f.getName.endsWith(".csv"))

  "CsvUtil.listFiles" should "return a list of FileInfo objects for valid directory path" in {
    val files: Iterable[FileInfo] = CsvUtil.listFiles(directoryPath)
    files should not be empty
    files.foreach { fileInfo =>
      fileInfo.name should startWith(directoryPath)
    }
  }

  it should "return count of files exists in valid directory path" in {
    val files: Iterable[FileInfo] = CsvUtil.listFiles(directoryPath)
    files should not be empty
    files.size should be (numberOfFilesExist)
  }

  it should "throw an exception for invalid directory path" in {
    val invalidPath = "/nonexistent/directory"
    an[RuntimeException] should be thrownBy {
      CsvUtil.listFiles(invalidPath)
    }
  }
}
