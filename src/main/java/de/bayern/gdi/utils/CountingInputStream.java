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
import java.io.InputStream;
import java.io.IOException;

/** CountingInputStream counts the number of bytes piped
 *  through the parent stream.
 */
public class CountingInputStream extends FilterInputStream {

    private long counter;

    public CountingInputStream(InputStream in) {
        super(in);
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

    @Override
    public int read() throws IOException {
        int x = in.read();
        if (x >= 0) {
            this.counter++;
        }
        return x;
    }

    @Override
    public int read(byte[] n, int off, int len) throws IOException {
        int x = in.read(n, off, len);
        if (x > 0) {
            this.counter++;
        }
        return x;
    }
}
