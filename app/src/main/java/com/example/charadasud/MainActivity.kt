package com.example.charadasud

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.charadasud.modelo.Game
import com.example.charadasud.modelo.Jugador
import com.example.charadasud.ui.theme.CharadasUDTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CharadasUDTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    JuegoScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuegoScreen() {
    val context = LocalContext.current

    // Estado observable para UI
    var nuevoNombre by remember { mutableStateOf("") }
    var estadoTexto by remember { mutableStateOf("Estado: --") }
    var palabraTexto by remember { mutableStateOf("Palabra: --") }
    var jugadoresCount by remember { mutableStateOf(0) }
    var segundosRestantes by remember { mutableStateOf<Int?>(null) }

    // Crea una instancia de Game que sobreviva recomposiciones
    val juego = remember { Game() }

    // Asignar listener y limpiar cuando se destruya el composable
    DisposableEffect(Unit) {
        val listener = object : Game.JuegoListener {
            override fun onTick(segundosRestantesCB: Int) {
                // Se llama desde el hilo principal (CountDownTimer), podemos actualizar estados
                segundosRestantes = segundosRestantesCB
                estadoTexto = "Tiempo: ${segundosRestantesCB}s"
                Log.d("JUEGO", "Tiempo restante: $segundosRestantesCB")
            }

            override fun onTiempoTerminado() {
                segundosRestantes = 0
                estadoTexto = "Tiempo finalizado"
                Toast.makeText(context, "¡Tiempo terminado!", Toast.LENGTH_SHORT).show()
                Log.d("JUEGO", "¡Tiempo terminado!")
            }

            override fun onNuevaPalabra(palabra: String) {
                palabraTexto = "Palabra: $palabra"
                Log.d("JUEGO", "Palabra nueva: $palabra")
            }

            override fun onJugadorCreado(jugador: Jugador) {
                jugadoresCount = juego.obtenerJugadores().size
                Log.d("JUEGO", "Jugador creado: $jugador")
            }
        }
        juego.listener = listener

            onDispose {
                juego.listener = null
                juego.liberar()
        }
    }

    // UI
    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = estadoTexto, style = MaterialTheme.typography.titleMedium)
        Text(text = palabraTexto, style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = nuevoNombre,
            onValueChange = { nuevoNombre = it },
            label = { Text("Nombre jugador") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val nombre = nuevoNombre.trim()
                if (nombre.isEmpty()) {
                    Toast.makeText(context, "Ingresa un nombre", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                juego.crearJugador(nombre)
                nuevoNombre = ""
                estadoTexto = "Jugadores: ${juego.obtenerJugadores().size}"
            }) {
                Text("Agregar jugador")
            }

            Button(onClick = {
                // iniciar ronda rápida (usa la primera categoría disponible)
                val cats = juego.obtenerNombresCategorias()
                if (cats.isEmpty()) {
                    Toast.makeText(context, "No hay categorías", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val cat = juego.seleccionarCategoria(cats.first())
                val palabra = juego.palabraAleatoriaDeCategoria(cat)
                // palabraTexto se actualizará por el callback onNuevaPalabra
                juego.iniciarTemporizador(10) // usa 60 en producción
                estadoTexto = "Ronda iniciada"
            }) {
                Text("Iniciar ronda (10s)")
            }

            Button(onClick = {
                juego.cancelarTemporizador()
                estadoTexto = "Temporizador detenido"
            }) {
                Text("Parar")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Jugadores registrados: $jugadoresCount")
        Text("Segundos restantes: ${segundosRestantes ?: "--"}")
    }
}
/*
*package com.example.charadasud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.charadasud.ui.theme.CharadasUDTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CharadasUDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CharadasUDTheme {
        Greeting("Android")
    }
}
* */