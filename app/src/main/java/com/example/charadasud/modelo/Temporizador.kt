package com.example.charadasud.modelo

import android.os.CountDownTimer

/**
 * Temporizador que envuelve CountDownTimer de Android.
 * Construir con segundosTotales (ej. 60).
 * start(...) -> onTick recibe segundos restantes, onFinish cuando llega a 0.
 */
class Temporizador(private val segundosTotales: Int) {

    private var timer: CountDownTimer? = null

    fun start(tickMillis: Long = 1000L, onTick: (Int) -> Unit, onFinish: () -> Unit) {
        cancel()
        timer = object : CountDownTimer(segundosTotales * 1000L, tickMillis) {
            override fun onTick(millisUntilFinished: Long) {
                val segundos = (millisUntilFinished / 1000).toInt()
                onTick(segundos)
            }

            override fun onFinish() {
                onFinish()
            }
        }.start()
    }

    fun cancel() {
        timer?.cancel()
        timer = null
    }
}
