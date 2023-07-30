module project.controller.files {
    requires javafx.controls;
    requires javafx.fxml;
    requires okhttp3;
    requires com.google.gson;


    opens project.controller.files to javafx.fxml;
    exports  project.controller.files;

}