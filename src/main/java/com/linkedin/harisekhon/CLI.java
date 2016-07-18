//  vim:ts=4:sts=4:sw=4:et
//
//  Author: Hari Sekhon
//  Date: 2014-09-15 20:49:22 +0100 (Mon, 15 Sep 2014)
//
//  https://github.com/harisekhon/lib-java
//
//  Port of Python version from https://github.com/harisekhon/pylib repo
//
//  License: see accompanying Hari Sekhon LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback
//  to help improve or steer this or other code I publish
//
//  https://www.linkedin.com/in/harisekhon
//

package com.linkedin.harisekhon;

import static com.linkedin.harisekhon.Utils.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
//import java.util.UnknownFormatConversionException;
import org.apache.commons.cli.*;
// 1.3+ API causes problems with Spark, use older API for commons-cli
//import org.apache.commons.cli.DefaultParser;
//import org.apache.commons.cli.Option;

public class CLI {

    private boolean debug = false;
    private int verbose = 0;
    private int verbose_default = 0;
    private int timeout = 0;
    private int timeout_default = 10;
    private int timeout_max = 86400;
    private String usage_msg = "usage: <prog> <options>";
    protected Options options = new Options();
    private CommandLine cmd;

    public final Logger log = Logger.getLogger(com.linkedin.harisekhon.Utils.class.getName());

    public CLI() {
        // 1.3+ API doesn't work in Spark which embeds older commons-cli
        // so have to use this older static Builder style, which is actually more horrible in Scala
        // which doesn't allow static method chaining
//        options.addOption(OptionBuilder.withLongOpt("timeout")
//                .hasArg()
//                .withArgName("secs")
//                .withDescription("Timeout for program (Optional)")
//                .create("t"));
//        options.addOption(OptionBuilder.withLongOpt("debug")
//                .withDescription("Debug mode (Optional)")
//                .create("D"));
//        options.addOption("v", "verbose", false, "Verbose mode");
//        options.addOption("h", "help", false, "Print usage help and exit");
        options.addOption("t", "timeout", true, String.format("Timeout in secs (default: %s)", timeout_default));
        options.addOption("D", "debug", false, "Debug mode");
        options.addOption("v", "verbose", false, "Verbose mode (-v, -vv, -vvv)");
//        options.addOption("V", "version", false, "Print version and exit");
    }

    // TODO: add logic for default host + port support
    public final void addHostOption(String name, String default_host, String default_port) {
        String name2 = "";
        if (name != null) {
            name2 = name + " ";
        }
//        if default_port is not None:
//            // assert isPort(default_port)
//            if not isPort(default_port):
//                raise CodingError("invalid default port supplied to add_hostoption()")
//        (host_envs, default_host) = getenvs2("HOST", default_host, name)
//        (port_envs, default_port) = getenvs2("PORT", default_port, name)
//        self.add_opt("-H", "--host", dest="host", help="%sHost (%s)" % (name2, host_envs), default=default_host)
//        self.add_opt("-P", "--port", dest="port", help="%sPort (%s)" % (name2, port_envs), default=default_port)

        options.addOption("H", "host", true, "Host ($HOST)");
        options.addOption("P", "port", true, "Port ($PORT)");
    }
    public final void addHostOption() {
        addHostOption(null, null, null);
    }

    public final void addUserOption(String name, String default_user, String default_password) {
        String name2 = "";
        if (name != null) {
            name2 = name + " ";
        }
//        (user_envs, default_user) = getenvs2(["USERNAME", "USER"], default_user, name)
//        (pw_envs, default_password) = getenvs2("PASSWORD", default_password, name)
//        self.add_opt("-u", "--user", dest="user", help="%sUsername (%s)" % (name2, user_envs), default=default_user)
//        self.add_opt("-p", "--password", dest="password", help="%sPassword (%s)" % (name2, pw_envs),
//                     default=default_password)
        options.addOption("u", "user", true, "Username");
        options.addOption("p", "password", true, "Password");
    }
    public final void addUserOption(){
        addUserOption(null, null, null);
    }

    public void setup(){
    }

    public static void main(String[] args) {
        CLI c = new CLI();
        c.main2(args);
    }

    public final void main2(String[] args){
        log.debug("running main()");
        log.setLevel(Level.DEBUG);
        setup();
        try {
//            addOptions();
//            add_default_opts();
        } catch (IllegalArgumentException e) {
            usage(e);
        }
        try {
            parseArgs2(args);
//            autoflush();
            if(verbose > 2) {
                log.setLevel(Level.DEBUG);
            } else if(verbose > 1){
                log.setLevel(Level.INFO);
            }
            if(debug){
                log.setLevel(Level.DEBUG);
            }
        } catch(Exception e){
            if(log.isDebugEnabled()){
                e.printStackTrace();
            }
            usage(e.getMessage());
        }
//        log.info("verbose level: %s".format(verbose));
        validate_int(timeout, "timeout", 0, timeout_max);
//        log.debug("setting timeout to {} secs", timeout);
        Thread t = new Thread(new Timeout(timeout));
        t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if(e instanceof QuitException){
                    println(((QuitException) e).status + ": " + ((QuitException) e).message);
                } else {
                    println(e.getMessage());
                }
                System.exit(getStatusCode("UNKNOWN"));
            }
        });
        t.start();
//        if(version){
//            println(version_string);
//            System.exit(exit_codes.get("UNKNOWN"));
//        }
        try {
            processArgs();
            run();
            end();
        } catch (IllegalArgumentException e){
            if(log.isDebugEnabled()){
                e.printStackTrace();
            }
            usage(e.getMessage());
        // not thrown by try block, how is Control-C thrown?
//        } catch (InterruptedException e){
//            System.out.println("Caught control-c...");
//            System.exit(getStatusCode("UNKNOWN"));
        }
    }

    // can't make this abstract as conflicts with static
    public void run() {
    }

    public void end(){
    }

    public final void usage (String msg, String status) {
        String msg2;
        if(msg == null){
            msg2 = "";
        } else {
            msg2 = msg + "\n";
        }
        if(status == null){
            status = "UNKNOWN";
        }
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(msg2, options);
        System.exit(getStatusCode("UNKNOWN"));
    }
    public final void usage(String msg){
        usage(msg, null);
    }
    public final void usage(Exception e){
        usage(e.getMessage(), null);
    }
    public final void usage(){
        usage(null, null);
    }

//  public static void no_args(String[] args){
//        if(args != null){
//            usage("invalid non-switch arguments supplied on command line");
//        }
//    }

    // leave this as optional not abstract as some cli tools may not need to add additional options
    public void addOptions(){
    }

    public int getVerbose(){
        return verbose;
    }

    public void setVerbose(int v){
//        log.debug("setting verbose to %s", verbose);
        verbose = v;
    }

    public int getVerboseDefault(){
        return verbose_default;
    }

    public void setVerboseDefault(int v){
//        log.debug("setting default verbose to %s", verbose);
        verbose_default = v;
    }

    public int getTimeout(){
        return timeout;
    }

    public void setTimeout(int secs){
        validate_int(secs, "timeout", 0, timeout_max);
//        log.debug("setting timeout to %s secs", secs);
        timeout = secs;
    }

    public int getTimeout_default(){
        return timeout_default;
    }

    // null prevents --timeout switch becoming exposed, whereas 0 will allow
    public void setTimeoutDefault(int secs){
//        validate_int(secs, "timeout default", 0, timeout_max);
        if (secs > timeout_max) {
            throw new IllegalArgumentException("set default timeout > timeout max");
        }
//        log.debug("setting default timeout to %s secs", secs);
        timeout_default = secs;
    }

    public int getTimeout_max(){
        return timeout_max;
    }

    public void setTimeoutMax(int secs) {
        // leave this to be able to set max to any amount
        // validate_int(secs, "timeout default", 0, timeout_max)
//        log.debug("setting max timeout to %s secs", secs);
        timeout_max = secs;
    }

//    def add_opt(self, *args, **kwargs):
//        self.__parser.add_option(*args, **kwargs)
//
//    def get_opt(self, name):
//        if not isStr(name):
//            throw new IllegalArgumentException("passed non-string as arg to CLI.get_opt()")
//        if not self.is_option_defined(name):
//            throw new IllegalArgumentException("{0} option not defined".format(name))
//        return getattr(self.options, name)
//
//    def is_option_defined(self, name):
//        return name in dir(self.options)
//
    public void timeoutHandler(Exception e){
        // consider letting exception propagate
        quit("UNKNOWN", String.format("self timeout out after %s second%s", timeout, plural(timeout)));
    }

//    def add_default_opts(self):
//        // This was a hack because main() was called more than once resulting in this being called more than once
//        // use separate objects in future
//        // for _ in ("--help", "--version", "--timeout", "--verbose", "--debug"):
//        //     try:
//        //         self.__parser.remove_option(_)
//        //     except ValueError:
//        //         pass
//
//        if self.__timeout_default is not None:
//            self.add_opt("-t", "--timeout", help="Timeout in secs (default: %d)" % self.__timeout_default,
//                         metavar="secs", default=self.__timeout_default)
//        self.add_opt("-v", "--verbose", help="Verbose mode (-v, -vv, -vvv)", action="count",
//                     default=self.__verbose_default)
//        self.add_opt("-V", "--version", action="store_true", help="Show version and exit")
//        // this would intercept and return exit code 0
//        // self.__parser.add_option("-h", "--help", action="help")
//        self.add_opt("-h", "--help", action="store_true", help="Show full help and exit")
//        self.add_opt("-D", "--debug", action="store_true", help=SUPPRESS_HELP, default=bool(os.getenv("DEBUG")))

    private void parseArgs2(String[] args) {
        log.debug("parseArgs2()");
        // 1.3+ API problem with Spark, go back to older API for commons-cli
        //CommandLineParser parser = new DefaultParser();
        CommandLineParser parser = new GnuParser();
        try {
            cmd = parser.parse(options, args);
            if(cmd.hasOption("h")){
                usage();
            }
            if(cmd.hasOption("D")){
                debug = true;
            }
            if(cmd.hasOption("v")){
                verbose += 1;
            }
            // TODO:
//                if(cmd.hasOption("V")){
//                    println("%s version %s");
//                    System.exit(getStatusCode("UNKNOWN"));
//                }
            timeout = timeout_default;
            if(cmd.hasOption("t")){
                timeout = Integer.valueOf(cmd.getOptionValue("t", String.valueOf(timeout)));
            }
        } catch (ParseException e){
            if(debug){
                e.printStackTrace();
            }
            log.error(e + "\n");
            usage();
        }
//            print("%(version)s" % self.__dict__)
//            sys.exit(ERRORS["UNKNOWN"])
//        if "timeout" in dir(self.options):
//            self.timeout = self.get_opt("timeout")
        String env_verbose = System.getenv("VERBOSE");
        if (env_verbose == null || env_verbose.trim().equals("")) {
            // pass
        } else {
            try{
                int v = Integer.valueOf(env_verbose.trim());
                if (Integer.valueOf(v) > verbose) {
//                    log.debug("environment variable $VERBOSE = %i, increasing verbosity", v);
                    verbose = v;
                }
            } catch(NumberFormatException e) {
//                log.warn(String.format("$VERBOSE environment variable is not an integer ('{}')", env_verbose));
            }
        }
        parseArgs();
    }

    public void parseArgs() {
    }

    public void processArgs() {
    }
}

////////////////

//        self.topfile = get_topfile()
//        //print("topfile = %s" % self.topfile)
//        self._docstring = get_file_docstring(self.topfile)
//        if self._docstring:
//            self._docstring = "\n" + self._docstring.strip() + "\n"
//        if self._docstring is None:
//            self._docstring = ""
//        self._topfile_version = get_file_version(self.topfile)
//        // this doesn"t work in unit tests
//        // if self._topfile_version:
//        //     raise CodingError("failed to get topfile version - did you set a __version__ in top cli program?") // pylint: disable=line-too-long
//        self._cli_version = self.__version__
//        self._utils_version = harisekhon.utils.__version__
//        // returns "python -m unittest" :-/
//        // prog = os.path.basename(sys.argv[0])
//        self._prog = os.path.basename(self.topfile)
//        self._github_repo = get_file_github_repo(self.topfile)
//        // if not self.github_repo:
//        //     self.github_repo = "https://github.com/harisekhon/pytools"
//        if self._github_repo:
//            self._github_repo = " - " + self._github_repo
//        // _hidden attributes are shown in __dict__
//        self.version = "%(_prog)s version %(_topfile_version)s " % self.__dict__ + \
//                       "=>  CLI version %(_cli_version)s  =>  Utils version %(_utils_version)s" % self.__dict__
//        self.usagemsg = "Hari Sekhon%(_github_repo)s\n\n%(_prog)s version %(_topfile_version)s\n%(_docstring)s\n" \
//                        % self.__dict__
//        self.usagemsg_short = "Hari Sekhon%(_github_repo)s\n\n" % self.__dict__
//        // set this in simpler client programs when you don"t want to exclude
//        // self.__parser = OptionParser(usage=self.usagemsg_short, version=self.version)
//        // self.__parser = OptionParser(version=self.version)
//        // will be added by default_opts later so that it"s not annoyingly at the top of the option help
//        // also this allows us to print full docstring for a complete description and not just the cli switches
//        // description=self._docstring // don"t want description printed for option errors
//        width = os.getenv("COLUMNS", None)
//        if not isInt(width) or not width:
//            width = Terminal().width
//        width = min(width, 200)
//        self.__parser = OptionParser(add_help_option=False, formatter=IndentedHelpFormatter(width=width))
//        // duplicate key error or duplicate options, sucks
//        // self.__parser.add_option("-V", dest="version", help="Show version and exit", action="store_true")
//        self.setup()
//
