package com.kilchichakov.fiveletters.model.dto

import com.kilchichakov.fiveletters.model.Day
import com.kilchichakov.fiveletters.model.LetterStat

data class GetLetterStatResponse(
        val sent: List<LetterStat>,
        val open: List<LetterStat>
)