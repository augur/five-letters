package com.kilchichakov.fiveletters

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FiveLettersApplication

fun main(args: Array<String>) {
	runApplication<FiveLettersApplication>(*args)
}
