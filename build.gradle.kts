import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml
import xyz.jpenilla.resourcefactory.bukkit.Permission
//import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.net.URI

plugins {
    `java-library`
    // Check for new versions at https://plugins.gradle.org/plugin/io.papermc.paperweight.userdev
    id("io.papermc.paperweight.userdev") version "1.7.1"
    // Adds runServer and runMojangMappedServer tasks for testing
    id("xyz.jpenilla.run-paper") version "2.3.0"
    // Generates plugin.yml based on the Gradle config
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.1.1"
//    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.chaws.automaticinventory"
version = "4.0.0"
description = "Automatic Inventory PaperMC Plugin"

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "papermc"
        url = URI("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "spigotmc"
        url = URI("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "bstats"
        url = URI("https://oss.sonatype.org/content/groups/public/")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

dependencies {
    paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")
    // paperweight.foliaDevBundle("1.21-R0.1-SNAPSHOT")
    // paperweight.devBundle("com.example.paperfork", "1.21-R0.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.2")

    // Add ASM dependency to support Java 21 class files
    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-commons:9.7")
}

// Option 1)
// For >=1.20.5 when you don"t care about supporting spigot
// paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

// Option 2)
// For 1.20.4 or below, or when you care about supporting Spigot on >=1.20.5
// Configure reobfJar to run when invoking the build task
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION
tasks {
    assemble {
//        dependsOn(shadowJar)
        dependsOn(reobfJar)
    }

//    named<ShadowJar>("shadowJar") {
//
//        relocate("org.bstats", "dev.chaws.automaticinventory.org.bstats")
//    }
}

bukkitPluginYaml {
    main = "dev.chaws.automaticinventory.AutomaticInventory"
    // TODO: Try POSTWORLD
    load = BukkitPluginYaml.PluginLoadOrder.STARTUP
    authors = listOf("Chaws", "Pugabyte", "AllTheCode", "RoboMWM", "Big_Scary")
    apiVersion = "1.21"
    commands.register("autosort") {
        description = "Toggles auto-sorting options."
        permission = "automaticinventory.sortchests"
        usage = "/AutoSort"
    }
    commands.register("depositall") {
        aliases = listOf("da", "dumpitems", "dumploot", "depositloot")
        description = "Deposits your non-hotbar inventory into any nearby chests containing matching items."
        permission = "automaticinventory.depositall"
        usage = "/DepositAll"
    }
    commands.register("quickdeposit") {
        description = "Toggles quick deposit (shift+left click on chests)."
        permission = "automaticinventory.quickdeposit"
        usage = "/quickdeposit"
    }
    commands.register("autorefill") {
        description = "Toggles auto refill, which refills your hotbar slots when items are depleted or break."
        permission = "automaticinventory.refillstacks"
        usage = "/autorefill"
    }

    permissions.register("automaticinventory.admin.*") {
        description = "Grants all administrative privileges."
        children = mapOf(
            "automaticinventory.user.*" to true
        )
    }
    permissions.register("automaticinventory.user.*") {
        description = "Grants all user privileges."
        children = mapOf(
            "automaticinventory.sortinventory" to true,
            "automaticinventory.sortchests" to true,
            "automaticinventory.refillstacks" to true,
            "automaticinventory.quickdeposit" to true,
            "automaticinventory.depositall" to true
        )
    }
    permissions.register("automaticinventory.sortinventory") {
        description = "Grants permission to auto-sort personal inventory."
        default = Permission.Default.TRUE
    }
    permissions.register("automaticinventory.sortchests") {
        description = "Grants permission to auto-sort chest content."
        default = Permission.Default.TRUE
    }
    permissions.register("automaticinventory.refillstacks") {
        description = "Grants permission to auto-refill depleted hotbar stacks."
        default = Permission.Default.TRUE
    }
    permissions.register("automaticinventory.quickdeposit") {
        description = "Grants permission to auto-deposit matching items into a chest with shift-right-click."
        default = Permission.Default.TRUE
    }
    permissions.register("automaticinventory.depositall") {
        description = "Grants permission to use /depositall."
        default = Permission.Default.TRUE
    }
}
