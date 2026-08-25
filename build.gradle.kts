import dev.lambdaurora.mcdev.api.McVersionLookup
import dev.lambdaurora.mcdev.api.ModUtils
import dev.lambdaurora.mcdev.api.ModVersionDependency
import dev.lambdaurora.mcdev.task.packaging.PackageModrinthTask

plugins {
	alias(libs.plugins.loom)
	alias(libs.plugins.lambdamcdev)
	alias(libs.plugins.licenser)
	`java-library`
	`maven-publish`
}

base.archivesName.set(project.property("mod_namespace") as String)

val mcVersion = libs.versions.minecraft.get()
val compatibleMcVersions: Set<String> = setOf("1.20")
val VERSION = project.property("mod_version") as String
version = "$VERSION+$mcVersion"

// This field defines the Java version your mod target.
val targetJavaVersion = Integer.parseInt(project.property("java_version").toString())

repositories {
	mavenCentral()
	maven {
		name = "ParchmentMC"
		url = uri("https://maven.parchmentmc.org/")
		content {
			includeGroup("org.parchmentmc.data")
		}
	}
	maven {
		name = "Gegy"
		url = uri("https://maven.gegy.dev/releases/")
		content {
			includeGroupAndSubgroups("dev.lambdaurora")
		}
	}

	exclusiveContent {
		forRepository {
			maven {
				name = "Ladysnake Libs"
				url = uri("https://maven.ladysnake.org/releases")
			}
		}
		filter {
			includeGroup("dev.onyxstudios.cardinal-components-api")
		}
	}
	exclusiveContent {
		forRepository {
			maven {
				name = "TerraformersMC"
				url = uri("https://maven.terraformersmc.com/")
			}
		}
		filter {
			includeGroup("dev.emi")
			includeGroupAndSubgroups("com.terraformersmc")
		}
	}
}

loom {
	splitEnvironmentSourceSets()
	mixin {
		useLegacyMixinAp = false
	}
}

fabricApi {
	configureDataGeneration {
		client = true
	}
	/*configureTests {
		eula = true
	}*/
}

dependencies {
	minecraft(libs.minecraft)
	@Suppress("UnstableApiUsage")
	mappings(loom.layered {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-1.20.1:2023.09.03@zip")
		mappings("dev.lambdaurora:yalmm-mojbackward:${mcVersion}+build.${libs.versions.mappings.yalmm.get()}")
	})
	modImplementation(libs.fabric.loader)
	modImplementation(libs.fabric.api)

	compileOnly(libs.jspecify)
	implementation(libs.yumi.commons.event)
	include(libs.yumi.commons.core)
	include(libs.yumi.commons.collections)
	include(libs.yumi.commons.event)
	modImplementation(libs.spruceui)
	include(libs.spruceui)

	"modClientImplementation"(libs.trinkets) {
		exclude(group = libs.fabric.loader.get().group)
		exclude(group = libs.fabric.api.get().group)
	}
}

java {
	sourceCompatibility = JavaVersion.toVersion(targetJavaVersion)
	targetCompatibility = JavaVersion.toVersion(targetJavaVersion)

	withSourcesJar()
}

lambdamcdev {
	/*manifests {
		fmj {
			val sourcesLink = "https://github.com/LambdAurora/AurorasLanterns"

			withName(project.property("mod_name") as String)
			withDescription(project.property("mod_description") as String)
			withAuthors("LambdAurora")
			withContact {
				it.withHomepage("https://lambdaurora.dev/projects/aurorascanvas")
					.withSources("$sourcesLink.git")
					.withIssues("$sourcesLink/issues")
			}
			withLicense("Lambda License")
			withIcon("assets/${namespace.get()}/icon.png")
			withEnvironment("*")
			withEntrypoints("yumi:init", "dev.lambdaurora.aurorascanvas.AurorasCanvas")
			withEntrypoints("yumi:client_init", "dev.lambdaurora.aurorascanvas.client.AurorasCanvasClient")
			withEntrypoints("fabric-datagen", "dev.lambdaurora.aurorascanvas.resource.AurorasCanvasStaticDatagen")
			withMixins(
				MixinEntry("${namespace.get()}.mixins.json"),
				MixinEntry("${namespace.get()}.client.mixins.json", ModEnvironment.CLIENT),
			)
			withDepend("fabricloader", ">=${libs.versions.fabric.loader.get()}")
			withDepend("minecraft", project.property("fabric_mc_constraints").toString())
			withDepend("java", ">=$targetJavaVersion")
			withDepend("fabric-api", ">=${libs.versions.fabric.api.get()}")
			withBreak("aurorasdeco", "<=1.0.0-beta.22")
			withModMenu {
				it.withCurseForge("https://www.curseforge.com/minecraft/mc-mods/aurorascanvas")
					.withDiscord("https://discord.lambdaurora.dev/")
					.withGitHubReleases("$sourcesLink/releases")
					.withModrinth("https://modrinth.com/mod/aurorascanvas")
					.withLink("modmenu.bluesky", "https://bsky.app/profile/lambdaurora.dev")
					.withLink("modmenu.donate", "https://donate.lambdaurora.dev/")
			}
		}

		if (supportNeoforge) {
			val fmj = this.fmj().get()

			nmt {
				fmj.copyTo(this)
				withLoaderVersion("[2,)")
				withBlurIcon(false)
				withYumiEntrypoints(
					"yumi:init",
					"dev.lambdaurora.auroraslanterns.AurorasLanterns",
					"dev.lambdaurora.auroraslanterns.platform.neoforge.NeoAurorasLanterns",
				)
				withYumiEntrypoints("yumi:client_init", "dev.lambdaurora.auroraslanterns.client.AurorasLanternsClient")
				withAccessTransformer("META-INF/accesstransformer.cfg")
				withMixins("${namespace.get()}.mixins.json", "${namespace.get()}.client.mixins.json")
				withDepend("minecraft", project.property("neoforge_mc_constraints").toString())
				withDepend("yumi_mc_core", "[${libs.versions.yumi.mc.foundation.get()},)")
				withDepend("fabric_api", "[${libs.versions.fabric.api.get()},)")
			}
		}
	}

	setupActionsRefCheck()*/
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.isDeprecation = true
	options.isIncremental = true
	options.release.set(targetJavaVersion)
}

tasks.processResources {
	inputs.property("version", project.version)

	filesMatching("fabric.mod.json") {
		expand("version" to (inputs.properties["version"] as String))
	}

	exclude(".cache/**")
}

tasks.jar {
	inputs.property("archivesName", base.archivesName)

	from("LICENSE") {
		rename { "${it}_${inputs.properties["archivesName"]}" }
	}
}

license {
	rule(file("metadata/HEADER"))
	include("**/*.java")
}

val README = ModUtils.parseReadme(
	project, "https://raw.githubusercontent.com/LambdAurora/AurorasCanvas/1.20/\$2"
)
val CHANGELOG_CONTENT = ModUtils.fetchChangelog(project, VERSION)

tasks.register<PackageModrinthTask>("packageModrinth") {
	this.group = "publishing"
	this.versionType.set(ModUtils.getVersionType(VERSION, mcVersion))
	this.versionName.set("${project.property("mod_name")} $VERSION (${McVersionLookup.getVersionTag(mcVersion)})")
	this.gameVersions.set(listOf(mcVersion) + compatibleMcVersions)
	this.loaders.set(listOf("fabric", "quilt"))
	this.dependencies.set(
		listOf(
			ModVersionDependency("P7dR8mSH", ModVersionDependency.Type.REQUIRED), // Fabric API
		)
	)
	this.changelog.set(CHANGELOG_CONTENT)
	this.readme.set(README)
	this.files.setFrom(tasks.remapJar.get())
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])

			pom {
				name.set(project.property("mod_name") as String)
				description.set("")
			}
		}
	}

	repositories {
		mavenLocal()
	}
}
