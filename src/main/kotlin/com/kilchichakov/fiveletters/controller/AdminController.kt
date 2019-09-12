package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.model.Letter
import com.kilchichakov.fiveletters.model.LetterPeriodType
import com.kilchichakov.fiveletters.model.UserData
import com.kilchichakov.fiveletters.service.LetterService
import com.kilchichakov.fiveletters.service.UserService
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.getCollection
import org.litote.kmongo.ne
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.random.Random

@RestController
@RequestMapping("admin")
@Secured("ROLE_ADMIN")
class AdminController {

    @Autowired
    lateinit var db: MongoDatabase

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var letterService: LetterService

    @GetMapping("/whoami")
    fun whoAmI(): String {
        val auth = SecurityContextHolder.getContext().authentication as UsernamePasswordAuthenticationToken
        return "hello, ${auth.name}"
    }

    @GetMapping("/test1")
    fun testCapacity(): String {
        repeat(1000) {i ->
            val userName = "test_${i}_${randomString(8)}"
            userService.registerNewUser(userName, userName)
            repeat(5000) {
                letterService.sendLetter(userName, randomString(2000), LetterPeriodType.MONTH, -60)
            }
        }
        return "0"
    }

    @GetMapping("/test2")
    fun removeAll(): String {
        val users = db.getCollection<UserData>()
        val filter = UserData::login ne "poupa"
        users.deleteMany(filter)
        val letters = db.getCollection<Letter>()
        val letterFilter = Letter::login ne "poupa"
        letters.deleteMany(filter)
        return "0"
    }

    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    private fun randomString(len: Int): String {
        val builder = StringBuilder()
        builder.ensureCapacity(len)
        repeat(len) { builder.append(charPool[Random.nextInt(0, charPool.size)]) }
        return builder.toString()
    }
}