package ru.vassuv.exceptionhandlerstarter.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import ru.vassuv.exceptionhandlerstarter.handler.GlobalExceptionHandler

@Configuration
open class ExceptionHandlerAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    open fun globalExceptionHandler(): GlobalExceptionHandler = GlobalExceptionHandler()
}