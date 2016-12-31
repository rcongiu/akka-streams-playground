package app

import java.nio.file.Paths

import akka.stream.scaladsl.FileIO

/**
  * Created by nuvol on 12/29/2016.
  */
object Sources {
  def namesSource = FileIO.fromPath(Paths.get("names.txt"))   // list is iterable and immutable. Seq is not immutable,
    // unless you use collection.immutable.seq
    .map( _.decodeString("UTF8") ).mapConcat( _.split("\n").toList )

  /* Example of sources - see page 420 of akka 2.4.16 pdf docs

// Create a source from an Iterable
Source(List(1, 2, 3))
// Create a source from a Future
Source.fromFuture(Future.successful("Hello Streams!"))
// Create a source from a single element
Source.single("only one element")
// an empty source
Source.empty
// Sink that folds over the stream and returns a Future
// of the final result as its materialized value
Sink.fold[Int, Int](0)(_ + _)
// Sink that returns a Future as its materialized value,
// containing the first element of the stream
Sink.head
// A Sink that consumes a stream without doing anything with the elements
Sink.ignore


   */

}
