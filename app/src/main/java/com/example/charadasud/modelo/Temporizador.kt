package com.example.charadasud.modelo

class Temporizador(
    var tiempoRestante: Int
) {
    fun reiniciar(tiempoInicial: Int) {
        tiempoRestante = tiempoInicial
    }

    fun disminuir() {
        if (tiempoRestante > 0) tiempoRestante--
    }

    fun estaFinalizado(): Boolean {
        return tiempoRestante <= 0
    }
}