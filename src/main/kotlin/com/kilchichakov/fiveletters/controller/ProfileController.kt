package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.aspect.Logged
import com.kilchichakov.fiveletters.controller.ControllerUtils.getLogin
import com.kilchichakov.fiveletters.model.dto.WhoAmIResponse
import com.kilchichakov.fiveletters.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("profile")
class ProfileController {

    @Autowired
    private lateinit var userService: UserService

    @GetMapping("/whoami")
    @Logged
    fun whoAmI(): WhoAmIResponse {
        val login = getLogin()!!
        LOG.info { "asked whoami" }
        val userData = userService.loadUserData(login)
        return WhoAmIResponse(userData.nickname.orEmpty(), userData.email.orEmpty(), userData.emailConfirmed)
                .logResult()
    }
}