package com.kilchichakov.fiveletters.service.job

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.model.job.PeriodicLetterStatJobPayload
import com.kilchichakov.fiveletters.service.LetterStatDataService
import org.springframework.stereotype.Component

@Component
class PeriodicLetterStatJobProcessor(
    private val letterStatDataService: LetterStatDataService
) {

    fun process(payload: PeriodicLetterStatJobPayload) {
        letterStatDataService.getLoginsWithUnorderedStatsSequence().forEach { login ->
            LOG.info { "ordering user=$login letter stat data" }
            letterStatDataService.orderStats(login)
        }
    }
}