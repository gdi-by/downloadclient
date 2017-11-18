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
package de.bayern.gdi.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Common string operations.
 */
public class StringUtils {
    private static final Logger log =
            Logger.getLogger(StringUtils.class.getName());
    private static final int TEN = 10;

    private StringUtils() {
    }

    /**
     * URL-encodes a given string in UTF-8.
     *
     * @param s The string to encode.
     * @return The encodes string.
     */
    public static String urlEncode(String s) {
        return urlEncode(s, "UTF-8");
    }

    /**
     * URL-encodes a given string in a given encoding.
     *
     * @param s   The string to encode.
     * @param enc The encoding to use.
     * @return The encodes string.
     */
    private static String urlEncode(String s, String enc) {
        try {
            return URLEncoder.encode(s, enc);
        } catch (UnsupportedEncodingException e) {
            log.log(Level.SEVERE, "encoding problem", e);
        }
        return s;
    }

    /**
     * Searches a needle of a set of needles in a haystack.
     *
     * @param haystack The haystack.
     * @param needles  The needles.
     * @return true if the haystack contains one of the needles.
     * false otherwise.
     */
    public static boolean contains(String[] haystack, String[] needles) {
        return Stream.of(needles)
                .anyMatch(within(haystack));
    }

    private static Predicate<String> within(String[] haystack) {
        return needle-> Stream.of(haystack)
                .anyMatch(needle::equals);
    }

    /**
     * Split a command line.
     *
     * @param toProcess the command line to process.
     * @return the command line broken into strings.
     * An empty or null toProcess parameter results in a zero sized array.
     * @throws IllegalArgumentException Thrown if quotes are unbalanced.
     */
    public static String[] splitCommandLine(String toProcess)
            throws IllegalArgumentException {

        if (toProcess == null || toProcess.length() == 0) {
            //no command? no string
            return new String[0];
        }
        // parse with a simple finite state machine

        State state = State.NORMAL;
        StringTokenizer tok = new StringTokenizer(toProcess, "\"\' ", true);
        ArrayList<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean lastTokenHasBeenQuoted = false;

        while (tok.hasMoreTokens()) {
            String nextTok = tok.nextToken();
            switch (state) {
                case IN_QUOTE:
                    if ("\'".equals(nextTok)) {
                        lastTokenHasBeenQuoted = true;
                        state = State.NORMAL;
                    } else {
                        current.append(nextTok);
                    }
                    break;
                case IN_DOUBLE_QUOTE:
                    if ("\"".equals(nextTok)) {
                        lastTokenHasBeenQuoted = true;
                        state = State.NORMAL;
                    } else {
                        current.append(nextTok);
                    }
                    break;
                default:
                    if ("\'".equals(nextTok)) {
                        state = State.IN_QUOTE;
                    } else if ("\"".equals(nextTok)) {
                        state = State.IN_DOUBLE_QUOTE;
                    } else if (" ".equals(nextTok)) {
                        if (lastTokenHasBeenQuoted || current.length() != 0) {
                            result.add(current.toString());
                            current.setLength(0);
                        }
                    } else {
                        current.append(nextTok);
                    }
                    lastTokenHasBeenQuoted = false;
                    break;
            }
        }
        if (lastTokenHasBeenQuoted || current.length() != 0) {
            result.add(current.toString());
        }
        if (state == State.IN_QUOTE || state == State.IN_DOUBLE_QUOTE) {
            throw new IllegalArgumentException(
                    "unbalanced quotes in " + toProcess);
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Splits a string by a pattern.
     *
     * @param s     The string to split.
     * @param delim The delimeter.
     * @param keep  Indicates if the delimeters should be kept.
     * @return The splitted string.
     */
    public static String[] split(String s, Pattern delim, boolean keep) {
        if (s == null) {
            s = "";
        }
        int lastMatch = 0;
        ArrayList<String> parts = new ArrayList<>();
        Matcher m = delim.matcher(s);
        while (m.find()) {
            String x = s.substring(lastMatch, m.start());
            if (!x.isEmpty()) {
                parts.add(x);
            }
            if (keep) {
                parts.add(m.group(0));
            }
            lastMatch = m.end();
        }
        String x = s.substring(lastMatch);
        if (!x.isEmpty()) {
            parts.add(x);
        }
        return parts.toArray(new String[parts.size()]);
    }

    /**
     * Joins a list of objects with a separator to a string.
     *
     * @param s   The list of objects.
     * @param sep The separator.
     * @return the joined string.
     */
    public static String join(List<?> s, String sep) {
        return s.stream()
                .map(Object::toString)
                .collect(Collectors.joining(sep));
    }

    /**
     * How many digit place does a number need?
     *
     * @param n The number.
     * @return the number of digits.
     */
    public static int places(int n) {
        int places = 1;
        for (int value = TEN; n > value; value *= TEN) {
            places++;
        }
        return places;
    }

    /**
     * Convert a whitespace separated list of double strings
     * to a double array.
     *
     * @param s the string with the double values.
     * @return the converted array. Returns an empty array
     * if the conversion fails.
     */
    public static double[] toDouble(String s) {
        try {
            String[] parts = s.split("[ \\t]+");
            double[] x = new double[parts.length];
            for (int i = 0; i < parts.length; i++) {
                x[i] = Double.parseDouble(parts[i]);
            }
            return x;
        } catch (NumberFormatException nfe) {
            return new double[0];
        }
    }

    /**
     * Splits a given string by a separator and ignores parts
     * that starts with a given prefixes (case insensitve).
     * Afterwards the parts are re-joined with the separator.
     *
     * @param s        The string.
     * @param sep      The separator.
     * @param prefixes The prefixes.
     * @return The treated string.
     */
    public static String ignorePartsWithPrefix(
            String s, String sep, String[] prefixes) {

        List<String> lcPrefixes = Stream.of(prefixes)
            .map(String::toLowerCase)
            .collect(Collectors.toList());

        List<String> use = Stream.of(s.split(Pattern.quote(sep)))
            .filter(str -> lcPrefixes.stream()
                .noneMatch(p -> str.toLowerCase().startsWith(p)))
            .collect(Collectors.toList());

        return join(use, sep);
    }

    /**
     * Checks if two strings are equal or both null.
     *
     * @param a The first string.
     * @param b The second strinh.
     * @return true if both strings are equal or both null.
     */
    public static boolean nullOrEquals(String a, String b) {
        return a == null && b == null || a != null && b != null && a.equals(b);
    }

    /**
     * States for the command line splitting.
     */
    private enum State {
        NORMAL,
        IN_QUOTE,
        IN_DOUBLE_QUOTE
    }
}
