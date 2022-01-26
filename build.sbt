ThisBuild / version := "1.0" 
ThisBuild / scalaVersion := "2.12.11" 

lazy val gcgen = (project in file("."))
    .settings(
        name := "gc-gen",
	    libraryDependencies ++= Seq(
			"net.liftweb" %% "lift-json" % "3.3.0",
			"org.apache.logging.log4j" % "log4j-api" % "2.17.0",
			"org.apache.logging.log4j" % "log4j-core" % "2.17.0",
			"org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.17.0"
    	)
	)
