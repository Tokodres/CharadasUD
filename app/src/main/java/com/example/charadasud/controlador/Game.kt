package com.example.charadasud.controlador

import com.example.charadasud.modelo.Categoria
import com.example.charadasud.modelo.Equipo
import com.example.charadasud.modelo.Jugador
import com.example.charadasud.modelo.Temporizador
import kotlin.random.Random

/**
 * Controlador principal del juego Charadas.
 * Maneja jugadores/equipos, categorías, palabras, temporizador y puntajes.
 */
class Game(
    val jugadores: MutableList<Jugador> = mutableListOf(),
    val equipos: MutableList<Equipo> = mutableListOf(),
    val categorias: MutableList<Categoria> = mutableListOf(),
    var equipoActual: Equipo? = null,
    var categoriaActual: Categoria? = null,
    var temporizador: Temporizador? = null,
    var palabraActual: String? = null,
    var listener: JuegoListener? = null
) {

    // Mapa interno para puntajes individuales (usa referencia de objeto Jugador como clave).
    // Es independiente de si la clase Jugador tiene o no una propiedad 'puntaje'.
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
        // Inicializa puntaje en el mapa si no existe
        playerScores.putIfAbsent(jugador, 0)
        // Notificar puntaje actual (útil si la UI espera un onPuntajeActualizado)
        listener?.onPuntajeActualizado(playerScores[jugador] ?: 0)
        return jugador
    }

    /**
     * Devuelve el puntaje del jugador (según el mapa interno).
     * Retorna 0 si no se encuentra.
     */
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

        // Notificar estado inicial al listener
        listener?.onPuntajeActualizado(equipoActual?.puntaje ?: 0)
        equipoActual?.let { listener?.onTurnoCambiado(it) }
    }

    fun pasarTurno() {
        if (equipos.isEmpty()) return
        val idx = equipos.indexOf(equipoActual).let { if (it < 0) 0 else it }
        equipoActual = equipos[(idx + 1) % equipos.size]

        equipoActual?.let { listener?.onTurnoCambiado(it) }
        listener?.onPuntajeActualizado(equipoActual?.puntaje ?: 0)
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
    // PUNTAJE: Aciertos
    // -----------------------
    /**
     * Registra un acierto:
     * - Si hay equipo en turno -> incrementa equipo.puntaje y notifica
     * - Si no hay equipos pero hay jugadores -> incrementa puntaje del primer jugador (modo individual)
     */
    fun registrarAcierto() {
        // Prioriza modo por equipos si existe equipoActual
        equipoActual?.let { equipo ->
            equipo.puntaje++
            listener?.onPuntajeActualizado(equipo.puntaje)
            palabraAleatoria()
            return
        }

        // Modo individual: si hay jugadores, asigna al primer jugador (habitualmente el único)
        if (jugadores.isNotEmpty()) {
            val jugador = jugadores.first()
            val nuevo = (playerScores[jugador] ?: 0) + 1
            playerScores[jugador] = nuevo
            listener?.onPuntajeActualizado(nuevo)
            palabraAleatoria()
            return
        }

        // Si no hay jugadores ni equipos: no hay lugar donde sumar puntaje
        // (opcional: podrías notificar / loggear)
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

    /**
     * Reinicia la ronda (nueva palabra + reinicia tiempo) y notifica la UI.
     * NOTA: no resetea puntajes.
     */
    fun reiniciarRonda() {
        palabraAleatoria()
        iniciarTemporizador(60)

        // Notificar puntaje actual (si estamos en equipos, usar equipoActual; si individual, usar primer jugador)
        val score = equipoActual?.puntaje ?: (jugadores.firstOrNull()?.let { playerScores[it] ?: 0 } ?: 0)
        listener?.onPuntajeActualizado(score)

        equipoActual?.let { listener?.onTurnoCambiado(it) }
    }

    /**
     * Reinicia completamente el juego:
     * - Resetea puntajes de equipos y jugadores
     * - Limpia palabra actual y establece turno al primer equipo (si existe)
     */
    fun reiniciarJuego() {
        equipos.forEach { it.puntaje = 0 }
        playerScores.keys.forEach { playerScores[it] = 0 }

        palabraActual = null
        equipoActual = equipos.firstOrNull()

        // Notificar cambios para que la UI se sincronice
        listener?.onPuntajeActualizado(equipoActual?.puntaje ?: (jugadores.firstOrNull()?.let { playerScores[it] ?: 0 } ?: 0))
        equipoActual?.let { listener?.onTurnoCambiado(it) }
        // Si quieres también notificar palabra nula, podrías definir un callback adicional.
    }

    // -----------------------
    // INTERFAZ DEL LISTENER
    // -----------------------
    interface JuegoListener {
        fun onTick(segundosRestantes: Int)
        fun onTiempoTerminado()
        fun onNuevaPalabra(palabra: String)
        fun onPuntajeActualizado(nuevoPuntaje: Int)
        fun onTurnoCambiado(equipo: Equipo)
    }
}
