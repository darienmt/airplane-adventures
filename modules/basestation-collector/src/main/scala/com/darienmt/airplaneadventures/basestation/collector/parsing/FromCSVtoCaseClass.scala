package com.darienmt.airplaneadventures.basestation.collector.parsing

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime}

import com.darienmt.airplaneadventures.basestation.data.BaseStation.isOnGroundType
import shapeless._

import scala.util.{Failure, Success, Try}

object FromCSVtoCaseClass {
  trait Read[A] { def reads(s: String): Try[A] }

  object Read {
    def apply[A](implicit readA: Read[A]): Read[A] = readA

    implicit object stringRead extends Read[String] {
      def reads(s: String): Try[String] = Success(s.trim())
    }

    implicit object intRead extends Read[Int] {
      def reads(s: String): Try[Int] = Try(s.toInt)
    }

    implicit object doubleRead extends Read[Double] {
      def reads(s: String): Try[Double] = Try(s.toDouble)
    }

    implicit object isOnGroundTypeRead extends Read[isOnGroundType] {
      def reads(s: String): Try[isOnGroundType] =
        s.trim().replace("\r", "") match {
          case "" => Success(false)
          case "0" => Success(false)
          case "1" => Success(true)
          case e => Failure(new Exception(s"The value $e is not a inOnGround value [0,1]"))
        }
    }

    implicit object localDateRead extends Read[LocalDate] {
      def reads(s: String): Try[LocalDate] = Try(LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy/MM/dd")))
    }

    implicit object localTimeRead extends Read[LocalTime] {
      def reads(s: String): Try[LocalTime] = Try(LocalTime.parse(s, DateTimeFormatter.ofPattern("HH:mm:ss.SSS")))
    }
  }

  trait FromRow[L <: HList] { def apply(row: List[String]): Try[L] }

  object FromRow {
    import HList.ListCompat._

    def apply[L <: HList](implicit fromRow: FromRow[L]): FromRow[L] = fromRow

    def fromFunc[L <: HList](f: List[String] => Try[L]): FromRow[L] = new FromRow[L] {
      def apply(row: List[String]) = f(row)
    }

    implicit val hnilFromRow: FromRow[HNil] = fromFunc {
      case Nil => Success(HNil)
      case _ => Failure(new RuntimeException("No more rows expected"))
    }

    implicit def hconsFromRow[H: Read, T <: HList: FromRow]: FromRow[H :: T] =
      fromFunc {
        case h :: t => for {
          hv <- Read[H].reads(h)
          tv <- FromRow[T].apply(t)
        } yield hv :: tv
        case Nil => Failure(new RuntimeException("Expected more cells"))
      }
  }

  trait RowParser[A] {
    def apply[L <: HList](row: List[String])(implicit
      gen: Generic.Aux[A, L],
      fromRow: FromRow[L]): Try[A] = fromRow(row).map(gen.from)
  }

  def rowParserFor[A]: RowParser[A] = new RowParser[A] {}
}
