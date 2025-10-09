package com.example.charadasud.modelo

// Jugador hereda de Persona(nombre)
class Jugador(
    nombre: String,
    var puntaje: Int = 0
) : Persona(nombre) {

    override fun toString(): String = "$nombre (puntaje: $puntaje)"
}
