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
package de.bayern.gdi;

import java.io.IOException;

import de.bayern.gdi.gui.Start;
import de.bayern.gdi.utils.Config;
import de.bayern.gdi.utils.StringUtils;

/**
 * @author Sascha L. Teichmann (sascha.teichmann@intevation.de)
 */
public class App {

    private static final String[] HEADLESS = {
        "-h",
        "--headless",
        "-headless"
    };

    private static final String[] CONFIG = {
        "-c=",
        "--config=",
        "-config="
    };

    private static final String[] USER = {
        "-u=",
        "--user=",
        "-user="
    };

    private static final String[] PASSWORD = {
        "-p=",
        "--password=",
        "-password="
    };

    private static final String[] HELP = {
        "-?",
        "--help",
        "-help"
    };

    private App() {
    }

    private static boolean runHeadless(String[] args) {
        return StringUtils.contains(args, HEADLESS);
    }

    private static boolean help(String[] args) {
        return StringUtils.contains(args, HELP);
    }

    private static String useConfig(String[] args) {
        return StringUtils.extractPostfix(args, CONFIG);
    }

    private static String user(String[] args) {
        return StringUtils.extractPostfix(args, USER);
    }

    private static String password(String[] args) {
        return StringUtils.extractPostfix(args, USER);
    }

    private static void helpAndExit() {
        System.out.println("java -jar downloader.jar [options]");
        System.out.println("with options:");
        System.out.println(
            "  -?|--help|-help: Print this message and exit.");
        System.out.println(
            "  -h|--headless|-headless: Start command line tool.");
        System.out.println(
              "  -c=<dir>|--config=<dir>|-config=<dir>:"
            + " Directory to overwrite default configuration.");
        System.out.println(
              "  -u=<user>|--user=<user>|-user=<user>:"
            + " User name for protected services.");
        System.out.println(
              "  -p=<password>|--password=<password>|-password=<password>:"
            + " Password for protected services.");
        System.out.println("without options (except '--config'):");
        System.out.println("  Start as GUI application.");
        System.exit(0);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (help(args)) {
            helpAndExit();
        }

        String config = useConfig(args);
        if (config != null) {
            try {
                Config.load(config);
            } catch (IOException ioe) {
                System.err.println(
                    "Loading config failed: " + ioe.getMessage());
                System.exit(1);
            }
        } else {
            Config.uninitialized();
        }

        if (runHeadless(args)) {
            System.exit(Headless.main(args, user(args), password(args)));
        }

        // Its kind of complicated to start a javafx application from
        // another class. See http://stackoverflow.com/a/25909862
        new Thread() {
            @Override
            public void run() {
                javafx.application.Application.launch(Start.class);
            }
        }.start();
        Start.waitForStart();
    }
}
