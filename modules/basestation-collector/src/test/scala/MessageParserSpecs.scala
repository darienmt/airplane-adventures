import com.darienmt.airplaneadventures.basestation.collector.MessageParser
import com.darienmt.airplaneadventures.basestation.data.BaseStation._
import org.scalatest.{ Matchers, WordSpec }

class MessageParserSpecs extends WordSpec with Matchers {

  "Message parser" should {
    "parse a SEL message " in {
      val message = MessageParser("SEL,,496,2286,4CA4E5,27215,2010/02/19,18:06:07.710,2010/02/19,18:06:07.710,RYR1427")
      message.isSuccess shouldBe true
      message.get shouldBe a[SelectionChangeMessage]
    }
  }
}
