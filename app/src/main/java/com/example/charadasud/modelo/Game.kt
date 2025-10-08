package com.example.charadasud.modelo

class Game(
    val jugadores: List<Jugador>,
    val categorias: List<Categoria>,
    var categoriaActual: Categoria? = null,
    var jugadorActual: Jugador? = null,
    var temporizador: Temporizador? = null
) {
    fun iniciarJuego() {
        jugadorActual = jugadores.firstOrNull()
        categoriaActual = categorias.firstOrNull()
        temporizador = Temporizador(60) // ejemplo: 60 segundos
    }

    fun siguienteJugador() {
        val indice = jugadores.indexOf(jugadorActual)
        val siguiente = (indice + 1) % jugadores.size
        jugadorActual = jugadores[siguiente]
    }
}