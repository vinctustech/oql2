name := "@vinctus/oql" //@vinctus/

version := "1.1.30"

description := "Object Query Language"

scalaVersion := "3.4.2"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:existentials",
  "-language:dynamics",
  "-explain"
)

organization := "com.vinctus"

githubOwner := "vinctustech"

githubRepository := "oql"

Global / onChangedBuildSource := ReloadOnSourceChanges

resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

resolvers += Resolver.githubPackages("vinctustech")

resolvers += Resolver.githubPackages("edadma", "importer")

resolvers += Resolver.githubPackages("edadma", "pretty")

enablePlugins(ScalaJSPlugin)

jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv()

enablePlugins(ScalablyTypedConverterPlugin)

stTypescriptVersion := "4.8.4"

stIgnore += "source-map-support"

enablePlugins(ParadoxPlugin)
enablePlugins(ParadoxMaterialThemePlugin)
//paradoxTheme := Some(builtinParadoxTheme("generic"))

Compile / paradox / target := baseDirectory.value / "docs"

Compile / paradoxMaterialTheme := {
  ParadoxMaterialTheme()
    .withColor("teal", "indigo")
    .withFavicon("assets/images/favicon.ico")
    .withLogo("assets/images/vinctus.png")
    .withRepository(uri("https://github.com/vinctustech/oql"))
    .withCopyright("© Vinctus Technologies Inc. All Rights Reserved 2021")
    .withSocial(uri("https://github.com/vinctustech"))
    .withLanguage(java.util.Locale.ENGLISH)
    .withSearch(tokenizer = "[\\s\\-\\.]+")
}

Compile / paradoxProperties ++=
  Map(
    // "github.base_url" -> "https://github.com/vinctustech/oql/blob/dev",
    "image.base_url" -> ".../assets/images"
  )

scalaJSUseMainModuleInitializer := true

Test / scalaJSUseMainModuleInitializer := false

Test / scalaJSUseTestModuleInitializer := true

//Test / scalaJSUseMainModuleInitializer := true
//
//Test / scalaJSUseTestModuleInitializer := false

Compile / npmDependencies ++= Seq(
  "pg" -> "8.10.0",
  "@types/pg" -> "8.6.6",
  "source-map-support" -> "0.5.21"
//  "big.js" -> "6.1.1",
//  "@types/big.js" -> "6.1.3"
)

libraryDependencies ++= Seq(
  "org.scalatest" %%% "scalatest" % "3.2.15" % "test",
  "io.github.edadma" %%% "rdb" % "0.1.0-pre.42",
  "com.vinctus" %%% "sjs-utils" % "0.1.0-snapshot.33",
  "com.lihaoyi" %%% "pprint" % "0.7.1"
//  "com.vinctus" %%% "mappable" % "0.1.2"
)

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %%% "scala-parser-combinators" % "2.1.1"
  //  "org.scala-lang.modules" %%% "scala-async" % "1.0.0-M1"
)

libraryDependencies ++= Seq(
  "io.github.cquiroz" %%% "scala-java-time" % "2.5.0"
)

mainClass := Some("com.vinctus." + "oql" + ".Main")

lazy val packageName = SettingKey[String]("packageName", "package name")

packageName := "oql"

scalaJSLinkerConfig ~= { _.withESFeatures(_.withESVersion(org.scalajs.linker.interface.ESVersion.ES2018)) }

publishMavenStyle := true

Test / publishArtifact := false

pomIncludeRepository := { _ =>
  false
}

licenses := Seq("ISC" -> url("https://opensource.org/licenses/ISC"))

homepage := Some(url("https://github.com/vinctustech/" + packageName.value))

pomExtra :=
  <scm>
    <url>git@github.com:vinctustech/{packageName.value}.git</url>
    <connection>scm:git:git@github.com:vinctustech/{packageName.value}.git</connection>
  </scm>
    <developers>
      <developer>
        <id>edadma</id>
        <name>Edward A. Maxedon, Sr.</name>
        <url>https://github.com/edadma</url>
      </developer>
    </developers>
