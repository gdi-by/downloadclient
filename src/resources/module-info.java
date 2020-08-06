module dlcfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.geotools.geometry;
    requires com.sothawo.mapjfx;

    opens de.bayern.gdi to javafx.fxml;
    exports de.bayern.gdi;
}
