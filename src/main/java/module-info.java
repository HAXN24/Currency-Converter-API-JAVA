module com.example.currencyconverterproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires okhttp3;
    requires com.google.gson;


    opens com.example.currencyconverterproject to javafx.fxml;
    exports com.example.currencyconverterproject;
}