package com.example.charadasud.vista

// Importaciones necesarias para Jetpack Compose y Android
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    val juego = remember { Game() }

    var pantalla by remember { mutableStateOf("menu") }
    var modoEquipo by remember { mutableStateOf<Boolean?>(null) }
    var nombreJugador by remember { mutableStateOf("") }
    var nombreEquipoA by remember { mutableStateOf("") }
    var nombreEquipoB by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf<Categoria?>(null) }
    var mensajeFinal by remember { mutableStateOf<String?>(null) }

    var tiempoRestante by remember { mutableStateOf(60) }
    var palabraActual by remember { mutableStateOf<String?>(null) }
    var puntaje by remember { mutableStateOf(0) }
    var equipoTurno by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        juego.listener = object : Game.JuegoListener {
            override fun onTick(segundosRestantes: Int) { tiempoRestante = segundosRestantes }
            override fun onTiempoTerminado() { mensajeFinal = "⏰ ¡Tiempo terminado!" }
            override fun onNuevaPalabra(palabra: String) { palabraActual = palabra }
            override fun onPuntajeActualizado(nuevoPuntaje: Int) { puntaje = nuevoPuntaje }
            override fun onTurnoCambiado(equipo: com.example.charadasud.modelo.Equipo) {
                equipoTurno = equipo.nombre
            }
        }
    }

    when (pantalla) {
        "menu" -> MenuPantalla(
            onSeleccionModo = { modoEquipo = it; pantalla = "config" }
        )

        "config" -> ConfiguracionPantalla(
            modoEquipo,
            nombreJugador,
            nombreEquipoA,
            nombreEquipoB,
            juego.categorias,
            onNombreJugadorChange = { nombreJugador = it },
            onNombreEquipoAChange = { nombreEquipoA = it },
            onNombreEquipoBChange = { nombreEquipoB = it },
            onCategoriaSeleccionada = { cat ->
                categoriaSeleccionada = cat
                if (modoEquipo == false) {
                    juego.crearJugador(nombreJugador)
                } else {
                    juego.crearEquipos(nombreEquipoA, nombreEquipoB)
                }
                juego.seleccionarCategoria(cat.nombre)
                juego.palabraAleatoria()
                juego.iniciarTemporizador(60)
                equipoTurno = juego.equipoActual?.nombre
                pantalla = "juego"
            }
        )

        "juego" -> JuegoPantalla(
            palabraActual,
            tiempoRestante,
            puntaje,
            equipoTurno,
            mensajeFinal,
            modoEquipo == true,
            onAdivinado = { juego.registrarAcierto() },
            onNoAdivinado = { juego.pasarTurno() },

            // 🔸 CORREGIDO: reinicia la ronda y sincroniza puntaje y turno
            onReiniciarRonda = {
                juego.reiniciarRonda()
                mensajeFinal = null
                puntaje = juego.equipoActual?.puntaje ?: 0
                equipoTurno = juego.equipoActual?.nombre
            },

            onVolverMenu = {
                juego.reiniciarJuego()
                pantalla = "menu"
                categoriaSeleccionada = null
                mensajeFinal = null
                puntaje = 0
            }
        )
    }
}

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

        if (modoEquipo == false) {
            OutlinedTextField(value = nombreJugador, onValueChange = onNombreJugadorChange, label = { Text("Nombre del jugador") })
        } else {
            OutlinedTextField(value = nombreEquipoA, onValueChange = onNombreEquipoAChange, label = { Text("Equipo A") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = nombreEquipoB, onValueChange = onNombreEquipoBChange, label = { Text("Equipo B") })
        }

        Spacer(Modifier.height(16.dp))
        Text("Selecciona una categoría:", color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))

        categorias.forEach { cat ->
            Button(
                onClick = { onCategoriaSeleccionada(cat) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
            ) { Text(cat.nombre) }
        }
    }
}

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
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        mensajeFinal?.let {
            Text(it, color = Color.Yellow, fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onAdivinado, modifier = Modifier.fillMaxWidth()) { Text("Adivinado ✅") }
            if (esEquipos) Button(onClick = onNoAdivinado, modifier = Modifier.fillMaxWidth()) { Text("No adivinó ❌") }
            Button(onClick = onReiniciarRonda, modifier = Modifier.fillMaxWidth()) { Text("Reiniciar Ronda 🔄") }
            Button(onClick = onVolverMenu, modifier = Modifier.fillMaxWidth()) { Text("Volver al menú 🔙") }
        }
    }
}
