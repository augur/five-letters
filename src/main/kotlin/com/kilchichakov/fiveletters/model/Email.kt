package com.kilchichakov.fiveletters.model

data class Email(val to: String,
                 val subject: String,
                 val text: String? = null,
                 val html: String? = null) {
    override fun toString(): String {
        return "Email(to='$to', subject='$subject', text.length=${text?.length}, html.length=${html?.length})"
    }
}