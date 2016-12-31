package app

import akka.Done
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, UniformFanInShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source, ZipWith}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by nuvol on 12/30/2016.
  */
object ZipInTest {
  def doZipIn()(implicit actorSystem:ActorSystem) = {

    implicit val materializer = ActorMaterializer()

    // can be simplified with val merged = Source.combine(sourceOne, sourceTwo)(Merge(_))

    // note, zipWith only takes 2 inputs, so we have to combine 2 of them
    val pickMaxOfThree = GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._
      val zip1 = b.add(ZipWith[Int, Int, Int](math.max _)) // Note , it's [In1, In2, Out]
      val zip2 = b.add(ZipWith[Int, Int, Int](math.max _)) // Note , it's [In1, In2, Out]
      zip1.out ~> zip2.in0
      UniformFanInShape(zip2.out, zip1.in0, zip1.in1, zip2.in1)
    }
    val resultSink = Sink.foreach(println)
    val g = RunnableGraph.fromGraph(GraphDSL.create(resultSink) { implicit b => sink =>
      import GraphDSL.Implicits._
      // importing the partial graph will return its shape (inlets & outlets)
      val pm3 = b.add(pickMaxOfThree)
      Source.single(3) ~> pm3.in(0)
      Source.single(4) ~> pm3.in(1)
      Source.single(1) ~> pm3.in(2)
      pm3.out ~> sink.in
      ClosedShape
    })

    g.run()
  }
}