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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/** CountingInputStream counts the number of bytes piped
 *  through the parent stream.
 */
public class CountingInputStream extends FilterInputStream {

    /** An interface to report the number of bytes counted. */
    public interface CountListener {
        /** bytesCounted is called to report the number of bytes already copied.
         *  @param counter The number of bytes.
         */
        void bytesCounted(long counter);
    }

    private long counter;
    private CountListener listener;

    public CountingInputStream(InputStream in) {
        super(in);
    }

    public CountingInputStream(InputStream in, CountListener listener) {
        super(in);
        this.listener = listener;
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
            this.listener.bytesCounted(this.counter);
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
     * @param listener The listener to report to. Can be null.
     * @return The factory to create a wrapper.
     */
    public static WrapInputStreamFactory createWrapFactory(
        final CountListener listener) {
        return in -> new CountingInputStream(in, listener);
    }
}
