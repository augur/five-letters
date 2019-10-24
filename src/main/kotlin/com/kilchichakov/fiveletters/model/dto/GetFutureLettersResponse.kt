package com.kilchichakov.fiveletters.model.dto

import com.kilchichakov.fiveletters.model.SealedLetterEnvelop

data class GetFutureLettersResponse(
        val letters: List<SealedLetterEnvelop>
)