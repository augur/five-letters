package com.kilchichakov.fiveletters

import com.kilchichakov.fiveletters.controller.ControllerUtils
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

open class ControllerTestSuite {

    protected val LOGIN = "someLogin"

    @BeforeEach
    open fun setUpEach() {
        mockkObject(ControllerUtils)
        every { ControllerUtils.getLogin() } returns LOGIN
    }

    @AfterEach
    open fun tearDownEach() {
        unmockkAll()
    }
}