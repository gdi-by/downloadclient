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

import de.bayern.gdi.config.Credentials;
import de.bayern.gdi.gui.Start;
import de.bayern.gdi.config.Config;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sascha L. Teichmann (sascha.teichmann@intevation.de)
 */
public class App {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    private App() {
        // Not to be instantiated.
    }

    private static void usage(Options options, int exitCode) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(
            "java -jar downloadclient.jar",
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
        } catch (IOException ex) {
            System.err.println("Loading config failed: " + ex.getMessage());
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

            if (line.hasOption("h")) {
                // First initialize log4j for headless execution
                final String pid = getProcessId("0");
                System.setProperty("logfilename", "logdlc-" + pid + ".txt");
            }

            // use configuration for gui and headless mode
            initConfig(line.getOptionValue("c"));

            if (line.hasOption("h")) {
                System.exit(Headless.runHeadless(
                    line.getArgs(),
                    createCredentials(line.getOptionValue("u"), line.getOptionValue("p"))));
            }

            startGUI();

        } catch (ParseException pe) {
            System.err.println("Cannot parse input: " + pe.getMessage());
            usage(options, 1);
        }
    }

    private static Credentials createCredentials(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            LOG.info("Username and Password are not passed");
            return null;
        }
        return new Credentials(username, password);
    }

    /**
     * As of Java 9 the method <code>ProcessHandle.current().pid()</code>
     * can be used.
     * This is a workaround for Java 8 which should work for Oracle and
     * OpenJDK.
     *
     * @param fallback the given id returned in case PID can not determined
     * @return the PID (process identifier) for the JVM running this class
     */
    private static String getProcessId(final String fallback) {
        // Note: may fail in some JVM implementations
        // therefore fallback has to be provided

        // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');

        if (index < 1) {
            // part before '@' empty (index = 0) / '@' not found (index = -1)
            return fallback;
        }

        try {
            return Long.toString(Long.parseLong(jvmName.substring(0, index)));
        } catch (NumberFormatException e) {
            // ignore
        }
        return fallback;
    }
}
