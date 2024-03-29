package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.aspect.Logged
import com.kilchichakov.fiveletters.model.Email
import com.kilchichakov.fiveletters.model.OneTimePassCode
import com.kilchichakov.fiveletters.model.dto.AdminChangePasswordRequest
import com.kilchichakov.fiveletters.model.job.DailyMailingJobPayload
import com.kilchichakov.fiveletters.model.job.EmailConfirmSendingJobPayload
import com.kilchichakov.fiveletters.model.job.Job
import com.kilchichakov.fiveletters.model.job.JobSchedule
import com.kilchichakov.fiveletters.model.job.RepeatMode
import com.kilchichakov.fiveletters.model.job.TestJobPayload
import com.kilchichakov.fiveletters.repository.JobRepository
import com.kilchichakov.fiveletters.service.InputValidationService
import com.kilchichakov.fiveletters.service.JobService
import com.kilchichakov.fiveletters.service.LetterService
import com.kilchichakov.fiveletters.service.LetterStatDataService
import com.kilchichakov.fiveletters.service.MailSenderService
import com.kilchichakov.fiveletters.service.PassCodeService
import com.kilchichakov.fiveletters.service.SystemService
import com.kilchichakov.fiveletters.service.TransactionWrapper
import com.kilchichakov.fiveletters.service.UserService
import com.mongodb.client.MongoDatabase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Date

@RestController
@RequestMapping("admin")
@Secured("ROLE_ADMIN")
class AdminController {

    private val ALL_USERS = "@all"

    @Autowired
    lateinit var systemService: SystemService

    @Autowired
    lateinit var passCodeService: PassCodeService

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var inputValidationService: InputValidationService

    @Autowired
    lateinit var letterStatService: LetterStatDataService

    @PostMapping("/registration")
    @Logged
    fun switchRegistration(@RequestParam enabled: Boolean) {
        ControllerUtils.getLogin()
        LOG.info { "asked to switch registration to $enabled" }
        systemService.switchRegistration(enabled)
    }

    @PostMapping("/passcodes/create/otp")
    @Logged
    fun generateOneTimePassCode(@RequestParam seconds: Long): OneTimePassCode {
        ControllerUtils.getLogin()
        //TODO validate raw seconds
        LOG.info { "asked to generate one-time passcode with seconds valid = $seconds" }
        return passCodeService.generateOneTimePassCode(seconds)
    }

    @PostMapping("/users/password/change")
    @Logged
    fun changeUserPassword(@RequestBody request: AdminChangePasswordRequest) {
        ControllerUtils.getLogin()
        inputValidationService.validate(request)
        LOG.info { "asked to change password for user ${request.login}" }
        userService.changeUserPassword(request.login, request.password)
    }

    @GetMapping("/users/letters/recalculate-stats")
    @Logged
    fun recalculateStats(@RequestParam user: String) {
        ControllerUtils.getLogin()
        LOG.info { "asked to recalculate letter stats for $user" }
        if (user == ALL_USERS) {
            userService.listAllUserLogins().forEach {
                letterStatService.recalculateStatData(it)
            }
        } else {
            letterStatService.recalculateStatData(user)
        }
    }

    @Autowired
    private lateinit var jobService: JobService

    @Autowired
    private lateinit var jobRepository: JobRepository

    @Autowired
    private lateinit var transactionWrapper: TransactionWrapper

    @Autowired
    private lateinit var emailSenderService: MailSenderService

    @GetMapping("/test1")
    @Logged
    fun runTest() {
        LOG.info { "running test1" }

        val email = Email(
                to = "user@example.com",
                subject = "test message",
                html = "<html>test text</html>"
        )
        emailSenderService.sendEmail(email)
    }
}