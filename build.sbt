ThisBuild / version := "1.0" 
ThisBuild / scalaVersion := "2.12.11" 

lazy val gcgen = (project in file("."))
    .settings(
        name := "gc-gen",
	    libraryDependencies ++= Seq(
			"net.liftweb" %% "lift-json" % "3.3.0",
    	)
	)
