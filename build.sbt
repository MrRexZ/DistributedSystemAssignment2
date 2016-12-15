name := "DistributedSystemAssignment2"

version := "1.0"

scalaVersion := "2.11.8"



javaOptions in run += s"-Djava.library.path=target/natives"

resolvers ++= Seq(
  "dunnololda's maven repo" at "https://raw.github.com/dunnololda/mvn-repo/master",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
  //"Local Maven Repository" at "file:///"+Path.userHome.absolutePath+"/.ivy2/local"
)


libraryDependencies ++= Seq(
  "com.github.dunnololda" %% "scage" % "11.9",
  "com.typesafe.akka" %% "akka-actor" % "2.4.12",
  "com.typesafe.akka" %% "akka-remote" % "2.4.12",
  "com.google.guava" % "guava" % "20.0",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
)
