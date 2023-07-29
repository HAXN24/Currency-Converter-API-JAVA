package com.example.currencyconverterproject;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Duration;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Controller {

    @FXML
    private Button convertButton;
    @FXML
    private Tooltip toolTip;
    @FXML
    public Button copy;
    @FXML
    private Label output;
    @FXML
    private ComboBox<String> currencyBoxOne;
    @FXML
    private ComboBox<String> currencyBoxTwo;
    @FXML
    private TextField enterAmountField;



    // currencyOne, currencyTwo = holds currency tickers of from and to
    private String currencyOne;
    private String currencyTwo;
    ArrayList<String> currencyList;


    // apiKey - holds the api key for api use
    private String apiKey;

    @FXML
    private ImageView logo;


    public void initialize(){

        //retrieve api key
        getApiKey();

        //load Logo
        loadLogo();


        try{
            // retrieve and store list of currencies
           currencyList = loadCurrencyList();

            // store the list and populate them into its appropriate boxes
            ObservableList<String> options = FXCollections.observableArrayList(currencyList);
            currencyBoxOne.setItems(options);
            currencyBoxTwo.setItems(options);

        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    @FXML
    private void setCurrencyOne(){

        this.currencyOne = currencyBoxOne.getValue();
    }

    @FXML
    private void setCurrencyTwo(){
        this.currencyTwo = currencyBoxTwo.getValue();
    }



    public String getApiKey(){

        // used environment variable to hide apikey instead.
        apiKey =  System.getenv("currencyExchangeAPI");
        return apiKey;

//        //use bufferedReader to read api key from .txt file
//        try (BufferedReader reader = new BufferedReader(new FileReader(apiPath))){
//
//           apiKey =  reader.readLine();
//        }  catch (IOException e) {
//            e.printStackTrace();
//        }


    }

    // method to load and set Logo file
    private void loadLogo(){

        //  <a href="https://www.freepik.com/icon/coin_584034">Icon by Freepik</a> - Credit to artist of image
        String myIconPath = System.getenv("myJavaPath") + "\\resources\\CoinImage.png";
        logo.setImage(new Image(new File(myIconPath).getAbsolutePath()));
    }



    // LIMIT TO USD, CAD, PHP
    private ArrayList<String> loadCurrencyList() throws IOException{

        // retrieve currency list through api call
        // Adding a timeout to the API call to avoid waiting indefinitely for a response
        // Setting a read timeout to handle slow responses from the API to ensure and improve overall responsive of application
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS) // 10 seconds connection timeout
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS) // 10 seconds read timeout
                .build();

        // request.Builder used to create an HTTP request
        Request request = new Request.Builder()
                .url("https://api.apilayer.com/currency_data/list")
                .addHeader("apikey", apiKey)
                .method("GET", null)
            .build();
        Response response = client.newCall(request).execute();

        //Deserialize and convert from Json to Java Object
        Gson gson = new Gson();

        // asserting != null to proceed
        assert response.body() != null;

        JsonElement jsonElement = gson.fromJson(response.body().charStream(), JsonElement.class);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        ArrayList<String> allCurrencies = new ArrayList<>(jsonObject.getAsJsonObject("currencies").keySet());

        // Create a limited list of currencies to "PHP", "CAD", & "USD"
        ArrayList<String> allowedCurrencies = new ArrayList<>(List.of("USD", "PHP", "CAD"));

        // Filter the list of all available currencies based on the allowed currencies
        ArrayList<String> filteredCurrencies = new ArrayList<>();

        for (String currency : allCurrencies) {
            if (allowedCurrencies.contains(currency)) {
                filteredCurrencies.add(currency);
            }
        }

        return filteredCurrencies;
    }


    @FXML
    private void convertCurrency() throws IOException {

        String amountInput =  commaJoiner(enterAmountField.getText());

        if(amountInput.equals("")){
            output.setText("Please enter an amount");
        }

        double conversionRate = getConversionRate();

        // calculate conversion
        double conversionResult = Double.parseDouble(amountInput) * conversionRate;


        // Format the conversion result with commas
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        String formattedResult = numberFormat.format(conversionResult);



        // Apply fade-out animation to the logo
        FadeTransition fadeOut = new FadeTransition(Duration.millis(1000), logo);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            // Set the output label text and make it visible
            output.setText(formattedResult + " " + currencyTwo);
            output.setOpacity(1.0);
        });
        fadeOut.play();


        // Add event handler for "Copy Result" Button

        copy.setOnAction(event -> {
            // Copy the result to the clipboard
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(formattedResult);
            clipboard.setContent(content);

            // Show "Copied!" tooltip on the copy button
            this.toolTip =  new Tooltip("Copied!");
            toolTip.setAutoHide(true);

            // Convert local coordinates of copy button to screen coordinates - Move to the upper right side of the button - screenX
            double screenX = copy.localToScreen(50, 100).getX();
            double screenY = copy.localToScreen(0, 0).getY();

            toolTip.show(copy.getScene().getWindow(), screenX, screenY - 30); // Offset by 30 pixels to go above the button


            // Hide the tooltip after a delay of 1.5 seconds
            PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
            delay.setOnFinished(e -> toolTip.hide());
            delay.play();
        });


    }





    // If user inputs a comma on text - splits the comma and joins them as string with no space
    private String commaJoiner(String amount){
        return String.join("",amount.split(","));

    }

    private double getConversionRate() throws IOException{


        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS) // 10 seconds connection timeout
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS) // 10 seconds read timeout
                .build();

        // getting "From: currency"  from currencyOne and converting to "To: Currency" currencyTwo
        Request request = new Request.Builder()
                .url("https://api.apilayer.com/currency_data/live?source=" + this.currencyOne +"&currencies=" + this.currencyTwo)
                .addHeader("apikey", apiKey)
                .method("GET", null)
            .build();
        Response response = client.newCall(request).execute();

        //Deserialize from Json to Java object
        Gson gson = new Gson();

        // asserting != null to proceed
        assert response.body() != null;

        JsonElement jsonElement = gson.fromJson(response.body().charStream(), JsonElement.class);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        // return conversion rate - convert to Float

        String key = currencyOne + currencyTwo;
        return Double.parseDouble(jsonObject.getAsJsonObject("quotes").get(key).getAsString());

    }



}