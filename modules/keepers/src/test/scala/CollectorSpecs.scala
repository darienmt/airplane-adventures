import akka.Done
import akka.actor.Status.Failure
import akka.actor.{ Actor, ActorRef, ActorSystem, Props, SupervisorStrategy, Terminated }
import akka.testkit.{ DefaultTimeout, ImplicitSender, TestKit }
import com.darienmt.keepers.{ Collector, Generator }
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }
import TestUtils._
import com.darienmt.keepers.Collector.StreamFinished

class CollectorSpecs extends TestKit(ActorSystem("CollectorSpecs"))
    with DefaultTimeout with ImplicitSender
    with WordSpecLike with Matchers with BeforeAndAfterAll {

  import system.dispatcher

  override def afterAll(): Unit = {
    shutdown()
  }

  def getActor(
    generator: Generator = generatorWithDelay()
  ): ActorRef = {
    val supervisor = system.actorOf(Props(new Actor {
      override def supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy
      override def receive: Receive = {
        case p: Props => sender ! context.actorOf(p)
      }
    }))

    supervisor ! Collector.props(testActor, generator)

    expectMsgType[ActorRef]
  }

  "the collector actor" should {

    "respond to an unknown message by sending it to his manager" in {
      val actor = getActor()
      actor ! "Test"
      watch(actor)
      expectMsgType[Terminated]
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

    "send StreamFinished, when the generator sends Done" in {
      val actor = getActor(generator = generatorWithDelay(500))
      expectMsg(StreamFinished)
    }

    "stop, when the generator sends Failure" in {
      val failingGenerator: Generator = () => {
        Thread.sleep(500)
        throw new Exception()
      }
      val actor = getActor(generator = failingGenerator)
      watch(actor)
      expectMsgType[Terminated]
    }

  }

}
