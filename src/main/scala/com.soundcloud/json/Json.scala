package com.soundcloud.json

import play.api.libs.json.{DefaultWrites, JsNull, JsObject, JsString, JsValue, Writes, Json => PlayJson}
import shapeless._

object Json {
  /**
    * An improved deriver for Writes instances for more than just case classes
    */
  object writes extends LabelledTypeClassCompanion[Writes] with DefaultWrites {
    @annotation.implicitAmbiguous("You have a Unit hiding somewhere in your types")
    implicit def noUnits: Writes[Unit] = null
    implicit def noUnitsBitte: Writes[Unit] = null

    object typeClass extends LabelledTypeClass[Writes] {

      override def emptyProduct: Writes[HNil] =
        Writes(_ => PlayJson.obj())

      override def product[H, T <: HList](name: String, headEv: Writes[H], tailEv: Writes[T]) =
        Writes[H :: T] {
          case head :: tail =>
            val h = headEv.writes(head)
            val t = tailEv.writes(tail)

            (h, t) match {
              case (JsNull, t: JsObject) => t
              case (h: JsValue, t: JsObject) => PlayJson.obj(name -> h) ++ t
              case _ => PlayJson.obj()
            }
        }

      override def project[F, G](instance: => Writes[G], to: F => G, from: G => F) =
        Writes[F](f => instance.writes(to(f)))

      override def emptyCoproduct: Writes[CNil] = Writes(_ => JsNull)

      override def coproduct[L, R <: Coproduct](name: String, cl: => Writes[L], cr: => Writes[R]) =
        Writes[L :+: R] { lr =>
          val r = lr match {
            case Inl(left) => cl writes left
            case Inr(right) => cr writes right
          }
          r match {
            case JsNull => JsString(name)
            case otherwise => otherwise
          }
        }
    }
  }
}
