package com.example.charadasud.modelo

/**
 * Representa un equipo en el juego.
 * @param nombre Nombre del equipo
 * @param jugadores Lista de jugadores que pertenecen al equipo
 * @param puntaje Puntaje acumulado del equipo
 */
class Equipo(
    val nombre: String,
    val jugadores: MutableList<Jugador> = mutableListOf(),
    var puntaje: Int = 0
) {
    override fun toString(): String = "$nombre (puntaje: $puntaje)"
}
