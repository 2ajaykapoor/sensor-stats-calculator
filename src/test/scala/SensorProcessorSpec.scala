import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import model.{FileInfo, SensorData}
import monix.execution.Scheduler.Implicits.global

import scala.io.Source

class SensorProcessorSpec extends AnyFlatSpec with Matchers {
  val leaderFile_1 = "leader-1.csv"
  val canonicalPath: String = new java.io.File(".").getCanonicalPath
  val currentDirectory = s"$canonicalPath/src/test/scala"
  val fileInfo: FileInfo = FileInfo(s"$currentDirectory/$leaderFile_1")
  val numberOfRows_leaderFile_1: Int = Source.fromFile(fileInfo.name).getLines.size
  "SensorProcessor" should "parse valid sensor data lines" in {
    val observable = SensorProcessor.readSensorDataFromFile(fileInfo)
    val result = observable.toListL.runSyncUnsafe()
    result should not be empty
    result.head shouldBe SensorData("sensor1", Some(10))
  }

  it should "calculate number of measurements processed" in {
    val observable = SensorProcessor.readSensorDataFromFile(fileInfo)
    val result = observable.toListL.runSyncUnsafe()
    result should not be empty
    result.size should equal(numberOfRows_leaderFile_1 - 1) //Excluding Header
  }

  it should "handle parsing humidity as NaN gracefully" in {
    val fileInfo = FileInfo(s"$currentDirectory/$leaderFile_1")
    val observable = SensorProcessor.readSensorDataFromFile(fileInfo)
    val result = observable.toListL.runSyncUnsafe()
    result should not be empty
    result(1) shouldBe SensorData("sensor2", None)
  }

  it should "handle parsing humidity greater than 100 gracefully" in {
    val fileInfo = FileInfo(s"$currentDirectory/$leaderFile_1")
    val observable = SensorProcessor.readSensorDataFromFile(fileInfo)
    val result = observable.toListL.runSyncUnsafe()
    result should not be empty
    result(2) shouldBe SensorData("sensor3", None)
  }

  it should "handle parsing humidity less than 0 gracefully" in {
    val fileInfo = FileInfo(s"$currentDirectory/$leaderFile_1")
    val observable = SensorProcessor.readSensorDataFromFile(fileInfo)
    val result = observable.toListL.runSyncUnsafe()
    result should not be empty
    result(3) shouldBe SensorData("sensor4", None)
  }

  it should "calculate sensor stats correctly" in {
    val sensorDataList = List(
      SensorData("sensor1", Some(40)),
      SensorData("sensor1", Some(60)),
      SensorData("sensor2", Some(70))
    )

    val stats = SensorProcessor.calculateSensorStats(sensorDataList)
    stats should contain key "sensor1"
    stats("sensor1") shouldBe (40, 50.0, 60)
  }

  it should "sort by average humidity in descending order" in {
    val sensorDataMap = Map(
      "sensor1" -> (40, 50.0, 60),
      "sensor2" -> (70, 70.0, 70),
      "sensor3" -> (20, 30.0, 40)
    )

    val result = SensorProcessor.sortProcessedSensorData(sensorDataMap)
    result shouldEqual List(
      "sensor2" -> (70, 70.0, 70),
      "sensor1" -> (40, 50.0, 60),
      "sensor3" -> (20, 30.0, 40)
    )
  }

  it should "extract valid measurements" in {
    val sensorDataList = List(
      SensorData("sensor1", Some(40)),
      SensorData("sensor2", None),
      SensorData("sensor3", Some(60))
    )

    val result = SensorProcessor.extractValidMeasurements(sensorDataList)
    result shouldEqual List(
      SensorData("sensor1", Some(40)),
      SensorData("sensor3", Some(60))
    )
  }

  it should "extract NaN measurements" in {
    val sensorDataList = List(
      SensorData("sensor1", Some(40)),
      SensorData("sensor2", None),
      SensorData("sensor3", Some(60))
    )

    val result = SensorProcessor.extractNanMeasurements(sensorDataList)
    result shouldEqual List(
      SensorData("sensor2", None)
    )
  }

  it should "extract sensors with only NaN measurements" in {
    val nanMeasurements = List(
      SensorData("sensor1", None),
      SensorData("sensor2", None)
    )

    val validMeasurements = List(
      SensorData("sensor1", Some(40)),
      SensorData("sensor3", Some(60))
    )

    val result = SensorProcessor.extractSensorsWithOnlyNaNMeasurements(nanMeasurements, validMeasurements)
    result shouldEqual List(SensorData("sensor2", None))
  }
}
