package com.example.charadasud

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.charadasud.modelo.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CharadasApp()
        }
    }
}

@Composable
fun CharadasApp() {
    val context = LocalContext.current
    val juego = remember { Game() }

    // Estados
    var nombreJugador by remember { mutableStateOf("") }
    var jugadorCreado by remember { mutableStateOf(false) }
    var categoriaSeleccionada by remember { mutableStateOf<Categoria?>(null) }
    var palabraActual by remember { mutableStateOf<String?>(null) }
    var tiempoRestante by remember { mutableStateOf(60) }
    var puntaje by remember { mutableStateOf(0) }
    var juegoTerminado by remember { mutableStateOf(false) }

    // Conectar la lógica del juego con la UI
    LaunchedEffect(Unit) {
        juego.listener = object : Game.JuegoListener {
            override fun onTick(segundosRestantes: Int) {
                tiempoRestante = segundosRestantes
            }

            override fun onTiempoTerminado() {
                juegoTerminado = true
            }

            override fun onNuevaPalabra(palabra: String) {
                palabraActual = palabra
            }

            override fun onJugadorCreado(jugador: Jugador) {
                Toast.makeText(context, "Jugador agregado: ${jugador.nombre}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- Pantalla de agregar jugador ---
    if (!jugadorCreado) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2196F3))
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("CHARADAS UD", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = nombreJugador,
                onValueChange = { nombreJugador = it },
                label = { Text("Nombre del jugador") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (nombreJugador.isNotBlank()) {
                        juego.crearJugador(nombreJugador)
                        jugadorCreado = true
                    } else {
                        Toast.makeText(context, "Ingresa un nombre", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
            ) {
                Text("Comenzar", color = Color.Black)
            }
        }
    } else {
        // --- Pantalla principal ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2196F3))
        ) {
            // 🟦 1/4 superior: título + jugador
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF1976D2)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CHARADAS UD", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Jugador: $nombreJugador", color = Color.White, fontSize = 18.sp)
                }
            }

            // 🟨 3/4 inferior: tablero 2x2
            Column(
                modifier = Modifier
                    .weight(3f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1️⃣ Categoría
                    CuadroJuego(
                        titulo = "Categoría",
                        modifier = Modifier.weight(1f)
                    ) {
                        juego.obtenerNombresCategorias().forEach { nombreCategoria ->
                            Button(
                                onClick = {
                                    val categoria = juego.seleccionarCategoria(nombreCategoria)
                                    categoriaSeleccionada = categoria
                                    palabraActual = juego.palabraAleatoriaDeCategoria(categoria)
                                    juego.iniciarTemporizador(60)
                                    juegoTerminado = false
                                    Toast.makeText(context, "Categoría: $nombreCategoria", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                Text(nombreCategoria, color = Color.Black)
                            }
                        }
                    }

                    // 2️⃣ Juego
                    CuadroJuego(
                        titulo = "Juego",
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = palabraActual ?: "Selecciona una categoría",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("⏱ $tiempoRestante s", color = Color.Blue, fontWeight = FontWeight.Bold)
                        if (palabraActual != null && !juegoTerminado) {
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    puntaje++
                                    palabraActual = juego.palabraAleatoriaDeCategoria(categoriaSeleccionada!!)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text("Adivinado ✅", color = Color.White)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 3️⃣ Game Over
                    CuadroJuego(
                        titulo = "Game Over",
                        modifier = Modifier.weight(1f)
                    ) {
                        if (juegoTerminado)
                            Text("⏰ ¡Tiempo terminado!", fontWeight = FontWeight.Bold)
                        else
                            Text("En juego...", color = Color.Gray)
                    }

                    // 4️⃣ Resultados
                    CuadroJuego(
                        titulo = "Resultados",
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Puntaje: $puntaje", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                juegoTerminado = false
                                puntaje = 0
                                palabraActual = null
                                categoriaSeleccionada = null
                                tiempoRestante = 60
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
                        ) {
                            Text("Reiniciar", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CuadroJuego(
    titulo: String,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
        Spacer(Modifier.height(6.dp))
        contenido()
    }
}
