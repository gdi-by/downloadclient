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

import de.bayern.gdi.gui.Start;
import de.bayern.gdi.utils.Config;

import java.io.IOException;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sascha L. Teichmann (sascha.teichmann@intevation.de)
 */
public class App {

    private static final Logger log
        = Logger.getLogger(App.class.getName());

    private App() {
        // Not to be instantiated.
    }

    private static void usage(Options options, int exitCode) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(
            "java -jar downloader.jar",
            "", options,
            "Without options (except '--config')"
            + " the GUI application is started.",
            true);
        System.exit(exitCode);
    }

    private static void startGUI() {
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

    private static void initConfig(String dir) {
        try {
            Config.initialize(dir);
        } catch (NullPointerException | IOException ex) {
            // TODO: Remove the NPE above!
            log.log(Level.SEVERE,
                () -> "Loading config failed: " + ex.getMessage());
            System.exit(1);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Options options = new Options();

        Option help = Option.builder("?")
            .hasArg(false)
            .longOpt("help")
            .desc("Print this message and exit.")
            .build();

        Option headless = Option.builder("h")
            .hasArg(false)
            .longOpt("headless")
            .desc("Start command line tool.")
            .build();

        Option conf = Option.builder("c")
            .hasArg(true)
            .longOpt("config")
            .desc("Directory to overwrite default configuration.")
            .build();

        Option user = Option.builder("u")
            .hasArg(true)
            .longOpt("user")
            .desc("User name for protected services.")
            .build();

        Option password = Option.builder("p")
            .hasArg(true)
            .longOpt("password")
            .desc("Password for protected services.")
            .build();

        options.addOption(help);
        options.addOption(headless);
        options.addOption(conf);
        options.addOption(user);
        options.addOption(password);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("?")) {
                usage(options, 0);
            }

            initConfig(line.getOptionValue("c"));

            if (line.hasOption("h")) {
                System.exit(Headless.main(
                    line.getArgs(),
                    line.getOptionValue("u"),
                    line.getOptionValue("p")));
            }

            startGUI();

        } catch (ParseException pe) {
            log.log(Level.SEVERE, pe.getMessage());
            usage(options, 1);
        }
    }
}
