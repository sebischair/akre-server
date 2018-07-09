name := """AKRec"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava, LauncherJarPlugin)

scalaVersion := "2.11.7"

lazy val webJarsPlay = file("..").getAbsoluteFile.toURI

libraryDependencies ++= Seq(
  filters,
  javaJdbc,
  cache,
  javaWs,
  "com.google.code.gson" % "gson" % "2.8.0",
  "org.jsoup" % "jsoup" % "1.8.3",
  "org.webjars" %% "webjars-play" % "2.5.0",
  "org.webjars" % "bootstrap" % "3.3.6",
  "org.webjars" % "html5shiv" % "3.7.3",
  "org.mongodb.morphia" % "morphia" % "1.2.1",
  "org.mongodb" % "mongo-java-driver" % "3.5.0",

  // DKPro Core components
  "de.tudarmstadt.ukp.dkpro.core" % "de.tudarmstadt.ukp.dkpro.core-asl" % "1.7.0",
  "de.tudarmstadt.ukp.dkpro.core" % "de.tudarmstadt.ukp.dkpro.core.io.text-asl" % "1.7.0",
  "de.tudarmstadt.ukp.dkpro.core" % "de.tudarmstadt.ukp.dkpro.core.tokit-asl" % "1.7.0",
  "de.tudarmstadt.ukp.dkpro.core" % "de.tudarmstadt.ukp.dkpro.core.opennlp-asl" % "1.7.0",
  "de.tudarmstadt.ukp.dkpro.core" % "de.tudarmstadt.ukp.dkpro.core.io.xml-asl" % "1.7.0", // for testing only!
  "de.tudarmstadt.ukp.dkpro.core" % "de.tudarmstadt.ukp.dkpro.core.treetagger-asl" % "1.7.0",
  "de.tudarmstadt.ukp.dkpro.core" % "de.tudarmstadt.ukp.dkpro.core.matetools-gpl" % "1.7.0",
  "de.tudarmstadt.ukp.dkpro.core" % "de.tudarmstadt.ukp.dkpro.core.languagetool-asl" % "1.7.0",
  "de.tudarmstadt.ukp.dkpro.core" % "de.tudarmstadt.ukp.dkpro.core.mallet-asl" % "1.7.0",
  // Apache RUTA (initial: 2.3.0)
  "org.apache.uima" % "ruta-core" % "2.3.0",
  // HTML parser
  "net.htmlparser.jericho" % "jericho-html" % "3.3",
  "junit" % "junit" % "4.12",
  "com.sun.jersey" % "jersey-bundle" % "1.19.1",
  "javax.ws.rs" % "jsr311-api" % "1.1.1",
  "com.sun.jersey" % "jersey-client" % "1.9.1",

  //For QueryExecutor
  "org.apache.jena" % "jena-core" % "3.1.0",
  "org.apache.jena" % "jena-arq" % "3.1.0",

  //Google Trends
  "commons-configuration" % "commons-configuration" % "1.6",
  "org.apache.commons" % "commons-lang3" % "3.0",
  "commons-httpclient" % "commons-httpclient" % "3.1",

  //Apache TIKA
  "org.apache.tika" % "tika-parsers" % "1.4",

  //AYLIEN
  "com.aylien.textapi" % "client" % "0.6.1"
)
