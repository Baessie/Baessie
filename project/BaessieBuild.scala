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
    aggregate = Seq(common, wsconnector, simulator, testtools)
  )

  lazy val testtools = Project(
    id = "testtools",
    base = file("testtools"),
    settings = Defaults.defaultSettings ++ settings ++ Seq(
      libraryDependencies ++= Dependencies.simulator ++ Dependencies.testkit
    ) ++ Seq(exportJars := true)
  )

  lazy val common = Project(
    id = "common",
    base = file("common"),
    settings = Defaults.defaultSettings ++ settings ++ Seq(
      libraryDependencies ++= Dependencies.simulator ++ Dependencies.testkit
    ) ++ Seq(exportJars := true)
  )

  lazy val simulator = Project(
    id = "simulator",
    base = file("simulator"),
    settings = Defaults.defaultSettings ++ settings ++ webSettings ++ Seq(
      libraryDependencies ++= Dependencies.simulator ++ Dependencies.jetty ++ Dependencies.testkit
    )
  ) dependsOn(common, wsconnector, testtools % "test")

  lazy val wsconnector = Project(
    id = "wsconnector",
    base = file("wsconnector"),
    settings = Defaults.defaultSettings ++ settings ++ Seq(
      libraryDependencies ++= Dependencies.simulator ++ Dependencies.testkit
    ) ++ Seq(exportJars := true) ++ Seq(packageOptions := Seq(ManifestAttributes(("Tapestry-Module-Classes", "org.baessie.ws.services.WsModule"))))
  ) dependsOn(common, testtools % "test")

  // Settings

  override lazy val settings = super.settings ++ buildSettings ++ Publish.versionSettings ++ dependencySettings

  def dependencySettings: Seq[Setting[_]] =
    ivyXML :=
      <dependencies>
        <dependency org="org.apache.tapestry" name="tapestry-core" rev="5.2.6">
            <exclude module="log4j"/>
            <exclude module="slf4j-log4j12"/>
        </dependency>
        <dependency org="org.apache.tapestry" name="tapestry-test" rev="5.2.6">
            <exclude module="testng"/>
            <exclude module="selenium-server"/>
            <exclude module="jetty-jndi"/>
            <exclude module="jetty-plus"/>
            <exclude module="jetty-webapp"/>
            <exclude module="jetty-xml"/>
            <exclude module="jetty-continuation"/>
            <exclude module="jetty-http"/>
            <exclude module="jetty-io"/>
            <exclude module="jetty-server"/>
            <exclude module="jetty-util"/>
            <exclude module="org.mortbay.jetty"/>
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

  val testkit = Seq(Test.scalatest, Test.tapestry, Test.jetty, Test.jettyWebapp)

  val simulator = Seq(tapestryCore, xmlUnit, slf4jApi, jettyServlet, Runtime.logback)

  val jetty = Seq(Test.jettyRun)
}

object Dependency {

  object V {
    val Jetty = "7.4.0.v20110414"
    val Logback = "0.9.28"
    val Scalatest = "1.6.1"
    val Slf4j = "1.6.0"
    val Tapestry = "5.2.6"
  }

  val jettyUtil = "org.eclipse.jetty" % "jetty-util" % V.Jetty
  val jettyXml = "org.eclipse.jetty" % "jetty-xml" % V.Jetty
  val jettyServlet = "org.eclipse.jetty" % "jetty-servlet" % V.Jetty
  val jsr250 = "javax.annotation" % "jsr250-api" % "1.0"
  val slf4jApi = "org.slf4j" % "slf4j-api" % V.Slf4j
  val tapestryCore = "org.apache.tapestry" % "tapestry-core" % V.Tapestry
  val xmlUnit = "xmlunit" % "xmlunit" % "1.3"

  object Provided {
    val javaxServlet = "org.apache.geronimo.specs" % "geronimo-servlet_3.0_spec" % "1.0" % "provided"
    val jetty = "org.eclipse.jetty" % "jetty-server" % V.Jetty % "provided"
  }

  object Runtime {
    val logback = "ch.qos.logback" % "logback-classic" % V.Logback % "runtime"
  }

  object Test {
    val jetty = "org.eclipse.jetty" % "jetty-server" % V.Jetty % "test"
    val jettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % V.Jetty % "test"
    val jettyRun = "org.eclipse.jetty" % "jetty-webapp" % V.Jetty % "jetty"
    val junit = "junit" % "junit" % "4.8" % "test"
    val logback = "ch.qos.logback" % "logback-classic" % V.Logback % "test"
    val scalatest = "org.scalatest" % "scalatest_2.9.1" % V.Scalatest % "test"
    val scalacheck = "org.scala-tools.testing" % "scalacheck_2.9.1" % "1.9" % "test"
    val tapestry = "org.apache.tapestry" % "tapestry-test" % V.Tapestry % "test"
  }

}
