package com.example.charadasud.vista

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CharadasApp() }
    }
}

@Composable
fun CharadasApp() {
    val juego = remember { Game() }

    var pantalla by remember { mutableStateOf("menu") }
    var modoEquipo by remember { mutableStateOf<Boolean?>(null) }
    var nombreJugador by remember { mutableStateOf("") }
    var nombreEquipoA by remember { mutableStateOf("") }
    var nombreEquipoB by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf<Categoria?>(null) }

    var tiempoRestante by remember { mutableStateOf(60) }
    var palabraActual by remember { mutableStateOf<String?>(null) }
    var puntaje by remember { mutableStateOf(0) }
    var equipoTurno by remember { mutableStateOf<String?>(null) }
    var ronda by remember { mutableStateOf(1) }
    var mensajeFinal by remember { mutableStateOf<String?>(null) }
    var finPartida by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        juego.listener = object : Game.JuegoListener {
            override fun onTick(segundosRestantes: Int) {
                tiempoRestante = segundosRestantes
                if (modoEquipo == false && segundosRestantes <= 0) {
                    // jugador individual termina la partida si se acaba el tiempo
                    finPartida = true
                }
            }
            override fun onTiempoTerminado() {
                if (modoEquipo == false) finPartida = true
                else mensajeFinal = "⏰ ¡Tiempo terminado!"
            }
            override fun onNuevaPalabra(palabra: String) { palabraActual = palabra }
            override fun onPuntajeActualizado(nuevoPuntaje: Int) { puntaje = nuevoPuntaje }
            override fun onTurnoCambiado(equipo: com.example.charadasud.modelo.Equipo) {
                equipoTurno = equipo.nombre
                ronda = juego.rondaActual
            }
            override fun onJuegoTerminado(ganadorEquipo: com.example.charadasud.modelo.Equipo?) {
                finPartida = true
            }
        }
    }

    when (pantalla) {
        "menu" -> MenuPantalla(onSeleccionModo = { modoEquipo = it; pantalla = "config" })

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
                if (modoEquipo == false) juego.crearJugador(nombreJugador)
                else juego.crearEquipos(nombreEquipoA, nombreEquipoB)

                juego.seleccionarCategoria(cat.nombre)
                juego.palabraAleatoria()
                juego.iniciarTemporizador(60)
                equipoTurno = juego.equipoActual?.nombre
                ronda = juego.rondaActual
                finPartida = false
                pantalla = "juego"
            }
        )

        "juego" -> JuegoPantalla(
            palabra = palabraActual,
            tiempo = tiempoRestante,
            puntaje = puntaje,
            equipoTurno = equipoTurno,
            ronda = ronda,
            mensajeFinal = mensajeFinal,
            finPartida = finPartida,
            esEquipos = modoEquipo == true,
            onAdivinado = {
                juego.registrarAcierto()
                if (modoEquipo == false && !finPartida) {
                    juego.iniciarTemporizador(60)
                }
            },
            onNoAdivinado = {
                if (modoEquipo == true) juego.pasarTurno()
                else finPartida = true
            },
            onReiniciarJuego = {
                juego.reiniciarJuegoCompleto()
                juego.seleccionarCategoria(categoriaSeleccionada?.nombre ?: "")
                juego.palabraAleatoria()
                juego.iniciarTemporizador(60)

                mensajeFinal = null
                puntaje = juego.equipoActual?.puntaje ?: 0
                equipoTurno = juego.equipoActual?.nombre
                ronda = juego.rondaActual
                palabraActual = juego.palabraActual
                finPartida = false
            },
            onCambiarCategoria = {
                pantalla = "config"
                finPartida = false
                mensajeFinal = null
                puntaje = 0
                ronda = 1
                juego.reiniciarJuegoCompleto()
            },
            onVolverMenu = {
                juego.reiniciarJuegoCompleto()
                pantalla = "menu"
                categoriaSeleccionada = null
                mensajeFinal = null
                puntaje = 0
                finPartida = false
                ronda = 1
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

        if (modoEquipo == false)
            OutlinedTextField(value = nombreJugador, onValueChange = onNombreJugadorChange, label = { Text("Nombre del jugador") })
        else {
            OutlinedTextField(value = nombreEquipoA, onValueChange = onNombreEquipoAChange, label = { Text("Equipo A") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = nombreEquipoB, onValueChange = onNombreEquipoBChange, label = { Text("Equipo B") })
        }

        Spacer(Modifier.height(16.dp))
        Text("Selecciona una categoría:", color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))

        categorias.forEach { cat ->
            Button(onClick = { onCategoriaSeleccionada(cat) }, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                Text(cat.nombre)
            }
        }
    }
}

@Composable
fun JuegoPantalla(
    palabra: String?,
    tiempo: Int,
    puntaje: Int,
    equipoTurno: String?,
    ronda: Int,
    mensajeFinal: String?,
    finPartida: Boolean,
    esEquipos: Boolean,
    onAdivinado: () -> Unit,
    onNoAdivinado: () -> Unit,
    onReiniciarJuego: () -> Unit,
    onCambiarCategoria: () -> Unit,
    onVolverMenu: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF2196F3)).padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (finPartida) {
            Text(
                text = if (esEquipos) "🏆 Fin de la partida" else "Puntaje alcanzado: $puntaje",
                color = Color.Yellow,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onReiniciarJuego, modifier = Modifier.fillMaxWidth()) { Text("Reiniciar Juego 🔄") }
            Button(onClick = onCambiarCategoria, modifier = Modifier.fillMaxWidth()) { Text("Cambiar Categoría 🎯") }
            Button(onClick = onVolverMenu, modifier = Modifier.fillMaxWidth()) { Text("Volver al Menú 🔙") }
            return
        }

        if (esEquipos) Text("Ronda: $ronda / 5", color = Color.White, fontSize = 20.sp)
        if (esEquipos) Text("Turno de: ${equipoTurno ?: ""}", color = Color.Yellow, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(12.dp))
        Text("Palabra:", color = Color.White, fontSize = 22.sp)
        Text(palabra ?: "", color = Color.Yellow, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text("⏱ Tiempo: $tiempo s", color = Color.White)
        Spacer(Modifier.height(12.dp))
        Text("Puntaje: $puntaje", color = Color.White, fontSize = 20.sp)

        mensajeFinal?.let { Text(it, color = Color.Yellow, fontSize = 20.sp) }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onAdivinado, modifier = Modifier.fillMaxWidth()) { Text("Adivinado ✅") }

            if (esEquipos)
                Button(onClick = onNoAdivinado, modifier = Modifier.fillMaxWidth()) { Text("Pasar Ronda ⏭") }
            else
                Button(onClick = onNoAdivinado, modifier = Modifier.fillMaxWidth()) { Text("No Adivinó ❌") }

            Spacer(Modifier.height(8.dp))
            Divider(color = Color.White, thickness = 1.dp)
            Spacer(Modifier.height(8.dp))

            Button(onClick = onReiniciarJuego, modifier = Modifier.fillMaxWidth()) { Text("Reiniciar Juego 🔄") }
            Button(onClick = onVolverMenu, modifier = Modifier.fillMaxWidth()) { Text("Volver al Menú 🔙") }
        }
    }
}