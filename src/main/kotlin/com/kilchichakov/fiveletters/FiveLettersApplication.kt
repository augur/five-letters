package com.kilchichakov.fiveletters

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(exclude = [MongoAutoConfiguration::class, MongoDataAutoConfiguration::class])
@EnableScheduling
class FiveLettersApplication

val LOG = KotlinLogging.logger {}

fun main(args: Array<String>) {
	runApplication<FiveLettersApplication>(*args)
}
