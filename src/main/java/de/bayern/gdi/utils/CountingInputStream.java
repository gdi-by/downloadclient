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

import de.bayern.gdi.processor.listener.CountListener;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** CountingInputStream counts the number of bytes piped
 *  through the parent stream.
 */
public class CountingInputStream extends FilterInputStream {

    /** Bytes per kB. */
    public static final int BYTES_PER_KB = 1000;

    private long counter;

    private List<CountListener> listener = new ArrayList<>();

    public CountingInputStream(InputStream in) {
        super(in);
    }

    /**
     * Adds a CountListener.
     * @param listenerToAdd listener to add, never <code>null</code>
     */
    public void addListener(CountListener listenerToAdd) {
        this.listener.add(listenerToAdd);
    }


    /** Resets the internal counter back to zero. */
    public void resetCounter() {
        this.counter = 0;
    }

    /** Returns the number of bytes copied so far.
     *  @return The number of bytes.
     */
    public long getCounter() {
        return this.counter;
    }

    private void reportCount() {
        if (this.listener != null) {
            if (this.counter % BYTES_PER_KB == 0) {
                this.listener.forEach(l -> l.bytesCounted(this.counter));
            }
        }
    }

    @Override
    public int read() throws IOException {
        int x = in.read();
        if (x >= 0) {
            this.counter++;
            reportCount();
        }
        return x;
    }

    @Override
    public int read(byte[] n, int off, int len) throws IOException {
        int x = in.read(n, off, len);
        if (x > 0) {
            this.counter += x;
            reportCount();
        }
        return x;
    }

    /** Instance to wrap an inputstream with a reporting counter.
     * @param listeners The listeners to report to. Can be null.
     * @return The factory to create a wrapper.
     */
    public static WrapInputStreamFactory createWrapFactory(
        final List<CountListener> listeners) {
        return in -> {
            CountingInputStream countingInputStream = new CountingInputStream(in);
            if (listeners != null) {
                listeners.forEach(l -> countingInputStream.addListener(l));
            }
            return countingInputStream;
        };
    }
}
