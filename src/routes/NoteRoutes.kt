package com.niran.routes

import com.niran.data.*
import com.niran.data.collections.Note
import com.niran.data.requests.AddOwnerToNoteRequest
import com.niran.data.requests.DeleteNoteRequest
import com.niran.data.responses.SimpleResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.noteRoutes() {
    route("/getNotes") {
        authenticate {
            get {
                val email = call.principal<UserIdPrincipal>()!!.name
                val notes = getNotesForUser(email)
                call.respond(OK, notes)
            }
        }
    }

    route("/addNote") {
        authenticate {
            post {

                val note = try {
                    call.receive<Note>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }

                saveNote(note).also { success ->
                    if (success) call.respond(OK) else call.respond(Conflict)
                }
            }
        }
    }

    route("/deleteNote") {
        authenticate {
            post {

                val request = try {
                    call.receive<DeleteNoteRequest>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }

                val email = call.principal<UserIdPrincipal>()!!.name

                deleteNoteForUser(request.id, email).also { success ->
                    if (success) call.respond(OK)
                    else call.respond(Conflict)
                }
            }
        }
    }

    route("/addOwnerToNote") {
        authenticate {
            post {

                val email = call.principal<UserIdPrincipal>()!!.name

                val request = try {
                    call.receive<AddOwnerToNoteRequest>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }

                if (!checkIfUserExists(request.owner)) {
                    call.respond(OK, SimpleResponse(false, "No User With This Email Exists"))
                    return@post
                }

                addOwnerToNote(email, request.noteId, request.owner).also { result ->
                    when (result) {
                        AddOwnerToNoteResult.SUCCESS ->
                            call.respond(OK, SimpleResponse(true, "${request.owner} can now see this note"))
                        AddOwnerToNoteResult.OWNER_ALREADY_EXISTS ->
                            call.respond(OK, SimpleResponse(false, "This user already an owner of this note"))
                        AddOwnerToNoteResult.ERROR -> call.respond(Conflict)
                    }
                }
            }
        }
    }
}