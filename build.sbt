
name := "airplane-adventures"

lazy val buildSettings = Seq(
  organization := "com.darienmt",
  scalaVersion := "2.11.8"
)

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture"
)

lazy val akkaVersion = "2.4.12"

lazy val akkaStreamsLib = Seq("com.typesafe.akka" %% "akka-stream" % akkaVersion)

lazy val akkaStreamKafkaLib = Seq("com.typesafe.akka" %% "akka-stream-kafka" % "0.13")

lazy val shapelessLib = Seq("com.chuusai" %% "shapeless" % "2.3.2")

lazy val catsLib = Seq("org.typelevel" %% "cats-core" % "0.8.1")

lazy val circeLib = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-java8"
).map(_ % "0.6.0")

lazy val scalaTestLib = Seq("org.scalatest" %% "scalatest" % "3.0.1")

lazy val commonLibraries = Seq(
)

lazy val commonSettings = buildSettings ++ commonLibraries ++
  (scalacOptions ++= compilerOptions) ++
  (scalastyleFailOnError := true)

lazy val root = project.in(file("."))
  .aggregate(basestationData, basestationCollector)

lazy val basestationData = project.in(file("modules/basestation-data"))
  .settings(commonSettings:_*)

lazy val basestationCollector = project.in(file("modules/basestation-collector"))
  .settings(commonSettings:_*)
  .settings(libraryDependencies ++= akkaStreamsLib ++ shapelessLib ++ catsLib ++
                                    circeLib ++ scalaTestLib ++ akkaStreamKafkaLib)
  .dependsOn(basestationData)
