scalaVersion := "2.12.1"

libraryDependencies := Seq(
  "com.lihaoyi" % "ammonite" % "0.8.2" cross CrossVersion.full,
  "com.typesafe.play" %% "play-json" % "2.6.0-M2",
  "com.chuusai" %% "shapeless" % "2.3.2"
)

initialCommands in (Compile, console) := """ammonite.Main().run()"""
