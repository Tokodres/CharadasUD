package com.example.charadasud.modelo

import kotlin.random.Random

class Game(
    val jugadores: MutableList<Jugador> = mutableListOf(),
    val categorias: MutableList<Categoria> = mutableListOf(),
    var categoriaActual: Categoria? = null,
    var jugadorActual: Jugador? = null,
    var temporizador: Temporizador? = null,
    var listener: JuegoListener? = null
) {

    init {
        if (categorias.isEmpty()) inicializarCategoriasPorDefecto()
    }

    private fun inicializarCategoriasPorDefecto() {
        categorias.addAll(
            listOf(
                Categoria("Animales", listOf("perro","gato","elefante","jirafa","tigre")),
                Categoria("Frutas", listOf("manzana","pera","banano","fresa","naranja")),
                Categoria("Profesiones", listOf("doctor","ingeniero","profesor","piloto","chef"))
            )
        )
    }

    /* ---------- Juego (control) ---------- */
    fun iniciarJuego() {
        jugadorActual = jugadores.firstOrNull()
        categoriaActual = categorias.firstOrNull()
    }

    fun siguienteJugador() {
        if (jugadores.isEmpty()) return
        val indiceActual = jugadores.indexOf(jugadorActual).let { if (it < 0) 0 else it }
        val siguiente = (indiceActual + 1) % jugadores.size
        jugadorActual = jugadores[siguiente]
    }

    /* ---------- Jugadores ---------- */
    fun crearJugador(nombre: String): Jugador {
        val jugador = Jugador(nombre = nombre)
        jugadores.add(jugador)
        listener?.onJugadorCreado(jugador)
        return jugador
    }

    fun obtenerJugadores(): List<Jugador> = jugadores.toList()
    fun obtenerJugadorActual(): Jugador? = jugadorActual

    /* ---------- Categorías y palabras ---------- */
    fun obtenerNombresCategorias(): List<String> = categorias.map { it.nombre }

    fun seleccionarCategoria(nombreCategoria: String): Categoria {
        val cat = categorias.find { it.nombre.equals(nombreCategoria, ignoreCase = true) }
            ?: throw IllegalArgumentException("Categoría no encontrada: $nombreCategoria")
        categoriaActual = cat
        return cat
    }

    fun palabraAleatoriaDeCategoria(categoria: Categoria = categoriaActual ?: throw IllegalStateException("No hay categoría seleccionada")): String {
        if (categoria.palabras.isEmpty()) throw IllegalStateException("La categoría no contiene palabras")
        val palabra = categoria.palabras[Random.nextInt(categoria.palabras.size)]
        listener?.onNuevaPalabra(palabra)
        return palabra
    }

    /* ---------- Temporizador: métodos públicos usados por la UI ---------- */

    /**
     * Inicia un temporizador de [segundos]. Cada segundo se invoca listener.onTick(...)
     * y al finalizar se invoca listener.onTiempoTerminado().
     */
    fun iniciarTemporizador(segundos: Int = 60) {
        // Cancela si hay uno activo
        temporizador?.cancel()
        temporizador = Temporizador(segundos)
        temporizador?.start(1000L,
            { secs -> listener?.onTick(secs) },
            { listener?.onTiempoTerminado() }
        )
    }

    /** Cancela el temporizador activo (si existe). */
    fun cancelarTemporizador() {
        temporizador?.cancel()
        temporizador = null
    }

    /** Libera recursos (llama a cancelarTemporizador()). */
    fun liberar() {
        cancelarTemporizador()
    }

    /* ---------- Listener ---------- */
    interface JuegoListener {
        fun onTick(segundosRestantes: Int)
        fun onTiempoTerminado()
        fun onNuevaPalabra(palabra: String)
        fun onJugadorCreado(jugador: Jugador)
    }
}
