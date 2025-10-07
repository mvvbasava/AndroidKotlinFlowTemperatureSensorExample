package com.example.androidkotlinflowtemperaturesensorexample

import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.androidkotlinflowtemperaturesensorexample.databinding.ActivityMainBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

// In your Activity or Fragment
class MainActivity : AppCompatActivity() {
    private val repository = TemperatureRepository()
    private lateinit var binding: ActivityMainBinding
    private var selectedOption: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedOption = getString(R.string.optionOne)
        binding.radioGroupOptions.setOnCheckedChangeListener {
                group, checkedId ->
            val radioButton = findViewById<RadioButton>(checkedId)
            selectedOption = radioButton.text.toString()
            Toast.makeText(this, "Selected: $selectedOption", Toast.LENGTH_SHORT).show()
        }
        binding.buttonSendRequest.setOnClickListener {
            // Toast.makeText(this, "Button Clicked", Toast.LENGTH_SHORT).show()
            Toast.makeText(this, selectedOption, Toast.LENGTH_LONG).show()
            if (selectedOption.isNotEmpty()) {
                sendDummyNetworkRequest(selectedOption)
            } else {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendDummyNetworkRequest(option: String) {
        var consumer1Job: Job? = null
        var consumer2Job: Job? = null
        var consumer3Job: Job? = null
        var consumer4JobProducer: Job? = null
        var consumer4JobConsumer1: Job? = null
        var consumer4JobConsumer2: Job? = null

        when (option) {
            /**
             * Example 1: Basic collection
             */
            getString(R.string.optionOne) -> {
                // Code for Option 1
                var i = 1
                // Example 1: Basic collection
                consumer1Job = lifecycleScope.launch {
                    binding.myScrollableTextView.text = ""
                    repository.getTemperatureReadings()
                        .take(5) // Only take 5 readings
                        .collect { temperature ->
                            val txt = buildString {
                                append("Temperature: ${i++}  - ")
                                append("%.2f".format(Locale.ENGLISH, temperature))
                                append("°C")
                                append("\n")
                                append("------------------------------")
                                append("\n")
                            }
                            println(txt)
                            binding.myScrollableTextView.append(txt)
                            /** Instead of Below */
                            /** println("Temperature: ${i++}  - ${String.format("%.2f", temperature)}°C") */
                            // Update UI here
                            updateTemperatureDisplay(temperature)
                        }
                }
            }
            /**
             * Example 2: With operators
             */
            getString(R.string.optionTwo) -> {
                // Code for Option 2
                // Example 2: With operators
                consumer2Job  = lifecycleScope.launch {
                    binding.myScrollableTextView.text = ""
                    repository.getTemperatureStatus()
                        .filter { it.contains("Hot") } // Only show hot temperatures
                        .take(3) // Stop after 3 hot readings
                        .collect { status ->
                            println("Alert: $status")
                            println("------------------------------")
                            binding.myScrollableTextView.append(buildString {
                                    append("Alert: $status")
                                    append("\n")
                                    append("------------------------------")
                                    append("\n")
                                }
                            )
                            showAlert(status)
                        }
                }
            }
            /**
             * Example 3: Combining flows
             */
            getString(R.string.optionThree) -> {
                consumer4JobProducer?.cancel()
                consumer4JobConsumer1?.cancel()
                consumer4JobConsumer2?.cancel()
                // Code for Option 3
                // Example 3: Combining flows
                consumer3Job = lifecycleScope.launch {
                    binding.myScrollableTextView.text = ""
                    val humidityFlow = getHumidityReadings() // Another flow
                    repository.getTemperatureReadings()
                        .combine(humidityFlow) { temp, humidity ->
                            buildString {
                                append("Temp: ")
                                append("%.1f".format(Locale.ENGLISH, temp))
                                append(" - ")
                                append("Humidity: ")
                                append(humidity.toString())
                            }
                            /** Instead of Below */
                            /**
                            getString(
                                R.string.temp_c_humidity,
                                String.format("%.1f", temp),
                                humidity
                            )
                            */
                        }
                        .collect { combined ->
                            println(combined)
                            println("------------------------------")
                            binding.myScrollableTextView.append(
                                buildString {
                                    append(combined)
                                    append("\n")
                                    append("------------------------------")
                                    append("\n")
                                }
                            )
                            updateWeatherDisplay(combined)
                        }
                        /*
                    repository.getTemperatureReadings()
                        .combine(humidityFlow) { temp, humidity ->
                            "Temp: ${String.format("%.1f", temp)}°C, Humidity: ${humidity}%"
                        }
                        .collect { combined ->
                            println(combined)
                            updateWeatherDisplay(combined)
                        }
                        */
                }
            }
            /**
             * Example 4: StateFlow (Hot Flow)
             */
            getString(R.string.optionFour) -> {
                // Example 4: StateFlow (Hot Flow)
                binding.myScrollableTextView.text = ""
                val temperatureStateFlow = MutableStateFlow(20.0)

                // Producer
                consumer4JobProducer = lifecycleScope.launch {
                    repository.getTemperatureReadings()
                        .collect { temp ->
                            temperatureStateFlow.value = temp
                        }
                }

                // Multiple consumers can observe the same StateFlow
                consumer4JobConsumer1 = lifecycleScope.launch {
                    temperatureStateFlow.collect { temp ->
                        val formattedTemp = "%.1f".format(Locale.ENGLISH, temp)
                        println("Consumer 1: $formattedTemp \n")
                        binding.myScrollableTextView.append(
                            buildString {
                                append("Consumer 1: ")
                                append("%.1f".format(Locale.ENGLISH, temp))
                                append("\n")
                            }
                        )
                    }
                }

                consumer4JobConsumer2 = lifecycleScope.launch {
                    temperatureStateFlow.collect { temp ->
                        val formattedTemp = "%.1f".format(Locale.ENGLISH, temp)
                        println("Consumer 2: $formattedTemp \n")
                        println("------------------------------")
                        binding.myScrollableTextView.append(
                            buildString {
                                append("Consumer 2: ")
                                append("%.1f".format(Locale.ENGLISH, temp))
                                append("\n")
                                append("------------------------------")
                                append("\n")
                            }
                        )
                    }
                }
            }
            else -> {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Helper functions (would update actual UI)
    private fun updateTemperatureDisplay(temp: Double) {
        // textView.text = "Temperature: ${String.format("%.2f", temp)}°C"
    }

    private fun showAlert(status: String) {
        // Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
    }

    private fun updateWeatherDisplay(info: String) {
        // weatherTextView.text = info
    }

    private fun getHumidityReadings(): Flow<Int> = flow {
        while (true) {
            emit(Random.nextInt(40, 80))
            delay(1000)
        }
    }
}
