# [fit] Case study:
# [fit] **type-level** programming in the real world

---

# [fit] :wave:
# My name is @folone

[https://github.com/folone/scalar-conf](https://github.com/folone/scalar-conf)

![](https://cdn.eyeem.com/thumb/921cbec0c6081762b3ee7f5dcc85587c0b5716e1-1463689016783/3900/3900?highRes=true)

---

![](https://dl.dropboxusercontent.com/u/4274210/sc_cmyk_white.ai)

---

![fill inline](https://i1.sndcdn.com/avatars-000115643651-71o36c-t500x500.jpg)![fill inline](https://i1.sndcdn.com/artworks-000037484402-x8gfft-t500x500.jpg)![fill inline](https://i1.sndcdn.com/avatars-000138955484-r3yxsu-t500x500.jpg)![fill inline](https://i1.sndcdn.com/avatars-000207169396-qjliyo-t500x500.jpg)
![fill inline](https://i1.sndcdn.com/avatars-000109087465-2uqbx8-t500x500.jpg)![fill inline](https://i1.sndcdn.com/avatars-000055035285-1i17eh-t500x500.jpg)![fill inline](https://i1.sndcdn.com/avatars-000077357035-7tbvde-t500x500.jpg)![fill inline](https://i1.sndcdn.com/avatars-000170331047-7qtaxt-t500x500.jpg)

---

* 12 hours uploaded every minute
* ~35k listening years every month
* >135M tracks (including content from majors: Sony/Universal/Warner)
* ~180M monthly active users

![left](https://cdn.eyeem.com/thumb/c23be843844f1f0129727da2ed2f0a3991ca1347-1463689289498/3900/3900)

^ quite unique: major labes and dark underground
^ 4x spotify audio (>1PB vs >5PB)
^ 10x smaller team than spotify
^ a lot more interactive, as in the next slide
^ by a huge margin bigger than any competitor in both usage and amount of data (collection size)
^ except for youtube: that's bigger, but they aren't in the audio business per se

---

![fill](https://dl.dropboxusercontent.com/u/4274210/sc_cmyk_white.ai)

:heart:

![fill](http://www.scala-lang.org/resources/img/smooth-spiral@2x.png)

^ story about rails app
^ also clojure/jruby

---

# [fit] Case study:
# [fit] **type-level** programming in the real world

^ Normally talk about typelevel things: there's a prolog in your scala
^ Most frequent question: this is all good and fine,
^ but where do you actually use this?

^ Plan:
^ * talk about jackson
^ * show play-json
^ * show limitations of its macro
^ * show what i've done with shapeless
^ * explain how it actually works

^ work on the stream and activities team
^ but started on internal-libraries team

---

# [fit] In the beginning there was
# [fit] `def json(o: Any): Result`

^ jackson
^ there was also a dependency on play-json to parse the payload
^ no typeclasses
^ even worse: JsValues from playjson would not be turned into json properly
^ and so there were a bunch of outages where people would accept a payload, do some things, then chane something on that payload and try to throw it back at jackson as a JsValue.

---

# [fit] `def typedJson[A : Writes](o: A): Result`

^ allude to the fact that it's not easy to change
^ the json library in a company with 200 mostly ruby developers

^ by now this has also replaced `def json`

---

# [fit] `implicit val writes = Json.writes[Track]`

^ people were generally happy with this syntax, but then for historical reasons
^ there were data transfer objects with more than 21 fields.

^ I ended up being the goto person on this, and people would come up
^ and be like "what the hell"

^ it is pretty hard to change these things
^ when you have so many people use the api

---

# [fit] `import Json.writes._ // Json.writes.deriveInstance`
# [fit] `implicit val writes = Json.writes[Track]`

---

```scala
@ import play.api.libs.json.{Json => PJson}
import play.api.libs.json.{Json => PJson}
@ case class Omg(_1: Int, _2: Int, _3: Int, _4: Int, _5: Int,
_6: Int, _7: Int, _8: Int, _9: Int, _10: Int, _11: Int, _12: Int,
_13: Int, _14: Int, _15: Int, _16: Int, _17: Int, _18: Int,
_19: Int, _20: Int, _21: Int, _22: Int, _23: Int)
defined class Omg
@ PJson.writes[Omg]
cmd9.sc:1: No unapply or unapplySeq function found for class Omg: <none> / <none>
val res9 = PJson.writes[Omg]
                       ^
Compilation Failed
@ import com.soundcloud.json.Json
import com.soundcloud.json.Json
@ import Json.writes._
import Json.writes._
@ Json.writes[Omg]
res11: play.api.libs.json.Writes[Omg] = play.api.libs.json.Writes$$anon$5@60ec44ee
```

^ Before https://github.com/soundcloud/api-web/blob/4ae946841fee104e2f5b59107b848058dd13d56c/src/main/scala/com/soundcloud/api/v2/representation/Track.scala#L175

^ After https://github.com/soundcloud/api-web/blob/a30f7e423ce4352e9c670b5fe8db049b3274a947/src/main/scala/com/soundcloud/api/v2/representation/Track.scala#L119

---

# [fit] How does this work exactly? ಠ_ಠ

---

# [fit] Two (mandatory) building blocks

* `HLists`
* `Generic` (tiny lie: what we actually need is a `LabelledGeneric`)
* [optional] `Coproducts`

---

# [fit] HList

```scala
@ import shapeless._
import shapeless._

@ val hlist = 1 :: "hello" :: HNil
hlist: Int :: String :: HNil = 1 :: hello :: HNil

```

---

```scala
@ hlist(0)
res7: Int = 1

@ hlist(1)
res8: String = hello

@ hlist(2)
<console>:16: error:
Implicit not found: Scary[Type].Please#Ignore
You requested to access an element at the position
TypelevelEncodingFor[2.type]
but the HList Int :: String :: HNil is too short.
       hlist(2)
            ^
Compilation failed.
```

---

# [fit] Generic

```scala
@ case class Track(id: Long, payload: String)
defined class Track

@ val generic = Generic[Track]
generic: shapeless.Generic[Track]{type Repr = Int :: String :: HNil} =
  anon$macro$3$1@7f8f5e52
```

---

```scala
@ val representation = generic.to(Track(1, "hello"))
representation: res0.Repr = 1 :: hello :: HNil

@ representation(0)
res10: Int = 1

@ representation(1)
res11: String = hello

@ representation(2)
<console>:19: error:
Implicit not found: Scary[Type].Please#Ignore
You requested to access an element at the position
TypelevelEncodingFor[2.type]
but the HList Int :: String :: HNil is too short.
       representation(2)
                     ^
```

---

```scala
@ generic.from(hlist)
res7: Track = Track(1L, "hello")
```

---

# [fit] Putting this together

```scala
object writes extends LabelledProductTypeClassCompanion[Writes] with DefaultWrites {
    object typeClass extends LabelledProductTypeClass[Writes] {
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
    }
}
```

---

```scala
override def emptyProduct: Writes[HNil] =
  Writes(_ => PlayJson.obj())
```

---

```scala
override def product[H, T <: HList](name: String,
  headEv: Writes[H], tailEv: Writes[T]) =
    Writes[H :: T] {
      case head :: tail =>
        val h = headEv.writes(head)
        val t = tailEv.writes(tail)

        (h, t) match {
          case (JsNull, t: JsObject) => t
          case (h: JsValue, t: JsObject) =>
            PlayJson.obj(name -> h) ++ t
          case _ => PlayJson.obj()
        }
    }
```

---

# [fit] The whole code

^ https://github.com/soundcloud/jvmkit/blob/master/jvmkit-core/src/main/scala/com/soundcloud/scalakit/json/Json.scala#L69-L108

---

# [fit] Three details

---

`import play.api.libs.json.DefaultWrites`
# [fit] `... with DefaultWrites`

^ to get all the instances defined in playjson for free

---

```scala
@annotation.implicitAmbiguous("You have a Unit hiding somewhere in your types")
implicit def noUnits: Writes[Unit] = null
implicit def noUnitsBitte: Writes[Unit] = null
```

^ to make the derrivation less eager
^ these days there's also one for futures

---

```scala
@ Json.writes[Unit]
cmd2.sc:1: You have a Unit hiding somewhere in your types
val res2 = Json.writes[Unit]
    ^
```

---

# Coproduc:+:s

---

```scala
@ import shapeless._
import shapeless._
@ {
  sealed trait Playable
  case class Track(id: Long, payload: String) extends Playable
  case class Album(tracks: List[Track]) extends Playable
  }
defined trait Playable
defined class Track
defined class Album

@ Generic[Playable]
res2: Generic[Playable]{type Repr = Album :+: Track :+: CNil} =
  $sess.cmd2$anon$macro$1$1@2f4344c6
```

---

```scala
@ import com.soundcloud.json.Json
import com.soundcloud.json.Json
@ import play.api.libs.json.{Json => PJson}
import play.api.libs.json.{Json => PJson}
@ PJson.writes[Playable]
cmd5.sc:1: not found: type Writes
val res5 = PJson.writes[Playable]
                       ^
Compilation Failed
@ import Json.writes._
import Json.writes._
@ Json.writes[Playable]
res6: play.api.libs.json.Writes[Playable] =
  play.api.libs.json.Writes$$anon$5@c5ff6e1
```

---

```scala
@ PJson.toJson(Album(List(Track(1, "payload1"),
                          Track(2, "payload2"))))
res7: play.api.libs.json.JsValue =
  {"tracks":[{"id":1,"payload":"payload1"},
             {"id":2,"payload":"payload2"}]}
```

---

# [fit] ?-
*questions
