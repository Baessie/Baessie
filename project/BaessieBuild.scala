import java.util.jar.Manifest
import sbt._
import Keys._
import com.github.siasia.WebPlugin._
import sbt.Package.ManifestAttributes

object BaessieBuild extends Build {
  lazy val buildSettings = Seq(
    organization := "org.baessie",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.9.1"
  )

  lazy val baessie = Project(
    id = "baessie",
    base = file("."),
    settings = parentSettings,
    aggregate = Seq(common, wsconnector, simulator)
  )

  lazy val common = Project(
    id = "common",
    base = file("common"),
    settings = Defaults.defaultSettings ++ settings ++ Seq(
      libraryDependencies ++= Dependencies.simulator
    ) ++ Seq(exportJars := true)
  )

  lazy val simulator = Project(
    id = "simulator",
    base = file("simulator"),
    dependencies = Seq(common, wsconnector),
    settings = Defaults.defaultSettings ++ settings ++ webSettings ++ Seq(
      libraryDependencies ++= Dependencies.simulator ++ Dependencies.jetty
    )
  )

  lazy val wsconnector = Project(
    id = "wsconnector",
    base = file("wsconnector"),
    dependencies = Seq(common),
    settings = Defaults.defaultSettings ++ settings ++ Seq(
      libraryDependencies ++= Dependencies.simulator
    ) ++ Seq(exportJars := true) ++ Seq(packageOptions := Seq(ManifestAttributes(("Tapestry-Module-Classes", "org.baessie.ws.services.WsModule"))))
  )

  // Settings

  override lazy val settings = super.settings ++ buildSettings ++ Publish.versionSettings ++ dependencySettings

  def dependencySettings: Seq[Setting[_]] =
  ivyXML :=
          <dependencies>
            <dependency org="org.apache.tapestry" name="tapestry-core" rev="5.2.6">
              <exclude module="log4j"/>
              <exclude module="slf4j-log4j12"/>
            </dependency>
          </dependencies>

  lazy val baseSettings = Defaults.defaultSettings ++ Publish.settings

  lazy val parentSettings = baseSettings ++ Seq(
    publishArtifact in Compile := true
  )

  lazy val defaultSettings = baseSettings ++ Seq(
    // compile options
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked") ++ (
            if (true || (System getProperty "java.runtime.version" startsWith "1.7")) Seq() else Seq("-optimize")), // -optimize fails with jdk7
    javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),

    // disable parallel tests
    parallelExecution in Test := false,

    // show full stack traces
    testOptions in Test += Tests.Argument("-oF")
  )
}

// Dependencies

object Dependencies {

  import Dependency._

  val testkit = Seq(Test.scalatest)

  val simulator = Seq(tapestryCore, xmlUnit, slf4jApi, jettyServlet, Runtime.logback)

  val jetty = Seq(Test.jettyRun)
}

object Dependency {

  // Versions

  object V {
    val Jetty = "7.4.0.v20110414"
    val Logback = "0.9.28"
    val Scalatest = "1.6.1"
    val Slf4j = "1.6.0"
    val Tapestry = "5.2.6"
  }

  // Compile

  val jettyUtil = "org.eclipse.jetty" % "jetty-util" % V.Jetty // Eclipse license
  val jettyXml = "org.eclipse.jetty" % "jetty-xml" % V.Jetty // Eclipse license
  val jettyServlet = "org.eclipse.jetty" % "jetty-servlet" % V.Jetty // Eclipse license
  val jsr250 = "javax.annotation" % "jsr250-api" % "1.0" // CDDL v1
  val slf4jApi = "org.slf4j" % "slf4j-api" % V.Slf4j // MIT
  val tapestryCore = "org.apache.tapestry" % "tapestry-core" % V.Tapestry
  val xmlUnit = "xmlunit" % "xmlunit" % "1.3"

  // ApacheV2

  // Provided

  object Provided {
    val javaxServlet = "org.apache.geronimo.specs" % "geronimo-servlet_3.0_spec" % "1.0" % "provided" //CDDL v1
    val jetty = "org.eclipse.jetty" % "jetty-server" % V.Jetty % "provided" // Eclipse license
  }

  // Runtime

  object Runtime {
    val logback = "ch.qos.logback" % "logback-classic" % V.Logback % "runtime" // MIT
  }

  // Test

  object Test {
    val jetty = "org.eclipse.jetty" % "jetty-server" % V.Jetty % "test" // Eclipse license
    val jettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % V.Jetty % "test" // Eclipse license
    val jettyRun = "org.eclipse.jetty" % "jetty-webapp" % V.Jetty % "jetty" // Eclipse license
    val junit = "junit" % "junit" % "4.8" % "test" // Common Public License 1.0
    val logback = "ch.qos.logback" % "logback-classic" % V.Logback % "test" // EPL 1.0 / LGPL 2.1
    val scalatest = "org.scalatest" % "scalatest_2.9.1" % V.Scalatest % "test" // ApacheV2
    val scalacheck = "org.scala-tools.testing" % "scalacheck_2.9.1" % "1.9" % "test" // New BSD
  }
}
