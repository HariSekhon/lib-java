//
//  Author: Hari Sekhon
//  Date: 2015-05-31 14:05:14 +0100 (Sun, 31 May 2015)
//
//  vim:ts=4:sts=4:sw=4:et
//
//  https://github.com/harisekhon/lib-java
//
//  License: see accompanying Hari Sekhon LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
//
//  http://www.linkedin.com/in/harisekhon
//

package HariSekhon;

import HariSekhon.Utils.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests for HariSekhon.Utils
 */
public class UtilsTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public UtilsTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( UtilsTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testUtils()
    {
        assertEquals("isIP(10.10.10.1) eq 10.10.10.1", "10.10.10.1", Utils.isIP("10.10.10.1"));
        assertEquals("isIP(10.10.10.10) eq 10.10.10.10", "10.10.10.10", Utils.isIP("10.10.10.10"));
        assertEquals("isIP(10.10.10.100) eq 10.10.10.100", "10.10.10.100", Utils.isIP("10.10.10.100"));
        assertEquals("isIP(254.0.0.254) eq 254.0.0.254", "254.0.0.254", Utils.isIP("254.0.0.254"));
        assertEquals("isIP(255.255.255.254) eq 255.255.255.254", "255.255.255.254", Utils.isIP("255.255.255.254"));
        assertEquals("isIP(10.10.10.0) eq 10.10.10.0", "10.10.10.0", Utils.isIP("10.10.10.0"));
        assertEquals("isIP(10.10.10.255) eq 10.10.10.255", "10.10.10.255", Utils.isIP("10.10.10.255"));
        assertEquals("isIP(10.10.10.256) eq null", null, Utils.isIP("10.10.10.256"));
        assertEquals("isIP(x.x.x.x) eq null", null, Utils.isIP("x.x.x.x"));
    }
}
