import sbt._
import Keys._

object ScalatestBuild extends Build {

   val includeTestPackageSet = Set("org.scalatest", 
                                   "org.scalatest.fixture", 
                                   "org.scalatest.concurrent", 
                                   "org.scalatest.testng", 
                                   "org.scalatest.junit", 
                                   "org.scalatest.events", 
                                   "org.scalatest.prop", 
                                   "org.scalatest.tools", 
                                   "org.scalatest.matchers", 
                                   "org.scalatest.suiteprop", 
                                   "org.scalatest.mock", 
                                   "org.scalatest.path")

   val excludedSuiteSet = Set("org.scalatest.BigSuite", 
                              "org.scalatest.events.TestLocationJUnit3Suite", 
                              "org.scalatest.events.TestLocationJUnitSuite", 
                              "org.scalatest.events.TestLocationMethodJUnit3Suite", 
                              "org.scalatest.events.TestLocationMethodJUnitSuite", 
                              "org.scalatest.events.TestLocationMethodTestNGSuite", 
                              "org.scalatest.events.TestLocationTestNGSuite", 
                              "org.scalatest.tools.SomeApiClassRunner", 
                              "org.scalatest.PackageAccessConstructorSuite")
                              
   lazy val root = Project("scalatest", file(".")) settings(
     organization := "org.scalatest",
     version := "1.9-2.10.0-M6-B2",
     scalaVersion := "2.10.0-M6",
     libraryDependencies ++= simpledependencies,
     resolvers += "Sonatype Public" at "https://oss.sonatype.org/content/groups/public",
     sourceGenerators in Compile <+= 
         (baseDirectory, sourceManaged in Compile) map genFiles("gen-main", "GenGen.scala")(GenGen.genMain),
     sourceGenerators in Test <+= 
         (baseDirectory, sourceManaged in Test) map genFiles("gen-test", "GenGen.scala")(GenGen.genTest),
     sourceGenerators in Compile <+= 
         (baseDirectory, sourceManaged in Compile) map genFiles("tables-main", "GenTable.scala")(GenTable.genMain),
     sourceGenerators in Test <+= 
         (baseDirectory, sourceManaged in Test) map genFiles("tables-test", "GenTable.scala")(GenTable.genTest), 
     sourceGenerators in Compile <+=
         (baseDirectory, scalaSource in Compile, sourceManaged in Compile) map genCode("gen-must-matchers", "GenMustMatchers.scala")(GenMustMatchers.genMain),
     sourceGenerators in Test <+=
         (baseDirectory, scalaSource in Test, sourceManaged in Test) map genCode("gen-must-matcher-tests", "GenMustMatchers.scala")(GenMustMatchers.genTest),
     testOptions in Test := Seq(Tests.Filter(className => isIncludedTest(className)))
   )

   def isIncludedTest(className: String) = {
     try {
       val packageName = className.substring(0, className.lastIndexOf("."))
       includeTestPackageSet.contains(packageName) && !excludedSuiteSet.contains(className)
     }
     catch {
       case e: Exception => 
         e.printStackTrace()
         false
     }
   }

   def simpledependencies = Seq(
     "org.scala-lang" % "scala-actors" % "2.10.0-M6",
     "org.scala-tools.testing" % "test-interface" % "0.5",  // TODO optional
     "org.scalacheck" % "scalacheck_2.10.0-M6" % "1.10.0",   // TODO optional
     "org.easymock" % "easymockclassextension" % "3.1",   // TODO optional
     "org.jmock" % "jmock-legacy" % "2.5.1", // TODO optional
     "org.mockito" % "mockito-all" % "1.9.0", // TODO optional
     "org.testng" % "testng" % "6.3.1",  // TODO optional
     "com.google.inject" % "guice" % "3.0", // TODO optional
     "junit" % "junit" % "4.10", // TODO optional
     "org.apache.ant" % "ant" % "1.7.1", // TODO optional
     "net.sourceforge.cobertura" % "cobertura" % "1.9.1" % "test",
     "commons-io" % "commons-io" % "1.3.2" % "test"
  )
  
  def genFiles(name: String, generatorSource: String)(gen: File => Unit)(basedir: File, outDir: File): Seq[File] = {
    val tdir = outDir / name
    val genTableSource = basedir / "project" / generatorSource
    def results = (tdir ** "*.scala").get
    if (results.isEmpty || results.exists(_.lastModified < genTableSource.lastModified)) {
      tdir.mkdirs()
      gen(tdir)
    }
    results
  }
  
  def genCode(name: String, generatorSource: String)(gen: (File,File) => Unit)(basedir: File, sourceDir: File, outDir: File): Seq[File] = {
    val tdir = outDir / name
    val genTableSource = basedir / "project" / generatorSource
    def results = (tdir ** "*.scala").get
    if (results.isEmpty || results.exists(_.lastModified < genTableSource.lastModified)) {
      tdir.mkdirs()
      gen(sourceDir / "org/scalatest", tdir / "org/scalatest")
    }
    results
  }
}
