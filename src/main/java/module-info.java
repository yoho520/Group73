module com.example.software {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires static lombok;
    requires java.persistence;


    opens com.example.software to javafx.fxml;
    exports com.example.software;
    // 添加这些导出声明
    exports com.example.software.financeapp.application;
    exports com.example.software.financeapp.controller;

    // 如果使用FXML，还需要这些opens声明
    opens com.example.software.financeapp.application to javafx.fxml, javafx.graphics;
    opens com.example.software.financeapp.controller to javafx.fxml;
    // 在module-info.java中添加
    exports com.example.software.financeapp.model.entity;
}