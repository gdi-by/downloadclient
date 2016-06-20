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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.bayern.gdi.utils.FileTracker;
import de.bayern.gdi.utils.I18n;
import de.bayern.gdi.utils.Misc;

/**
 * Starts an external process with optional arguments and
 * an optional working directory.
 */
public class ExternalProcessJob implements Job {

    /** An argument for an external call. */
    public static class Arg {

        /** The argmuent. */
        protected String arg;
        public Arg(String arg) {
            this.arg = arg;
        }

        /**
         * Expands the argument.
         * @param fileTracker The fileTracker used to expand the argument.
         * @param tmpNames A set of temporary names.
         * @return The expanded argument.
         */
        public String[] getArgs(FileTracker fileTracker, Set<File> tmpNames) {
            return new String[] {this.arg};
        }
    }

    /** Generate a unique file name with an extension. */

    public static class UniqueArg extends Arg {
        public UniqueArg(String arg) {
            super(arg);
        }

        @Override
        public String[] getArgs(FileTracker fileTracker, Set<File> tmpNames) {
            if (fileTracker == null) {
                return super.getArgs(fileTracker, tmpNames);
            }
            File file = Misc.uniqueFile(
                fileTracker.getDirectory(), "download-", this.arg, tmpNames);

            if (file == null) {
                throw new IllegalArgumentException("No unique name available.");
            }
            tmpNames.add(file);

            return new String[] {file.getName()};
        }
    }

    /** A global globbing argument for an external call. */
    public static class GlobalGlob extends Arg {
        public GlobalGlob(String arg) {
            super(arg);
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
                ? toString(fileTracker.globalGlob(arg))
                : super.getArgs(fileTracker, tmpNames);
        }
    }

    /** A delta globbing argument for an external call. */
    public static class DeltaGlob extends GlobalGlob {
        public DeltaGlob(String arg) {
            super(arg);
        }

        @Override
        public String[] getArgs(FileTracker fileTracker, Set<File> tmpNames) {
            return fileTracker != null
                ? toString(fileTracker.deltaGlob(arg))
                : super.getArgs(fileTracker, tmpNames);
        }
    }

    private String command;
    private Arg[] arguments;

    private FileTracker fileTracker;

    public ExternalProcessJob() {
    }

    public ExternalProcessJob(
        String      command,
        FileTracker fileTracker,
        Arg[]       arguments
    ) {
        this.command     = command;
        this.arguments   = arguments;
        this.fileTracker = fileTracker;
    }

    private List<String> commandList() {
        int n = this.arguments != null
            ? this.arguments.length
            : 0;
        List<String> list = new ArrayList<String>(n + 1);
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
                // TODO: i18n
                throw new JobExecutionException(
                    "Scanning dir '"
                    + this.fileTracker.getDirectory() + "' failed.");
            }
        }

        ProcessBuilder builder = new ProcessBuilder(commandList());
        if (this.fileTracker != null
        && this.fileTracker.getDirectory() != null) {
            builder.directory(this.fileTracker.getDirectory());
        }

        if (p != null) {
            p.broadcastMessage(
                I18n.format("external.process.start", command));
        }

        try {
            Process process = builder.start();
            // XXX: Implement some kind of cancellation mechanism.
            int exitcode = process.waitFor();
            if (exitcode != 0) {
                throw new JobExecutionException(
                    I18n.format("external.process.error", command, exitcode));
            }
        } catch (IOException | InterruptedException e) {
            throw new JobExecutionException(
                I18n.format("external.process.failed", command), e);
        }

        if (p != null) {
            p.broadcastMessage(
                I18n.format("external.process.end", command));
        }
    }
}
