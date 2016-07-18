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
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback
//  to help improve or steer this or other code I publish
//
//  https://www.linkedin.com/in/harisekhon
//

// my linkedin account is unique and will outlast my personal domains

package com.linkedin.harisekhon;

import static com.linkedin.harisekhon.Utils.*;

import java.io.*;
import java.net.URL;
import java.net.UnknownHostException;
//import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Collections;
import java.util.HashSet;
import java.util.List;

// JUnit 3
//import junit.framework.Test;
//import junit.framework.TestCase;
//import junit.framework.TestSuite;
// JUnit 4
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;

import static org.junit.Assert.*;
//import static org.hamcrest.CoreMatchers.containsString;
import static java.lang.Math.pow;

/**
 * Unit tests for com.linkedin.harisekhon.Utils
 */
public class UtilsTest { // extends TestCase { // JUnit 3
    /**
     * Create the test case
     *
     * @param testName name of the test case
     *
    public UtilsTest( String testName )
    {
        super( testName );
    }
    */

    /**
     * @return the suite of tests being tested
     *
    public static Test suite()
    {
        return new TestSuite( UtilsTest.class );
    }
    */

    // no method called should take 1 sec, but occasionally this causes build failures on slower systems like Travis VMs
    // or Docker containers so setting a little higher to 3 secs
    @Rule
    public Timeout globalTimeout = Timeout.seconds(3);
    // specified in millis - this should never take even 1 second
    //@Test(timeout=1000)
    // done at global level above now

    // not really designed to be instantiated since there's no state but anyway
    @Test
    public void test_utils_instance(){
        Utils u = new com.linkedin.harisekhon.Utils();
        assert(u instanceof Utils);
    }

    @Test
    public void test_getStatusCode(){
        // programs depends on this for interoperability with Nagios compatiable monitoring systems
        assertSame("getStatus(OK)",             0,  getStatusCode("OK"));
        assertSame("getStatus(WARNING)",        1,  getStatusCode("WARNING"));
        assertSame("getStatus(CRITICAL)",       2,  getStatusCode("CRITICAL"));
        assertSame("getStatus(UNKNOWN)",        3,  getStatusCode("UNKNOWN"));
        assertSame("getStatus(DEPENDENT)",      4,  getStatusCode("DEPENDENT"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_getStatusCode_exception(){
        getStatusCode("somethingInvalid");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_setStatus_exception(){
        setStatus("somethingInvalid");
    }

    @Test
    public void test_setStatus_getStatus_and_set_shortcuts(){
        // start unknown - but when repeatedly testing this breaks so reset to UNKNOWN at end
        assertTrue(isUnknown());
        assertTrue(getStatus().equals("UNKNOWN"));
        assertEquals(getStatusCode(), 3);
        warning();
        status();
        status2();
        status3();
        assertTrue(isWarning());
        assertTrue(getStatus().equals("WARNING"));

        // shouldn't change from warning to unknown
        unknown();
        assertTrue(isWarning());
        assertTrue(getStatus().equals("WARNING"));
        assertEquals(getStatusCode(), 1);

        // critical should override unknown
        setStatus("OK");
        assertTrue(isOk());
        unknown();
        assertTrue(isUnknown());
        critical();
        assertTrue(isCritical());
        assertTrue(getStatus().equals("CRITICAL"));
        assertEquals(getStatusCode(), 2);

        // critical should override warning
        setStatus("WARNING");
        assertTrue(isWarning());
        critical();
        assertTrue(isCritical());
        unknown(); // shouldn't change critical
        assertTrue(isCritical());
        assertTrue(getStatus().equals("CRITICAL"));

        setStatus("UNKNOWN");
    }

    // JUnit 4.9+ requires System Rules for system exit checks
//    @Rule
//    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

//    @Test
//    public void test_quit_status_msg() {
//        exit.expectSystemExit();
//        quit("CRITICAL", "test");
//    }
//
//    @Test
//    public void test_quit_msg() {
//        exit.expectSystemExit();
//        quit("test");
//    }

    @Test
    public void test_println() {
        println("test");
        println(1.0);
        println(1L);
        println(true);
        assert(true);
    }

    @Test(expected=QuitException.class)
    public void test_quit_exception(){
        try {
            quit("CRITICAL", "blah");
        } catch (QuitException e){
            assert "CRITICAL".equals(e.status);
            assert "blah".equals(e.message);
            throw e;
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_quit_invalid_status_exception(){
        quit("INVALID_STATUS", "blah");
    }

//    @Test
//    public void test_HostOptions(){
//        HostOptions();
//    }

    @Test
    public void test_load_tlds_nodups() throws IOException {
        loadTlds("tlds-alpha-by-domain.txt");
        loadTlds("tlds-alpha-by-domain.txt");
        assertTrue(tlds.size() > 1000);
        assertTrue(tlds.size() < 2000);
    }

    @Test
    public void test_load_tlds_skip() throws IOException, IllegalStateException {
        String filename = "faketld.txt";
//        URL url = com.linkedin.harisekhon.Utils.class.getResource("/tlds-alpha-by-domain.txt");
        URL url = com.linkedin.harisekhon.Utils.class.getResource("/");
        if(url == null){
            throw new IOException("can't get resource directory path!");
        }
//        String resourcePath = new File(url.getFile()).getParent();
//        File f = new File(String.format("%s/%s". resourcePath, filename));
        File f = new File(String.format("%s/%s", url.getFile(), filename));
        FileWriter fw = new FileWriter(f);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("=");
        bw.close();
        loadTlds(filename);
        if(tlds.contains("=")){
            throw new IllegalStateException("tlds contain '=' which should have been excluded by loadTlds()");
        }
        f.delete();
        assert(tlds.size() == 0);
    }

    @Test(expected=IOException.class)
    public void test_load_tlds_nonexistent() throws Exception {
        loadTlds("nonexistentfile.txt");
        // shouldn't reach here
        throw new Exception("loadTlds() failed to thrown an IOException for a nonexistent file");
    }

    @Test(expected=IllegalStateException.class)
    public void test_check_tldcount_too_high() throws IOException {
        loadTlds("tlds-alpha-by-domain.txt");
        for(int i=0; i<1000; i++){
            tlds.add(String.format("%d", i));
        }
        checkTldCount();
    }

    @Test(expected = IllegalStateException.class)
    public void test_check_tldcount_too_low(){
        tlds.clear();
        checkTldCount();
    }

    // ====================================================================== //
    // tests both array to arraylist at same time
    @Test
    public void test_array_to_arraylist(){
        String[] a = {"node1:9200","node2","node3:8080","node4","node5"};
        assertArrayEquals("arrayToArraylist()", a, arraylistToArray(arrayToArraylist(a)));
    }

    @Test
    public void test_set_to_array(){
        String[] a = {"test"};
        HashSet<String> b = new HashSet<String>();
        b.add("test");
        assertArrayEquals("setToArray()", a, setToArray(b));
    }

    // ====================================================================== //

    @Test
    public void test_name(){
        assertEquals("name(null)", "", name(null));
        assertEquals("name(blank)", "", name(""));
        assertEquals("name(blah)", "blah ", name("blah"));
    }

    @Test
    public void test_requireName(){
        assertEquals("requireName(blah)", "blah", requireName("blah"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_requireName_exception_null() throws IllegalArgumentException {
        assertEquals("requireName(null)", "", requireName(null));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_requireName_exception_blank() throws IllegalArgumentException {
        assertEquals("requireName(blank)", "", requireName(""));
    }

//    @Test(expected=IllegalArgumentException.class)
//    public void test_arg_error() throws IllegalArgumentException {
//        arg_error("blah");
//    }
//
//    @Test(expected=IllegalStateException.class)
//    public void test_state_error() throws IllegalStateException {
//        state_error("blah");
//    }

    // ====================================================================== //
    @Test
    public void test_check_regex(){
        //println(check_regex("test", "test"));
        assertTrue("check_regex(test,test)",   check_regex("test", "test"));
        assertTrue("check_regex(test,test)",   check_regex("test", "te.t"));
        assertFalse("check_regex(test,test2)", check_regex("test", "^est"));
        assertFalse("check_regex(null,test)",  check_regex(null, "test"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_check_regex_exception() throws IllegalArgumentException {
        check_regex("test", "*est");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_check_regex_null_regex_exception() throws IllegalArgumentException {
        check_regex("test", null);
    }

    // ====================================================================== //
    @Test
    public void test_check_string(){
        //println(checkString("test", "test"));
        assertTrue("checkString(test,test)",   checkString("test", "test"));             // will use ==
        assertTrue("checkString(test,test)",   checkString("test", "test", true));
        assertTrue("checkString(test,test)",   checkString("test", new String("test"))); // will use .equals()
        assertFalse("checkString(test,test2)", checkString("test", "test2"));
        assertFalse("checkString(null,test2)", checkString(null, "test2"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_check_string_null_exception() throws IllegalArgumentException {
        checkString("test", null);
    }

    // ====================================================================== //
//    @Test
//    public void test_get_options() {
//        get_options(new String[]{"-H", "host1.internal", "-P", "80"});
//    }

    // ====================================================================== //
    @Test
    public void test_expand_units(){
        //println(expandUnits(10, "kb")); // => 10240
        //println(10240L);
        assertEquals("expandUnits(10, KB)",    10240L,             expandUnits(10L, "KB"));
        assertEquals("expandUnits(10, mB)",    10485760,           expandUnits(10, "mB"));
        assertEquals("expandUnits(10, Gb)",    10737418240L,       expandUnits(10L, "Gb"));
        assertEquals("expandUnits(10, tb)",    10995116277760L,    expandUnits(10L, "tb"));
        assertEquals("expandUnits(10, Pb)",    11258999068426240L, expandUnits(10L, "Pb"));
        assertEquals("expandUnits(10, KB, name)",  1024L,          expandUnits(1L, "KB", "name"));
        assertEquals("expandUnits(10, KB, name)",  10240.0,        expandUnits(10.0, "KB", "name"),   0);
        assertEquals("expandUnits(10, KB)",    10240.0,            expandUnits(10.0, "KB"),   0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_expand_units_null_exception() throws IllegalArgumentException {
        expandUnits(10, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_expand_units_invalid_units_exception() throws IllegalArgumentException {
        expandUnits(10, "Kbps");
    }

    @Test
    public void test_plural(){
        assertEquals("plural(1)", "", plural(1));
        assertEquals("plural('1')", "", plural("1"));
        assertEquals("plural()", "", plural(""));
        assertEquals("plural( )", "", plural(" "));
        assertEquals("plural(null)", "", plural(null));
        assertEquals("plural(0)", "s", plural(0));
        assertEquals("plural('0')", "s", plural("0"));
        assertEquals("plural(2)", "s", plural(2));
        assertEquals("plural(3.0)", "s", plural(3.0));
        assertEquals("plural(4.0)", "s", plural("4.0"));
        // list tests
//        ArrayList a = new ArrayList();
//        assertEquals("plural(arraylist_0)", "s", plural(a));
//        a.add(1);
//        assertEquals("plural(arraylist_1)", "", plural(a));
//        a.add(2);
//        assertEquals("plural(arraylist_2)", "s", plural(a));
        // raw array tests
//        String[] b = new String[]{};
//        assertEquals("plural(array_0)", "s", plural(b));
//        b.add(1);
//        assertEquals("plural(array_1)", "", plural(b));
//        b.add(2);
//        assertEquals("plural(array_2)", "s", plural(b));
    }

    // ====================================================================== //
    @Test
    public void test_hr(){
        hr();
        assert(true);
    }

    // ====================================================================== //
    @Test
    public void test_human_units(){
        //println(humanUnits(1023     * pow(1024,1)));
        assertEquals("humanUnits(1023)",   "1023 bytes",   humanUnits(1023));
        assertEquals("human units KB",      "1023KB",       humanUnits(1023 * pow(1024, 1)));
        assertEquals("humanUnits MB",      "1023.1MB",     humanUnits(1023.1 * pow(1024, 2)));
        assertEquals("human units GB",      "1023.2GB",     humanUnits(1023.2 * pow(1024, 3)));
        assertEquals("human units TB",      "1023.31TB",    humanUnits(1023.31 * pow(1024, 4)));
        assertEquals("human units PB",      "1023.01PB",    humanUnits(1023.012 * pow(1024, 5)));
        assertEquals("human units EB",      "1023EB",       humanUnits(1023 * pow(1024, 6)));

        assertEquals("humanUnits(1023)",   "1023 bytes",   humanUnits(1023, "b"));
        assertEquals("humanUnits(1023)",   "1023B",        humanUnits(1023, "b", true));
        assertEquals("human units KB",      "1023KB",       humanUnits(1023, "KB"));
        assertEquals("humanUnits MB",      "1023.1MB",     humanUnits(1023.1, "MB"));
        assertEquals("human units GB",      "1023.2GB",     humanUnits(1023.2, "GB"));
        assertEquals("human units TB",      "1023.31TB",    humanUnits(1023.31, "TB"));
        assertEquals("human units PB",      "1023.01PB",    humanUnits(1023.012, "PB"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_human_units_invalid_units_exception() throws IllegalArgumentException {
        humanUnits(pow(1024, 7), "");
    }

    // ====================================================================== //
    @Test
    public void test_resolve_ip() throws UnknownHostException {
        // if not on a decent OS assume I'm somewhere lame like a bank where internal resolvers don't resolve internet addresses
        // this way my continous integration tests still run this one
//        if(isLinuxOrMac()){
            assertEquals("resolveIp(a.resolvers.level3.net)",  "4.2.2.1",  resolveIp("a.resolvers.level3.net"));
            assertEquals("validate_resolvable()", "4.2.2.1",  validate_resolvable("a.resolvers.level3.net"));
//        }
        assertEquals("resolveIp(4.2.2.1)",    "4.2.2.1",  resolveIp("4.2.2.1"));
        assertEquals("validate_resolvable()",  "4.2.2.2",  validate_resolvable("4.2.2.2"));
    }

    // Some DNS servers return default answers for anything that doesn't resolve in order to redirect you to a landing page
//    @Test(expected=UnknownHostException.class)
//    public void test_resolve_ip_nonexistenthost_exception() throws UnknownHostException {
//        resolveIp("nonexistenthost.local");
//    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_resolve_ip_null_exception() throws IllegalArgumentException, UnknownHostException {
        resolveIp(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_resolve_ip_blank_exception() throws IllegalArgumentException, UnknownHostException {
        resolveIp(" ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_resolvable_null_exception() throws IllegalArgumentException, UnknownHostException {
        validate_resolvable(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_resolvable_blank_exception() throws IllegalArgumentException, UnknownHostException {
        validate_resolvable(" ");
    }

    // Some DNS servers return default answers for anything that doesn't resolve in order to redirect you to a landing page
//    @Test(expected=UnknownHostException.class)
//    public void test_validate_resolvable_blank_exception() throws IllegalArgumentException, UnknownHostException {
//        validate_resolvable("nonexistenthost.local");
//    }

    // ====================================================================== //
    @Test
    public void test_strip_scheme(){
        assertEquals("strip_scheme(file:/blah)",                        "/blah",                        strip_scheme("file:/blah"));
        assertEquals("strip_scheme(file:///path/to/blah)",              "/path/to/blah",                strip_scheme("file:///path/to/blah"));
        assertEquals("strip_scheme(http://blah)",                       "blah",                         strip_scheme("http://blah"));
        assertEquals("strip_scheme(hdfs:///blah)",                      "/blah",                        strip_scheme("hdfs:///blah"));
        assertEquals("strip_scheme(hdfs://namenode/path/to/blah)",      "namenode/path/to/blah",        strip_scheme("hdfs://namenode/path/to/blah"));
        assertEquals("strip_scheme(hdfs://namenode:8020/path/to/blah)", "namenode:8020/path/to/blah",   strip_scheme("hdfs://namenode:8020/path/to/blah"));
    }

    // ====================================================================== //
    @Test
    public void test_strip_scheme_host(){
        assertEquals("stripSchemeHost(file:/blah)",                               "/blah",            stripSchemeHost("file:/blah"));
        assertEquals("stripSchemeHost(file:///path/to/blah)",                     "/path/to/blah",    stripSchemeHost("file:///path/to/blah"));
        assertEquals("stripSchemeHost(hdfs:///path/to/blah)",                     "/path/to/blah",    stripSchemeHost("hdfs:///path/to/blah"));
        assertEquals("stripSchemeHost(http://my.domain.com/blah)",                "/blah",            stripSchemeHost("http://my.domain.com/blah"));
        assertEquals("stripSchemeHost(hdfs://nameservice1/hdfsfile)",             "/hdfsfile",        stripSchemeHost("hdfs://nameservice1/hdfsfile"));
        assertEquals("stripSchemeHost(hdfs://nameservice1:8020/hdfsfile)",        "/hdfsfile",        stripSchemeHost("hdfs://nameservice1:8020/hdfsfile"));
        assertEquals("stripSchemeHost(hdfs://namenode.domain.com/hdfsfile)",      "/hdfsfile",        stripSchemeHost("hdfs://namenode.domain.com/hdfsfile"));
        assertEquals("stripSchemeHost(hdfs://namenode.domain.com:8020/hdfsfile)", "/hdfsfile",        stripSchemeHost("hdfs://namenode.domain.com:8020/hdfsfile"));
        assertEquals("stripSchemeHost(hdfs://nameservice1/path/to/hdfsfile)",     "/path/to/hdfsfile", stripSchemeHost("hdfs://nameservice1/path/to/hdfsfile"));
    }

//    @Test(expected=IllegalArgumentException.class)
//    public void test_usage(){
//        usage("test");
//    }

//    @Test(expected=IllegalArgumentException.class)
//    public void test_usage_blank(){
//        usage("");
//    }

//    @Test(expected=IllegalArgumentException.class)
//    public void test_usage_empty(){
//        usage();
//    }

    // ====================================================================== //


    @Test
    public void test_uniq_array(){
        String[] myArray = {"one","two","three","","one"};
        String[] myArray_deduped = {"one","two","three",""};
        String[] myArray_test = uniqArray(myArray);
        // The ordering is highly dependent on JDK version and fails on Oracle JDK 8 in Travis so must sort the arrays for comparison
        Arrays.sort(myArray_deduped);
        Arrays.sort(myArray_test);
        assertArrayEquals(myArray_deduped, myArray_test);
    }

    @Test
    public void test_uniq_arraylist(){
        List<String> myList = Arrays.asList("one", "two", "three", "", "one");
        String[] myArray_deduped = {"one","two","three",""};
        String[] myArray_test = arraylistToArray(uniqArraylist(myList));
        // The ordering is highly dependent on JDK version and fails on Oracle JDK 8 in Travis so must sort the arrays for comparison
        Arrays.sort(myArray_deduped);
        Arrays.sort(myArray_test);
//        Collections.sort(myList);
//        String[] myArray_test = arraylistToArray(myList);
        assertArrayEquals(myArray_deduped, myArray_test);
    }

    @Test
    public void test_uniq_array_ordered() {
        assertArrayEquals("uniqArrayOrdered(one,two,three,,one)", new String[]{ "one", "two", "three", ""}, uniqArrayOrdered(new String[]{"one", "two", "three", "", "one"}));
    }

    @Test
    public void test_uniq_arraylist_ordered(){
        String[] a = {"one","two","three","","one"};
        String[] b = {"one","two","three",""};
        assertArrayEquals("uniqArraylistOrdered(one,two,three,,one)", b, arraylistToArray(uniqArraylistOrdered(arrayToArraylist(a))));
    }

    // ====================================================================== //
    //                          O S   H e l p e r s
    // ====================================================================== //

    /*
    @Test
    public void print_java_properties(){
        print_java_properties();
    }
    */
    //import static org.hamcrest.CoreMatchers.
    @Test
    public void test_getOS(){
        assertTrue("getOS()", getOS().matches(".*(?:Linux|Mac|Windows).*"));
    }

    @Test
    public void test_isOS(){
        assertTrue(isOS(System.getProperty("os.name")));
    }

    @Test
    public void test_isLinux() throws UnsupportedOSException {
        if(isLinux()){
            assertEquals("isLinux()", "Linux", getOS());
            linuxOnly();
        }
    }

    @Test
    public void test_isLinux_exception() throws Exception {
        if(!isLinux()){
            try {
                linuxOnly();
                throw new Exception("failed to raise UnsupportedOSException");
            } catch (UnsupportedOSException e){
                // pass
            }
        }
    }

    @Test
    public void test_isMac(){
        if(isMac()){
            assertEquals("isMac()", "Mac OS X", getOS());
            macOnly();
        }
    }

    @Test
    public void test_isMac_exception() throws Exception {
        if(!isMac()){
            try {
                macOnly();
                throw new Exception("failed to raise UnsupportedOSException");
            } catch (UnsupportedOSException e){
                // pass
            }
        }
    }

    @Test
    public void test_isLinuxOrMac(){
        if(isLinuxOrMac()){
            assertTrue("isLinuxOrMac()", getOS().matches("Linux|Mac OS X"));
            linuxMacOnly();
        }
    }

    @Test
    public void test_isLinuxOrMac_exception() throws Exception {
        if(!isLinuxOrMac()){
            try {
                linuxMacOnly();
                throw new Exception("failed to raise UnsupportedOSException");
            } catch (UnsupportedOSException e){
                // pass
            }
        }
    }

    // ====================================================================== //
    //             V a l i d a t i o n    M e t h o d s
    // ====================================================================== //

    @Test
    public void test_isAlNum(){
        assertTrue("isAlNum(ABC123efg)", isAlNum("ABC123efg"));
        assertTrue("isAlNum(0)",         isAlNum("0"));
        assertFalse("isAlNum(1.2)",      isAlNum("1.2"));
        assertFalse("isAlNum(\"\")",     isAlNum(""));
        assertFalse("isAlNum(null)",     isAlNum(null));
        assertFalse("isAlNum(hari@domain.com)",     isAlNum("hari@domain.com"));
    }

    @Test
    public void test_validate_alnum(){
        assertEquals("validateAlnum(Alnum2Test99, alnum test)", "Alnum2Test99", validateAlnum("Alnum2Test99", "alnum test"));
        assertEquals("validateAlnum(0, alnum zero)", "0", validateAlnum("0", "alnum zero"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_alnum_exception(){
        validateAlnum("1.2", "alnum exception");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_alnum_null_exception(){
        validateAlnum(null, "alnum exception");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_alnum_blank_exception(){
        validateAlnum(" ", "alnum exception");
    }

    // ====================================================================== //
    @Test
    public void test_isAwsAccessKey(){
        assertTrue(isAwsAccessKey(repeatString("A", 20)));
        assertTrue(isAwsAccessKey(repeatString("1", 20)));
        assertTrue(isAwsAccessKey(repeatString("A1", 10)));
        assertFalse(isAwsAccessKey(repeatString("@", 20)));
        assertFalse(isAwsAccessKey(repeatString("A", 40)));
        assertFalse(isAwsAccessKey(null));
    }

    @Test
    public void test_validate_aws_access_key(){
        assertEquals("validateAwsAccessKey(A * 20)", repeatString("A", 20), validateAwsAccessKey(repeatString("A", 20)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_access_key_exception(){
        validateAwsAccessKey(repeatString("A", 21));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_access_key_null_exception(){
        validateAwsAccessKey(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_access_key_blank_exception(){
        validateAwsAccessKey(" ");
    }

    // ====================================================================== //

    @Test
    public void test_isAwsBucket(){
        assertTrue("isAwsBucket(BucKeT63)", isAwsBucket("BucKeT63"));
        assertFalse("isAwsBucket(BucKeT63)", isAwsBucket("B@cKeT63"));
        assertFalse("isAwsBucket(null)", isAwsBucket(null));
    }

    @Test
    public void test_validate_aws_bucket(){
        assertEquals("validateAwsBucket(BucKeT63)", "BucKeT63", validateAwsBucket("BucKeT63"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_bucket_exception(){
        validateAwsBucket("B@cKeT63");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_bucket_null_exception(){
        validateAwsBucket(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_bucket_blank_exception(){
        validateAwsBucket(" ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_bucket_ip_exception(){
        validateAwsBucket("4.2.2.1");
    }

    // ====================================================================== //

    @Test
    public void test_isAwsHostname(){
        assertTrue(isAwsHostname("ip-172-31-1-1"));
        assertTrue(isAwsHostname("ip-172-31-1-1.eu-west-1.compute.internal"));
        assertFalse(isAwsHostname("harisekhon"));
        assertFalse(isAwsHostname("10.10.10.1"));
        assertFalse(isAwsHostname(repeatString("A", 40)));
        assertFalse(isAwsHostname(null));
    }

    @Test
    public void test_validate_aws_hostname(){
        assertEquals("validateAwsHostname(ip-172-31-1-1)", "ip-172-31-1-1", validateAwsHostname("ip-172-31-1-1"));
        assertEquals("validateAwsHostname(ip-172-31-1-1.eu-west-1.compute.internal)", "ip-172-31-1-1.eu-west-1.compute.internal", validateAwsHostname("ip-172-31-1-1.eu-west-1.compute.internal"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_hostname_exception() {
        validateAwsHostname("harisekhon");
    }
    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_hostname_exception2() {
        validateAwsHostname("10.10.10.1");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_hostname_exception3() {
        validateAwsHostname(repeatString("A", 40));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_hostname_null_exception() {
        validateAwsHostname(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_hostname_blank_exception() {
        validateAwsHostname(" ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_hostname_ip_exception() {
        validateAwsHostname("4.2.2.1");
    }

    // ====================================================================== //

    @Test
    public void test_isAwsFqdn(){
        assertTrue(isAwsFqdn("ip-172-31-1-1.eu-west-1.compute.internal"));
        assertFalse(isAwsFqdn("ip-172-31-1-1"));
        assertFalse(isAwsFqdn(null));
    }

    @Test
    public void test_validate_aws_fqdn(){
        assertEquals("validateAwsFqdn(ip-172-31-1-1.eu-west-1.compute.internal)", "ip-172-31-1-1.eu-west-1.compute.internal", validateAwsFqdn("ip-172-31-1-1.eu-west-1.compute.internal"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_fqdn_exception() {
        validateAwsFqdn("ip-172-31-1-1");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_fqdn_null_exception() {
        validateAwsFqdn(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_fqdn_blank_exception() {
        validateAwsFqdn(" ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_fqdn_ip_exception() {
        validateAwsFqdn("4.2.2.1");
    }

    // ====================================================================== //

    @Test
    public void test_isAwsSecretKey(){
        assertTrue(isAwsSecretKey(repeatString("A", 40)));
        assertTrue(isAwsSecretKey(repeatString("1", 40)));
        assertTrue(isAwsSecretKey(repeatString("A1", 20)));
        assertFalse(isAwsSecretKey(repeatString("@", 40)));
        assertFalse(isAwsSecretKey(repeatString("A", 20)));
        assertFalse(isAwsSecretKey(null));
    }

    @Test
    public void test_validate_aws_secret_key(){
        assertEquals("validateAwsSecretKey(A * 40)", repeatString("A", 40), validateAwsSecretKey(repeatString("A", 40)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_secret_key_exception(){
        validateAwsSecretKey(repeatString("A", 41));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_secret_key_null_exception(){
        validateAwsSecretKey(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_aws_secret_key_blank_exception(){
        validateAwsSecretKey(" ");
    }

    // ====================================================================== //
    @Test
    public void test_isChars(){
        assertTrue(isChars("Alpha-01_", "A-Za-z0-9_-"));
        assertFalse(isChars("Alpha-01_*", "A-Za-z0-9_-"));
        assertFalse(isChars("Alpha-01_*", null));
        assertFalse(isChars(null, "A-Za-z0-9_-"));
        assertFalse(isChars(null, null));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_isChars_exception(){
        isChars("Alpha-01_*", "B-A");
    }

    @Test
    public void test_validate_chars(){
        assertEquals("validateChars(...)", "log_date=2015-05-23_10", validateChars("log_date=2015-05-23_10", "validate chars", "A-Za-z0-9_=-"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_chars_exception(){
        validateChars("Alpha-01_*", "validate chars", "A-Za-z0-9_-");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_chars_null_exception(){
        validateChars("Alpha-01_*", "validate chars", null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_chars_null_arg_exception(){
        validateChars(null, "validate chars", "A-Za-z0-9_-");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_chars_blank_exception(){
        validateChars("Alpha-01_*", "validate chars", " ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_chars_blank_arg_exception(){
        validateChars(null, "validate chars", "A-Za-z0-9_-");
    }

    // ====================================================================== //
    @Test
    public void test_isCollection(){
        assertTrue(isCollection("students.grades"));
        assertFalse(isCollection("wrong@.grades"));
        assertFalse(isCollection(null));
    }

    @Test
    public void test_validate_collection(){
        assertEquals("validateCollection(students.grades)", "students.grades", validateCollection("students.grades"));
        assertEquals("validateCollection(students.grades, name)", "students.grades", validateCollection("students.grades", "name"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_collection_exception(){
        validateCollection("wrong@grades", "name");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_collection_null_exception(){
        validateCollection(null, "name");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_collection_blank_exception(){
        validateCollection(" ", "name");
    }

    // ====================================================================== //
    @Test
    public void test_isDatabaseName(){
        assertTrue(isDatabaseName("mysql1"));
        assertFalse(isDatabaseName("my@sql"));
        assertFalse(isDatabaseName(null));
    }

    @Test
    public void test_validate_database(){
        assertEquals("validateDatabase(mysql)", "mysql", validateDatabase("mysql"));
        assertEquals("validateDatabase(mysql, MySQL)", "mysql", validateDatabase("mysql", "MySQL"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_exception(){
        validateDatabase("my@sql", "name");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_null_exception(){
        validateDatabase(null, "name");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_blank_exception(){
        validateDatabase(" ", "name");
    }

    // ====================================================================== //
    @Test
    public void test_isDatabaseColumnName(){
        assertTrue(isDatabaseColumnName("myColumn_1"));
        assertFalse(isDatabaseColumnName("'column'"));
        assertFalse(isDatabaseColumnName(null));
    }

    @Test
    public void test_validate_database_columnname(){
        assertEquals("validateDatabaseColumnname(myColumn_1)", "myColumn_1", validateDatabaseColumnname("myColumn_1"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_columname_exception(){
        validateDatabaseColumnname("'column'");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_columname_null_exception(){
        validateDatabaseColumnname(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_columname_blank_exception(){
        validateDatabaseColumnname(" ");
    }

    // ====================================================================== //
    @Test
    public void test_isDatabaseFieldName(){
        assertTrue(isDatabaseFieldName("2"));
        assertTrue(isDatabaseFieldName("age"));
        assertTrue(isDatabaseFieldName("count(*)"));
        assertFalse(isDatabaseFieldName("@something"));
        assertFalse(isDatabaseFieldName(null));
    }

    @Test
    public void test_validate_database_fieldname(){
        assertEquals("validateDatabaseFieldname(age)", "age", validateDatabaseFieldname("age"));
        assertEquals("validateDatabaseFieldname(10)", "10", validateDatabaseFieldname("10"));
        assertEquals("validateDatabaseFieldname(count(*))", "count(*)", validateDatabaseFieldname("count(*)"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_fieldname_exception(){
        validateDatabaseFieldname("@something");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_fieldname_null_exception(){
        validateDatabaseFieldname(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_fieldname_blank_exception(){
        validateDatabaseFieldname(" ");
    }

    // ====================================================================== //
    @Test
    public void test_isDatabaseTableName(){
        assertTrue(isDatabaseTableName("myTable_1"));
        assertTrue(isDatabaseTableName("default.myTable_1", true));
        assertFalse(isDatabaseTableName("'table'"));
        assertFalse(isDatabaseTableName("default.myTable_1", false));
        assertFalse(isDatabaseTableName("default.myTable_1"));
        assertFalse(isDatabaseTableName(null));
    }

    @Test
    public void test_validate_database_tablename(){
        assertEquals("validateDatabaseTablename(myTable)", "myTable", validateDatabaseTablename("myTable"));
        assertEquals("validateDatabaseTablename(myTable, Hive)", "myTable", validateDatabaseTablename("myTable", "Hive"));
        assertEquals("validateDatabaseTablename(myTable, Hive)", "myTable", validateDatabaseTablename("myTable", false));
        assertEquals("validateDatabaseTablename(default.myTable, Hive, true)", "default.myTable", validateDatabaseTablename("default.myTable", "Hive", true));
        assertEquals("validateDatabaseTablename(default.myTable, true)", "default.myTable", validateDatabaseTablename("default.myTable", true));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_tablename_exception(){
        validateDatabaseTablename("default.myTable");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_tablename_qualified_exception(){
        validateDatabaseTablename("default.myTable", false);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_tablename_null_exception(){
        validateDatabaseTablename(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_tablename_blank_exception(){
        validateDatabaseTablename(" ");
    }

    // ====================================================================== //
    @Test
    public void test_isDatabaseViewName(){
        assertTrue(isDatabaseViewName("myView_1"));
        assertTrue(isDatabaseViewName("default.myView_1", true));
        assertFalse(isDatabaseViewName("'view'"));
        assertFalse(isDatabaseViewName("default.myView_1", false));
        assertFalse(isDatabaseViewName("default.myView_1"));
    }

    @Test
    public void test_validate_database_viewname(){
        assertEquals("validateDatabaseViewname(myView)", "myView", validateDatabaseViewname("myView"));
        assertEquals("validateDatabaseViewname(myView, Hive)", "myView", validateDatabaseViewname("myView", "Hive"));
        assertEquals("validateDatabaseViewname(default.myView, Hive, true)", "default.myView", validateDatabaseViewname("default.myView", "Hive", true));
        assertEquals("validateDatabaseViewname(default.myView, Hive, true)", "default.myView", validateDatabaseViewname("default.myView", true));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_viewname_exception(){
        validateDatabaseViewname("default.myView");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_viewname_null_exception(){
        validateDatabaseViewname(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_viewname_blank_exception(){
        validateDatabaseViewname(" ");
    }

    // ====================================================================== //

    @Test
    public void test_isDirname(){
        assertTrue("isDirname(test_Dir)", isDirname("test_Dir"));
        assertTrue("isDirname(/tmp/test)", isDirname("/tmp/test"));
        assertTrue("isDirname(./test)", isDirname("./test"));
        assertFalse("isDirname(@me)", isDirname("@me"));
        assertFalse("isDirname(@me)", isDirname(null));
    }

    @Test
    public void test_validate_dirname(){
        assertEquals("validateDirname(./src)",     "./src",    validateDirname("./src", "dirname"));
        assertEquals("validateDirname(./src, true)",   "./src",    validateDirname("./src", "dirname", true));
        assertEquals("validateDirname(/etc)",  "/etc",     validateDirname("/etc", "dirname"));
        assertEquals("validateDirname(/etc/)",     "/etc/",    validateDirname("/etc/", "dirname"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_dirname_exception(){
        validateDirname("b@dDir");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_dirname_null_exception(){
        validateDirname(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_dirname_blank_exception(){
        validateDirname(" ");
    }

    @Test
    public void test_validate_directory(){
        if(isLinuxOrMac()){
            assertEquals("validateDirectory(./src)",   "./src",    validateDirectory("./src", "directory"));
            assertEquals("validateDirectory(./src, true)",     "./src",    validateDirectory("./src", "directory", true));
            assertEquals("validateDirectory(/etc)",    "/etc",     validateDirectory("/etc", "directory"));
            assertEquals("validateDirectory(/etc/)",   "/etc/",    validateDirectory("/etc/", "directory"));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_directory_exception(){
        validateDirectory("b@dDir");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_directory_null_exception(){
        validateDirectory(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_directory_blank_exception(){
        validateDirectory(" ");
    }
    @Test(expected=IllegalArgumentException.class)
    public void test_validate_directory_nonexistent_exception(){
        validateDirectory("/etc/nonexistent");
    }

    @Test
    public void test_validate_dir(){
        if(isLinuxOrMac()){
            assertEquals("validateDir(./src)",     "./src",    validateDir("./src", "directory"));
            assertEquals("validateDir(./src, true)",   "./src",    validateDir("./src", "directory", true));
            assertEquals("validateDir(/etc)",      "/etc",     validateDir("/etc", "dir"));
            assertEquals("validateDir(/etc/)",     "/etc/",    validateDir("/etc/", "dir"));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_dir_exception(){
        validateDir("b@dDir");
    }

    // ====================================================================== //
    @Test
    public void test_validate_double() {
        validate_double(2.0, "two", 2, 3);
        validate_double(3.0, "three", 2, 3);
        validate_double("2.1", "two string", 2, 3);
        validate_float(2.0f, "two", 2f, 3f);
        validate_float("2.0", "two string", 2f, 3f);
        validate_long(2L, "two", 2L, 3L);
        validate_long("2", "two string", 2L, 3L);
        validate_int(2, "two", 2, 3);
        validate_int("2", "two string", 2, 3);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_double_exception(){
        validate_double("a", "non-double", 2, 3);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_double_lower_exception(){
        validate_double(2.0, "name", 3, 4);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_double_higher_exception(){
        validate_double(4.0, "name", 2, 3);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_double_inverted_thresholds_exception(){
        validate_double(2.0, "name", 3, 2);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_float_exception(){
        validate_float("a", "non-float", 2f, 3f);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_long_exception(){
        validate_long("a", "non-long", 2L, 3L);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_int_exception(){
        validate_int("a", "non-int", 2, 3);
    }

    // ====================================================================== //
    @Test
    public void test_isDomain(){
        assertTrue(isDomain("localDomain"));
        assertTrue(isDomain("harisekhon.com"));
        assertTrue(isDomain("1harisekhon.com"));
        assertTrue(isDomain("com"));
        assertTrue(isDomain("compute.internal"));
        assertTrue(isDomain("eu-west-1.compute.internal"));
        assertTrue(isDomain(repeatString("a", 63) + ".com"));
        assertFalse(isDomain(repeatString("a", 64) + ".com"));
        assertFalse(isDomain("harisekhon")); // not a valid TLD
        assertFalse(isDomain(null)); // not a valid TLD
    }

    @Test
    public void test_validate_domain(){
        assertEquals("validateDomain(harisekhon.com)", "harisekhon.com", validateDomain("harisekhon.com"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_domain_exception() {
        validateDomain(repeatString("a", 64) + ".com");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_domain_exception2() {
        validateDomain("harisekhon");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_domain_null_exception() {
        validateDomain(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_domain_blank_exception() {
        validateDomain(" ");
    }

    // ====================================================================== //
    @Test
    public void test_isDomainStrict(){
        assertFalse(isDomainStrict("com"));
        assertTrue(isDomainStrict("domain.com"));
        assertTrue(isDomainStrict("123domain.com"));
        assertTrue(isDomainStrict("domain1.com"));
        assertTrue(isDomainStrict("domain.local"));
        assertTrue(isDomainStrict("domain.localDomain"));
        assertFalse(isDomainStrict(null));
    }

    @Test
    public void test_validate_domain_strict(){
        assertEquals("validateDomainStrict(harisekhon.com)", "harisekhon.com", validateDomainStrict("harisekhon.com"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_domain_strict_exception() {
        validateDomainStrict("com");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_domain_strict_null_exception() {
        validateDomainStrict(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_domain_strict_blank_exception() {
        validateDomainStrict(" ");
    }

    // ====================================================================== //
    @Test
    public void test_isDnsShortName(){
        assertTrue(isDnsShortName("myHost"));
        assertFalse(isDnsShortName("myHost.domain.com"));
        assertFalse(isDnsShortName(null));
    }

    // ====================================================================== //
    @Test
    public void test_isEmail(){
        assertTrue(isEmail("hari'sekhon@gmail.com"));
        assertTrue(isEmail("hari@LOCALDOMAIN"));
        assertFalse(isEmail("harisekhon"));
        assertFalse(isEmail(null));
    }

    @Test
    public void test_validate_email(){
        assertEquals("validateEmail(hari@domain.com)", "hari@domain.com", validateEmail("hari@domain.com"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_email_exception() {
        validateEmail("harisekhon");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_email_null_exception() {
        validateEmail(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_email_blank_exception() {
        validateEmail(" ");
    }

    // ====================================================================== //
    @Test
    public void test_isFileName(){
        assertTrue(isFilename("some_File.txt"));
        assertTrue(isFilename("/tmp/test"));
        assertFalse(isFilename("@me"));
        assertFalse(isFilename(null));
    }

    @Test
    public void test_validate_filename(){
        assertEquals("validate_filename(./pom.xml)", "./pom.xml", validate_filename("./pom.xml"));
        assertEquals("validate_filename(/etc/passwd)", "/etc/passwd", validate_filename("/etc/passwd"));
        assertEquals("validate_filename(/etc/passwd, name)", "/etc/passwd", validate_filename("/etc/passwd", "name"));
        assertEquals("validate_filename(/nonexistentfile)", "/nonexistentfile", validate_filename("/nonexistentfile", "nonexistentfile", true));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_filename_exception() {
        validate_filename("@me");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_filename_null_exception() {
        validate_filename(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_filename_blank_exception() {
        validate_filename(" ");
    }

    @Test
    public void test_validate_file(){
        assertEquals("validate_file(./pom.xml)", "./pom.xml", validate_file("./pom.xml"));
        assertEquals("validate_file(./pom.xml)", "./pom.xml", validate_file("./pom.xml", "name"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_file_exception() {
        validate_file("/nonexistent");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_file_trailingslash_exception() {
        validate_filename("/etc/passwd/", "/etc/passwd/");
    }

    // ====================================================================== //
    @Test
    public void test_isFqdn(){
        assertTrue(isFqdn("hari.sekhon.com"));
        assertFalse(isFqdn("hari@harisekhon.com"));
        assertFalse(isFqdn(null));
    }

    @Test
    public void test_validate_fqdn(){
        assertEquals("validate_fqdn(www.harisekhon.com)", "www.harisekhon.com", validate_fqdn("www.harisekhon.com"));
        // permissive because of short tld style internal domains
        assertEquals("validate_fqdn(myhost.local, name)", "myhost.local", validate_fqdn("myhost.local", "name"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_fqdn_exception() {
        validate_fqdn("hari@harisekhon.com");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_fqdn_null_exception() {
        validate_fqdn(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_fqdn_blank_exception() {
        validate_fqdn(" ");
    }

    // ====================================================================== //
    @Test
    public void test_isHex(){
        assertTrue(isHex("0xAf09b"));
        assertFalse(isHex("9"));
        assertFalse(isHex("0xhari"));
        assertFalse(isHex(null));
    }

    // ====================================================================== //
    //@Test(timeout=1000)
    @Test
    public void test_isHost(){
        assertTrue(isHost("harisekhon.com"));
        assertTrue(isHost("harisekhon"));
        assertTrue(isHost("10.10.10.1"));
        assertTrue(isHost("10.10.10.10"));
        assertTrue(isHost("10.10.10.100"));
        assertTrue(isHost("10.10.10.0"));
        assertTrue(isHost("10.10.10.255"));
        assertTrue(isHost("ip-172-31-1-1"));
        assertFalse(isHost("10.10.10.256"));
        assertFalse(isHost(repeatString("a", 256)));
        assertFalse(isHost(null));
    }

    @Test
    public void test_validate_host(){
        assertEquals("validate_host(10.10.10.10)", "10.10.10.10", validate_host("10.10.10.10", "name"));
        assertEquals("validate_host(myHost)",      "myHost",      validate_host("myHost"));
        assertEquals("validate_host(myHost.myDomain.com)",  "myHost.myDomain.com",  validate_host("myHost.myDomain.com"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_host_exception() {
        validate_host("10.10.10.256");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_host_null_exception() {
        validate_host(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_host_blank_exception() {
        validate_host(" ");
    }

    @Test
    public void test_validate_hosts(){
        String[] a = {"node1:9200","node2:80","node3","node4","node5"};
        String[] b = {"node1:9200","node2:80","node3:8080","node4:8080","node5:8080"};
        assertArrayEquals("validate_hosts()", b, validate_hosts(a, "8080"));
        assertArrayEquals("validate_hosts()", b, validate_hosts(a, 8080));
        assertArrayEquals("validate_hosts()", b, arraylistToArray(validate_hosts(arrayToArraylist(a), "8080")));
        assertArrayEquals("validate_hosts()", b, arraylistToArray(validate_hosts(arrayToArraylist(a), 8080)));
        assertEquals("validate_hosts(myHost)",     "myHost:8080",     validate_hosts("myHost", 8080));
        assertEquals("validate_hosts(myHost)",     "myHost:8081,myHost2:9200",    validate_hosts("myHost,myHost2:9200", 8081));
        assertEquals("validate_hosts(myHost.myDomain.com)", "myHost.myDomain.com:8080", validate_hosts("myHost.myDomain.com", "8080"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_hosts_exception() {
        validate_hosts("10.10.10.254", "80800");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_hosts_exception2() {
        validate_hosts("10.10.10.256", "8080");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_hosts_emptyarray_exception() {
        validate_hosts(new String[]{}, 8080);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_hosts_emptyarraylist_exception() {
        validate_hosts(new ArrayList<String>(), 8080);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_hosts_blank_exception() {
        validate_hosts(" ", 8080);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_hosts_invalid_port_exception() {
        validate_hosts("10.10.10.10", 80000);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_hosts_invalid_hostport_exception() {
        validate_hosts("10.10.10.10:80000", 8000);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_hosts_null_exception() {
        String s = new String("");
        s = null;
        validate_hosts(s, 8000);
    }

    @Test
    public void test_validate_hostport(){
        assertEquals("validate_hostport(10.10.10.10:8080)", "10.10.10.10:8080", validate_hostport("10.10.10.10:8080", "name", true));
        assertEquals("validate_hostport(myHost)",      "myHost",      validate_hostport("myHost"));
        assertEquals("validate_hostport(myHost2)",     "myHost2",     validate_hostport("myHost2", "name2"));
        assertEquals("validate_hostport(myHost.myDomain.com)",  "myHost.myDomain.com",  validate_hostport("myHost.myDomain.com", "fqdn_host", false, true));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_hostport_exception() {
        validate_hostport("10.10.10.256:8080");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_hostport_exception2() {
        validate_hostport("10.10.10.10:80800");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_hostport_exception3() {
        validate_hostport("10.10.10.10:8080:8080");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_hostport_port_required_exception() {
        validate_hostport("10.10.10.10", "name", true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_hostport_null_exception() {
        validate_hostport(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_hostport_blank_exception() {
        validate_hostport(" ");
    }

    /*
    @Test
    public void test_validate_host_port_user_password(){
    }
    */

    // ====================================================================== //
    @Test
    public void test_isHostname(){
        assertTrue(isHostname("harisekhon.com"));
        assertTrue(isHostname("harisekhon"));
        assertTrue(isHostname("a"));
        assertTrue(isHostname("harisekhon1.com"));
        assertTrue(isHostname("1"));
        assertTrue(isHostname("1harisekhon.com"));
        assertTrue(isHostname(repeatString("a", 63)));
        assertFalse(isHostname(repeatString("a", 64)));
        assertFalse(isHostname("-help"));
        assertFalse(isHostname("hari~sekhon"));
        assertFalse(isHostname(null));
    }

    @Test
    public void test_validate_hostname(){
        assertEquals("validate_hostname(myHost)",      "myHost",      validate_hostname("myHost", "name"));
        assertEquals("validate_hostname(myHost.myDomain.com)",  "myHost.myDomain.com",  validate_hostname("myHost.myDomain.com"));
        assertEquals("validate_hostname(harisekhon1.com)",  "harisekhon1.com",  validate_hostname("harisekhon1.com"));
        assertEquals("validate_hostname(repeatString(a))", repeatString("a", 63),  validate_hostname(repeatString("a", 63)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_hostname_exception() {
        validate_hostname("hari~sekhon");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_hostname_null_exception() {
        validate_hostname(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_hostname_blank_exception() {
        validate_hostname(" ");
    }

    // ====================================================================== //
    @Test
    public void test_isInterface(){
        assertTrue(isInterface("eth0"));
        assertTrue(isInterface("bond3"));
        assertTrue(isInterface("lo"));
        assertTrue(isInterface("docker0"));
        assertTrue(isInterface("vethfa1b2c3"));
        assertFalse(isInterface("vethfa1b2z3"));
        assertFalse(isInterface("b@interface"));
        assertFalse(isInterface(null));
    }

    @Test
    public void test_validate_interface(){
        assertEquals("validate_interface(eth0)",  "eth0",  validate_interface("eth0"));
        assertEquals("validate_interface(bond3)", "bond3", validate_interface("bond3"));
        assertEquals("validate_interface(lo)",    "lo",    validate_interface("lo"));
        assertEquals("validate_interface(docker0)", "docker0", validate_interface("docker0"));
        assertEquals("validate_interface(vethfa1b2c3)",    "vethfa1b2c3",    validate_interface("vethfa1b2c3"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_interface_exception() {
        validate_interface("hvethfa1b2z3");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_interface_null_exception() {
        validate_interface(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_interface_blank_exception() {
        validate_interface(" ");
    }

    // ====================================================================== //
    @Test
    public void test_isIP(){
        assertTrue("isIP(10.10.10.1)",        isIP("10.10.10.1"));
        assertTrue("isIP(10.10.10.10)",       isIP("10.10.10.10"));
        assertTrue("isIP(10.10.10.100)",      isIP("10.10.10.100"));
        assertTrue("isIP(254.0.0.254)",       isIP("254.0.0.254"));
        assertTrue("isIP(255.255.255.254)",   isIP("255.255.255.254"));
        assertTrue("isIP(10.10.10.0)",        isIP("10.10.10.0"));
        assertTrue("isIP(10.10.10.255)",      isIP("10.10.10.255"));
        assertFalse("isIP(10.10.10.256)",     isIP("10.10.10.256"));
        assertFalse("isIP(x.x.x.x)",          isIP("x.x.x.x"));
        assertFalse("isIP(null)",             isIP(null));
    }

    @Test
    public void test_validate_ip(){
        assertEquals("validate_ip(validate_ip(10.10.10.1)",     "10.10.10.1",   validate_ip("10.10.10.1", "name"));
        assertEquals("validate_ip(validate_ip(10.10.10.10)",    "10.10.10.10",  validate_ip("10.10.10.10"));
        assertEquals("validate_ip(validate_ip(10.10.10.100)",   "10.10.10.100", validate_ip("10.10.10.100"));
        assertEquals("validate_ip(validate_ip(10.10.10.254)",   "10.10.10.254", validate_ip("10.10.10.254"));
        assertEquals("validate_ip(validate_ip(10.10.10.255)",   "10.10.10.255", validate_ip("10.10.10.255"));
        assertEquals("validate_ip(validate_ip(254.0.0.254)",    "254.0.0.254",  validate_ip("254.0.0.254"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_ip_exception() {
        validate_ip("10.10.10.256");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_ip_null_exception() {
        validate_ip(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_ip_blank_exception() {
        validate_ip(" ");
    }

    // ====================================================================== //
    @Test
    public void test_isKrb5Princ(){
        assertTrue(isKrb5Princ("tgt/HARI.COM@HARI.COM"));
        assertTrue(isKrb5Princ("hari"));
        assertTrue(isKrb5Princ("hari@HARI.COM"));
        assertTrue(isKrb5Princ("hari/my.host.local@HARI.COM"));
        assertTrue(isKrb5Princ("cloudera-scm/admin@REALM.COM"));
        assertTrue(isKrb5Princ("cloudera-scm/admin@SUB.REALM.COM"));
        assertTrue(isKrb5Princ("hari@hari.com"));
        assertFalse(isKrb5Princ("hari$HARI.COM"));
        assertFalse(isKrb5Princ(null));
    }

    @Test
    public void test_validate_krb5_princ(){
        assertEquals("validate_krb5_princ(tgt/HARI.COM@HARI.COM)", "tgt/HARI.COM@HARI.COM", validate_krb5_princ("tgt/HARI.COM@HARI.COM", "name"));
        assertEquals("validate_krb5_princ(hari)", "hari", validate_krb5_princ("hari"));
        assertEquals("validate_krb5_princ(hari@HARI.COM)", "hari@HARI.COM", validate_krb5_princ("hari@HARI.COM"));
        assertEquals("validate_krb5_princ(hari/my.host.local@HARI.COM)", "hari/my.host.local@HARI.COM", validate_krb5_princ("hari/my.host.local@HARI.COM"));
        assertEquals("validate_krb5_princ(cloudera-scm/admin@REALM.COM)", "cloudera-scm/admin@REALM.COM", validate_krb5_princ("cloudera-scm/admin@REALM.COM"));
        assertEquals("validate_krb5_princ(cloudera-scm/admin@SUB.REALM.COM)", "cloudera-scm/admin@SUB.REALM.COM", validate_krb5_princ("cloudera-scm/admin@SUB.REALM.COM"));
        assertEquals("validate_krb5_princ(hari@hari.com)", "hari@hari.com", validate_krb5_princ("hari@hari.com"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_krb5_princ_exception() {
        validate_krb5_princ("hari$HARI.COM");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_krb5_princ_null_exception() {
        validate_krb5_princ(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_krb5_princ_blank_exception() {
        validate_krb5_princ(" ");
    }

    // ====================================================================== //

    @Test
    public void test_validate_krb5_realm(){
        assertEquals("validate_krb5_realm(harisekhon.com)", "harisekhon.com", validate_krb5_realm("harisekhon.com"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_krb5_realm_exception() {
        validate_krb5_realm("hari$HARI.COM");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_krb5_realm_null_exception() {
        validate_krb5_realm(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_krb5_realm_blank_exception() {
        validate_krb5_realm(" ");
    }

    // ====================================================================== //
    @Test
    public void test_isLabel(){
        assertTrue(isLabel("st4ts used_(%%)"));
        assertFalse(isLabel("b@dlabel"));
        assertFalse(isLabel(" "));
        assertFalse(isLabel(null));
    }

    @Test
    public void test_validate_label(){
        assertEquals("validate_label(st4ts_used (%%))", "st4ts_used (%%)", validate_label("st4ts_used (%%)"));
        assertEquals("validate_label(st4ts_used (%%))", "st4ts_used (%%)", validate_label("st4ts_used (%%)"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_label_exception() {
        validate_label("b@dlabel");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_label_null_exception() {
        validate_label(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_label_blank_exception() {
        validate_label(" ");
    }

    // ====================================================================== //
    @Test
    public void test_isLdapDn(){
        assertTrue(isLdapDn("uid=hari,cn=users,cn=accounts,dc=local"));
        assertFalse(isLdapDn("hari@LOCAL"));
    }

    @Test
    public void test_validate_ldap_dn(){
        assertEquals("validate_ldap_dn(uid=hari,cn=users,cn=accounts,dc=local)", "uid=hari,cn=users,cn=accounts,dc=local", validate_ldap_dn("uid=hari,cn=users,cn=accounts,dc=local"));
        assertEquals("validate_ldap_dn(uid=hari,cn=users,cn=accounts,dc=local, name)", "uid=hari,cn=users,cn=accounts,dc=local", validate_ldap_dn("uid=hari,cn=users,cn=accounts,dc=local", "name"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_ldap_dn_exception() {
        validate_ldap_dn("hari@LOCAL");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_ldap_dn_null_exception() {
        validate_ldap_dn(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_ldap_dn_blank_exception() {
        validate_ldap_dn(" ");
    }

    // ====================================================================== //

    @Test
    public void test_isMinVersion(){
        assertTrue(isMinVersion("1.3.0", 1.3));
        assertTrue(isMinVersion("1.3.0-alpha", 1.3));
        assertTrue(isMinVersion("1.3", 1.3));
        assertTrue(isMinVersion("1.4", 1.3));
        assertTrue(isMinVersion("1.3.1", 1.2));
        assertFalse(isMinVersion("1.3.1", 1.4));
        assertFalse(isMinVersion("1.2.99", 1.3));
        assertFalse(isMinVersion("1.3", null));
        assertFalse(isMinVersion("hari", 1.3));
        assertFalse(isMinVersion(null, 1.3));
    }

    // ====================================================================== //
    @Test
    public void test_isNagiosUnit(){
        assertTrue(isNagiosUnit("s"));
        assertTrue(isNagiosUnit("ms"));
        assertTrue(isNagiosUnit("us"));
        assertTrue(isNagiosUnit("b"));
        assertTrue(isNagiosUnit("Kb"));
        assertTrue(isNagiosUnit("Mb"));
        assertTrue(isNagiosUnit("Gb"));
        assertTrue(isNagiosUnit("Tb"));
        assertTrue(isNagiosUnit("c"));
        assertTrue(isNagiosUnit("%"));
        assertFalse(isNagiosUnit("Kbps"));
        assertFalse(isNagiosUnit(null));
    }

    @Test
    public void test_validate_units(){
        assertEquals("validate_units(s)",   "s",    validate_units("s", "name"));
        assertEquals("validate_units(ms)",  "ms",   validate_units("ms"));
        assertEquals("validate_units(us)",  "us",   validate_units("us"));
        assertEquals("validate_units(B)",   "B",    validate_units("B"));
        assertEquals("validate_units(KB)",  "KB",   validate_units("KB"));
        assertEquals("validate_units(MB)",  "MB",   validate_units("MB"));
        assertEquals("validate_units(GB)",  "GB",   validate_units("GB"));
        assertEquals("validate_units(TB)",  "TB",   validate_units("TB"));
        assertEquals("validate_units(c)",   "c",    validate_units("c"));
        assertEquals("validate_units(%%)",  "%",    validate_units("%"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_units_exception() {
        validate_units("Kbps");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_units_null_exception() {
        validate_units(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_units_blank_exception() {
        validate_units(" ");
    }

    // ====================================================================== //

    @Test
    public void test_validate_node_list(){
        assertEquals("validate_node_list(String)", "node1,node2,node3,node4,node5", validate_node_list("node1 ,node2 node3  node4, node5"));
        String[] a = {"node1","node2","node3","node4","node5"};
        assertArrayEquals("validate_node_list(ArrayList<String>)",  arraylistToArray(new ArrayList<String>(Arrays.asList(a))), arraylistToArray(validate_node_list(arrayToArraylist(a))));
        assertArrayEquals("validate_node_list(String[])",  a, validate_node_list(a));
    }

    @Test
    public void test_validate_nodeport_list(){
        assertEquals("validate_nodeport_list(String)", "node1:9200,node2,node3:8080,node4,node5", validate_nodeport_list("node1:9200 ,node2 node3:8080 node4, node5"));
        String[] a = {"node1:9200","node2","node3:8080","node4","node5"};
        assertArrayEquals("validate_nodeport_list(ArrayList<String>)", arraylistToArray(new ArrayList<String>(Arrays.asList(a))), arraylistToArray(validate_nodeport_list(arrayToArraylist(a))));
        assertArrayEquals("validate_nodeport_list(String[])", a, validate_nodeport_list(a));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_node_list_exception() {
        validate_node_list("bad~host");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_node_list_empty_exception() {
        validate_node_list(new ArrayList<String>());
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_node_list_emptyfinal_exception() {
        ArrayList<String> a = new ArrayList<String>();
        a.add(" ");
        validate_node_list(a);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_node_list_null_exception() {
        String n = null;
        validate_node_list(n);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_node_list_blank_exception() {
        validate_node_list(" ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_nodeport_list_exception() {
        validate_nodeport_list("bad@host");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_nodeport_list_empty_exception() {
        ArrayList<String> a = new ArrayList<String>();
        validate_nodeport_list(a);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_nodeport_list_nullstring_exception() {
        String n = null;
        validate_nodeport_list(n);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_nodeport_list_emptystring_exception() {
        validate_nodeport_list(" ");
    }

    // ====================================================================== //
    @Test
    public void test_isNoSqlKey(){
        assertTrue(isNoSqlKey("HariSekhon:check_riak_write.pl:riak1:1385226607.02182:20abc"));
        assertFalse(isNoSqlKey("HariSekhon@check_riak_write.pl"));
        assertFalse(isNoSqlKey(null));
    }

    @Test
    public void test_validate_nosql_key(){
        assertEquals("validate_nosql_key(HariSekhon:check_riak_write.pl:riak1:1385226607.02182:20abc)", "HariSekhon:check_riak_write.pl:riak1:1385226607.02182:20abc", validate_nosql_key("HariSekhon:check_riak_write.pl:riak1:1385226607.02182:20abc"));
        assertEquals("validate_nosql_key(HariSekhon:check_riak_write.pl:riak1:1385226607.02182:20abc, name)", "HariSekhon:check_riak_write.pl:riak1:1385226607.02182:20abc", validate_nosql_key("HariSekhon:check_riak_write.pl:riak1:1385226607.02182:20abc", "name"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_nosql_key_exception() {
        validate_nosql_key("HariSekhon@check_riak_write.pl");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_nosql_key_null_exception() {
        validate_nosql_key(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_nosql_key_blank_exception() {
        validate_nosql_key(" ");
    }

    // ====================================================================== //
    @Test
    public void test_isPort(){
        assertTrue(isPort("80"));
        assertTrue(isPort(80));
        assertTrue(isPort(65535));
        assertFalse(isPort(65536));
        assertFalse(isPort("a"));
        assertFalse(isPort("-1"));
        assertFalse(isPort("0"));
        assertFalse(isPort(-1));
        assertFalse(isPort(0));
        assertFalse(isPort(null));
    }

    @Test
    public void test_validate_port(){
        assertEquals("validate_port(1)",     1,         validate_port(1, "name"));
        assertEquals("validate_port(80)",    80,        validate_port(80));
        assertEquals("validate_port(65535)", 65535,     validate_port(65535));
        assertEquals("validate_port(1)",     "1",       validate_port("1"));
        assertEquals("validate_port(80)",    "80",      validate_port("80"));
        assertEquals("validate_port(65535)", "65535" ,  validate_port("65535"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_port_exception() {
        validate_port(65536);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_parse_port_exception() {
        parse_port("test");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_parse_port_parse_exception() {
        validate_port("test");
    }

    // ====================================================================== //
    @Test
    public void test_isProcessName(){
        assertTrue(isProcessName("../my_program"));
        assertTrue(isProcessName("ec2-run-instances"));
        assertTrue(isProcessName("sh <defunct>"));
        assertFalse(isProcessName("./b@dfile"));
        assertFalse(isProcessName("[init] 3"));
        assertFalse(isProcessName(null));
    }

    @Test
    public void test_validate_process_name(){
        assertEquals("validate_process_name(../my_program)", "../my_program", validate_process_name("../my_program", "name"));
        assertEquals("validate_process_name(ec2-run-instances)", "ec2-run-instances", validate_process_name("ec2-run-instances"));
        assertEquals("validate_process_name(sh <defunct>)", "sh <defunct>", validate_process_name("sh <defunct>"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_process_exception() {
        validate_process_name("./b@dfile");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_process_null_exception() {
        validate_process_name(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_process_blank_exception() {
        validate_process_name(" ");
    }

    // ====================================================================== //
    @Test
    public void test_validate_program_path(){
        if(isLinuxOrMac()){
            assertEquals("validate_program_path()", "/bin/sh", validate_program_path("/bin/sh", "sh"));
            assertEquals("validate_program_path()", "/bin/sh", validate_program_path("/bin/sh", "shell", "sh"));
            validate_program_path("/bin/sh", "sh", ".*/sh$");
            validate_program_path("/bin/sh", "sh", "/bin/sh$");
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_program_path_exception() {
        validate_program_path("/bin/sh-nonexistent", "sh-nonexistent");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_program_path_dir_exception() {
        validate_program_path("/bin", "/bin");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_program_path_nonexecutable_exception() {
        // one of my colleagues has had an executable /etc/hosts (probably chmod'd everything 777 to try to get stuff working)
        // so now instead will make this a bit more resilient for such things
        String path = "/etc/hosts";
        File f = new File(path);
        if(!f.canExecute()) {
            validate_program_path(path, path);
        } else {
            // your /etc/hosts has been set as executable, so bypassing test in locally flawed environment
            throw new IllegalArgumentException();
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_program_path_invalidregex_exception() {
        validate_program_path("/bin/sh", "sh", "(.*");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_program_path_nomatchregex_exception() {
        validate_program_path("/bin/sh", "sh", ".*/bash$");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_program_path_nullname_exception() {
        validate_program_path("/bin/sh", null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_program_path_null_exception() {
        validate_program_path(null, "null");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_program_path_blank_exception() {
        validate_program_path(" ", "blank");
    }

    // ====================================================================== //
    @Test
    public void test_isRegex(){
        assertTrue(isRegex(".*"));
        assertTrue(isRegex("(.*)"));
        assertFalse(isRegex("(.*"));
        assertFalse(isRegex(null));
    }

    @Test
    public void test_validate_regex(){
        assertEquals("validate_regex(some[Rr]egex.*(capture))", "some[Rr]egex.*(capture)",  validate_regex("some[Rr]egex.*(capture)", "myRegex"));
        assertEquals("validate_regex(some[Rr]egex.*(capture), null, true)", "some[Rr]egex.*(capture)",  validate_regex("some[Rr]egex.*(capture)", null, true));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_regex_exception() {
        validate_regex("(.*", "myString");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_regex_posixshellescapes_exception() {
        validate_regex("$(badcommand)", "myString", true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_regex_posixshellescapes2_exception() {
        validate_regex("`badcommand`", "myString", true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_regex_null_exception() {
        validate_regex(null, "myString");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_regex_blank_exception() {
        validate_regex(" ", "myString");
    }

    // ====================================================================== //
    @Test
    public void test_isUrl(){
        assertTrue(isUrl("www.google.com"));
        assertTrue(isUrl("http://www.google.com"));
        assertTrue(isUrl("https://gmail.com"));
        assertTrue(isUrl("1"));
        assertTrue(isUrl("http://cdh43:50070/dfsnodelist.jsp?whatNodes=LIVE"));
        assertFalse(isUrl("-help"));
        assertFalse(isUrl(null));
    }

    @Test
    public void test_validate_url(){
        assertEquals("validate_url(www.google.com)",        "http://www.google.com", validate_url(" www.google.com ", "name"));
        assertEquals("validate_url(http://www.google.com)", "http://www.google.com", validate_url(" http://www.google.com "));
        assertEquals("validate_url(http://gmail.com)",      "http://gmail.com",      validate_url(" http://gmail.com "));
        assertEquals("validate_url(http://cdh43:50070/dfsnodelist.jsp?whatNodes=LIVE)",      "http://cdh43:50070/dfsnodelist.jsp?whatNodes=LIVE",      validate_url(" http://cdh43:50070/dfsnodelist.jsp?whatNodes=LIVE "));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_url_exception() {
        validate_url("-help");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_url_null_exception() {
        validate_url(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_url_blank_exception() {
        validate_url(" ");
    }

    // ====================================================================== //
    @Test
    public void test_isUrlPathSuffix(){
        assertTrue(isUrlPathSuffix("/"));
        assertTrue(isUrlPathSuffix("/?var=something"));
        assertTrue(isUrlPathSuffix("/dir1/file.php?var=something+else&var2=more%20stuff"));
        assertTrue(isUrlPathSuffix("/*"));
        assertTrue(isUrlPathSuffix("/~hari"));
        assertFalse(isUrlPathSuffix("hari"));
        assertFalse(isUrlPathSuffix(null));
    }

    @Test
    public void test_validate_url_path_suffix(){
        assertEquals("validate_url_path_suffix(/)", "/", validate_url_path_suffix("/", "name"));
        assertEquals("validate_url_path_suffix(/?var=something)", "/?var=something", validate_url_path_suffix("/?var=something"));
        assertEquals("validate_url_path_suffix(/dir1/file.php?var=something+else&var2=more%20stuff)", "/dir1/file.php?var=something+else&var2=more%20stuff", validate_url_path_suffix("/dir1/file.php?var=something+else&var2=more%20stuff"));
        assertEquals("validate_url_path_suffix(/*)", "/*", validate_url_path_suffix("/*"));
        assertEquals("validate_url_path_suffix(/~hari)", "/~hari", validate_url_path_suffix("/~hari"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_url_path_suffix_exception() {
        validate_url_path_suffix("hari");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_url_path_suffix_blank_exception() {
        validate_url_path_suffix(" ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_url_path_suffix_null_exception() {
        validate_url_path_suffix(null);
    }

    // ====================================================================== //
    @Test
    public void test_isUser(){
        assertTrue(isUser("hadoop"));
        assertTrue(isUser("hari1"));
        assertTrue(isUser("mysql_test"));
        assertTrue(isUser("cloudera-scm"));
        assertTrue(isUser("nonexistentuser"));
        assertFalse(isUser("-hari"));
        assertFalse(isUser("1hari"));
        assertTrue(isUser("null"));
        assertFalse(isUser(null));
    }

    @Test
    public void test_validate_user(){
        assertEquals("validate_user(hadoop, name)", "hadoop", validate_user("hadoop", "name"));
        assertEquals("validate_user(hari1)", "hari1", validate_user("hari1"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_user_exception() {
        validate_user("-hari");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_user_exception2() {
        validate_user("1hari");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_user_null_exception() {
        validate_user(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_user_blank_exception2() {
        validate_user(" ");
    }

    // ====================================================================== //

    @Test
    public void test_validate_user_exists() throws IOException {
        if(isLinuxOrMac()){
            assertEquals("validate_user_exists(root)", "root", validate_user_exists("root", "root"));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_user_exists_exception() throws IOException {
        if(isLinuxOrMac()) {
            validate_user_exists("nonexistentuser", "nonexistentuser");
        }
    }

    // ====================================================================== //
    @Test
    public void test_isVersion(){
        assertTrue(isVersion("1"));
        assertTrue(isVersion("2.1.2"));
        assertTrue(isVersion("2.2.0.4"));
        assertTrue(isVersion("3.0"));
        assertFalse(isVersion("a"));
        assertFalse(isVersion("3a"));
        assertFalse(isVersion("1.0-2"));
        assertFalse(isVersion("1.0-a"));
        assertFalse(isVersion(null));
    }

    @Test
    public void test_isVersionLax(){
        assertTrue(isVersionLax("1"));
        assertTrue(isVersionLax("2.1.2"));
            assertTrue(isVersionLax("2.2.0.4"));
        assertTrue(isVersionLax("3.0"));
        assertFalse(isVersionLax("a"));
        assertTrue(isVersionLax("3a"));
        assertTrue(isVersionLax("1.0-2"));
        assertTrue(isVersionLax("1.0-a"));
        assertFalse(isVersionLax("hari"));
        assertFalse(isVersionLax(null));
    }

    // ====================================================================== //

    @Test
    public void test_validate_database_query_select_show(){
        assertEquals("validateDatabaseQuerySelectShow(SELECT count(*) from database.table)", "SELECT count(*) from database.table;", validateDatabaseQuerySelectShow("SELECT count(*) from database.table;"));
        assertEquals("validateDatabaseQuerySelectShow(select count(*) from database.created_date)", "select count(*) from database.created_date", validateDatabaseQuerySelectShow("select count(*) from database.created_date"));
        assertEquals("validateDatabaseQuerySelectShow(SELECT count(*) from product_updates)", "SELECT count(*) from product_updates", validateDatabaseQuerySelectShow("SELECT count(*) from product_updates"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_query_select_show_delete_exception() {
        validateDatabaseQuerySelectShow("DELETE FROM myTable;");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_query_select_show_drop_exception() {
        validateDatabaseQuerySelectShow("select * from (DROP myTable);");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_query_select_show_null_exception() {
        validateDatabaseQuerySelectShow(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_database_query_select_show_blank_exception() {
        validateDatabaseQuerySelectShow(" ");
    }

    // ====================================================================== //

    @Test
    public void test_validate_password(){
        assertEquals("validate_password(wh@tev3r)", "wh@tev3r", validate_password("wh@tev3r"));
        assertEquals("validate_password(wh@tev3r)", "wh@tev3r", validate_password("wh@tev3r", "name"));
        assertEquals("validate_password(wh@tev3r)", "$(badcommand)", validate_password("$(badcommand)", "name", true));
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_password_exception() {
        validate_password("`badcommand`");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_password_exception2() {
        validate_password("$(badcommand)");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_password_null_exception() {
        validate_password(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_validate_password_blank_exception() {
        validate_password(" ");
    }

    // ====================================================================== //

    @Test
    public void test_get_calling_method(){
        assertEquals("get_calling_method()", "sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)", get_calling_method());
    }

    // ====================================================================== //

    @Test
    public void test_validate_vlog(){
        vlog("vlog");
        vlog("vlog2");
        vlog("vlog3");
        vlog(null);
        vlog(null);
        vlog(null);
        vlog_option("vlog_option", "myOpt");
        vlog_option("vlog_option", true);
        vlog_option("vlog_option", false);
        vlog_option(null, "myOpt");
        vlog_option(null, false);
    }

    // ====================================================================== //

    @Test
    public void test_user_exists() throws IOException {
        assertTrue(userExists("root"));
        assertFalse(userExists("nonexistent"));
        assertFalse(userExists("b@d"));
        assertFalse(userExists(" "));
        assertFalse(userExists(null));
    }

    @Test
    public void test_verbose(){
        setVerbose(2);
//        assertEquals("getVerbose() 2", 2, getVerbose());
        setVerbose(1);
//        assertEquals("getVerbose() 1", 1, getVerbose());
        setVerbose(3);
//        assertEquals("getVerbose() 3", 3, getVerbose());
        setVerbose(4);
        setVerbose(5);
        setVerbose(-1);
        setVerbose(0);
        setVerbose(2);
    }


    @Test
    public void test_version(){
        version();
    }

    @Test
    public void test_getVersion(){
        getVersion();
    }

    // ====================================================================== //    
    @Test
    public void test_validate_which() throws IOException {
        if(isLinuxOrMac()){
            assertEquals("which(sh)",                           "/bin/sh",      which("sh"));
            assertEquals("which(/bin/bash)",                    "/bin/bash",    which("/bin/bash"));
        }
    }

    @Test(expected=IOException.class)
    public void test_which_nonexecutable_exception() throws IOException {
//        if(isLinuxOrMac()) {
            which("/etc/resolv.conf");
//        }
    }

    @Test(expected=IOException.class)
    public void test_which_nonexistent_exception() throws IOException {
        which("nonexistentprogram");
    }

    @Test(expected=IOException.class)
    public void test_which_fullpath_nonexistent_exception() throws IOException {
        which("/explicit/nonexistent/path");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_which_null_exception() throws IOException {
        which(null);
    }

    // ====================================================================== //
    /*
    @Test
    public void test_sec2min(){
        assertEquals("sec2min(65)", "1:05", sec2min(65));
        assertEquals("sec2min(30)", "0:30",   sec2min(30));
        assertEquals("sec2min(3601)", "60:01", sec2min(3601));
        assertEquals("sec2min(0)", "0:00", sec2min(0));
        
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void test_sec2min_exception_negative(){
        sec2min("-1");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void test_sec2min_exception_nonnumeric(){
        sec2min("aa");
    }
     */
    
    // ====================================================================== //
    
    // @Test(expected= IndexOutOfBoundsException.class)
    // public void test_validate_* {
    // change validate_* to throwing exceptions for better testing and wrap in a generic exception catcher in Nagios code to convert to one liners
    
    //@Test
    // public void test_validate* {
    //      try {
    //          ...
    //          fail("Expected a BlahException to be thrown");
    //      } catch (BlahException e) {
    //          assertThat(e.getMessage(), is("some message"));
    //      }
    // }
    
    //@Ignore("Test disabled, re-enable later")
    // or use this more flexible one which allows use of matchers such as .containsString()
    //@Rule
    //public ExpectedException thrown = ExpectedException.none();
    
    //@Ignore("Test is ignored as a demonstration")
    //@Test
    //public void validate_*() throw IndexOutOfBoundsException {
    //  thrown.expect(IndexOutOfBoundsException.class);
    //  thrown.expectMessage("exact msg");
    //  thrown.expectMessage(JUnitMatchers.containsString("partial msg"));
    //  // call code to generate exception
    //}
}
