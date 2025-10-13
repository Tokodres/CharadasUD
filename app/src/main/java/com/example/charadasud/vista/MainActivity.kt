package com.example.charadasud.vista

// Importaciones necesarias para Jetpack Compose y Android
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.charadasud.controlador.Game
import com.example.charadasud.modelo.Categoria

/**
 * MainActivity:
 * Vista principal del juego Charadas.
 * Su única responsabilidad es manejar la interfaz de usuario (UI)
 * y comunicarse con el controlador (Game.kt), quien tiene la lógica del juego.
 */
class MainActivity : ComponentActivity() {
    // Método que se ejecuta al crear la actividad principal
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define que el contenido de la pantalla usará Jetpack Compose
        setContent {
            CharadasApp() // Llama a la función composable principal
        }
    }
}

/**
 * CharadasApp:
 * Función principal que contiene el flujo completo del juego.
 * Aquí se decide qué pantalla mostrar (menú, configuración o juego)
 * y se manejan los estados principales de la interfaz.
 */
@Composable
fun CharadasApp() {
    // Se recuerda una única instancia del controlador Game durante toda la ejecución
    val juego = remember { Game() }

    // Variables de estado de la UI (se redibujan al cambiar)
    var pantalla by remember { mutableStateOf("menu") } // Controla la pantalla actual: "menu", "config" o "juego"
    var modoEquipo by remember { mutableStateOf<Boolean?>(null) } // true = modo equipos, false = jugador solo
    var nombreJugador by remember { mutableStateOf("") } // Nombre en modo solitario
    var nombreEquipoA by remember { mutableStateOf("") } // Nombre del equipo A
    var nombreEquipoB by remember { mutableStateOf("") } // Nombre del equipo B
    var categoriaSeleccionada by remember { mutableStateOf<Categoria?>(null) } // Categoría elegida del modelo
    var mensajeFinal by remember { mutableStateOf<String?>(null) } // Mensaje de fin de ronda o tiempo

    // Estados dinámicos del juego (se actualizan mediante el listener del controlador)
    var tiempoRestante by remember { mutableStateOf(60) }
    var palabraActual by remember { mutableStateOf<String?>(null) }
    var puntaje by remember { mutableStateOf(0) }
    var equipoTurno by remember { mutableStateOf<String?>(null) }

    /**
     * Configura un listener del controlador Game para recibir actualizaciones
     * en tiempo real y reflejarlas en la interfaz de usuario.
     */
    LaunchedEffect(Unit) {
        juego.listener = object : Game.JuegoListener {
            // Actualiza el contador de tiempo en pantalla
            override fun onTick(segundosRestantes: Int) { tiempoRestante = segundosRestantes }

            // Muestra un mensaje cuando el tiempo acaba
            override fun onTiempoTerminado() { mensajeFinal = "⏰ ¡Tiempo terminado!" }

            // Muestra la nueva palabra seleccionada
            override fun onNuevaPalabra(palabra: String) { palabraActual = palabra }

            // Actualiza el puntaje del jugador o equipo
            override fun onPuntajeActualizado(nuevoPuntaje: Int) { puntaje = nuevoPuntaje }

            // Indica de quién es el turno en modo equipos
            override fun onTurnoCambiado(equipo: com.example.charadasud.modelo.Equipo) {
                equipoTurno = equipo.nombre
            }
        }
    }

    // Control de navegación entre pantallas según el estado "pantalla"
    when (pantalla) {
        // Pantalla inicial del menú principal
        "menu" -> MenuPantalla(
            onSeleccionModo = { modoEquipo = it; pantalla = "config" } // Pasa a configuración
        )

        // Pantalla de configuración del juego
        "config" -> ConfiguracionPantalla(
            modoEquipo,
            nombreJugador,
            nombreEquipoA,
            nombreEquipoB,
            juego.categorias, // Se obtienen las categorías del modelo
            onNombreJugadorChange = { nombreJugador = it },
            onNombreEquipoAChange = { nombreEquipoA = it },
            onNombreEquipoBChange = { nombreEquipoB = it },

            // Al seleccionar una categoría, se inicializa el juego desde el controlador
            onCategoriaSeleccionada = { cat ->
                categoriaSeleccionada = cat

                // Dependiendo del modo, se crean jugador o equipos
                if (modoEquipo == false) {
                    juego.crearJugador(nombreJugador)
                } else {
                    juego.crearEquipos(nombreEquipoA, nombreEquipoB)
                }

                // Se configura la categoría y se inicia el temporizador
                juego.seleccionarCategoria(cat.nombre)
                juego.palabraAleatoria()
                juego.iniciarTemporizador(60)

                // Se inicializa el turno actual (en modo equipos)
                equipoTurno = juego.equipoActual?.nombre

                // Se cambia la pantalla al modo de juego
                pantalla = "juego"
            }
        )

        // Pantalla principal del juego
        "juego" -> JuegoPantalla(
            palabraActual,
            tiempoRestante,
            puntaje,
            equipoTurno,
            mensajeFinal,
            modoEquipo == true, // true si es equipos
            onAdivinado = { juego.registrarAcierto() }, // Suma puntaje
            onNoAdivinado = { juego.pasarTurno() }, // Cambia el turno
            onReiniciarRonda = { juego.reiniciarRonda() }, // Reinicia ronda actual
            onVolverMenu = {
                // Reinicia todo el juego y vuelve al menú principal
                juego.reiniciarJuego()
                pantalla = "menu"
                categoriaSeleccionada = null
                mensajeFinal = null
                puntaje = 0
            }
        )
    }
}

/**
 * MenuPantalla:
 * Muestra el menú inicial para seleccionar el modo de juego.
 */
@Composable
fun MenuPantalla(onSeleccionModo: (Boolean) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF2196F3)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎭 CHARADAS UD", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onSeleccionModo(false) }) { Text("Jugador Solo") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onSeleccionModo(true) }) { Text("Equipos") }
    }
}

/**
 * ConfiguracionPantalla:
 * Permite ingresar los nombres del jugador o equipos y elegir una categoría.
 */
@Composable
fun ConfiguracionPantalla(
    modoEquipo: Boolean?,
    nombreJugador: String,
    nombreEquipoA: String,
    nombreEquipoB: String,
    categorias: List<Categoria>,
    onNombreJugadorChange: (String) -> Unit,
    onNombreEquipoAChange: (String) -> Unit,
    onNombreEquipoBChange: (String) -> Unit,
    onCategoriaSeleccionada: (Categoria) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF2196F3)).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Configuración", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        // Si es modo individual, se pide solo el nombre del jugador
        if (modoEquipo == false) {
            OutlinedTextField(value = nombreJugador, onValueChange = onNombreJugadorChange, label = { Text("Nombre del jugador") })
        } else {
            // Si es modo equipos, se piden los dos nombres
            OutlinedTextField(value = nombreEquipoA, onValueChange = onNombreEquipoAChange, label = { Text("Equipo A") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = nombreEquipoB, onValueChange = onNombreEquipoBChange, label = { Text("Equipo B") })
        }

        Spacer(Modifier.height(16.dp))
        Text("Selecciona una categoría:", color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))

        // Lista de botones con las categorías disponibles (vienen del modelo)
        categorias.forEach { cat ->
            Button(
                onClick = { onCategoriaSeleccionada(cat) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
            ) { Text(cat.nombre) }
        }
    }
}

/**
 * JuegoPantalla:
 * Muestra la interfaz principal del juego durante la ronda.
 */
@Composable
fun JuegoPantalla(
    palabra: String?,
    tiempo: Int,
    puntaje: Int,
    equipoTurno: String?,
    mensajeFinal: String?,
    esEquipos: Boolean,
    onAdivinado: () -> Unit,
    onNoAdivinado: () -> Unit,
    onReiniciarRonda: () -> Unit,
    onVolverMenu: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF2196F3)).padding(16.dp),
        verticalArrangement = Arrangement.Center, // <- centrado vertical
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sección superior: información del turno y palabra
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (esEquipos) {
                Text("Turno de: ${equipoTurno ?: ""}", color = Color.Yellow, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
            }
            Text("Palabra:", color = Color.White, fontSize = 22.sp)
            Text(palabra ?: "", color = Color.Yellow, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text("⏱ Tiempo: $tiempo s", color = Color.White)
            Spacer(Modifier.height(12.dp))
            Text("Puntaje: $puntaje", color = Color.White, fontSize = 20.sp)
        }

        // Mensaje de fin (solo si existe)
        mensajeFinal?.let {
            Text(it, color = Color.Yellow, fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        // Sección inferior: botones de acción
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onAdivinado, modifier = Modifier.fillMaxWidth()) { Text("Adivinado ✅") }
            if (esEquipos) Button(onClick = onNoAdivinado, modifier = Modifier.fillMaxWidth()) { Text("No adivinó ❌") }
            Button(onClick = onReiniciarRonda, modifier = Modifier.fillMaxWidth()) { Text("Reiniciar Ronda 🔄") }
            Button(onClick = onVolverMenu, modifier = Modifier.fillMaxWidth()) { Text("Volver al menú 🔙") }
        }
    }
}
