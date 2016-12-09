import akka.Done
import akka.actor.Actor.Receive
import akka.actor.Status.Failure
import akka.actor.{ Actor, ActorRef, ActorSystem, Props, SupervisorStrategy, Terminated }
import akka.testkit.{ DefaultTimeout, ImplicitSender, TestKit }
import com.darienmt.airplaneadventures.basestation.collector.actors.Collector
import com.darienmt.airplaneadventures.basestation.collector.actors.Collector.{ StreamFinished, Tick, UnknownMessage }
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

import scala.concurrent.Future
import scala.concurrent.duration._

class CollectorSpecs extends TestKit(ActorSystem("CollectorSpecs"))
    with DefaultTimeout with ImplicitSender
    with WordSpecLike with Matchers with BeforeAndAfterAll {

  import system.dispatcher

  override def afterAll(): Unit = {
    shutdown()
  }

  def generatorWithDelay(delay: Int = 10000): Collector.Generator = () => Future {
    Thread.sleep(delay)
    Done
  }

  def getActor(
    generator: Collector.Generator = generatorWithDelay(),
    heartbeatInterval: FiniteDuration = 1 minutes
  ): ActorRef = {
    val supervisor = system.actorOf(Props(new Actor {
      override def supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy
      override def receive: Receive = {
        case p: Props => sender ! context.actorOf(p)
      }
    }))

    supervisor ! Collector.props(testActor, generator, heartbeatInterval)

    expectMsgType[ActorRef]
  }

  "the collector actor" should {

    "respond to an unknown message by sending it to his manager" in {
      val actor = getActor()
      actor ! "Test"
      expectMsgType[UnknownMessage]
    }

    "stop when it receives a Failure" in {
      val actor = getActor()
      watch(actor)
      actor ! Failure(new Exception())
      expectMsgType[Terminated]
    }

    "send StreamFinished when it receives a Done" in {
      val actor = getActor()
      actor ! Done
      expectMsg(StreamFinished)
    }

    "send a Tick on every heartbeat" in {
      val actor = getActor(heartbeatInterval = 1 second)
      expectMsg(Tick)
    }

    "send StreamFinished, when the generator sends Done" in {
      val actor = getActor(generator = generatorWithDelay(500))
      expectMsg(StreamFinished)
    }

    "stop, when the generator sends Failure" in {
      val failingGenerator: Collector.Generator = () => {
        Thread.sleep(500)
        throw new Exception()
      }
      val actor = getActor(generator = failingGenerator)
      watch(actor)
      expectMsgType[Terminated]
    }

  }

}
