import SensorProcessor.calculateSensorStats
import cats.effect.ExitCode
import monix.eval.{Task, TaskApp}
import monix.reactive.Observable
import utils.CsvUtil
import monix.execution.Scheduler.Implicits.global


object Start extends TaskApp {


  override def run(args: List[String]): Task[ExitCode] = {
    if (args.length != 1) {
      println("Required one argument only.")
      Task(ExitCode.Error)
    }
    val csvFiles = CsvUtil.listFiles(args.head)

    val sensorDataObservable = Observable.fromIterable(csvFiles)
      .flatMap(SensorProcessor.readSensorDataFromFile)
      .toListL

    sensorDataObservable.runToFuture.foreach { sensorDataList =>
      val totalFiles = csvFiles.size
      val totalMeasurementsCount = sensorDataList.size
      val validMeasurements = sensorDataList.filter(_.humidity.isDefined)
      val nanMeasurements = sensorDataList.filterNot(_.humidity.isDefined)
      val sensorsWithOnlyNaNMeasurements = nanMeasurements.filterNot { sensorData =>
        validMeasurements.exists(_.sensorId == sensorData.sensorId)
      }
      val succeedMeasurementsCount = validMeasurements.size
      val failedMeasurementsCount = totalMeasurementsCount - succeedMeasurementsCount

      println(s"Processed $totalFiles files")
      println(s"Processed $totalMeasurementsCount measurements")
      println(s"Succeed measurements: $succeedMeasurementsCount")
      println(s"Failed measurements: $failedMeasurementsCount")
      //DSC Sort Implementation
      implicit val ordered: Ordering[Double] = Ordering.Double.TotalOrdering.reverse
      val sensorStats = calculateSensorStats(validMeasurements).toList.sortBy(_._2._2)
      sensorStats.foreach { case (sensorId, (min, avg, max)) =>
        println(s"Sensor $sensorId ||  Min=$min, Avg=$avg, Max=$max")
      }
      sensorsWithOnlyNaNMeasurements.map { sensorData =>
        println(s"Sensor ${sensorData.sensorId} ||  Min=NaN, Avg=NaN, Max=NaN")
      }
    }
    Task(ExitCode.Success)
  }
}
