
name := "airplane-adventures"

lazy val buildSettings = Seq(
  organization := "darienmt",
  scalaVersion := "2.11.8"
)

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:postfixOps",
  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Xfuture"
)

lazy val akkaVersion = "2.4.12"

lazy val akkaLib = Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion
)

lazy val akkaStreamKafkaLib = Seq("com.typesafe.akka" %% "akka-stream-kafka" % "0.13")

lazy val shapelessLib = Seq("com.chuusai" %% "shapeless" % "2.3.2")

lazy val catsLib = Seq("org.typelevel" %% "cats-core" % "0.8.1")

lazy val circeLib = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-java8"
).map(_ % "0.6.0")

lazy val loggingLib = Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.3"
)

lazy val testLib = Seq(
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test"
)

lazy val commonLibraries = Seq(
)

lazy val commonSettings = buildSettings ++ commonLibraries ++
  (scalacOptions ++= compilerOptions) ++
  (scalastyleFailOnError := true)

lazy val root = project.in(file("."))
  .aggregate(basestationData, keepers, basestationCollector, basestationRepeater, basestationRawCollector)

lazy val basestationData = project.in(file("modules/basestation-data"))
  .settings(commonSettings:_*)

lazy val keepers = project.in(file("modules/keepers"))
  .settings(commonSettings:_*)
  .settings(libraryDependencies ++= akkaLib ++ testLib ++ akkaStreamKafkaLib ++ loggingLib)


lazy val basestationCollector = project.in(file("modules/basestation-collector"))
  .settings(commonSettings:_*)
  .settings(libraryDependencies ++= akkaLib ++ shapelessLib ++ catsLib ++
                                    circeLib ++ testLib ++ akkaStreamKafkaLib ++
                                    loggingLib
  )
  .aggregate(basestationData, keepers)
  .dependsOn(basestationData, keepers)
  .enablePlugins(sbtdocker.DockerPlugin, JavaServerAppPackaging)
  .settings(dockerSettings)


lazy val basestationRepeater = project.in(file("modules/basestation-repeater"))
  .settings(commonSettings:_*)
  .settings(libraryDependencies ++= akkaLib ++ testLib ++ loggingLib)
  .enablePlugins(sbtdocker.DockerPlugin, JavaServerAppPackaging)
  .settings(dockerSettings)

lazy val basestationRawCollector = project.in(file("modules/basestation-raw-collector"))
  .settings(commonSettings:_*)
  .settings(libraryDependencies ++= akkaLib ++ testLib ++ akkaStreamKafkaLib ++ loggingLib)
  .aggregate(keepers)
  .dependsOn(keepers)
  .enablePlugins(sbtdocker.DockerPlugin, JavaServerAppPackaging)
  .settings(dockerSettings)

// Docker
addCommandAlias("dockerize", ";clean;compile;test;basestationCollector/docker;basestationRepeater/docker;basestationRawCollector/docker")

lazy val dockerSettings = Seq(
  dockerfile in docker := {
    val appDir: File = stage.value
    val targetDir = "/app"

    new Dockerfile {
      from("anapsix/alpine-java:8")
      entryPoint(s"$targetDir/bin/${executableScriptName.value}")
      copy(appDir, targetDir)
    }
  },
  imageNames in docker := Seq(
    // Sets the latest tag
    ImageName(s"${organization.value}/${name.value.toLowerCase}:latest"),

    // Sets a name with a tag that contains the project version
    ImageName(
      repository = s"${organization.value}/${name.value.toLowerCase}",
      tag = Some("v" + version.value)
    )
  )
)