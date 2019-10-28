package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.aspect.Logged
import com.kilchichakov.fiveletters.controller.ControllerUtils.getLogin
import com.kilchichakov.fiveletters.controller.ControllerUtils.processAndRespondCode
import com.kilchichakov.fiveletters.model.dto.GetFutureLettersResponse
import com.kilchichakov.fiveletters.model.dto.GetNewLettersResponse
import com.kilchichakov.fiveletters.model.dto.GetTimePeriodsResponse
import com.kilchichakov.fiveletters.model.dto.LetterDto
import com.kilchichakov.fiveletters.model.dto.OperationCodeResponse
import com.kilchichakov.fiveletters.model.dto.SendLetterRequest
import com.kilchichakov.fiveletters.service.InputValidationService
import com.kilchichakov.fiveletters.service.LetterService
import com.kilchichakov.fiveletters.service.TimePeriodService
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

    @Autowired
    lateinit var timePeriodService: TimePeriodService

    @Autowired
    protected lateinit var inputValidationService: InputValidationService

    @PostMapping("/send")
    @Logged
    fun send(@RequestBody request: SendLetterRequest): OperationCodeResponse {
        return processAndRespondCode { login ->
            inputValidationService.validate(request)
            LOG.info { "asked to send new letter: $request" }
            letterService.sendLetter(login!!, request.message, request.period, request.timezoneOffset)
        }.also { LOG.info { "result is $it" } }
    }

    @GetMapping("/periods")
    @Logged
    fun getTimePeriods(): GetTimePeriodsResponse {
        LOG.info { "asked to get available time periods" }
        return GetTimePeriodsResponse(timePeriodService.listAllTimePeriods())
                .logResult()
    }

    @GetMapping("/new")
    @Logged
    fun getNewLetters(): GetNewLettersResponse {
        LOG.info { "asked to get new letters" }
        val letters = letterService.getNewLetters(getLogin()!!)
        return GetNewLettersResponse(letters.map { LetterDto(it._id!!.toHexString(), it.sendDate, it.message) })
                .logResult()
    }

    @GetMapping("/future")
    @Logged
    fun getFutureLetters(): GetFutureLettersResponse {
        LOG.info { "asked to get future letters" }
        val letters = letterService.getFutureLetters(getLogin()!!)
        return GetFutureLettersResponse(letters)
                .logResult()
    }

    @PostMapping("/markRead")
    @Logged
    fun markAsRead(@RequestBody letterId: String): OperationCodeResponse {
        return processAndRespondCode { login ->
            LOG.info { "asked to mark letter as read: $letterId" }
            letterService.markLetterAsRead(login!!, letterId)
        }.logResult()
    }
}