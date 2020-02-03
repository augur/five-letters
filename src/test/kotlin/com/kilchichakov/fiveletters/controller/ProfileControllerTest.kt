package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.ControllerTestSuite
import com.kilchichakov.fiveletters.exception.ErrorCode
import com.kilchichakov.fiveletters.model.UserData
import com.kilchichakov.fiveletters.model.dto.UpdateProfileRequest
import com.kilchichakov.fiveletters.service.UserService
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
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

    @Test
    fun `should perform update userData`() {
        // Given
        val nick = "poupa"
        val email = "loupa@poupa"
        val request = mockk<UpdateProfileRequest>()
        every { request.nickname } returns nick
        every { request.email } returns email

        // When
        val actual = controller.updateProfile(request)

        // Then
        assertThat(actual.code).isEqualTo(ErrorCode.NO_ERROR.numeric)
        verify { userService.updateUserData(LOGIN, email, nick) }
        confirmVerified(userService)
    }
}