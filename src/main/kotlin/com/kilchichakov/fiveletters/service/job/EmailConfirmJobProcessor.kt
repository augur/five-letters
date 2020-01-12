package com.kilchichakov.fiveletters.service.job

import com.kilchichakov.fiveletters.model.Email
import com.kilchichakov.fiveletters.model.job.EmailConfirmSendingJobPayload
import com.kilchichakov.fiveletters.service.MailSenderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class EmailConfirmJobProcessor(
        @Value("\${API_URL}")
        private val baseUrl: String,
        @Autowired
        private val mailSenderService: MailSenderService
) {
    private val CONFIRMATION_SUBJECT = "Daily Time Capsule: email confirmation"

    fun process(payload: EmailConfirmSendingJobPayload) {
        val email = Email(payload.email, CONFIRMATION_SUBJECT, html = buildHtml(payload.code))
        mailSenderService.sendEmail(email)
    }

    private fun buildHtml(code: String): String {
        val link = "$baseUrl/common/confirmEmail?code=$code"
        return """
            <html>
                <body>
                    Email confirmation <a href="$link">link</a>
                </body>
            </html>
        """.trimIndent()
    }
}