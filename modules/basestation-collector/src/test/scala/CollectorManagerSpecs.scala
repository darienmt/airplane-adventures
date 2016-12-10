
import TestUtils._
import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.testkit.{ DefaultTimeout, ImplicitSender, TestKitBase }
import com.darienmt.airplaneadventures.basestation.collector.actors.CollectorManager.{ RetryConfig, StartCollecting, UnknownMessage }
import com.darienmt.airplaneadventures.basestation.collector.actors.{ Collector, CollectorManager }
import com.darienmt.airplaneadventures.basestation.collector.streams.BaseStation2Kafka.{ SinkConfig, SourceConfig }
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ BeforeAndAfterAll, FlatSpec, Matchers, WordSpec }

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

  def startCollecting(): StartCollecting = StartCollecting(
    SourceConfig("address", 10),
    SinkConfig("addess", 10, "topic")
  )

  def getActor(
    collectorProps: Props = doingNothingActorProps(),
    generator: Collector.Generator = generatorWithDelay(),
    retryConfig: RetryConfig = defaultRetry()
  ): ActorRef = {
    val collectorPropsGenerator: CollectorManager.CollectorProps = (_, _) => collectorProps
    val streamGenerator: CollectorManager.StreamGenerator = (_, _) => generator()
    val props = CollectorManager.props(collectorPropsGenerator, streamGenerator, retryConfig)
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
      actor ! startCollecting()

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
