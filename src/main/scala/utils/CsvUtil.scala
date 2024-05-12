package utils

import model.FileInfo

import java.io.File

object CsvUtil {
  def listFiles(directoryPath: String): Iterable[FileInfo] = {
      val directory = new File(directoryPath)
      if (directory.exists() && directory.isDirectory) {
        val files = directory.listFiles()
        files.map(file => FileInfo(s"$directoryPath/${file.getName}"))
      } else {
        throw new RuntimeException("Invalid directory path or directory does not exist.")
      }
  }
}
