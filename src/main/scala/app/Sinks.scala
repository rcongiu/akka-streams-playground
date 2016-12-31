package app

import java.nio.file.Paths

import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink}
import akka.util.ByteString

import scala.concurrent.Future

/**
  * Created by nuvol on 12/30/2016.
  */
object Sinks {
  /**
    * creates a line sink to file.
    *
    * @param filename
    * @return
    */
  def lineSink(filename: String): Sink[String, Future[IOResult]] =
    Flow[String]
      .map(s => ByteString(s + "\n")) // ByteString is needed to wrap the string.
      // Bytettring brings efficient concat/substring/thread safety
      .toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)

}
