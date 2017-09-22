name := "holdem-flop-eqcalc"

organization := "com.sorrentocorp"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.11"

scalacOptions ++= Seq("-deprecation", "-feature")

val sparkVersion = "2.1.1"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql"  % sparkVersion % "provided",
  "org.apache.spark" %% "spark-hive" % sparkVersion % "provided",
  "org.tresamigos"   %% "smv"        % "2.1-SNAPSHOT",
  "com.sorrentocorp" %% "spock"      % "1.0.0-SNAPSHOT",
  "org.scalatest"    %% "scalatest"  % "2.2.6" % "test"
)

parallelExecution in Test := false

mainClass in assembly := Some("org.tresamigos.smv.SmvApp")

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

assemblyJarName in assembly := s"${name.value}-${version.value}-jar-with-dependencies.jar"

// allow Ctrl-C to interrupt long-running tasks without exiting sbt,
// if the task implementation correctly handles the signal
cancelable in Global := true

val smvInit = if (sys.props.contains("smvInit")) {
  val files = sys.props.get("smvInit").get.split(",")
  files
    .map { f =>
      IO.read(new File(f))
    }
    .mkString("\n")
} else ""

initialCommands in console := s"""
import org.apache.spark.sql.SparkSession
import org.tresamigos.smv._
val spark = SparkSession.builder().master("local").appName("spock runner").getOrCreate()
SmvApp.init(Array("-m", "None"), Option(spark))
${smvInit}
"""

// clean up spark context
cleanupCommands in console := "spark.stop"

// Uncomment the following to include python scripts in the fat jar
// unmanagedResourceDirectories in Compile += (sourceDirectory in Compile).value / "python"
