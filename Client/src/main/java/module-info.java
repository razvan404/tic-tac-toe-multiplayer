module application.gui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens application.gui to javafx.fxml;
    exports application.gui;
    exports application.controller;
    exports application.models;
    exports application.service;
    opens application.controller to javafx.fxml;
}