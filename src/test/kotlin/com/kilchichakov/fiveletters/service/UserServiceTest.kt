package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.exception.SystemStateException
import com.kilchichakov.fiveletters.exception.TermsOfUseException
import com.kilchichakov.fiveletters.model.UserData
import com.kilchichakov.fiveletters.repository.SystemStateRepository
import com.kilchichakov.fiveletters.repository.UserDataRepository
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder


@ExtendWith(MockKExtension::class)
internal class UserServiceTest {

    @RelaxedMockK
    lateinit var userDataRepository: UserDataRepository

    @RelaxedMockK
    lateinit var systemStateRepository: SystemStateRepository

    @RelaxedMockK
    lateinit var passwordEncoder: PasswordEncoder

    @InjectMockKs
    lateinit var service: UserService

    @Test
    fun `should register new user`() {
        // Given
        val login = "loupa"
        val password = "pw"
        val slot = slot<UserData>()
        val encoded = "encoded"

        every { systemStateRepository.read().registrationEnabled } returns true
        every { userDataRepository.insertNewUser(capture(slot)) } just Runs
        every { passwordEncoder.encode(any()) } returns encoded

        // When
        service.registerNewUser(login, password, true)

        // Then
        assertThat(slot.captured._id).isNull()
        assertThat(slot.captured.login).isEqualTo(login)
        assertThat(slot.captured.password).isEqualTo(encoded)
        verify {
            systemStateRepository.read().registrationEnabled
            passwordEncoder.encode(password)
            userDataRepository.insertNewUser(slot.captured)
        }
        confirmVerified(systemStateRepository, passwordEncoder, userDataRepository)
    }

    @Test
    fun `should fail to register if licence is not accepted`() {
        // When
        assertThrows<TermsOfUseException> {
            service.registerNewUser("lg", "pw", false)
        }
    }

    @Test
    fun `should fail to register if registration is disabled`() {
        every { systemStateRepository.read().registrationEnabled } returns false
        assertThrows<SystemStateException> {
            service.registerNewUser("lg", "pw", true)
        }
    }

    @Test
    fun `should load normal user`() {
        // Given
        val login = "poupa"
        val password = "encoded"
        val userData = UserData(null, login, password, "", false)
        every { userDataRepository.loadUserData(any()) } returns userData

        // When
        val actual = service.loadUserByUsername(login)

        // Then
        assertThat(actual.username).isEqualTo(login)
        assertThat(actual.authorities).isEmpty()
        assertThat(actual.password).isEqualTo(password)
        verify {
            userDataRepository.loadUserData(login)
        }
        confirmVerified(userDataRepository)
    }

    @Test
    fun `should load admin user`() {
        // Given
        val login = "loupa"
        val password = "encoded"
        val userData = UserData(null, login, password, "", true)
        every { userDataRepository.loadUserData(any()) } returns userData

        // When
        val actual = service.loadUserByUsername(login)

        // Then
        assertThat(actual.username).isEqualTo(login)
        assertThat(actual.authorities).containsExactly(SimpleGrantedAuthority("ROLE_ADMIN"))
        assertThat(actual.password).isEqualTo(password)
        verify {
            userDataRepository.loadUserData(login)
        }
        confirmVerified(userDataRepository)
    }
}