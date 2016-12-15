
import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.testkit.{ DefaultTimeout, ImplicitSender, TestKitBase }
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpec }
import TestUtils._
import com.darienmt.keepers.CollectorManager.{ StartCollecting, UnknownMessage }
import com.darienmt.keepers.{ CollectorManager, Generator, RetryConfig }

import scala.concurrent.duration._

class CollectorManagerSpecs extends WordSpec
    with Matchers with BeforeAndAfterAll
    with TestKitBase with DefaultTimeout with ImplicitSender {

  implicit lazy val system = ActorSystem("CollectorSpecs")

  import system.dispatcher

  override def afterAll(): Unit = {
    shutdown(system)
  }

  def doingNothingActorProps(): Props = Props(new Actor() {
    def receive = {
      case msg => sender() ! msg
    }
  })

  def defaultRetry(): RetryConfig = RetryConfig(1 second, 100 millisecond, 50 millisecond, 0)

  def getActor(
    collectorProps: Props = doingNothingActorProps(),
    generator: Generator = generatorWithDelay(),
    retryConfig: RetryConfig = defaultRetry()
  ): ActorRef = {
    val collectorPropsGenerator: CollectorManager.CollectorProps = (_, _) => collectorProps
    val props = CollectorManager.props(collectorPropsGenerator, generator, retryConfig)
    system.actorOf(props)
  }

  "the collector manager" should {
    "respond to an unknown message by sending it the sender" in {
      val actor = getActor()
      actor ! "Test"
      expectMsgType[UnknownMessage]
    }

    "should call restart the collector" in {
      val failingActor = Props(new Actor() with ActorLogging {

        import system.dispatcher

        override def preStart(): Unit = {
          context.system.scheduler.scheduleOnce(5 millisecond, self, "Tick")
        }

        def receive = {
          case _ => {
            testActor ! 1
            throw new Exception("Bye bye")
          }
        }
      })

      val actor = getActor(
        collectorProps = failingActor,
        generator = generatorWithDelay(1000),
        retryConfig = RetryConfig(100 millisecond, 10 millisecond, 50 millisecond, 0)
      )
      actor ! StartCollecting

      val msgs = receiveWhile[Int](300 millisecond) {
        case _ => 1
      }
      msgs.size shouldBe 4

      val moreMsgs = receiveWhile[Int](300 millisecond) {
        case _ => 1
      }
      moreMsgs.size shouldBe 2
    }
  }
}
