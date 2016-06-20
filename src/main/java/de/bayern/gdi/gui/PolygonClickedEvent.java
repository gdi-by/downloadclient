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


package de.bayern.gdi.gui;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

public class PolygonClickedEvent extends Event {

    /**
     * the event type.
     */
    public static final EventType<PolygonClickedEvent> NOTIFY =
            new EventType(EventType.ROOT, "NOTIFY");

    /**
     * Constructor.
     */
    public PolygonClickedEvent() {
        this(NOTIFY);
    }

    /**
     * Constructor.
     * @param arg0 the event source
     */
    public PolygonClickedEvent(EventType<? extends Event> arg0) {
        super(arg0);
    }

    /**
     * Constructor.
     * @param arg0 the event source
     * @param arg1 the event target
     * @param arg2 the event type
     */
    public PolygonClickedEvent(Object arg0, EventTarget arg1,
                               EventType<? extends Event> arg2) {
        super(arg0, arg1, arg2);
    }
}
