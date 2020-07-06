package de.bayern.gdi.gui;

import de.bayern.gdi.utils.DocumentResponseHandler;
import de.bayern.gdi.utils.FileResponseHandler;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Misc;
import de.bayern.gdi.utils.Unauthorized;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FxMain {

    private static final Logger LOG
        = LoggerFactory.getLogger(FxMain.class.getName());

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    private static final String LOGONAME = "icon_118x118_300dpi.jpg";

    @Inject
    private FXMLLoader fxmlLoader;

    public void start(Stage primaryStage, Application.Parameters parameters) {
        try (InputStream mainfxml = getClass().getResourceAsStream("/download-client.fxml")) {
            Parent root = fxmlLoader.load(mainfxml);
            Scene scene = new Scene(root, WIDTH, HEIGHT);
            DataBean dataBean = forceDataBean();
            Controller controller = fxmlLoader.getController();
            controller.setPrimaryStage(primaryStage);
            controller.setDataBean(dataBean);
            Unauthorized unauthorized = new WarningPopup();
            FileResponseHandler.setUnauthorized(unauthorized);
            DocumentResponseHandler.setUnauthorized(unauthorized);
            primaryStage.setTitle(I18n.getMsg("GDI-BY Download-Client"));
            Image image = new Image(Misc.getResource("img/" + LOGONAME));
            primaryStage.getIcons().add(image);
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent e) {
                    LOG.info(I18n.format("dlc.stop"));
                    Platform.exit();
                    System.exit(0);
                }
            });
        } catch (IOException ioe) {
            LOG.error("Could not find UI description file: "
                + ioe.getMessage(), ioe);
        }

    }

    private static DataBean forceDataBean() {
        try {
            return new DataBean();
        } catch (IOException ioe) {
            LOG.error(ioe.getMessage(), ioe);
            System.exit(1);
        }
        // Not reached.
        return null;
    }

}
