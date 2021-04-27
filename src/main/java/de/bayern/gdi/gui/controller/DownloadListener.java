/*
 * DownloadClient Geodateninfrastruktur Bayern
 *
 * (c) 2016 GSt. GDI-BY (gdi.bayern.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.bayern.gdi.gui.controller;

import de.bayern.gdi.processor.ProcessorEvent;
import de.bayern.gdi.processor.ProcessorListener;
import de.bayern.gdi.utils.I18n;
import javafx.application.Platform;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Keeps track of download progression and errors.
 */
@Named
@Singleton
public class DownloadListener implements ProcessorListener {

    @Inject
    private StatusLogController statusLogController;

    @Override
    public void processingFailed(ProcessorEvent pe) {
        Platform.runLater(
            () -> statusLogController.setStatusTextUI(
                I18n.format(
                    "status.error",
                    pe.getException().getMessage())));
    }

    @Override
    public void receivedMessage(ProcessorEvent pe) {
        Platform.runLater(
            () -> statusLogController.setStatusTextUI(pe.getMessage())
        );
    }

    @Override
    public void processingFinished() {
    }
}
