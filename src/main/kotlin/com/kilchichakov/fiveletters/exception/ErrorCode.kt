package com.kilchichakov.fiveletters.exception

enum class ErrorCode(val numeric: Int) {
    NO_ERROR(0),
    DB(1000),
    STATE(2000),
    GENERIC_ERROR(9000)
}