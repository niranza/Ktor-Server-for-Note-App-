package com.niran.data.requests

data class AddOwnerToNoteRequest(
    val noteId: String,
    val owner: String
)
