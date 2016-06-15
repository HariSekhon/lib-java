//
//  Author: Hari Sekhon
//  Date: 2014-09-15 20:49:22 +0100 (Mon, 15 Sep 2014)
//
//  https://github.com/harisekhon/lib-java
//
//  License: see accompanying Hari Sekhon LICENSE file
//
//  If you"re using my code you"re welcome to connect with me on LinkedIn and optionally send me feedback
//  to help improve or steer this or other code I publish
//
//  https://www.linkedin.com/in/harisekhon
//

package com.linkedin.harisekhon;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import java.util.HashMap;

public class CLITest {

    CLI cli;
    HashMap myDict = new HashMap<String, Integer>();

    class SubCLI extends CLI {
        void run() {
            System.out.println("running SubCLI()");
        }
    }

    @Before
    public void setUp() {
        myDict.put("one", 1);
        myDict.put("two", 2);
        myDict.put("three", 3);
        cli = new SubCLI();
    }

//    set_default_port(80)
    // try {
    //     cli.set_default_port("a")
    //     throw new  Exception("failed to throw CodingError when sending invalid port to set_default_port()")
    // } catch( CodingError {
    //     pass

//        public void test_add_hostoption() {
//            cli.add_hostoption(name = "Ambari", default_host = "localhost", default_port = 8080);
//        }
//
//        public void test_add_useroption() {
//            cli.add_useroption(name = "Ambari", default_user = "admin", default_password = "mysecret");
//        }

//        public void test_add_hostoption_dup_Exception() {
//            try {
//                cli.add_hostoption();
//                cli.add_hostoption();
//                throw new RuntimeException("failed to throw IllegalStateException when duplicating add_hostoption()");
//            } catch (IllegalStateException e) {
//                 pass
//            }
//        }

//    public void test_add_hostoption_port_error_Exception() {
//        try {
//            cli.add_hostoption(default_port = "error");
//            throw new RuntimeException("failed to throw CodingError when sending invalid port to add_hostoption");
//        } catch (IllegalArgumentException e) {
//            pass
//        }
//    }
//
//    public void test_useroption_dup_Exception() {
//        try {
//            cli.add_useroption();
//            cli.add_useroption();
//            throw new RuntimeException("failed to throw OptionConflictError from optparse OptionParser when duplicating add_useroption");
//        } catch (IllegalStateException e) {
//             pass
//        }
//    }

    @Test
    // set_verbose() won't work because parse args resets it so we change verbose_default instead
    public void test_verbose_default_setters_getters() {
        cli.setVerbose_default(2);
        assertEquals("cli.getVerbose_default", 2, cli.getVerbose_default());
        cli.main();
    }

    @Test
    public void test_reinit_set_verbose() {
        cli.setVerbose(0);
        assertEquals("cli.getVerbose()", 0, cli.getVerbose());
    }

//        void test_usage() {
//            cli.__init__();
//            try {
//                cli.usage();
//                throw new Exception("failed to exit on CLI.usage()");
//            } catch(IllegalArgumentException e) {
//                if(e.code != 3){
//                    throw new Exception("wrong exit code %s != 3 when exiting usage() from base class CLI" % _.code)
//                }
//            }
//        }

    @Test(expected=IllegalArgumentException.class)
    public void test_usage_message() {
        cli.usage("test message");
    }

    //void test_parser_version() {
    //    print("parser version = %s" % cli.__parser.get_version())
    // I don"t populate version in OptionParser now as it creates the switch too high in the option order
    // assertTrue(re.search(" version (? {None|%(version_regex)s), CLI version %(version_regex)s, " +
    //                           "Utils version %(version_regex)s" % globals(), cli.__parser.get_version()))

    @Test(expected=IllegalArgumentException.class)
    public void test_set_timeout_max_set_timeout_Exception() {
        cli.setTimeout_max(5);
        cli.setTimeout(6);
    }

//    @Test
//    public void test_set_timeout_default() {
//        cli = SubCLI();
//        cli.setTimeout_max(null);
//        cli.setTimeout_default(999999);
//        cli.timeout_default(9);
//        assertEquals("cli.setTimeout_default(9)", 9, cli.setTimeout_default(9));
//        cli.setTimeout_default(null);
//    }

    @Test(expected=IllegalArgumentException.class)
    public void test_set_timeout_max_set_timeout_default_Exception() {
        cli.setTimeout_max(10);
        cli.setTimeout_default(11);
    }

//    @Test
//    public void test_timeout_default_max_normal() {
//        cli = SubCLI();
//        cli.setTimeout_default(null);
//        assertEquals(cli.getTimeout_default, null);
//        cli.setTimeout_max(30);
//        cli.setTimeout(22);
//        assertEquals(22, cli.getTimeout);
//        cli.main();
//    }

//    public void test_timeout_default_sleep_Exception() {
//        cli.setTimeout_default(1);
//            cli.run = lambda { time.sleep(3);
//        try {
//            cli.main();
//            throw new RuntimeException("failed to self-timeout after 1 second");
//        } catch(IllegalArgumentException e){
//               if(e.code != 3){
//                    throw new RuntimeException("wrong exit code != 3 when self timing out CLI");
//                }
//        }
//    }
}
