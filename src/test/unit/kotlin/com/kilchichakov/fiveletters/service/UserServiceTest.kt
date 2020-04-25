package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.exception.DataException
import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.exception.SystemStateException
import com.kilchichakov.fiveletters.exception.TermsOfUseException
import com.kilchichakov.fiveletters.model.OneTimePassCode
import com.kilchichakov.fiveletters.model.AuthData
import com.kilchichakov.fiveletters.model.UserData
import com.kilchichakov.fiveletters.repository.SystemStateRepository
import com.kilchichakov.fiveletters.repository.AuthDataRepository
import com.kilchichakov.fiveletters.repository.UserDataRepository
import com.kilchichakov.fiveletters.repository.UserDataRepository.UpdateUserDataResult
import com.mongodb.client.ClientSession
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.spyk
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
    lateinit var transactionWrapper: TransactionWrapper

    @RelaxedMockK
    lateinit var passCodeService: PassCodeService

    @RelaxedMockK
    lateinit var authDataRepository: AuthDataRepository

    @RelaxedMockK
    lateinit var systemStateRepository: SystemStateRepository

    @RelaxedMockK
    lateinit var passwordEncoder: PasswordEncoder

    @RelaxedMockK
    lateinit var userDataRepository: UserDataRepository

    @RelaxedMockK
    lateinit var jobService: JobService

    @InjectMockKs
    lateinit var service: UserService

    @Test
    fun `should register new user`() {
        // Given
        val login = "loupa"
        val password = "pw"
        val slot = slot<AuthData>()
        val encoded = "encoded"
        val code = "xx-yy-zz"
        val passCode = mockk<OneTimePassCode>()
        val session = mockk<ClientSession>()
        val email = "some@email"

        val spy = spyk(service)
        every { spy.updateUserData(any(), any(), any()) } just runs

        every { transactionWrapper.executeInTransaction(any()) } answers {
            firstArg<(ClientSession)->Any>().invoke(session)
        }
        every { passCodeService.getPassCode(any()) } returns passCode
        every { systemStateRepository.read().registrationEnabled } returns true
        every { authDataRepository.insertNewUser(capture(slot), any()) } just Runs
        every { passwordEncoder.encode(any()) } returns encoded

        // When
        spy.registerNewUser(login, password, true, code, email)

        // Then
        assertThat(slot.captured._id).isNull()
        assertThat(slot.captured.login).isEqualTo(login)
        assertThat(slot.captured.password).isEqualTo(encoded)
        verify {
            passCodeService.getPassCode(code)
            systemStateRepository.read().registrationEnabled
            passwordEncoder.encode(password)
            passCodeService.usePassCode(passCode, login, session)
            authDataRepository.insertNewUser(slot.captured, session)
            spy.updateUserData(login, email, "")
        }
        confirmVerified(systemStateRepository, passwordEncoder, authDataRepository, passCodeService)
    }

    @Test
    fun `should fail to register if licence is not accepted`() {
        // When
        assertThrows<TermsOfUseException> {
            service.registerNewUser("lg", "pw", false, "pscd", "some-email")
        }
    }

    @Test
    fun `should fail to register if registration is disabled`() {
        every { systemStateRepository.read().registrationEnabled } returns false
        assertThrows<SystemStateException> {
            service.registerNewUser("lg", "pw", true, "pscd", "some-email")
        }
    }

    @Test
    fun `should load normal user`() {
        // Given
        val login = "poupa"
        val password = "encoded"
        val userData = AuthData(null, login, password, false)
        every { authDataRepository.loadUserData(any()) } returns userData

        // When
        val actual = service.loadUserByUsername(login)

        // Then
        assertThat(actual.username).isEqualTo(login)
        assertThat(actual.authorities).isEmpty()
        assertThat(actual.password).isEqualTo(password)
        verify {
            authDataRepository.loadUserData(login)
        }
        confirmVerified(authDataRepository)
    }

    @Test
    fun `should load admin user`() {
        // Given
        val login = "loupa"
        val password = "encoded"
        val userData = AuthData(null, login, password, true)
        every { authDataRepository.loadUserData(any()) } returns userData

        // When
        val actual = service.loadUserByUsername(login)

        // Then
        assertThat(actual.username).isEqualTo(login)
        assertThat(actual.authorities).containsExactly(SimpleGrantedAuthority("ROLE_ADMIN"))
        assertThat(actual.password).isEqualTo(password)
        verify {
            authDataRepository.loadUserData(login)
        }
        confirmVerified(authDataRepository)
    }

    @Test
    fun `should change user password with success`() {
        // Given
        val login = "someLogin"
        val newPwd = "another pwd"
        val encoded = "0xF0F0"
        every { passwordEncoder.encode(any()) } returns encoded
        every { authDataRepository.changePassword(any(), any()) } returns true

        // When
        service.changeUserPassword(login, newPwd)

        // Then
        verify {
            passwordEncoder.encode(newPwd)
            authDataRepository.changePassword(login, encoded)
        }
        confirmVerified(passwordEncoder, authDataRepository)
    }

    @Test
    fun `should change user password with failure`() {
        // Given
        val login = "someLogin"
        val newPwd = "another pwd"
        val encoded = "0xF0F0"
        every { passwordEncoder.encode(any()) } returns encoded
        every { authDataRepository.changePassword(any(), any()) } returns false

        // When
        assertThrows<DatabaseException> {
            service.changeUserPassword(login, newPwd)
        }

        // Then
        verify {
            passwordEncoder.encode(newPwd)
            authDataRepository.changePassword(login, encoded)
        }
        confirmVerified(passwordEncoder, authDataRepository)
    }

    @Test
    fun `should load user data`() {
        // Given
        val login = "someLogin"
        val userData = mockk<UserData>()
        every { userDataRepository.loadUserData(any()) } returns userData

        // When
        val actual = service.loadUserData(login)

        // Then
        assertThat(actual).isEqualTo(userData)
        verify { userDataRepository.loadUserData(login) }
        confirmVerified(userDataRepository)
    }

    @Test
    fun `should throw if userData not found`() {
        // Given
        val login = "someLogin"
        every { userDataRepository.loadUserData(any()) } returns null

        // When
        assertThrows<DataException> { service.loadUserData(login) }
    }

    @Test
    fun `should update userData with changed email`() {
        // Given
        val login = "loupa"
        val nick = "poupa"
        val email = "loupa@poupa"
        every { userDataRepository.updateUserData(any(), any(), any()) } returns UpdateUserDataResult(true, true)

        // When
        service.updateUserData(login, email, nick)

        // Then
        verify {
            userDataRepository.updateUserData(login, email, nick)
            jobService.scheduleEmailConfirmation(login)
        }
        confirmVerified(userDataRepository, jobService)
    }

    @Test
    fun `should update userData without changed email`() {
        // Given
        val login = "loupa"
        val nick = "poupa"
        val email = "loupa@poupa"
        every { userDataRepository.updateUserData(any(), any(), any()) } returns UpdateUserDataResult(true, false)

        // When
        service.updateUserData(login, email, nick)

        // Then
        verify {
            userDataRepository.updateUserData(login, email, nick)
        }
        confirmVerified(userDataRepository, jobService)
    }

    @Test
    fun `should throw if not updated profile`() {
        // Given
        val login = "loupa"
        val nick = "poupa"
        val email = "loupa@poupa"
        //every { userDataRepository.updateUserData(any(), any(), any()) } returns false

        // When
        assertThrows<DatabaseException> { service.updateUserData(login, email, nick) }
    }

    @Test
    fun `should set confirmation code`() {
        // Given
        val login = "loupa"
        val code = "poupa"
        val session = mockk<ClientSession>()
        every { userDataRepository.setEmailConfirmationCode(any(), any(), any()) } returns true

        // When
        service.setConfirmationCode(login, code, session)

        // Then
        verify { userDataRepository.setEmailConfirmationCode(login, code, session) }
        confirmVerified(userDataRepository)
    }

    @Test
    fun `should throw if not set confirmation code`() {
        // Given
        val login = "loupa"
        val code = "poupa"
        val session = mockk<ClientSession>()
        every { userDataRepository.setEmailConfirmationCode(any(), any(), any()) } returns false

        // When
        assertThrows<DatabaseException> { service.setConfirmationCode(login, code, session) }
    }
}