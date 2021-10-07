package com.niran.routes

import com.niran.data.checkIfUserExists
import com.niran.data.collections.User
import com.niran.data.registerUser
import com.niran.data.requests.AccountRequest
import com.niran.data.responses.SimpleResponse
import com.niran.security.getHashWithSalt
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.registerRoute() {
    route("/register") {
        post {

            val request = try {
                call.receive<AccountRequest>()
            } catch (e: ContentTransformationException) {
                call.respond(BadRequest)
                return@post
            }

            val userExists = checkIfUserExists(request.email)

            if (!userExists) registerUser(User(request.email, getHashWithSalt(request.password))).also { success ->
                if (success) call.respond(OK, SimpleResponse(true, "Successfully created Account"))
                else call.respond(OK, SimpleResponse(false, "Unknown Error Occurred"))
            } else call.respond(OK, SimpleResponse(false, "A User with that E-Mail already exists"))
        }
    }
}