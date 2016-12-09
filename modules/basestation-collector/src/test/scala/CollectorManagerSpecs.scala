import akka.Done
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.darienmt.airplaneadventures.basestation.collector.actors.{Collector, CollectorManager}
import com.darienmt.airplaneadventures.basestation.collector.actors.CollectorManager.{RetryConfig, UnknownMessage}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import TestUtils._

import scala.concurrent.duration._

class CollectorManagerSpecs extends TestKit(ActorSystem("CollectorSpecs"))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  import system.dispatcher

  override def afterAll(): Unit = {
    shutdown()
  }

  def doingNothingActorProps(): Props = Props(new Actor() {
    def receive = {
      case msg => sender() ! msg
    }
  })

  def getActor(
                collectorProps: Props = doingNothingActorProps(),
                generator: Collector.Generator = generatorWithDelay(),
                terminator: Option[CollectorManager.Terminator] = None
              ): ActorRef = {
    val collectorPropsGenerator: CollectorManager.CollectorProps = (_,_) => collectorProps
    val streamGenerator: CollectorManager.StreamGenerator = (_,_) => generator()
    val retryConfig = RetryConfig(1 second, 100 millisecond, 50 millisecond, 0)
    val props = if (terminator.isEmpty) {
      CollectorManager.props(collectorPropsGenerator, streamGenerator, retryConfig)
    } else {
      CollectorManager.props(collectorPropsGenerator, streamGenerator, retryConfig, terminator.get)
    }
    system.actorOf(props)
  }

  "the collector manager" should {

    "respond to an unknown message by sending it the sender" in {
      val actor = getActor()
      actor ! "Test"
      expectMsgType[UnknownMessage]
    }



  }
}
