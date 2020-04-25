import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kmongoVersion = "3.11.0"
val javaJwtVersion = "3.8.1"
val mockkioVersion = "1.9.3"
val junitVersion = "5.5.2"
val assertJVersion = "3.11.1"
val embeddedMongoVersion = "2.2.0"
val muLoggingVersion = "1.5.9"
val slf4jVersion = "1.7.5"

plugins {
	id("org.springframework.boot") version "2.2.4.RELEASE"
	id("io.spring.dependency-management") version "1.0.7.RELEASE"
	kotlin("jvm") version "1.3.61"
	kotlin("plugin.spring") version "1.3.61"
    `maven-publish`
}

group = "com.kilchichakov"
version = "20.5.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenLocal()
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("org.junit:junit-bom:$junitVersion")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.litote.kmongo:kmongo:$kmongoVersion")
	implementation("com.auth0:java-jwt:$javaJwtVersion")
	implementation("io.github.microutils:kotlin-logging:$muLoggingVersion")
	implementation("org.slf4j:slf4j-api:$slf4jVersion")
	//implementation("org.slf4j:slf4j-log4j12:$slf4jVersion")

	testImplementation("dev.ktobe:ktobe:0.0.1")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("io.mockk:mockk:$mockkioVersion")

	testImplementation("org.junit.jupiter:junit-jupiter-api")
	testImplementation("org.junit.jupiter:junit-jupiter-engine")
	testImplementation("org.junit.jupiter:junit-jupiter-params")
	testImplementation("org.junit.platform:junit-platform-launcher")
	testImplementation("org.junit.platform:junit-platform-runner")
	testImplementation("org.junit.platform:junit-platform-commons")

	testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:$embeddedMongoVersion")
	testImplementation("org.assertj:assertj-core:$assertJVersion")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

tasks.test {
	useJUnitPlatform()

	maxHeapSize = "1G"
	exclude("**/*RepositoryTest*")
}

sourceSets {
	test {
		java {
			srcDir("src/test/unit/kotlin")
			srcDir("src/test/integration/kotlin")
			srcDir("src/test/mongo/kotlin")
		}
	}
}


configure<ProcessResources>("processResources") {
	filesMatching("application.properties") {
		expand(project.properties)
	}
}

publishing {
	repositories {
		maven {
			name = "GitHubPackages"
			url = uri("https://maven.pkg.github.com/augur/five-letters")
			credentials {
				username = System.getenv("GPR_USER")
				password = System.getenv("GPR_API_KEY")
			}
		}
	}
	publications {
		register("gpr", MavenPublication::class) {
			from(components["java"])
		}
	}
}

inline fun <reified C> Project.configure(name: String, configuration: C.() -> Unit) {
	(this.tasks.getByName(name) as C).configuration()
}