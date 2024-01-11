plugins {
    java 
}

repositories {
    mavenCentral()
}

val generatedDir = file("${buildDir}/generated/src/main/java")
val codecGeneration = configurations.create("codecGeneration")

dependencies {
    "codecGeneration"("uk.co.real-logic:sbe-tool:1.29.0")
    implementation("org.agrona:agrona:1.20.0")
}

sourceSets {
    main {
        java.srcDir(generatedDir)
    }
}

tasks.register("generateCodecs", JavaExec::class) {
    group = "sbe"
    val codecsFile = "src/main/java/playground/sbePlay/sbe/protocol-codecs.xml"
    val sbeFile = "src/main/java/playground/sbePlay/sbe/sbe.xsd"
    inputs.files(codecsFile, sbeFile)
    outputs.dir(generatedDir)
    classpath = codecGeneration
    mainClass.set("uk.co.real_logic.sbe.SbeTool")
    args = listOf(codecsFile)
    systemProperties["sbe.output.dir"] = generatedDir
    systemProperties["sbe.target.language"] = "Java"
    systemProperties["sbe.validation.xsd"] = sbeFile
    systemProperties["sbe.validation.stop.on.error"] = "true"
    outputs.dir(generatedDir)
}


tasks.getByName("build").dependsOn("generateCodecs")
