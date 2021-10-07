package com.niran.routes

import com.niran.data.checkPasswordForEmail
import com.niran.data.requests.AccountRequest
import com.niran.data.responses.SimpleResponse
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.loginRoute() {
    route("/login") {
        post {

            val request = try {
                call.receive<AccountRequest>()
            } catch (e: ContentTransformationException) {
                call.respond(BadRequest)
                return@post
            }

            checkPasswordForEmail(request.email, request.password).also { success ->
                if (success) call.respond(HttpStatusCode.OK, SimpleResponse(true, "You are now logged in!"))
                else call.respond(HttpStatusCode.OK, SimpleResponse(false, "Incorrect E-Mail or password"))
            }
        }
    }
}