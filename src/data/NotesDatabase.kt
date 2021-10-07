package com.niran.data

import com.niran.data.AddOwnerToNoteResult.*
import com.niran.data.collections.Note
import com.niran.data.collections.User
import com.niran.data.requests.AddOwnerToNoteRequest
import com.niran.security.checkHashForPassword
import org.litote.kmongo.contains
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.setValue

private val client = KMongo.createClient().coroutine
private val database = client.getDatabase("NotesDatabase")
private val users = database.getCollection<User>()
private val notes = database.getCollection<Note>()

suspend fun registerUser(user: User): Boolean = users.insertOne(user).wasAcknowledged()

suspend fun checkIfUserExists(email: String): Boolean =
    users.findOne(User::email eq email) != null

suspend fun checkPasswordForEmail(email: String, passwordToCheck: String): Boolean {
    val user = users.findOne(User::email eq email) ?: return false
    return checkHashForPassword(passwordToCheck, user.password)
}

suspend fun getNotesForUser(email: String): List<Note> =
    notes.find(Note::owners contains email).toList()

suspend fun saveNote(note: Note): Boolean {
    val noteAlreadyExists = notes.findOneById(note.id) != null
    return if (noteAlreadyExists) notes.updateOneById(note.id, note).wasAcknowledged()
    else notes.insertOne(note).wasAcknowledged()
}

suspend fun addOwnerToNote(ownerEmail: String, noteId: String, newOwnerEmail: String): AddOwnerToNoteResult {
    val note = notes.findOne(Note::id eq noteId, Note::owners contains ownerEmail) ?: return ERROR
    val owners = note.owners
    if (newOwnerEmail in owners) return OWNER_ALREADY_EXISTS
    val newOwners = owners + newOwnerEmail
    notes.updateOneById(noteId, setValue(Note::owners, newOwners)).wasAcknowledged().also { success ->
        return if (success) SUCCESS else ERROR
    }
}

enum class AddOwnerToNoteResult { SUCCESS, OWNER_ALREADY_EXISTS, ERROR }

suspend fun deleteNoteForUser(noteId: String, email: String): Boolean {
    val note = notes.findOne(Note::id eq noteId, Note::owners contains email) ?: return false
    val owners = note.owners
    return if (owners.size > 1) {
        val newOwners = owners - email
        notes.updateOneById(noteId, setValue(Note::owners, newOwners)).wasAcknowledged()
    } else notes.deleteOneById(noteId).wasAcknowledged()
}
