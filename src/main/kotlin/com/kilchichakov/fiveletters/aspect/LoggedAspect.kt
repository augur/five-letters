package com.kilchichakov.fiveletters.aspect

import com.kilchichakov.fiveletters.LOG
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.lang.Exception
import java.util.concurrent.atomic.AtomicLong

@Aspect
@Component
class LoggedAspect {

    companion object {
        private val traceIndex = AtomicLong(0)

        private const val ENTRY = "entry"
    }

    @Around("@annotation(com.kilchichakov.fiveletters.aspect.Logged)")
    fun around(pjp: ProceedingJoinPoint): Any? {
        val isTop = MDC.get(ENTRY) == null
        if (isTop) MDC.put(ENTRY, "0x" + traceIndex.incrementAndGet().toString(16))
        try {
            return pjp.proceed()
//        } catch (t: Throwable) {
//            //LOG.error { "caught $t" }
//            throw t
        } finally {
            if (isTop) MDC.remove(ENTRY)
        }
    }
}