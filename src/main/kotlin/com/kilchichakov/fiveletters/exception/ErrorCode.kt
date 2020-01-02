package com.kilchichakov.fiveletters.exception

enum class ErrorCode(val numeric: Int) {
    NO_ERROR(0),
    DB(1000),
    STATE(2000),
    TOU(3000),
    DATA(4000),
    EXTERNAL_SRV(5000),
    GENERIC_ERROR(9000)
}