package app

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString
import akka.{Done, NotUsed}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by nuvol on 11/21/2016.
  */
object Main {
  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()


  def main(args: Array[String]): Unit = {
    println("Starting...")


    val source: Source[Int, NotUsed] = Source(1 to 1000)

    // prints all the numbers in the source
    // source.runForeach(i => println(i))(materializer)

    try {
      Await.result(Future.sequence(
        Seq(
          //   doFibonacci(),
          //   doFactorial(),
          //   doOther(),
          //   doBroadcast(),
          // doCount(),
          // doSinkAndMat()
          //  CreateCustomStage.doDigestCalculation(),
          // MergeTest.doMerge(),
          //  ZipInTest.doZipIn(),
          PriorityWorkerPool.doPriorityPool()
        )), 10 seconds)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    } finally {
      println("Terminating actor system")
      system.terminate()
    }
  }

  /**
    * Calculates factorials, save to a file.
    */
  def doFactorial(): Future[Done] = {

    val source: Source[Int, NotUsed] = Source(1 to 1000)
    val factorials: Source[BigInt, NotUsed] = source.scan(BigInt(1))((acc, next) => acc * next)

    val result: Future[IOResult] =
      factorials
        .map(_.toString)
        .runWith(Sinks.lineSink("factorials.txt"))

    factorials
      .zipWith(Source(0 to 100))((num, idx) => s"$idx! = $num")
      .throttle(1, 0.01.second, 10, ThrottleMode.shaping)
      .runForeach(println)
  }

  /**
    * Calculates Fibonacci series, saves it to a file
    */
  def doFibonacci(): Future[IOResult] = {
    val source: Source[Int, NotUsed] = Source(1 to 1000)
    val fibonacci: Source[(BigInt, BigInt), NotUsed] = source.scan((BigInt(1), BigInt(1)))((acc, next) => (acc._2, acc._1 + acc._2))

    fibonacci
      .map(_._1.toString)
      .runWith(Sinks.lineSink("fibonacci.txt"))
  }


  /**
    * Example of GraphDSL with broadcast
    *
    * @return
    */
  def doBroadcast() = {
    val source: Source[Int, NotUsed] = Source(1 to 1000)

    val fibonacci =
      Flow[Int].scan((BigInt(1), BigInt(1)))((acc, next) => (acc._2, acc._1 + acc._2))
        .map(_._1.toString)
    val factorials =
      Flow[Int].scan(BigInt(1))((acc, next) => acc * next)
        .map(_.toString)


    val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._
      //import akka.stream.scaladsl.GraphDSL.Implicits._

      val bcast = builder.add(Broadcast[Int](2))

      source ~> bcast.in

      bcast.out(0) ~> fibonacci ~> Sinks.lineSink("bcast0.txt")
      bcast.out(1) ~> factorials ~> Sinks.lineSink("bcast1.txt")

      ClosedShape
    })

    g.run()
    Future {
      Done
    }
  }

  def doOther() = {
    val source = FileIO.fromPath(Paths.get("names.txt")) // list is iterable and immutable. Seq is not immutable,
      // unless you use collection.immutable.seq
      .map(_.decodeString("UTF8")).mapConcat(_.split("\n").toList)

    val respons: (String) => String = (s: String) =>
      if (s.toLowerCase.contains("robert")) {
        s"You are pretty cool, ${s}\n"
      } else {
        s"You suck, ${s}\n"
      }

    val f = Flow[String].map(s => ByteString(respons(s)))
      .toMat(FileIO.toPath(Paths.get("pippo.txt")))(Keep.right)

    source.runWith(f)
  }

  /**
    * example of how to use a materializer to count up-to-now number of events
    */
  def doCount() = {
    val count = Flow[String].map(_ => 1)
    val sumSink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0) {
      _ + _
    }
    val source = Sources.namesSource

    // : RunnableGraph[Future[Int]]
    val counterGraph =
      source
        .via(count)
        .toMat(sumSink)(Keep.right)


    val sum = counterGraph.run()
    sum.map(c => println(s"Total tweet processed: $c"))
  }


  def doSinkAndMat() = {
    val source = Sources.namesSource
    val lsnk = Sinks.lineSink("concat.txt")
    val concatSink = Flow[String]
      .fold[String]("Concat:") {
      "," + _ + _
    }
      .to(lsnk)
    val mainSink = Sinks.lineSink("mysink.txt")
    source.scan[String]("sourcefold: ") {
      _ + _
    }.toMat(mainSink)(Keep.right).run()
  }

  def doGroupBy() = {
    val source = Sources.namesSource

    //val counts: Source[(String, Int), NotUsed] = source.runWith(Sink.ignore)
    // split the words into separate streams first
    //   .groupBy(, identity)
    //transform each element to pair with number of words in it
    //   .map(_ -> 1)
    // add counting logic to the streams
    //   .reduce((l, r) => (l._1, l._2 + r._2))
    // get a stream of word counts
    //    .mergeSubstreams
    Future {
      Done
    }

  }


}
