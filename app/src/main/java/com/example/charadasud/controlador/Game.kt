package com.example.charadasud.controlador

import com.example.charadasud.modelo.Categoria
import com.example.charadasud.modelo.Equipo
import com.example.charadasud.modelo.Jugador
import com.example.charadasud.modelo.Temporizador
import kotlin.random.Random

/**
 * ---------------------------------------------------------------
 * CLASE: Game
 * ---------------------------------------------------------------
 * Controlador principal del juego de Charadas.
 *
 * Su función es manejar toda la lógica del juego:
 *  - Crear jugadores o equipos
 *  - Administrar categorías y palabras
 *  - Controlar el temporizador
 *  - Llevar el puntaje
 *  - Informar a la vista (MainActivity) de los cambios mediante un listener
 *
 * Esta clase NO maneja la interfaz gráfica (eso lo hace la vista).
 * ---------------------------------------------------------------
 */
class Game(
    val jugadores: MutableList<Jugador> = mutableListOf(),   // Lista de jugadores individuales
    val equipos: MutableList<Equipo> = mutableListOf(),      // Lista de equipos (modo por equipos)
    val categorias: MutableList<Categoria> = mutableListOf(),// Lista de categorías disponibles
    var equipoActual: Equipo? = null,                        // Equipo que está jugando actualmente
    var categoriaActual: Categoria? = null,                  // Categoría actualmente seleccionada
    var temporizador: Temporizador? = null,                  // Objeto temporizador del juego
    var palabraActual: String? = null,                       // Palabra actual que se debe adivinar
    var listener: JuegoListener? = null                      // Listener para comunicar eventos con la vista
) {

    // -----------------------------------------------------------
    // BLOQUE DE INICIALIZACIÓN
    // -----------------------------------------------------------
    init {
        // Si no hay categorías definidas, se agregan unas por defecto.
        if (categorias.isEmpty()) inicializarCategoriasPorDefecto()
    }

    /**
     * Inicializa categorías predeterminadas del juego
     * (solo se llama una vez al inicio si la lista está vacía).
     */
    private fun inicializarCategoriasPorDefecto() {
        categorias.addAll(
            listOf(
                Categoria("Animales", listOf("perro", "gato", "elefante", "jirafa", "tigre")),
                Categoria("Películas", listOf("Titanic", "Avatar", "Inception", "Avengers", "Matrix")),
                Categoria("Profesiones", listOf("doctor", "ingeniero", "profesor", "piloto", "chef"))
            )
        )
    }

    // -----------------------------------------------------------
    // SECCIÓN: MANEJO DE JUGADORES (Modo individual)
    // -----------------------------------------------------------

    /**
     * Crea un nuevo jugador y lo agrega a la lista de jugadores.
     * @param nombre Nombre del jugador
     * @return El objeto Jugador creado
     */
    fun crearJugador(nombre: String): Jugador {
        val jugador = Jugador(nombre)
        jugadores.add(jugador)
        return jugador
    }

    // -----------------------------------------------------------
    // SECCIÓN: MANEJO DE EQUIPOS (Modo por equipos)
    // -----------------------------------------------------------

    /**
     * Crea dos equipos con los nombres dados (o nombres genéricos si están vacíos).
     * @param nombreA Nombre del primer equipo
     * @param nombreB Nombre del segundo equipo
     */
    fun crearEquipos(nombreA: String, nombreB: String) {
        equipos.clear() // Limpia los equipos anteriores
        val equipo1 = Equipo(nombreA.ifBlank { "Equipo A" })
        val equipo2 = Equipo(nombreB.ifBlank { "Equipo B" })
        equipos.add(equipo1)
        equipos.add(equipo2)
        equipoActual = equipos.firstOrNull() // Asigna turno inicial
    }

    /**
     * Cambia el turno entre los equipos.
     * Si hay más de un equipo, pasa al siguiente de la lista.
     */
    fun pasarTurno() {
        if (equipos.isEmpty()) return
        // Encuentra el índice del equipo actual y pasa al siguiente (en bucle)
        val idx = equipos.indexOf(equipoActual).let { if (it < 0) 0 else it }
        equipoActual = equipos[(idx + 1) % equipos.size]
        // Notifica a la vista que el turno cambió
        listener?.onTurnoCambiado(equipoActual!!)
    }

    // -----------------------------------------------------------
    // SECCIÓN: MANEJO DE CATEGORÍAS Y PALABRAS
    // -----------------------------------------------------------

    /**
     * Selecciona una categoría por su nombre (ignora mayúsculas/minúsculas).
     * Si no se encuentra, lanza una excepción.
     */
    fun seleccionarCategoria(nombre: String) {
        categoriaActual = categorias.find { it.nombre.equals(nombre, ignoreCase = true) }
            ?: throw IllegalArgumentException("Categoría no encontrada")
    }

    /**
     * Escoge una palabra aleatoria de la categoría actual
     * y notifica a la vista que se debe mostrar.
     */
    fun palabraAleatoria() {
        val cat = categoriaActual ?: return
        palabraActual = cat.palabras.random()
        listener?.onNuevaPalabra(palabraActual!!)
    }

    // -----------------------------------------------------------
    // SECCIÓN: MANEJO DE PUNTAJE
    // -----------------------------------------------------------

    /**
     * Registra un acierto del equipo actual:
     * - Incrementa el puntaje del equipo
     * - Notifica el nuevo puntaje
     * - Cambia la palabra
     */
    fun registrarAcierto() {
        equipoActual?.let {
            it.puntaje++ // Suma un punto
            listener?.onPuntajeActualizado(it.puntaje)
            palabraAleatoria() // Genera nueva palabra
        }
    }

    // -----------------------------------------------------------
    // SECCIÓN: TEMPORIZADOR
    // -----------------------------------------------------------

    /**
     * Inicia el temporizador del juego.
     * @param segundos Duración del temporizador (por defecto 60 s)
     */
    fun iniciarTemporizador(segundos: Int = 60) {
        temporizador?.cancel() // Cancela el anterior si estaba corriendo
        temporizador = Temporizador(segundos)
        temporizador?.start(
            1000L, // Intervalo de 1 segundo
            { listener?.onTick(it) },          // Actualiza segundos restantes
            { listener?.onTiempoTerminado() }  // Llama al terminar el tiempo
        )
    }

    /**
     * Reinicia la ronda (nueva palabra + reinicia tiempo).
     */
    fun reiniciarRonda() {
        palabraAleatoria()
        iniciarTemporizador(60)
    }

    /**
     * Reinicia completamente el juego:
     * - Restablece los puntajes
     * - Limpia la palabra actual
     * - Devuelve el turno al primer equipo
     */
    fun reiniciarJuego() {
        equipos.forEach { it.puntaje = 0 }
        palabraActual = null
        equipoActual = equipos.firstOrNull()
    }

    // -----------------------------------------------------------
    // SECCIÓN: LISTENER DE EVENTOS (Comunicación con la vista)
    // -----------------------------------------------------------

    /**
     * Interfaz que define los eventos que la vista debe escuchar.
     * La vista (MainActivity) implementa este listener para reaccionar
     * a los cambios en el juego en tiempo real.
     */
    interface JuegoListener {
        fun onTick(segundosRestantes: Int)                // Llamado cada segundo
        fun onTiempoTerminado()                           // Llamado al terminar el tiempo
        fun onNuevaPalabra(palabra: String)               // Llamado cuando hay una palabra nueva
        fun onPuntajeActualizado(nuevoPuntaje: Int)       // Llamado cuando cambia el puntaje
        fun onTurnoCambiado(equipo: Equipo)               // Llamado cuando se cambia de equipo
    }
}
