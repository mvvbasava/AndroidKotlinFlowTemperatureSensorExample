package com.example.androidkotlinflowtemperaturesensorexample

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random

// ViewModel or Repository class
class TemperatureRepository {
    // Create a Flow that emits temperature readings
    fun getTemperatureReadings(): Flow<Double> = flow {
        while (true) {
            // Simulate getting temperature from a sensor
            val temperature = 20.0 + Random.nextDouble(-5.0, 5.0)
            emit(temperature) // Emit the temperature value
            delay(1000) // Wait 1 second before next reading
        }
    }.flowOn(Dispatchers.IO) // Run on IO dispatcher

    // Flow with transformation
    fun getTemperatureStatus(): Flow<String> = flow {
        while (true) {
            val temperature = 20.0 + Random.nextDouble(-5.0, 5.0)
            emit(temperature)
            delay(1000)
        }
    }
        .map { temp ->
            // Transform temperature to status message
            when {
                temp < 18 -> "🥶 Cold: ${String.format("%.1f", temp)}°C"
                temp > 22 -> "🥵 Hot: ${String.format("%.1f", temp)}°C"
                else -> "😊 Comfortable: ${String.format("%.1f", temp)}°C"
            }
        }
        .flowOn(Dispatchers.IO)
}
