package app

import java.security.MessageDigest

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString

import scala.language.postfixOps
import scala.language.implicitConversions
/**
  * Created by nuvol on 12/30/2016.
  */
object CreateCustomStage {

  implicit def byteStringToString(bs:ByteString):String = bs.decodeString("UTF-8")

  def doDigestCalculation()(implicit actorSystem: ActorSystem) = {
    implicit val materializer = ActorMaterializer()

    val source = Sources.namesSource.map( ByteString(_) )
    val digest = source.via(new DigestCalculator("SHA-256")).map(_.decodeString("UTF-8")).
      toMat(Sinks.lineSink("digest.txt"))(Keep.left)


    val m = source.runForeach( x => println(byteStringToString(x)))

    digest.run()
   // digest.runForeach(println)

  }
}


import akka.stream.stage._
class DigestCalculator(algorithm: String) extends GraphStage[FlowShape[ByteString, ByteString]] {
  val in = Inlet[ByteString]("DigestCalculator.in")
  val out = Outlet[ByteString]("DigestCalculator.out")
  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    val digest = MessageDigest.getInstance(algorithm)

    setHandler(out, new OutHandler {
      override def onPull() = {
        pull(in)
      }
    })

    setHandler(in, new InHandler {
      override def onPush() = {
        val chunk = grab(in)
        digest.update(chunk.toArray)
        pull(in)
      }

      override def onUpstreamFinish(): Unit = {
        emit(out, ByteString(digest.digest()))
        completeStage()
      }
    })
  }
}

