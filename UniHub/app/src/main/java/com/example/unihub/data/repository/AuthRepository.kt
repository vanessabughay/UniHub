package com.example.unihub.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun registerUser(
        name: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val userData = hashMapOf(
                            "nome" to name,
                            "email" to email
                        )
                        firestore.collection("usuarios").document(uid).set(userData)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { e ->
                                onError(e.message ?: "Erro ao salvar no Firestore")
                            }
                    } else {
                        onError("Erro ao obter UID do usuário.")
                    }
                } else {
                    val message = when (val e = task.exception) {
                        is FirebaseAuthWeakPasswordException -> "A senha deve ter pelo menos 6 caracteres."
                        is FirebaseAuthInvalidCredentialsException -> "E-mail inválido."
                        is FirebaseAuthUserCollisionException -> "Este e-mail já está cadastrado."
                        else -> e?.message ?: "Erro ao cadastrar usuário."
                    }
                    onError(message)
                }
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    val message = when (val e = task.exception) {
                        is FirebaseAuthInvalidUserException -> "Usuário não encontrado."
                        is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha inválidos."
                        else -> e?.message ?: "Erro ao fazer login."
                    }
                    onError(message)
                }
            }
    }
}
