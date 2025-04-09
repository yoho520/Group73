module com.example.software_group73 {
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
    requires org.json;

    opens com.example.software_group73 to javafx.fxml;
    exports com.example.software_group73;
    // 添加这些导出声明
    exports com.stockanalyzer;
    exports com.stockanalyzer.util;

    // 如果你需要使用FXML，可能还需要这样的开放声明
    opens com.stockanalyzer to javafx.fxml;
}