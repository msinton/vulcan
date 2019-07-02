package enumeratum

import enumeratum.EnumEntry.Lowercase
import enumeratum.VulcanEnumSpec.Suit
import org.scalacheck.Gen
import org.scalatest.EitherValues._
import org.scalatest.FunSpec
import org.scalatest.prop.PropertyChecks
import vulcan.{AvroDoc, AvroNamespace, Codec, encode, decode}

final class VulcanEnumSpec extends FunSpec with PropertyChecks {
  describe("VulcanEnum") {
    describe("schema") {
      it("should use an enum schema") {
        assert {
          Codec[Suit].schema.right.value.toString ===
            """{"type":"enum","name":"Suit","namespace":"com.example","doc":"The different card suits","symbols":["clubs","diamonds","hearts","spades"]}"""
        }
      }
    }

    it("should roundtrip enum values") {
      val gen = Gen.oneOf[Suit](Suit.Diamonds, Suit.Hearts, Suit.Spades)
      forAll(gen) { suit =>
        val roundtrip = encode(suit).right.flatMap(decode[Suit])
        assert(roundtrip.right.value === suit)
      }
    }

    it("should error if withNameOption does not handle schema symbol") {
      val roundtrip = encode[Suit](Suit.Clubs).right.flatMap(decode[Suit])
      assert {
        roundtrip.left.value.message ===
          "clubs is not a member of Suit (clubs, diamonds, hearts, spades)"
      }
    }
  }
}

object VulcanEnumSpec {

  @AvroNamespace("com.example")
  @AvroDoc("The different card suits")
  sealed trait Suit extends EnumEntry with Lowercase

  object Suit extends Enum[Suit] with VulcanEnum[Suit] {
    case object Clubs extends Suit
    case object Diamonds extends Suit
    case object Hearts extends Suit
    case object Spades extends Suit

    val values = findValues

    override def withNameOption(name: String): Option[Suit] =
      if (name == "clubs") None
      else super.withNameOption(name)
  }
}