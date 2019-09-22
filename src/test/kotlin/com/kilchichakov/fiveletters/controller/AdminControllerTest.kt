package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.ControllerTestSuite
import com.kilchichakov.fiveletters.service.SystemService
import com.mongodb.client.MongoDatabase
import io.mockk.confirmVerified
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AdminControllerTest : ControllerTestSuite() {

    @RelaxedMockK
    lateinit var systemService: SystemService

    @InjectMockKs
    lateinit var controller: AdminController

    @Test
    fun `should switch registration`() {
        // Given
        val enabled = true

        // When
        controller.switchRegistration(enabled)

        // Then
        verify {
            ControllerUtils.getLogin()
            systemService.switchRegistration(enabled)
        }
        confirmVerified(ControllerUtils, systemService)
    }

}