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

package de.bayern.gdi.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.bayern.gdi.model.ProxyConfiguration;
import de.bayern.gdi.utils.Config;
import de.bayern.gdi.utils.FileTracker;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Log;
import de.bayern.gdi.utils.Misc;
import de.bayern.gdi.utils.StringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starts an external process with optional arguments and
 * an optional working directory.
 */
public class ExternalProcessJob implements Job {

    private static final Logger log
        = Logger.getLogger(ExternalProcessJob.class.getName());

    /** An argument for an external call. */
    public static class Arg {

        /** The argument. */
        protected String argument;
        public Arg(String argument) {
            this.argument = argument;
        }

        /**
         * Expands the argument.
         * @param fileTracker The fileTracker used to expand the argument.
         * @param tmpNames A set of temporary names.
         * @return The expanded argument.
         */
        public String[] getArgs(FileTracker fileTracker, Set<File> tmpNames) {
            return new String[] {this.argument};
        }
    }

    /** Generate a unique file name with an extension. */
    public static class UniqueArg extends Arg {
        public UniqueArg(String argument) {
            super(argument);
        }

        @Override
        public String[] getArgs(FileTracker fileTracker, Set<File> tmpNames) {
            if (fileTracker == null) {
                return super.getArgs(fileTracker, tmpNames);
            }
            File file = Misc.uniqueFile(fileTracker.getDirectory(),
                "download-", this.argument, tmpNames);

            if (file == null) {
                throw new IllegalArgumentException("No unique name available.");
            }
            tmpNames.add(file);

            return new String[] {file.getName()};
        }
    }

    /** A global globbing argument for an external call. */
    public static class GlobalGlob extends Arg {
        public GlobalGlob(String argument) {
            super(argument);
        }

        /**
         * Converts a list of files to an array of file names.
         * @param files The list of files.
         * @return The array of file names.
         */
        protected static String[] toString(List<File> files) {
            String[] args = new String[files.size()];
            for (int i = 0; i < args.length; i++) {
                args[i] = files.get(i).getName();
            }
            return args;
        }

        @Override
        public String[] getArgs(FileTracker fileTracker, Set<File> tmpNames) {
            return fileTracker != null
                ? toString(fileTracker.globalGlob(this.argument))
                : super.getArgs(fileTracker, tmpNames);
        }
    }

    /** A delta globbing argument for an external call. */
    public static class DeltaGlob extends GlobalGlob {
        public DeltaGlob(String argument) {
            super(argument);
        }

        @Override
        public String[] getArgs(FileTracker fileTracker, Set<File> tmpNames) {
            return fileTracker != null
                ? toString(fileTracker.deltaGlob(this.argument))
                : super.getArgs(fileTracker, tmpNames);
        }
    }

    private String command;
    private Arg[] arguments;

    private FileTracker fileTracker;

    private Log logger;

    public ExternalProcessJob() {
    }

    public ExternalProcessJob(
        String      command,
        FileTracker fileTracker,
        Arg[]       arguments,
        Log         logger
    ) {
        this.command     = command;
        this.arguments   = arguments;
        this.fileTracker = fileTracker;
        this.logger      = logger;
    }

    /**
     * Returns the list of the command and the arguments to be executed.
     * @return the command and its arguments.
     */
    public List<String> commandList() {
        int n = this.arguments != null
            ? this.arguments.length
            : 0;
        List<String> list = new ArrayList<>(n + 1);
        list.add(command);
        if (n > 0) {
            Set<File> tmpNames = new HashSet<>();
            for (Arg argument: arguments) {
                String[] args = argument.getArgs(this.fileTracker, tmpNames);
                for (String arg: args) {
                    list.add(arg);
                }
            }
        }
        return list;
    }

    private void logExtra(String msg) {
        if (this.logger != null) {
            this.logger.log(msg);
        }
    }

    private void broadcastMessage(Processor p, String msg) {
        logExtra(msg);
        if (p != null) {
            p.broadcastMessage(msg);
        }
    }

    private void broadcastException(Processor p, JobExecutionException jee) {
        logExtra(jee.getMessage());
        if (p != null) {
            p.broadcastException(jee);
        }
    }

    /**
     * Runs the external process.
     * @throws JobExecutionException Thrown
     *         if the external proccess could not be started.
     */
    @Override
    public void run(Processor p) throws JobExecutionException {

        if (this.fileTracker != null) {
            this.fileTracker.push();
            if (!this.fileTracker.scan()) {
                String msg = I18n.format(
                    "external.process.scan.dir.failed",
                    this.fileTracker.getDirectory());
                JobExecutionException jee = new JobExecutionException(msg);
                broadcastException(p, jee);
                throw jee;
            }
        }
        List<String> cmd = commandList();
        ProcessBuilder builder = new ProcessBuilder(cmd);
        if (Config.getInstance().getProxyConfig() != null) {
            ProxyConfiguration pConf = Config.getInstance().getProxyConfig();
            Map<String, String> env = builder.environment();

            env.put("http_proxy", pConf.getHttpProxyString());
            env.put("https_proxy", pConf.getHttpsProxyString());
            env.put("no_proxy", pConf.getHttpNonProxyHosts());
        }

        if (this.fileTracker != null
        && this.fileTracker.getDirectory() != null) {
            builder.directory(this.fileTracker.getDirectory());
        }

        builder.redirectErrorStream(true);

        broadcastMessage(p, I18n.format("external.process.start",
            StringUtils.join(cmd, " ")));

        try {
            Process process = builder.start();
            // Copy output of sub-process to log.
            final BufferedReader in =
                new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        String line;
                        while ((line = in.readLine()) != null) {
                            logExtra(line);
                        }
                    } catch (IOException ioe) {
                        logExtra(ioe.getMessage());
                    } finally {
                        closeGraceful();
                    }
                }

                private void closeGraceful() {
                    try {
                        in.close();
                    } catch (IOException ioe) {
                        log.log(Level.INFO, ioe.getMessage(), ioe);
                    }
                }
            };
            t.setDaemon(true);
            t.start();

            // XXX: Implement some kind of cancellation mechanism.
            int exitcode = process.waitFor();
            if (exitcode != 0) {
                JobExecutionException jee = new JobExecutionException(
                    I18n.format("external.process.error", command, exitcode));
                broadcastException(p, jee);
                throw jee;
            }
        } catch (IOException | InterruptedException e) {
            JobExecutionException jee = new JobExecutionException(
                I18n.format("external.process.failed", command), e);
            broadcastException(p, jee);
            throw jee;
        }

        broadcastMessage(p,
            I18n.format("external.process.end", command));
    }
}
