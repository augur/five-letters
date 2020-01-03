package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.aspect.Logged
import com.kilchichakov.fiveletters.model.OneTimePassCode
import com.kilchichakov.fiveletters.model.dto.AdminChangePasswordRequest
import com.kilchichakov.fiveletters.model.job.EmailConfirmSendingJobPayload
import com.kilchichakov.fiveletters.model.job.Job
import com.kilchichakov.fiveletters.model.job.JobSchedule
import com.kilchichakov.fiveletters.repository.JobRepository
import com.kilchichakov.fiveletters.service.InputValidationService
import com.kilchichakov.fiveletters.service.LetterService
import com.kilchichakov.fiveletters.service.PassCodeService
import com.kilchichakov.fiveletters.service.SystemService
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

    @Autowired
    lateinit var systemService: SystemService

    @Autowired
    lateinit var passCodeService: PassCodeService

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var inputValidationService: InputValidationService

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

    @Autowired
    private lateinit var jobRepository: JobRepository

    @GetMapping("/test1")
    @Logged
    fun runTest() {
        LOG.info { "running test1" }
        val job = Job(null, JobSchedule(nextExecutionTime = Date()), EmailConfirmSendingJobPayload("emalll"))

        jobRepository.insertJob(job)

        LOG.info { jobRepository.loadReadyJobs() }
    }
}