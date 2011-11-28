import sbt._
import Keys._
import com.github.siasia.WebPlugin._
import sbt.Package.ManifestAttributes
import scala.xml._

object BaessieBuild extends Build {

  import Resolvers._

  lazy val buildSettings = Seq(
    organization := "org.baessie",
    version := "0.1-SNAPSHOT",
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
    settings = Defaults.defaultSettings ++ defaultSettings ++ settings ++ Seq(
      libraryDependencies ++= Dependencies.simulator ++ Dependencies.testkit
    ) ++ Seq(exportJars := true)
  )

  lazy val common = Project(
    id = "common",
    base = file("common"),
    settings = Defaults.defaultSettings ++ defaultSettings ++ settings ++ Seq(
      libraryDependencies ++= Dependencies.simulator ++ Dependencies.testkit
    ) ++ Seq(exportJars := true)
  )

  lazy val simulator = Project(
    id = "simulator",
    base = file("simulator"),
    settings = Defaults.defaultSettings ++ defaultSettings ++ settings ++ webSettings ++ Seq(
      libraryDependencies ++= Dependencies.simulator ++ Dependencies.jetty ++ Dependencies.testkit
    )
  ) dependsOn(common, wsconnector, testtools % "test")

  lazy val wsconnector = Project(
    id = "wsconnector",
    base = file("wsconnector"),
    settings = Defaults.defaultSettings ++ defaultSettings ++ settings ++ Seq(
      libraryDependencies ++= Dependencies.simulator ++ Dependencies.testkit
    ) ++ Seq(exportJars := true) ++ Seq(packageOptions := Seq(ManifestAttributes(("Tapestry-Module-Classes", "org.baessie.ws.services.WsModule"))))
  ) dependsOn(common, testtools % "test")

  // Settings

  override lazy val settings = super.settings ++ buildSettings ++ dependencySettings

  object Resolvers {
    val sonatypeNexusSnapshots = "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    val sonatypeNexusStaging = "Sonatype Nexus Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  }

  lazy val publishSetting = publishTo <<= (version) {
    version: String =>
      if (version.trim.endsWith("SNAPSHOT"))
        Some(sonatypeNexusSnapshots)
      else
        Some(sonatypeNexusStaging)
  }

  lazy val mavenCentral = Seq(
    homepage := Some(new URL("http://github.com/Baessie")),
    startYear := Some(2011),
    licenses := Seq(("Simplified BSD License", new URL("http://github.com/Baessie/Baessie/raw/HEAD/LICENSE.txt"))),
    pomExtra <<= (pomExtra, name, description) {
      (pom, name, desc) => pom ++ Group(
        <scm>
          <url>http://github.com/Baessie/Baessie</url>
          <connection>scm:git:git://github.com/Baessie/Baessie.git</connection>
        </scm>
        <inceptionYear>2011</inceptionYear>
        <url>http://baessie.org</url>
        <organization>
          <name>Baessie</name>
          <url>http://baessie.org</url>
        </organization>
        <developers>
          <developer>
            <id>argoyle</id>
            <name>Joakim Olsson</name>
            <email>joakim@unbound.se</email>
            <url>https://github.com/argoyle</url>
          </developer>
          <developer>
            <id>jotu</id>
            <name>Joacim Turesson</name>
            <email>joacim.turesson@autenta.se</email>
            <url>https://github.com/jotu</url>
          </developer>
          <developer>
            <id>pliljenberg</id>
            <name>Peter Liljenberg</name>
            <email>pliljenberg@gmail.com</email>
            <url>https://github.com/pliljenberg</url>
          </developer>
        </developers>
      )
    }
  )

  // https://github.com/harrah/xsbt/issues/257#issuecomment-2697049
  lazy val issue257Hack = pomPostProcess :=
    Rewrite.rewriter {
      case e: Elem if e.label == "classifier" &&
        Set("sources", "javadoc").contains(e.child.mkString) =>
        NodeSeq.Empty
    }

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

  lazy val baessieSettings = Defaults.defaultSettings ++ Seq(
    publishSetting,
    issue257Hack,
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
  ) ++ mavenCentral

  lazy val parentSettings = baessieSettings ++ Seq(
    publishArtifact in Compile := true
  )

  lazy val defaultSettings = baessieSettings ++ Seq(
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

  val testkit = Seq(Test.scalatest, Test.junit, Test.tapestry, Test.jetty, Test.jettyWebapp)

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
    val jettyRun = "org.eclipse.jetty" % "jetty-webapp" % V.Jetty % "container"
    val junit = "junit" % "junit" % "4.8" % "test"
    val logback = "ch.qos.logback" % "logback-classic" % V.Logback % "test"
    val scalatest = "org.scalatest" % "scalatest_2.9.1" % V.Scalatest % "test"
    val scalacheck = "org.scala-tools.testing" % "scalacheck_2.9.1" % "1.9" % "test"
    val tapestry = "org.apache.tapestry" % "tapestry-test" % V.Tapestry % "test"
  }

}
