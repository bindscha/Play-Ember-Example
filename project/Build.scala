import sbt._
import Keys._
import play.Project._

object SampleBuild extends Build {

  lazy val CommonProject = play.Project(
    BuildSettings.PROJECT_NAME + "-Common",
    path = file("modules/common")).settings(
      (BuildSettings.GLOBAL_SETTINGS ++
        Seq(
          libraryDependencies ++= (Dependencies.DATABASE ++ Dependencies.HASHING),
          resolvers ++= Resolvers.TYPESAFE)))

  lazy val WebsiteProject = play.Project(
    BuildSettings.PROJECT_NAME + "-Website",
    path = file("modules/website")).settings(
      (BuildSettings.GLOBAL_SETTINGS ++
        Seq(
          libraryDependencies ++= (Dependencies.DATABASE ++ Dependencies.HASHING ++ Dependencies.MAIL),
          resolvers ++= Resolvers.TYPESAFE))).dependsOn(CommonProject)

  lazy val ApiProject = play.Project(
    BuildSettings.PROJECT_NAME + "-API",
    path = file("modules/api")).settings(
      (BuildSettings.GLOBAL_SETTINGS ++
        Seq(
          resolvers ++= Resolvers.TYPESAFE))).dependsOn(CommonProject)

  lazy val AdminAreaProject = play.Project(
    BuildSettings.PROJECT_NAME + "-Admin",
    path = file("modules/admin")).settings(
      (BuildSettings.GLOBAL_SETTINGS ++
        Seq(
          resolvers ++= Resolvers.TYPESAFE))).dependsOn(CommonProject)

  lazy val SampleProject = play.Project(
    BuildSettings.PROJECT_NAME).settings(
      (BuildSettings.GLOBAL_SETTINGS ++
        com.typesafe.sbt.SbtScalariform.scalariformSettings ++
        Seq(
          libraryDependencies ++= (Dependencies.DATABASE ++ Dependencies.HASHING ++ Dependencies.MAIL),
          resolvers ++= Resolvers.TYPESAFE,

          // Generate Eclipse configuration for root project
          com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys.skipParents in ThisBuild := false,

          // Override doc task to generate one documentation for all subprojects 
          doc <<= Tasks.docTask(file("documentation/api")),
          aggregate in doc := false))).dependsOn(
        WebsiteProject,
        ApiProject,
        AdminAreaProject)

  object BuildSettings {
    val ORGANIZATION = "com.lakemind"
    val ORGANIZATION_NAME = "LakeMind"
    val ORGANIZATION_HOMEPAGE = "http://www.lakemind.com"

    val PROJECT_NAME = "Sample"
    val PROJECT_VERSION = "0.1.0"

    val INCEPTION_YEAR = 2011

    val PUBLISH_DOC = Option(System.getProperty("publish.doc")).isDefined

    val SCALA_VERSION = "2.10.0"
    val BINARY_SCALA_VERSION = CrossVersion.binaryScalaVersion(SCALA_VERSION)

    val GLOBAL_SETTINGS: Seq[sbt.Project.Setting[_]] = Seq(
      organization := ORGANIZATION,
      organizationName := ORGANIZATION_NAME,
      organizationHomepage := Some(url(ORGANIZATION_HOMEPAGE)),

      version := PROJECT_VERSION,

      scalaVersion := SCALA_VERSION,
      scalaBinaryVersion := BINARY_SCALA_VERSION,

      publishArtifact in packageDoc := PUBLISH_DOC,

      scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-encoding", "utf8"),
      javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-g", "-encoding", "utf8"))
  }

  object Resolvers {
    val TYPESAFE_RELEASES = "Typesafe Releases Repository" at "http://repo.typesafe.com/typesafe/releases/"
    val TYPESAFE_SNAPSHOTS = "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/"
    val TYPESAFE = Seq(TYPESAFE_RELEASES) ++ (if (BuildSettings.PROJECT_VERSION.endsWith("SNAPSHOT")) Seq(TYPESAFE_SNAPSHOTS) else Nil)
  }

  object Dependencies {

    val AKKA = Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.1.2",
      "com.typesafe.akka" %% "akka-agent" % "2.1.2",
      "com.typesafe.akka" %% "akka-remote" % "2.1.2",
      "com.typesafe.akka" %% "akka-zeromq" % "2.1.2",
      "org.zeromq" %% "zeromq-scala-binding" % "0.0.6",
      "com.typesafe.akka" %% "akka-kernel" % "2.1.2",
      "com.typesafe.akka" %% "akka-slf4j" % "2.1.2",
      "ch.qos.logback" % "logback-classic" % "1.0.11")

    val DATABASE = Seq(
      jdbc,
      anorm,
      "com.typesafe.slick" %% "slick" % "1.0.0",
      "com.typesafe.play" %% "play-slick" % "0.3.2",
      "postgresql" % "postgresql" % "9.1-901.jdbc4")

    val HASHING = Seq(
      "org.mindrot" % "jbcrypt" % "0.3m")

    val MAIL = Seq(
      "com.typesafe" %% "play-plugins-mailer" % "2.1.0")

  }

  object Tasks {

    // ----- Generate documentation
    def docTask(docRoot: java.io.File, maximumErrors: Int = 10) = (dependencyClasspath in Test, compilers, streams) map { (classpath, compilers, streams) =>
      // Clear the previous version of the doc
      IO.delete(docRoot)

      // Grab all jars and source files
      val jarFiles = (file("app") ** ("*.scala" || "*.java") +++ (file("modules") ** ("*.jar"))).get
      val sourceFiles = (file("app") ** ("*.scala" || "*.java") +++ (file("modules") ** ("*.scala" || "*.java"))).get

      // Run scaladoc
      new Scaladoc(maximumErrors, compilers.scalac)(
        BuildSettings.PROJECT_NAME + " " + BuildSettings.PROJECT_VERSION + " - " + "Scala API",
        sourceFiles,
        classpath.map(_.data) ++ jarFiles,
        docRoot,
        Seq(
          "-external-urls:" + (Map(
            "scala" -> "http://www.scala-lang.org/api/current/") map (p => p._1 + "=" + p._2) mkString (";")),
          "-skip-packages", Seq(
            "controllers") mkString (":"),
          "-doc-footer", "Copyright (c) " +
            BuildSettings.INCEPTION_YEAR + "-" + java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) +
            " " + BuildSettings.ORGANIZATION_NAME + ". All rights reserved.",
          "-diagrams"),
        streams.log)

      // Return documentation root
      docRoot
    }

  }

  // ----- Augment sbt.Project with a settings method that takes a Seq

  class ImprovedProject(val sbtProject: Project) {
    def settings(ss: Seq[sbt.Project.Setting[_]]): Project =
      sbtProject.settings(ss: _*)
  }

  implicit def project2improvedproject(sbtProject: Project): ImprovedProject = new ImprovedProject(sbtProject)
  implicit def improvedproject2project(improvedProject: ImprovedProject): Project = improvedProject.sbtProject

}
