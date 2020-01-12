package com.kilchichakov.fiveletters.service.job

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.model.Email
import com.kilchichakov.fiveletters.model.Letter
import com.kilchichakov.fiveletters.model.UserData
import com.kilchichakov.fiveletters.model.job.DailyMailingJobPayload
import com.kilchichakov.fiveletters.service.LetterService
import com.kilchichakov.fiveletters.service.MailSenderService
import com.kilchichakov.fiveletters.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.lang.Exception

@Component
class DailyMailingJobProcessor(
        @Value("\${UI_URL}")
        private val baseUrl: String,
        @Autowired
        private val mailSenderService: MailSenderService,
        @Autowired
        private val letterService: LetterService,
        @Autowired
        private val userService: UserService
) {

    private val SUBJECT = "Your Daily Time Capsules"


    fun process(payload: DailyMailingJobPayload) {
        val letters = letterService.getLettersForMailing()
        val byUsers = letters.groupBy { it.login }
        LOG.info { "grouped letters: $byUsers" }
        byUsers.forEach { (login, letters) ->
            try {
                LOG.info { "for user $login" }
                val userData = userService.loadUserData(login)
                if (userData.emailConfirmed) {
                    val email = buildEmail(userData, letters.sortedByDescending { it.sendDate })
                    mailSenderService.sendEmail(email)
                    letterService.markLettersAsMailed(letters.map { it._id!!.toString() })
                } else {
                    LOG.info { "user $login email is unconfirmed" }
                }
            } catch (e: Exception) {
                LOG.error { "caught $e for user $login" }
            }
        }
    }

    private fun buildEmail(userData: UserData, letters: List<Letter>): Email {
        val name = if (userData.nickname.isNullOrBlank()) userData.login else userData.nickname
        return Email(
                userData.email!!,
                SUBJECT,
                html = """
                    <html>
                        <body>
                           Hi $name, here are your time capsules:
                           
                           ${letters.joinToString("") { formatLetter(it) }}
                           
                           Don't forget to create new <a href="$baseUrl">Time Capsules</a> today.
                        </body>
                    </html>                    

                """.trimIndent()
        )
    }

    private fun formatLetter(letter: Letter): String {
        return """
            <p>
                Date: ${letter.sendDate}<br>
                ${letter.message}
            </p>
        """.trimIndent()
    }
}