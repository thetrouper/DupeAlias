plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
  id("xyz.jpenilla.run-paper") version "2.3.1"
  id("com.gradleup.shadow") version "9.0.0-rc1"
}

group = "me.trouper.dupealias"
version = "0.0.2"
description = "A powerful dupe plugin with niche features for servers looking to stand out."

java {
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}


repositories {
  mavenLocal()
  maven {
    name = "matteodev"
    url = uri("https://maven.devs.beer/")
  }
}

dependencies {
  paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")
  implementation("me.trouper:alias:1.0-1.21.5-SNAPSHOT")
  compileOnly("dev.lone:api-itemsadder:4.0.10")
}

tasks {
  shadowJar {
    archiveClassifier.set("")
  }

  build {
    dependsOn(shadowJar)
  }

  compileJava {
    options.release = 21
  }
  javadoc {
    options.encoding = Charsets.UTF_8.name()
  }
}