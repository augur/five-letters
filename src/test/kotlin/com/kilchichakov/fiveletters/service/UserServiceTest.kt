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
import com.mongodb.client.ClientSession
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
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

        every { transactionWrapper.executeInTransaction(any()) } answers {
            firstArg<(ClientSession)->Any>().invoke(session)
        }
        every { passCodeService.getPassCode(any()) } returns passCode
        every { systemStateRepository.read().registrationEnabled } returns true
        every { authDataRepository.insertNewUser(capture(slot), any()) } just Runs
        every { passwordEncoder.encode(any()) } returns encoded

        // When
        service.registerNewUser(login, password, true, code)

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
        }
        confirmVerified(systemStateRepository, passwordEncoder, authDataRepository, passCodeService)
    }

    @Test
    fun `should fail to register if licence is not accepted`() {
        // When
        assertThrows<TermsOfUseException> {
            service.registerNewUser("lg", "pw", false, "pscd")
        }
    }

    @Test
    fun `should fail to register if registration is disabled`() {
        every { systemStateRepository.read().registrationEnabled } returns false
        assertThrows<SystemStateException> {
            service.registerNewUser("lg", "pw", true, "pscd")
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
}