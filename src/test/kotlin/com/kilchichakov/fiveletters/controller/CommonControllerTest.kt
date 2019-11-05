package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.ControllerTestSuite
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class CommonControllerTest : ControllerTestSuite() {

    @InjectMockKs
    lateinit var controller: CommonController

    private val VERSION = "0.0.1-test"

    @BeforeEach
    override fun setUpEach() {
        super.setUpEach()
        controller = CommonController(VERSION)
    }

    @Test
    fun `should get version`() {
        // When
        val actual = controller.getVersion()

        // Then
        assertThat(actual).isEqualTo(VERSION)
    }
}