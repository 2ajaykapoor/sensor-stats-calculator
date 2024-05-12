import model.{FileInfo, SensorData}
import monix.eval.Task
import monix.reactive.Observable

import java.nio.file.{Files, Paths}
import scala.util.{Failure, Success, Try}

object SensorProcessor {
  def readSensorDataFromFile(fileInfo: FileInfo): Observable[SensorData] =
    Observable.fromLinesReader(Task(Files.newBufferedReader(Paths.get(fileInfo.name))))
      .drop(1) // Skip header line
      .map(parseSensorDataLine)
      .onErrorHandle(_ => SensorData("", None)) // Handle parsing errors

  def parseSensorDataLine(line: String): SensorData = {
    val parts = line.split(",")
    //To check Humidity has valid data range
    SensorData(parts(0), Try(parts(1).toInt) match {
      case Success(num)  if 0 to 100 contains num => Some(num)
      case Success(_) => None
      case Failure(_) => None
    })
  }

  def calculateSensorStats(sensorDataList: List[SensorData]): Map[String, (Int, Double, Int)] =
    sensorDataList
      .groupBy(_.sensorId)
      .view.mapValues { dataList =>
        val humidityValues = dataList.flatMap(_.humidity)
        val min = humidityValues.min
        val avg = humidityValues.sum.toDouble / humidityValues.size
        val max = humidityValues.max
        (min, avg, max)
      }.toMap

  def sortProcessedSensorData(sensorDataMap : Map[String, (Int, Double, Int)]): List[(String, (Int, Double, Int))] = {
    // Implementing Sort by Desc
    implicit val ordered: Ordering[Double] = Ordering.Double.TotalOrdering.reverse
    sensorDataMap.toList.sortBy(_._2._2)
  }

  def extractValidMeasurements(sensorDataList: List[SensorData]): List[SensorData] = sensorDataList
    .filter(_.humidity.isDefined)

  def extractNanMeasurements(sensorDataList: List[SensorData]): List[SensorData] = sensorDataList
    .filterNot(_.humidity.isDefined)

  def extractSensorsWithOnlyNaNMeasurements(nanMeasurements: List[SensorData],
                                            validMeasurements: List[SensorData]): List[SensorData] =
    nanMeasurements.filterNot { sensorData =>
    validMeasurements.exists(_.sensorId == sensorData.sensorId)
  }
}
