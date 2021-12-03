name := "advent_of_code_2021"

version := "0.1"

scalaVersion := "2.13.7"

val AkkaVersion = "2.6.17"

libraryDependencies ++= Seq(
"com.typesafe.akka" %% "akka-stream" % AkkaVersion,

"com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,
"junit" % "junit" % "4.13.1" % Test,
"com.novocode" % "junit-interface" % "0.11" % Test
)