name := """Akre"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava, LauncherJarPlugin)

scalaVersion := "2.11.11"

lazy val webJarsPlay = file("..").getAbsoluteFile.toURI

libraryDependencies ++= Seq(
  filters,
  javaWs,
  "com.google.code.gson" % "gson" % "2.8.0",
  "org.jsoup" % "jsoup" % "1.8.3",
  "org.mongodb.morphia" % "morphia" % "1.2.1",

  "org.apache.uima" % "uimaj-core" % "2.3.1",
  "de.tudarmstadt.ukp.dkpro.core" % "de.tudarmstadt.ukp.dkpro.core.io.text-asl" % "1.7.0",


  "junit" % "junit" % "4.12",
  "com.sun.jersey" % "jersey-bundle" % "1.19.1",
  "javax.ws.rs" % "jsr311-api" % "1.1.1",
  "com.sun.jersey" % "jersey-client" % "1.9.1",
  //For QueryExecutor
  "org.apache.jena" % "jena-arq" % "3.1.0",
  //Google Trends
  "commons-configuration" % "commons-configuration" % "1.6",
  "org.apache.commons" % "commons-lang3" % "3.0",
  "commons-httpclient" % "commons-httpclient" % "3.1",
  "org.apache.tika" % "tika-parsers" % "1.4",
  //AYLIEN
  "com.aylien.textapi" % "client" % "0.6.1",
  //HTML to plain text
  "org.jsoup" % "jsoup" % "1.8.3"
)

unmanagedResourceDirectories in (Compile, runMain) <+=  baseDirectory ( _ /"../myresources")

routesGenerator := InjectedRoutesGenerator