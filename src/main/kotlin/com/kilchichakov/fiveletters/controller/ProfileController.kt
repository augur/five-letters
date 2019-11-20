package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.aspect.Logged
import com.kilchichakov.fiveletters.controller.ControllerUtils.getLogin
import com.kilchichakov.fiveletters.controller.ControllerUtils.processAndRespondCode
import com.kilchichakov.fiveletters.model.dto.OperationCodeResponse
import com.kilchichakov.fiveletters.model.dto.UpdateProfileRequest
import com.kilchichakov.fiveletters.model.dto.WhoAmIResponse
import com.kilchichakov.fiveletters.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("profile")
class ProfileController {

    @Autowired
    private lateinit var userService: UserService

    @GetMapping
    @Logged
    fun whoAmI(): WhoAmIResponse {
        val login = getLogin()!!
        LOG.info { "asked whoami" }
        val userData = userService.loadUserData(login)
        return WhoAmIResponse(userData.nickname.orEmpty(), userData.email.orEmpty(), userData.emailConfirmed)
                .logResult()
    }

    @PostMapping
    @Logged
    fun updateProfile(@RequestBody request: UpdateProfileRequest): OperationCodeResponse {
        return processAndRespondCode {
            LOG.info { "asked to update userData - $request" }
            userService.updateUserData(it!!, request.email, request.nickname)
        }.logResult()
    }
}