package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.aspect.Logged
import com.kilchichakov.fiveletters.controller.ControllerUtils.getLogin
import com.kilchichakov.fiveletters.controller.ControllerUtils.processAndRespondCode
import com.kilchichakov.fiveletters.model.Page
import com.kilchichakov.fiveletters.model.dto.GetFutureLettersResponse
import com.kilchichakov.fiveletters.model.dto.GetLetterStatResponse
import com.kilchichakov.fiveletters.model.dto.GetNewLettersResponse
import com.kilchichakov.fiveletters.model.dto.GetTimePeriodsResponse
import com.kilchichakov.fiveletters.model.dto.LetterDto
import com.kilchichakov.fiveletters.model.dto.OperationCodeResponse
import com.kilchichakov.fiveletters.model.dto.PageRequest
import com.kilchichakov.fiveletters.model.dto.SendLetterFreeDateRequest
import com.kilchichakov.fiveletters.model.dto.SendLetterRequest
import com.kilchichakov.fiveletters.service.InputValidationService
import com.kilchichakov.fiveletters.service.LetterService
import com.kilchichakov.fiveletters.service.LetterStatDataService
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
    lateinit var letterStatDataService: LetterStatDataService

    @Autowired
    lateinit var timePeriodService: TimePeriodService

    @Autowired
    private lateinit var inputValidationService: InputValidationService

    @PostMapping("/send")
    @Logged
    fun send(@RequestBody request: SendLetterRequest): OperationCodeResponse {
        return processAndRespondCode { login ->
            inputValidationService.validate(request)
            LOG.info { "asked to send new letter: $request" }
            letterService.sendLetter(login!!, request.message, request.period)
        }.logResult()
    }

    @PostMapping("/sendFreeDate")
    @Logged
    fun sendFreeDate(@RequestBody request: SendLetterFreeDateRequest): OperationCodeResponse {
        return processAndRespondCode { login ->
            inputValidationService.validate(request)
            LOG.info { "asked to send new letter: $request" }
            letterService.sendLetter(login!!, request.message, request.openDate)
        }.logResult()
    }

    @GetMapping("/periods")
    @Logged
    fun getTimePeriods(): GetTimePeriodsResponse {
        LOG.info { "asked to get available time periods" }
        return GetTimePeriodsResponse(timePeriodService.listAllTimePeriods())
                .logResult()
    }

    @PostMapping("/inbox")
    @Logged
    fun getInboxPage(@RequestBody request: PageRequest): Page<LetterDto> {
        val login = getLogin()!!
        inputValidationService.validate(request)
        LOG.info { "asked to get inbox page, request $request" }
        val page = letterService.getInboxPage(login, request)
        return Page(
                page.elements.map { LetterDto(it._id!!.toString(), it.sendDate, it.openDate, it.message, it.read, it.mailSent, it.archived) },
                pageNumber = page.pageNumber,
                pageSize = page.pageSize,
                total = page.total
        )
                .logResult()
    }

    @GetMapping("/future")
    @Logged
    @Deprecated("switch to more efficient getLetterStats()")
    fun getFutureLetters(): GetFutureLettersResponse {
        val login = getLogin()!!
        LOG.info { "asked to get future letters" }
        val letters = letterService.getFutureLetters(login)
        return GetFutureLettersResponse(letters)
                .logResult()
    }

    @GetMapping("/stats")
    @Logged
    fun getLetterStats(): GetLetterStatResponse {
        val login = getLogin()!!
        LOG.info { "asked for letter stats" }
        return letterStatDataService.getLetterStats(login)
                .logResult()
    }

    @PostMapping("/markRead")
    @Logged
    fun markAsRead(@RequestBody letterId: String): OperationCodeResponse {
        return processAndRespondCode { login ->
            //TODO check single letterId
            LOG.info { "asked to mark letter as read: $letterId" }
            letterService.markLetterAsRead(login!!, letterId)
        }.logResult()
    }
}