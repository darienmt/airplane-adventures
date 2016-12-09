import akka.Done
import com.darienmt.airplaneadventures.basestation.collector.actors.Collector

import scala.concurrent.{ExecutionContext, Future}

object TestUtils {

  def generatorWithDelay(delay: Int = 10000)(implicit executor: ExecutionContext): Collector.Generator = () => Future {
    Thread.sleep(delay)
    Done
  }
}
