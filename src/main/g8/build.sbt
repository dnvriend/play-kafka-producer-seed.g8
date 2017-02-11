name := "$name$"

organization := "$organization$"

version := "1.0.0"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.16"

resolvers += "Confluent Maven Repo" at "http://packages.confluent.io/maven/"

// fp
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.8"
// kafka
libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "0.13"
libraryDependencies += "io.confluent" % "kafka-avro-serializer" % "3.1.2"
libraryDependencies += "org.apache.kafka" % "kafka-streams" % "0.10.1.1-cp1"
libraryDependencies += "com.sksamuel.avro4s" %% "avro4s-core" % "1.6.4"

// ws
libraryDependencies += ws
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.5.10"

// akka
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-query-experimental" % akkaVersion


fork in Test := true

parallelExecution := false

licenses +=("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

import de.heikoseeberger.sbtheader.license.Apache2_0

headers := Map(
  "scala" -> Apache2_0("2016", "$author_name$"),
  "conf" -> Apache2_0("2016", "$author_name$", "#")
)

import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform

SbtScalariform.autoImport.scalariformPreferences := SbtScalariform.autoImport.scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)

enablePlugins(PlayScala)
enablePlugins(AutomateHeaderPlugin)
