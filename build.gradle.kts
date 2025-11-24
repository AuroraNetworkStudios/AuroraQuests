import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import xyz.jpenilla.runtask.task.AbstractRun
import groovy.util.Node
import groovy.util.NodeList
import java.net.URI
import java.util.*

fun loadProperties(filename: String): Properties {
    val properties = Properties()
    if (!file(filename).exists()) {
        return properties
    }
    file(filename).inputStream().use { properties.load(it) }
    return properties
}

plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.3"
    id("maven-publish")
    id("xyz.jpenilla.run-paper") version "2.3.0"
}

group = "gg.auroramc"
version = "2.2.0"

repositories {
    flatDir {
        dirs("libs")
    }
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.auroramc.gg/releases/")
    maven("https://repo.auroramc.gg/snapshots/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://maven.citizensnpcs.co/repo")
    maven("https://jitpack.io/")
    //maven("https://repo.projectshard.dev/repository/releases/")
    maven("https://repo.oraxen.com/releases")
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
    maven("https://repo.fancyinnovations.com/releases")
    maven("https://repo.tabooproject.org/repository/releases/")
    maven("https://repo.nexomc.com/releases/")
    maven("https://repo.nightexpressdev.com/releases")
    maven("https://repo.pyr.lol/snapshots")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("gg.auroramc:Aurora:2.4.0-SNAPSHOT")
    compileOnly("gg.auroramc:AuroraLevels:1.6.2")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("dev.aurelium:auraskills-api-bukkit:2.2.0")
    compileOnly("io.lumine:Mythic-Dist:5.6.1")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.7")
    compileOnly("net.citizensnpcs:citizens-main:2.0.33-SNAPSHOT") {
        exclude(group = "*", module = "*")
    }
    compileOnly("com.github.Xiao-MoMi:Custom-Fishing:2.3.3")
    //compileOnly("com.nisovin.shopkeepers:ShopkeepersAPI:2.23.3")
    compileOnly("com.github.Gypopo:EconomyShopGUI-API:1.7.2")
    compileOnly("io.th0rgal:oraxen:1.179.0")
    compileOnly("com.github.brcdev-minecraft:shopgui-api:3.0.0") {
        exclude(group = "org.spigotmc", module = "spigot-api")
    }
    compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
    compileOnly(name = "MythicDungeons-2.0.0-SNAPSHOT", group = "net.playavalon", version = "2.0.0-SNAPSHOT")
    compileOnly(name = "znpcs-5.0", group = "io.github.gonalez.znpcs", version = "5.0")
    compileOnly(name = "Shopkeepers-2.23.3", group = "com.nisovin.shopkeepers", version = "2.23.3")
    compileOnly(name = "SuperiorSkyblock2-2025.1", group = "com.bgsoftware", version = "2025.1")
    //compileOnly("com.bgsoftware:SuperiorSkyblockAPI:2025.1")
    compileOnly("lol.pyr:znpcsplus-api:2.1.0-SNAPSHOT")
    compileOnly("de.oliver:FancyNpcs:2.6.0")
    compileOnly("ink.ptms.adyeshach:all:2.0.0-snapshot-1")
    compileOnly("com.nexomc:nexo:1.8.0")
    compileOnly("su.nightexpress.excellentshop:Core:4.20.0") {
        exclude(group = "com.github.Xyness", module = "SimpleClaimSystem")
    }
    compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.9.5-SNAPSHOT")

    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.2")

    compileOnly("org.quartz-scheduler:quartz:2.3.2")
    compileOnly("com.cronutils:cron-utils:9.2.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<ShadowJar> {
    archiveFileName.set("AuroraQuests-${project.version}.jar")

    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }

    relocate("co.aikar.commands", "gg.auroramc.quests.libs.acf")
    relocate("co.aikar.locales", "gg.auroramc.quests.libs.locales")
    relocate("org.bstats", "gg.auroramc.quests.libs.bstats")

    exclude("acf-*.properties")
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

runPaper.folia.registerTask()

tasks {
    build {
        dependsOn(shadowJar)
    }
    runServer {
        downloadPlugins {
            modrinth("AuroraLib", "2.4.1")
            //hangar("PlaceholderAPI", "2.11.6")
            //url("https://download.luckperms.net/1606/bukkit/loader/LuckPerms-Bukkit-5.5.17.jar")
        }
        minecraftVersion("1.21.10")
    }
}

val publishing = loadProperties("publish.properties")

publishing {
    repositories {
        maven {
            name = "AuroraMC"
            url = if (version.toString().endsWith("SNAPSHOT")) {
                URI.create("https://repo.auroramc.gg/snapshots/")
            } else {
                URI.create("https://repo.auroramc.gg/releases/")
            }
            credentials {
                username = publishing.getProperty("username")
                password = publishing.getProperty("password")
            }
        }
    }

    publications.create<MavenPublication>("mavenJava") {
        groupId = "gg.auroramc"
        artifactId = "AuroraQuests"
        version = project.version.toString()

        from(components["java"])

        pom.withXml {
            val dependency = (asNode().get("dependencies") as NodeList).first() as Node
            (dependency.get("dependency") as NodeList).forEach {
                val node = it as Node
                val artifactIdList = node.get("artifactId") as NodeList
                val artifactId = (artifactIdList.first() as Node).text()
                if (artifactId in listOf("acf-paper")) {
                    assert(it.parent().remove(it))
                }
            }
        }
    }
}

tasks.withType<AbstractRun>().configureEach {
//    javaLauncher = javaToolchains.launcherFor {
//        vendor.set(JvmVendorSpec.JETBRAINS)
//        languageVersion.set(JavaLanguageVersion.of(21))
//    }
    jvmArgs(
        // "-XX:+AllowEnhancedClassRedefinition", //
        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" // Enable remote debugging
    )
}