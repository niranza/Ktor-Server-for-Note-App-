package com.niran

import com.niran.data.checkPasswordForEmail
import com.niran.routes.loginRoute
import com.niran.routes.noteRoutes
import com.niran.routes.registerRoute
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    //Add Additional information to Headers (such as Date)
    install(DefaultHeaders)

    //Logging http requests and responses
    install(CallLogging)

    //Being able to decide requests and responses format
    install(ContentNegotiation) {

        //Configure Json Format
        gson {
            setPrettyPrinting()
        }
    }

    //Authentication
    install(Authentication) {
        configureAuth()
    }

    //Defining URL end points (REST)
    install(Routing) {
        //Configuring route
        registerRoute()
        loginRoute()
        noteRoutes()
    }
}

private fun Authentication.Configuration.configureAuth() {
    //Using Basic authentication. Use Oauth in production apps
    basic {
        realm = "Note Server"
        validate { credentials ->
            val email = credentials.name
            val password = credentials.password
            if (checkPasswordForEmail(email, password)) UserIdPrincipal(email)
            else null
        }
    }
}