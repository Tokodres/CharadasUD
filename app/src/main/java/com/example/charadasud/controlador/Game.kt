package com.example.charadasud.controlador

import com.example.charadasud.modelo.Categoria
import com.example.charadasud.modelo.Equipo
import com.example.charadasud.modelo.Jugador
import com.example.charadasud.modelo.Temporizador

class Game(
    val jugadores: MutableList<Jugador> = mutableListOf(),
    val equipos: MutableList<Equipo> = mutableListOf(),
    val categorias: MutableList<Categoria> = mutableListOf(),
    var equipoActual: Equipo? = null,
    var categoriaActual: Categoria? = null,
    var temporizador: Temporizador? = null,
    var palabraActual: String? = null,
    var rondaActual: Int = 1,
    val totalRondas: Int = 5,
    var listener: JuegoListener? = null
) {

    private val playerScores: MutableMap<Jugador, Int> = mutableMapOf()

    init {
        if (categorias.isEmpty()) inicializarCategoriasPorDefecto()
    }

    private fun inicializarCategoriasPorDefecto() {
        categorias.addAll(
            listOf(
                Categoria("Animales", listOf("perro", "gato", "elefante", "jirafa", "tigre")),
                Categoria("Películas", listOf("Titanic", "Avatar", "Inception", "Avengers", "Matrix")),
                Categoria("Profesiones", listOf("doctor", "ingeniero", "profesor", "piloto", "chef"))
            )
        )
    }

    // -----------------------
    // JUGADORES (modo individual)
    // -----------------------
    fun crearJugador(nombre: String): Jugador {
        val jugador = Jugador(nombre)
        jugadores.add(jugador)
        playerScores.putIfAbsent(jugador, 0)
        listener?.onPuntajeActualizado(playerScores[jugador] ?: 0)
        return jugador
    }

    fun obtenerPuntajeJugador(jugador: Jugador): Int = playerScores[jugador] ?: 0

    // -----------------------
    // EQUIPOS (modo por equipos)
    // -----------------------
    fun crearEquipos(nombreA: String, nombreB: String) {
        equipos.clear()
        val equipo1 = Equipo(nombreA.ifBlank { "Equipo A" })
        val equipo2 = Equipo(nombreB.ifBlank { "Equipo B" })
        equipos.add(equipo1)
        equipos.add(equipo2)
        equipoActual = equipos.firstOrNull()

        listener?.onPuntajeActualizado(equipoActual?.puntaje ?: 0)
        equipoActual?.let { listener?.onTurnoCambiado(it) }
    }

    fun pasarTurno() {
        if (equipos.isEmpty()) return

        val idx = equipos.indexOf(equipoActual).let { if (it < 0) 0 else it }
        equipoActual = equipos[(idx + 1) % equipos.size]

        // Aumenta ronda cuando vuelva al primer equipo
        if (idx + 1 >= equipos.size) rondaActual++

        // Comprobar si se terminó el juego
        if (rondaActual > totalRondas) {
            val ganador = equipos.maxByOrNull { it.puntaje }
            listener?.onJuegoTerminado(ganador)
            return
        }

        equipoActual?.let { listener?.onTurnoCambiado(it) }
        listener?.onPuntajeActualizado(equipoActual?.puntaje ?: 0)
        reiniciarRonda()
    }

    // -----------------------
    // CATEGORÍAS Y PALABRAS
    // -----------------------
    fun seleccionarCategoria(nombre: String) {
        categoriaActual = categorias.find { it.nombre.equals(nombre, ignoreCase = true) }
            ?: throw IllegalArgumentException("Categoría no encontrada")
    }

    fun palabraAleatoria() {
        val cat = categoriaActual ?: return
        palabraActual = cat.palabras.random()
        listener?.onNuevaPalabra(palabraActual!!)
    }

    // -----------------------
    // PUNTAJE
    // -----------------------
    fun registrarAcierto() {
        equipoActual?.let { equipo ->
            equipo.puntaje++
            listener?.onPuntajeActualizado(equipo.puntaje)
            palabraAleatoria()
            return
        }

        if (jugadores.isNotEmpty()) {
            val jugador = jugadores.first()
            val nuevo = (playerScores[jugador] ?: 0) + 1
            playerScores[jugador] = nuevo
            listener?.onPuntajeActualizado(nuevo)
            palabraAleatoria()
        }
    }

    // -----------------------
    // TEMPORIZADOR
    // -----------------------
    fun iniciarTemporizador(segundos: Int = 60) {
        temporizador?.cancel()
        temporizador = Temporizador(segundos)
        temporizador?.start(
            1000L,
            { segundosRestantes -> listener?.onTick(segundosRestantes) },
            { listener?.onTiempoTerminado() }
        )
    }

    fun reiniciarRonda() {
        palabraAleatoria()
        iniciarTemporizador(60)
        val score = equipoActual?.puntaje ?: (jugadores.firstOrNull()?.let { playerScores[it] ?: 0 } ?: 0)
        listener?.onPuntajeActualizado(score)
        equipoActual?.let { listener?.onTurnoCambiado(it) }
    }

    fun reiniciarJuegoCompleto() {
        equipos.forEach { it.puntaje = 0 }
        playerScores.keys.forEach { playerScores[it] = 0 }
        rondaActual = 1
        palabraActual = null
        equipoActual = equipos.firstOrNull()
        listener?.onPuntajeActualizado(equipoActual?.puntaje ?: 0)
        equipoActual?.let { listener?.onTurnoCambiado(it) }
    }

    // -----------------------
    // LISTENER
    // -----------------------
    interface JuegoListener {
        fun onTick(segundosRestantes: Int)
        fun onTiempoTerminado()
        fun onNuevaPalabra(palabra: String)
        fun onPuntajeActualizado(nuevoPuntaje: Int)
        fun onTurnoCambiado(equipo: Equipo)
        fun onJuegoTerminado(ganador: Equipo?)
    }
}
