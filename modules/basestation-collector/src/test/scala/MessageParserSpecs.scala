import com.darienmt.airplaneadventures.basestation.collector.parsing.MessageParser
import com.darienmt.airplaneadventures.basestation.data.BaseStation._
import org.scalatest.{Matchers, WordSpec}

import scala.util.Failure

class MessageParserSpecs extends WordSpec with Matchers {

  "Message parser" should {
    "parse a SEL message" in {
      val message = MessageParser("SEL,,496,2286,4CA4E5,27215,2010/02/19,18:06:07.710,2010/02/19,18:06:07.710,RYR1427")
      message shouldBe a[SelectionChangeMessage]
    }

    "parse a ID message" in {
      val message = MessageParser("ID,,496,7162,405637,27928,2010/02/19,18:06:07.115,2010/02/19,18:06:07.115,EZY691A")
      message shouldBe a[IdMessage]
    }

    "parse a AIR message" in {
      val message = MessageParser("AIR,,496,5906,400F01,27931,2010/02/19,18:06:07.128,2010/02/19,18:06:07.128")
      message shouldBe a[NewAircraftMessage]
    }

    "parse a STA message" in {
      val message = MessageParser("STA,,5,179,400AE7,10103,2008/11/28,14:58:51.153,2008/11/28,14:58:51.153,RM")
      message shouldBe a[StatusChangeMessage]
    }

    "parse a CLK message" in {
      val message = MessageParser("CLK,,496,-1,,-1,2010/02/19,18:18:19.036,2010/02/19,18:18:19.036")
      message shouldBe a[ClickMessage]
    }

    "parse a MSG1 message" in {
      val message = MessageParser("MSG,1,145,256,7404F2,11267,2008/11/28,23:48:18.611,2008/11/28,23:53:19.161,RJA1118,,,,,,,,,,,")
      message shouldBe a[ESIdentificationAncCategoryMessage]
    }

    "parse a MSG2 message" in {
      val message = MessageParser("MSG,2,496,603,400CB6,13168,2008/10/13,12:24:32.414,2008/10/13,12:28:52.074,,0,76.4,258.3,54.05735,-4.38826,,,,,,0")
      message shouldBe a[ESSurfacePositionMessage]
    }

    "parse a MSG3 message" in {
      val message = MessageParser("MSG,3,496,211,4CA2D6,10057,2008/11/28,14:53:50.594,2008/11/28,14:58:51.153,,37000,,,51.45735,-1.02826,,,0,0,0,0")
      message shouldBe a[ESAirbornePositionMessage]
    }

    "parse a MSG4 message" in {
      val message = MessageParser("MSG,4,496,469,4CA767,27854,2010/02/19,17:58:13.039,2010/02/19,17:58:13.368,,,288.6,103.2,,,-832,,,,,")
      message shouldBe a[ESAirborneVelocityMessage]
    }

    "parse a MSG5 message" in {
      val message = MessageParser("MSG,5,496,329,394A65,27868,2010/02/19,17:58:12.644,2010/02/19,17:58:13.368,,10000,,,,,,,0,,0,0")
      message shouldBe a[SurveillanceAltMessage]
    }

    "parse a MSG6 message" in {
      val message = MessageParser("MSG,6,496,237,4CA215,27864,2010/02/19,17:58:12.846,2010/02/19,17:58:13.368,,33325,,,,,,0271,0,0,0,0")
      message shouldBe a[SurveillanceIdMessage]
    }

    "parse a MSG7 message" in {
      val message = MessageParser("MSG,7,496,742,51106E,27929,2011/03/06,07:57:36.523,2011/03/06,07:57:37.054,,3775,,,,,,,,,,0")
      message shouldBe a[AirToAirMessage]
    }

    "parse a MSG8 message" in {
      val message = MessageParser("MSG,8,496,194,405F4E,27884,2010/02/19,17:58:13.244,2010/02/19,17:58:13.368,,,,,,,,,,,,0")
      message shouldBe a[AllCallReplyMessage]
    }

    "if there is an error, an error message should be generated" in {
      val message = MessageParser("MSG,8,496,194,405F4E,27884,2010/02/19,17:58:13.244,2010/AA/19,17:58:13.368,,,,,,,,,,,,0")
      message shouldBe a[ErrorMessage]
    }

    "if there is an error on the number of csv and the index, an error message should be generated" in {
      val message = MessageParser("\"MSG,8,496,194,405F4E,27884,2010/02/19,17:58:13.244,2010/02/19")
      message shouldBe a[ErrorMessage]
    }
  }
}
