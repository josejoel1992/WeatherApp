package WeatherApp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherInformationApp extends Application {
    private static final String API_KEY = "75c642140e2ce67f18862941a6c4f0d8";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String GEO_URL = "http://api.openweathermap.org/geo/1.0/direct";
    private static final String ZIP_URL = "http://api.openweathermap.org/geo/1.0/zip";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Weather Information App");

        Label inputLabel = new Label("Enter City, ZIP code, or Coordinates (lat, long):");
        TextField inputField = new TextField();
        Button fetchButton = new Button("Fetch Weather");
        Label weatherOutput = new Label();

        fetchButton.setOnAction(event -> {
            String input = inputField.getText();
            if (!input.isEmpty()) {
                try {
                    double[] coordinates = getCoordinates(input);
                    if (coordinates != null) {
                        JSONObject weatherData = getWeatherData(coordinates[0], coordinates[1]);
                        weatherOutput.setText(weatherData.toJSONString());
                    } else {
                        weatherOutput.setText("Invalid input or location not found.");
                    }
                } catch (IOException | ParseException e) {
                    weatherOutput.setText("Error fetching weather data: " + e.getMessage());
                    showErrorDialog("Error fetching weather data", e.getMessage());
                    e.printStackTrace();
                }
            } else {
                weatherOutput.setText("Please enter a city name, ZIP code, or coordinates");
            }
        });

        VBox layout = new VBox(10, inputLabel, inputField, fetchButton, weatherOutput);
        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static JSONObject getWeatherData(double lat, double lon) throws IOException, ParseException {
        String urlString = BASE_URL + "?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY + "&units=metric";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(response.toString());
    }

    private static double[] getCoordinates(String input) throws IOException, ParseException {
        Pattern coordPattern = Pattern.compile("(-?\\d+\\.\\d+),\\s*(-?\\d+\\.\\d+)");
        Matcher coordMatcher = coordPattern.matcher(input);

        if (coordMatcher.matches()) {
            return new double[]{Double.parseDouble(coordMatcher.group(1)), Double.parseDouble(coordMatcher.group(2))};
        } else if (input.matches("\\d{5}")) {
            return getCoordinatesFromZip(input);
        } else {
            return getCoordinatesFromCity(input);
        }
    }

    private static double[] getCoordinatesFromZip(String zip) throws IOException, ParseException {
        String urlString = ZIP_URL + "?zip=" + zip + "&appid=" + API_KEY;
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONParser parser = new JSONParser();
        JSONObject jsonResponse = (JSONObject) parser.parse(response.toString());
        double lat = (double) jsonResponse.get("lat");
        double lon = (double) jsonResponse.get("lon");
        return new double[]{lat, lon};
    }

    private static double[] getCoordinatesFromCity(String city) throws IOException, ParseException {
        String urlString = GEO_URL + "?q=" + city.replace(" ", "%20") + "&limit=1&appid=" + API_KEY;
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONParser parser = new JSONParser();
        JSONArray jsonResponse = (JSONArray) parser.parse(response.toString());

        if (jsonResponse.isEmpty()) {
            return null;
        }

        JSONObject location = (JSONObject) jsonResponse.get(0);
        double lat = (double) location.get("lat");
        double lon = (double) location.get("lon");
        return new double[]{lat, lon};
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}