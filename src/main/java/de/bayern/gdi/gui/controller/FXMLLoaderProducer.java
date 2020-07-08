package de.bayern.gdi.gui.controller;

import de.bayern.gdi.utils.I18n;
import javafx.fxml.FXMLLoader;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class FXMLLoaderProducer {

    @Inject
    Instance<Object> instance;

    @Produces
    public FXMLLoader createLoader() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setResources(I18n.getBundle());
        fxmlLoader.setLocation(getClass().getResource("/"));
        fxmlLoader.setControllerFactory(param -> instance.select(param).get());
        return fxmlLoader;
    }

}
