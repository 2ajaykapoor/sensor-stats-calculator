import SensorProcessor.{calculateSensorStats, extractNanMeasurements, extractSensorsWithOnlyNaNMeasurements, extractValidMeasurements, sortProcessedSensorData}
import cats.effect.ExitCode
import monix.eval.{Task, TaskApp}
import monix.reactive.Observable
import utils.CsvUtil

object Start extends TaskApp {
  override def run(args: List[String]): Task[ExitCode] = {
    if (args.length != 1) {
      println(s"Required one argument only.")
      Task(ExitCode.Error)
    } else {
      val csvFiles = CsvUtil.listFiles(args.head)

      val sensorDataObservable = Observable.fromIterable(csvFiles)
        .flatMap(SensorProcessor.readSensorDataFromFile)
        .toListL

      val resultTask = sensorDataObservable.flatMap { sensorDataList =>
        val totalFiles = csvFiles.size
        val totalMeasurementsCount = sensorDataList.size
        val validMeasurements = extractValidMeasurements(sensorDataList)
        val nanMeasurements = extractNanMeasurements(sensorDataList)
        val succeedMeasurementsCount = validMeasurements.size
        val failedMeasurementsCount = totalMeasurementsCount - succeedMeasurementsCount

        println(s"Processed $totalFiles files")
        println(s"Processed $totalMeasurementsCount measurements")
        println(s"Succeeded measurements: $succeedMeasurementsCount")
        println(s"Failed measurements: $failedMeasurementsCount")

        val sensorStats = sortProcessedSensorData(calculateSensorStats(validMeasurements))
        val sensorsWithOnlyNaNMeasurements = extractSensorsWithOnlyNaNMeasurements(nanMeasurements, validMeasurements)

        // Print sensorStats
        sensorStats.foreach { case (sensorId, (min, avg, max)) =>
          println(s"Sensor $sensorId ||  Min=$min, Avg=$avg, Max=$max")
        }

        // Print sensorsWithOnlyNaNMeasurements
        sensorsWithOnlyNaNMeasurements.foreach { sensorData =>
          println(s"Sensor ${sensorData.sensorId} ||  Min=NaN, Avg=NaN, Max=NaN")
        }
        Task(ExitCode.Success)
      }

      resultTask.onErrorHandle { throwable =>
        println(s"Error occurred: ${throwable.getMessage}")
        ExitCode.Error
      }
    }
  }
}
