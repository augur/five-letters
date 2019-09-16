package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.controller.ControllerUtils.getLogin
import com.kilchichakov.fiveletters.controller.ControllerUtils.processAndRespondCode
import com.kilchichakov.fiveletters.model.dto.GetNewLettersResponse
import com.kilchichakov.fiveletters.model.dto.LetterDto
import com.kilchichakov.fiveletters.model.dto.OperationCodeResponse
import com.kilchichakov.fiveletters.model.dto.SendLetterRequest
import com.kilchichakov.fiveletters.service.LetterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("letter")
class LetterController {

    @Autowired
    lateinit var letterService: LetterService

    @PostMapping("/send")
    fun send(@RequestBody request: SendLetterRequest): OperationCodeResponse {
        return processAndRespondCode { login ->
            letterService.sendLetter(login!!, request.message, request.period, request.timezoneOffset)
        }
    }

    @GetMapping("/new")
    fun getNewLetters(): GetNewLettersResponse {
        val letters = letterService.getNewLetters(getLogin()!!)
        return GetNewLettersResponse(letters.map { LetterDto(it._id!!.toHexString(), it.sendDate, it.message) })
    }

    @PostMapping("/markRead")
    fun markAsRead(@RequestBody letterId: String): OperationCodeResponse {
        return processAndRespondCode { login ->
            letterService.markLetterAsRead(login!!, letterId)
        }
    }
}