import sbt._
import Keys._
import java.io.File

object Publish {
  final val Snapshot = "-SNAPSHOT"

  lazy val settings = Seq(
    crossPaths  := false,
    pomExtra    := baessiePomExtra,
    publishTo   := baessiePublishTo,
    credentials ++= baessieCredentials
  )

  lazy val versionSettings = Seq(
    commands += stampVersion
  )

  def baessiePomExtra = {
    <inceptionYear>2011</inceptionYear>
    <url>http://baessie.org</url>
    <organization>
      <name>Baessie</name>
      <url>http://baessie.org</url>
    </organization>
    <licenses>
      <license>
        <name>MIT</name>
        <url>URL to MIT license</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
  }

  def baessiePublishTo: Option[Resolver] = {
    val property = Option(System.getProperty("baessie.publish.repository"))
    val repo = property map { "Baessie Publish Repository" at _ }
    val m2repo = Path.userHome / ".m2" /"repository"
    repo orElse Some(Resolver.file("Local Maven Repository", m2repo))
  }

  def baessieCredentials: Seq[Credentials] = {
    val property = Option(System.getProperty("baessie.publish.credentials"))
    property map (f => Credentials(new File(f))) toSeq
  }

  def stampVersion = Command.command("stamp-version") { state =>
    append((version in ThisBuild ~= stamp) :: Nil, state)
  }

  // TODO: replace with extracted.append when updated to sbt 0.10.1
  def append(settings: Seq[Setting[_]], state: State): State = {
    val extracted = Project.extract(state)
    import extracted._
    val append = Load.transformSettings(Load.projectScope(currentRef), currentRef.build, rootProject, settings)
    val newStructure = Load.reapply(session.original ++ append, structure)
    Project.setProject(session, newStructure, state)
  }

  def stamp(version: String): String = {
    if (version endsWith Snapshot) (version stripSuffix Snapshot) + "-" + timestamp(System.currentTimeMillis)
    else version
  }

  def timestamp(time: Long): String = {
    val format = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss")
    format.format(new java.util.Date(time))
  }
}
