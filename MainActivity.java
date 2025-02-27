package com.example.weatherapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText cityInput;
    private Button fetchButton;
    private TextView weatherResult;
    private static final String API_KEY = "YOUR_API_KEY"; // Replace with your API Key
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityInput = findViewById(R.id.cityInput);
        fetchButton = findViewById(R.id.fetchButton);
        weatherResult = findViewById(R.id.weatherResult);

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityInput.getText().toString().trim();
                if (!city.isEmpty()) {
                    fetchWeather(city);
                } else {
                    weatherResult.setText("Please enter a city name.");
                }
            }
        });
    }

    private void fetchWeather(String cityName) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String urlString = String.format(API_URL, cityName, API_KEY);
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    // Update UI on main thread
                    runOnUiThread(() -> parseWeatherData(result.toString()));
                } else {
                    runOnUiThread(() -> weatherResult.setText("City not found or API error."));
                }
            } catch (Exception e) {
                Log.e("WeatherApp", "Error fetching weather data", e);
                runOnUiThread(() -> weatherResult.setText("Error fetching data."));
            }
        });
    }

    private void parseWeatherData(String result) {
        try {
            JSONObject json = new JSONObject(result);
            JSONObject main = json.getJSONObject("main");
            double temp = main.getDouble("temp");
            weatherResult.setText("Temperature: " + temp + "°C");
        } catch (Exception e) {
            Log.e("WeatherApp", "Error parsing JSON", e);
            weatherResult.setText("Error parsing data.");
        }
    }
}