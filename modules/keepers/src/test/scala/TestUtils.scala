import akka.Done
import com.darienmt.keepers.Generator

import scala.concurrent.{ ExecutionContext, Future }

object TestUtils {

  def generatorWithDelay(delay: Int = 10000)(implicit executor: ExecutionContext): Generator = () => Future {
    Thread.sleep(delay)
    Done
  }
}
