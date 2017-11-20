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

import de.bayern.gdi.utils.StringUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.Assert.assertEquals;

/**
 * Class to test String Utilities.
 */
@RunWith(JUnit4.class)
public class StringUtilsTest {

    private static final String[] PREFIXES = {
            "b:",
            "d:"
    };

    /**
     * Test StringUtils.ignorePartsWithPrefix.
     */
    @Test
    public void testIgnorePartsWithPrefix() {

        String in = "a:A1 b:Ignored C:c1 d:Ignored a:a2 c:c2";
        String want = "a:A1 C:c1 a:a2 c:c2";

        String out = StringUtils.ignorePartsWithPrefix(in, " ", PREFIXES);

        assertEquals(want, out);
    }

    /**
     * Test StringUtils.contains.
     */
    @Test
    public void testContains() {
        String[] needles = {"a", "b", "c"};
        String[] needles2 = {"a", "i", "o"};
        String[] haystack = {"d", "e", "f"};
        String[] haystack2 = {"a", "e", "f"};
        Assert.assertFalse(StringUtils.contains(needles, haystack));
        Assert.assertTrue(StringUtils.contains(needles, haystack2));
        Assert.assertTrue(StringUtils.contains(needles2, haystack2));
        Assert.assertFalse(StringUtils.contains(needles2, haystack));
    }


    private static final class SCL {
        String have;
        String []want;

        private SCL(String have, String []want) {
            this.have = have;
            this.want = want;
        }
    }

    private static final SCL[] SCL_CASES = {
        new SCL(null,
            new String[0]),
        new SCL("",
            new String[0]),
        new SCL("hello world",
            new String[] {"hello", "world"}),
        new SCL("hello 'planet world'",
            new String[] {"hello", "planet world"}),
        new SCL("hello \"planet world\"",
            new String[] {"hello", "planet world"}),
        new SCL("hello' world'",
            new String[] {"hello world"})
    };


    /**
     * Test StringUtils.splitCommandLine.
     */
    @Test
    public void testSplitCommandLine() {
        for (SCL c: SCL_CASES) {
            String[] got = StringUtils.splitCommandLine(c.have);
            Assert.assertArrayEquals(c.want, got);
        }

        try {
            StringUtils.splitCommandLine("\"");
            Assert.fail("Unbalanced \" not detected.");
            StringUtils.splitCommandLine("'");
            Assert.fail("Unbalanced ' not detected.");
        } catch (IllegalArgumentException iae) {
            // Test passed.
        }
    }

    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int FIVE = 5;
    private static final int SEVEN = 7;

    private static final int M1000 = 1000;
    private static final int M999 = 999;
    private static final int M256 = 256;
    private static final int M13 = 13;

    /**
     * Test StringUtils.places.
     */
    @Test
    public void testPlaces() {
        System.err.println(StringUtils.places(M1000));
        Assert.assertEquals("" + ZERO, ONE, StringUtils.places(ZERO));
        Assert.assertEquals("" + M1000, FOUR, StringUtils.places(M1000));
        Assert.assertEquals("" + M999, THREE, StringUtils.places(M999));
        Assert.assertEquals("" + M256, THREE, StringUtils.places(M256));
        Assert.assertEquals("" + M13, TWO, StringUtils.places(M13));
        Assert.assertEquals("" + SEVEN, ONE, StringUtils.places(SEVEN));

        Assert.assertEquals("" + -M1000, FIVE, StringUtils.places(-M1000));
        Assert.assertEquals("" + -M999, FOUR, StringUtils.places(-M999));
        Assert.assertEquals("" + -M256, FOUR, StringUtils.places(-M256));
        Assert.assertEquals("" + -M13, THREE, StringUtils.places(-M13));
        Assert.assertEquals("" + -SEVEN, TWO, StringUtils.places(-SEVEN));
    }

    private static final double EPS = 0.00001;

    private static void assertDoubleEquals(double [] a, double [] b) {
        if (a.length != b.length) {
            Assert.fail("arrays are not of same size");
        }
        for (int i = 0; i < a.length; i++) {
            if (Math.abs(a[i] - b[i]) > EPS) {
                Assert.fail("values are different");
            }
        }
    }

    @Test
    public void testToDouble() {
        final String have = "1 2 4 3 5";
        final double[] want = new double[] {ONE, TWO, FOUR, THREE, FIVE};
        double [] got = StringUtils.toDouble(have);
        assertDoubleEquals(want, got);
        got = StringUtils.toDouble("1 bad 2");
        assertDoubleEquals(new double[0], got);
    }
}
