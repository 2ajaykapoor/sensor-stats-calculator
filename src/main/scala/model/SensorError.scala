package model

sealed trait SensorError
case object ParsingError extends SensorError
