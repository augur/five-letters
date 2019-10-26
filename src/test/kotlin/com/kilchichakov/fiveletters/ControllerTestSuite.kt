package com.kilchichakov.fiveletters

import com.kilchichakov.fiveletters.controller.ControllerUtils
import com.kilchichakov.fiveletters.service.InputValidationService
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.SpyK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

open class ControllerTestSuite {

    protected val LOGIN = "someLogin"

    @SpyK
    protected var inputValidationService = InputValidationService()

    @BeforeEach
    open fun setUpEach() {
        mockkObject(ControllerUtils)
        every { ControllerUtils.getLogin() } returns LOGIN
        every { inputValidationService.validate(any()) } just Runs
    }

    @AfterEach
    open fun tearDownEach() {
        unmockkAll()
    }
}