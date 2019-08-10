package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.controller.ControllerUtils.processAndRespondCode
import com.kilchichakov.fiveletters.model.dto.OperationCodeResponse
import com.kilchichakov.fiveletters.model.dto.RegisterRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.IllegalStateException

@RestController
@RequestMapping("register")
class RegisterController {

    @PostMapping
    fun register(@RequestBody request: RegisterRequest): OperationCodeResponse {
        return processAndRespondCode {
            println(request)
            throw IllegalStateException("wtf is happening")
        }
    }
}