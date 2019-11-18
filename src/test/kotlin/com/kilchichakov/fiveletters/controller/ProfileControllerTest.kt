package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.ControllerTestSuite
import com.kilchichakov.fiveletters.model.UserData
import com.kilchichakov.fiveletters.service.UserService
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(MockKExtension::class)
internal class ProfileControllerTest : ControllerTestSuite() {

    @RelaxedMockK
    lateinit var userService: UserService

    @InjectMockKs
    lateinit var controller: ProfileController

    @Test
    fun `should return whoAmI response`() {
        // Given
        val nick = "loupa"
        val email = "loupa@poupa"
        val userData = UserData(null, LOGIN, nick, email, true)
        every { userService.loadUserData(any()) } returns userData

        // When
        val actual = controller.whoAmI()

        // Then
        assertThat(actual.nickname).isEqualTo(nick)
        assertThat(actual.email).isEqualTo(email)
        assertThat(actual.emailConfirmed).isTrue()
        verify {
            userService.loadUserData(LOGIN)
        }
        confirmVerified(userService)
    }

}