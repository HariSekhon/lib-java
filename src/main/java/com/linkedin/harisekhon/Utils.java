//
//  Author: Hari Sekhon
//  Date: 2015-05-30 12:53:28 +0100 (Sat, 30 May 2015)
//
//  vim:ts=4:sts=4:sw=4:noet
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

// Port of my personal libraries from other languages I've been using for several years

// my linkedin account is unique and will outlast my personal domains
package com.linkedin.harisekhon;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.lang.Math.pow;

// Stuff to still be ported:
// TODO: autoflush
// TODO: env support
// TODO: threshold range support
// TODO: cmd
// TODO: curl and curl_json
// TODO: get_field
// TODO: get_path_owner
// TODO: go_flock_yourself / flock_off

// TODO: if progname check_* stderr to stdout and trap exit codes to 2

// Methods should not allow unhandled Exceptions since we want to catch and provide concise one liner errors

public final class Utils {

    private static final String utils_version = "1.16.0";

    public static String msg = "";
    public static final String nagios_plugins_support_msg = "Please try latest version from https://github.com/harisekhon/nagios-plugins, re-run on command line with -vvv and if problem persists paste full output from -vvv mode in to a ticket requesting a fix/update at https://github.com/harisekhon/nagios-plugins/issues/new";
    public static final String option_format_calling_method = "%-50s %-25s %s";
    public static final String option_format = "%-25s %s";
    public static final String nagios_plugins_support_msg_api = "API may have changed. " + nagios_plugins_support_msg;

    private static final HashMap<String, Integer> exit_codes = new HashMap<String, Integer>();
    private static String status = "UNKNOWN";
    private static int verbose = 0;
    private static int timeout = -1;
    private static boolean debug = false;
    public static boolean nagios_plugin = false;

    // keeping this lowercase to make it easier to do String.toLowerCase() case insensitive matches
    private static final ArrayList<String> valid_units = new ArrayList<String>(
        Arrays.asList(
            "%",
            "s",
            "ms",
            "us",
            "b",
            "kb",
            "mb",
            "gb",
            "tb",
            "c"
        )
    );

    public static final Logger log = Logger.getLogger(com.linkedin.harisekhon.Utils.class.getName());
//    public static final Logger log = Logger.getLogger("com.linkedin.harisekhon.Utils");

    // ===================================================================== //
    //
    //                             R e g e x
    //
    // ===================================================================== //

    // neither Google's com.Guava google.common.net.HostSpecifier nor Apache Commons org.apache.commons.validator.routines.DomainValidator are suitable for my needs here, must port the more flexible regex methods from my old Perl library

    // years and years of Regex expertise and testing has gone in to this, do not edit!
    // This also gives flexibility to work around some situations where domain names may not be quite valid (eg .local/.intranet) but still keep things quite tight
    // There are certain scenarios where Google Guava and Apache Commons libraries don't help with these
    // AWS regex from http://blogs.aws.amazon.com/security/blog/tag/key+rotation
    public static final String aws_access_key_regex     = "(?<![A-Z0-9])[A-Z0-9]{20}(?![A-Z0-9])";
    public static final String aws_host_component       = "ip-(?:10-\\d+-\\d+-\\d+|172-1[6-9]-\\d+-\\d+|172-2[0-9]-\\d+-\\d+|172-3[0-1]-\\d+-\\d+|192-168-\\d+-\\d+)";
    public static final String aws_secret_key_regex     = "(?<![A-Za-z0-9/+=])[A-Za-z0-9/+=]{40}(?![A-Za-z0-9/+=])";
    public static final String ip_prefix_regex 			= "\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}";
    // now allowing 0 or 255 as the final octet due to CIDR
    public static final String ip_regex 				= ip_prefix_regex + "(?:25[0-5]|2[0-4][0-9]|[01]?[1-9][0-9]|[01]?0[1-9]|[12]00|[0-9])\\b";
    // must permit numbers as valid host identifiers that are being used in the wild in FQDNs
    public static final String hostname_component_regex = "\\b[A-Za-z0-9](?:[A-Za-z0-9_\\-]{0,61}[a-zA-Z0-9])?\\b";
    public static final String domain_component			= "\\b[a-zA-Z0-9](?:[a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\b";
    public static final String domain_regex;
    public static final String domain_regex_strict;
    public static final String hostname_regex;
    public static final String aws_hostname_regex;
    public static final String host_regex;
    public static final String dirname_regex 			= "[/\\w\\s\\\\.:,*()=%?+-]+";
    public static final String filename_regex 			= dirname_regex + "[^/]";
    public static final String rwxt_regex 	  	 		= "[r-][w-][x-][r-][w-][x-][r-][w-][xt-]";
    public static final String fqdn_regex;
    public static final String aws_fqdn_regex;
    public static final String email_regex;
    public static final String subnet_mask_regex 		= "\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[1-9][0-9]|[01]?0[1-9]|[12]00|[0-9])\\b";
    public static final String mac_regex				= "\\b[0-9A-F-af]{1,2}[:-](?:[0-9A-Fa-f]{1,2}[:-]){4}[0-9A-Fa-f]{1,2}\\b";
    public static final String process_name_regex		= "\\s*[\\w./<>-][\\w\\s./<>-]+";
    public static final String url_path_suffix_regex	= "/(?:[\\w.,:/%&?!=*|\\[\\]~+-]+)?"; // there is an RFC3987 regex but it's gigantic, this is easier to reason about and serves my needs
    public static final String user_regex				= "\\b[A-Za-z][A-Za-z0-9_-]*[A-Za-z0-9]\\b";
    public static final String url_regex;
    public static final String column_regex				= "\\b[\\w:]+\\b";
    public static final String ldap_dn_regex			= "\\b\\w+=[\\w\\s]+(?:,\\w+=[\\w\\s]+)*\\b";
    public static final String krb5_principal_regex;
    public static final String threshold_range_regex	= "^(@)?(-?\\d+(?:\\.\\d+)?)(:)(-?\\d+(?:\\.\\d+)?)?$";
    public static final String threshold_simple_regex	= "^(-?\\d+(?:\\.\\d+)?)$";
    public static final String label_regex              = "\\s*[\\%\\(\\)\\/\\*\\w-][\\%\\(\\)\\/\\*\\w\\s-]*";
    public static final String version_regex            = "\\d(\\.\\d+)*";
    public static final String version_regex_lax        = version_regex + "-?.*";

    public static HashSet<String> tlds = new HashSet<String>();
    public static final String tld_regex;

    protected static final void loadTlds(String filename) throws IOException {
        int tld_count = 0;
        try {
            final InputStream resourceAsStream = Utils.class.getResourceAsStream("/" + filename);
            if (resourceAsStream == null) {
                throw new IOException(String.format("file '%s' does not exist under resources!", filename));
            }
            // works on disk, not on jar
//            URL url = com.linkedin.harisekhon.Utils.class.getResource("/" + filename);
//            File file = new File(url.getFile());
//            Scanner scanner = new Scanner(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream));
            String line;
            while((line = br.readLine()) != null) {
                line = line.replaceFirst("[#;].*", "");
                line = line.trim();
                if (line.matches("^\\s*$")) {
                    continue;
                }
                if (line.matches("^[A-Za-z0-9-]+$")) {
                    tlds.add(line);
                    tld_count += 1;
                } else {
                    log.warn(String.format("TLD: '%s' from tld file '%s' not validated, skipping that TLD", line, filename));
                }
            }
            br.close();
        } catch (IOException e){
            log.error(e.getMessage());
            throw e;
        }
        log.debug(tld_count + " TLDs loaded from '" + filename + "'");
    }

    public static void checkTldCount() throws IllegalStateException {
        long tld_count = tlds.size();
        log.debug(String.format("%d total unique TLDs loaded from resources", tld_count));
        if (tld_count < 1000) {
            String err_msg = String.format("%d TLDs loaded, expected >= 1000", tld_count);
            log.fatal(err_msg);
            throw new IllegalStateException(err_msg);
        }
        if (tld_count > 2000) {
            String err_msg =  String.format("%d TLDs loaded, expected <= 2000", tld_count);
            log.fatal(err_msg);
            throw new IllegalStateException(err_msg);
        }
    }

    // can't throw any Exception up from static initializer
    static {
        // autoboxing
        exit_codes.put("OK",        0);
        exit_codes.put("WARNING",   1);
        exit_codes.put("CRITICAL",  2);
        exit_codes.put("UNKNOWN",   3);
        exit_codes.put("DEPENDENT", 4);

        log.setLevel(Level.INFO);

        // let the class fail to initialize if missing a resource to prevent relying on the regexes which won't match
        try {
            loadTlds("tlds-alpha-by-domain.txt");
            checkTldCount();
            loadTlds("custom_tlds.txt");
            // XXX: TODO: this fails correctly but doesn't give stack trace or file name
//            loadTlds("custom_tldsa.txt");
//            log.trace("tld_regex = " + tld_regex);
        } catch(IOException e){
            // logged by loadTlds when throwing exception
//            log.error(e.getMessage());
            quit("UNKNOWN", String.format("unable to load a resource file containing TLDs for generated domain/fqdn regex generation: %s", e.getMessage()));
//            throw e;
        }

        tld_regex = "\\b(?i:" + StringUtils.join(tlds.iterator(), "|") + ")\\b";

        //tld_regex				      = "\\b(?i:[A-Za-z]{2,4}|london|museum|travel|local|localdomain|intra|intranet|internal)\\b";
        domain_regex				= "(?:" + domain_component + "\\.)*" + tld_regex;
        domain_regex_strict 		= "(?:" + domain_component + "\\.)+" + tld_regex;
        hostname_regex              = String.format("%s(?:\\.%s)?", hostname_component_regex, domain_regex);
        aws_hostname_regex          = aws_host_component + "(?:\\." + domain_regex + ")?";
        aws_fqdn_regex              = aws_host_component + "\\." + domain_regex;
        host_regex 	  			    = String.format("\\b(?:%s|%s)\\b", hostname_regex, ip_regex);
        fqdn_regex 	  	 		    = hostname_component_regex + "\\." + domain_regex;
        krb5_principal_regex        = String.format("%s(?:/%s)?(?:@%s)?", user_regex, hostname_regex, domain_regex);
        email_regex 	  	 		= "\\b[A-Za-z0-9](?:[A-Za-z0-9\\._\\%\\'\\+-]{0,62}[A-Za-z0-9\\._\\%\\+-])?@" + domain_regex + "\\b";
        url_regex				    = "\\b(?i:https?://)?" + host_regex + "(?::\\d{1,5})?(?:" + url_path_suffix_regex + ")?";
    }

    // ===================================================================== //
    //
    //                S t a t u s   h e l p e r   m e t h o d s
    //
    // ===================================================================== //


    public static final String getStatus(){
        return status;
    }

    public static final int getStatusCode(){
        return exit_codes.get(status);
    }

    public static final void setStatus (String key) {
        if(exit_codes.containsKey(key)){
            status = key;
        } else {
            throw new IllegalArgumentException("invalid status '" + key + "' passed to setStatus(), must be one of: " + exit_codes.keySet().toString());
        }
    }

    public static final void unknown(){
        if(getStatus() == null || "OK".equalsIgnoreCase(getStatus())){
            setStatus("UNKNOWN");
        }
    }

    public static final void warning(){
        if(getStatus() == null || ! "CRITICAL".equalsIgnoreCase(getStatus())){
            setStatus("WARNING");
        }
    }

    public static final void critical(){
        setStatus("CRITICAL");
    }

    public static final Boolean isOk(){
        // encapsulated and cannot return null, also reversed equality below cannot get NPE
//        if(getStatus() == null){
//            return false;
//        }
        return "OK".equals(getStatus());
    }


    public static final Boolean isWarning(){
        // encapsulated and cannot return null, also reversed equality below cannot get NPE
//        if(getStatus() == null){
//            return false;
//        }
        return "WARNING".equalsIgnoreCase(getStatus());
    }

    public static final Boolean isCritical(){
        // encapsulated and cannot return null, also reversed equality below cannot get NPE
//        if(getStatus() == null){
//            return false;
//        }
        return "CRITICAL".equalsIgnoreCase(getStatus());
    }

    public static final Boolean isUnknown(){
        // encapsulated and cannot return null, also reversed equality below cannot get NPE
//        if(getStatus() == null){
//            return false;
//        }
        return "UNKNOWN".equalsIgnoreCase(getStatus());
    }


    public static final int getStatusCode (String key) {
        if(key != null && exit_codes.containsKey(key)){
            return exit_codes.get(key);
        } else {
            throw new IllegalArgumentException("invalid status '" + key + "' passed to getStatusCode()");
        }
    }


    public static final void status(){
        vlog("status: " + getStatus());
    }

    public static final void status2(){
        vlog2("status: " + getStatus());
    }

    public static final void status3(){
        vlog3("status: " + getStatus());
    }


    // ===================================================================== //

    public static final Boolean check_regex (String string, String regex) {
        if(string == null){
//            throw new IllegalArgumentException("undefined string passed to check_regex()");
            return false;
        }
        if(regex == null){
            throw new IllegalArgumentException("undefined regex passed to check_regex()");
        }
        if(! isRegex(regex)){
            throw new IllegalArgumentException("invalid regex passed to check_regex()");
        }
        if(string.matches(regex)){
            return true;
        }
//        critical();
        return false;
    }


    public static final Boolean checkString(String str, String expected, Boolean no_msg) {
        if(expected == null){
            throw new IllegalArgumentException("passed null as expected string to checkString()");
        }
        if(str == null) {
            return false;
        }
        if (str.equals(expected)){
            return true;
        }
//        critical();
        /* implement when msg and thresholds are done
        if(nomsg){

        }
        */
        return false;
    }
    public static final Boolean checkString(String str, String expected) {
        return checkString(str, expected, false);
    }


    public static final double expandUnits(double num, String units, String name) {
        name = name(name);
        if(units == null){
            throw new IllegalArgumentException("null passed for units to expandUnits()");
        }
        name = name.trim();
        String units2 = units.trim();
        if(! name.isEmpty()){
            name = " for " + name;
        }
        int power = 1;
        if(units2.matches("(?i)^B?$")){
            return num;
        } else if(units2.matches("(?i)^KB?$")){ power = 1;
        } else if(units2.matches("(?i)^MB?$")){ power = 2;
        } else if(units2.matches("(?i)^GB?$")){ power = 3;
        } else if(units2.matches("(?i)^TB?$")){ power = 4;
        } else if(units2.matches("(?i)^PB?$")) {
            power = 5;
        } else {
            throw new IllegalArgumentException(String.format("unrecognized units '%s' passed to expandUnits()%s", units2, name));
        }
        return (num * ( pow(1024, power) ) );
    }
    public static final double expandUnits(double num, String units) {
        return expandUnits(num, units, null);
    }
    public static final long expandUnits(long num, String units, String name) {
        return (long) expandUnits((double)num, units, name);
    }
    public static final long expandUnits(long num, String units) {
        return expandUnits(num, units, null);
    }


    public static final void hr(){
        log.info("# " + StringUtils.repeat("=", 76) + " #");
    }


    public static final String humanUnits(double num, String units, Boolean terse) {
        if(units == null){
            units = "";
        }
        units = units.trim();
        if(!units.isEmpty()){
            num = expandUnits(num, units, "humanUnits");
        }
        if (num >= pow(1024, 7)) {
            throw new IllegalArgumentException(String.format("determined suspicious units for number '%s', larger than Exabytes?!!", num));
        } else if(num >= pow(1024, 6)){
            //num_str = String.format("%.2f", num / pow(1024, 6)).replaceFirst("\\.0+$", "");
            num = num / pow(1024, 6);
            units = "EB";
        } else if(num >= pow(1024, 5)){
            //num_str = String.format("%.2f", num / pow(1024, 5));
            num = num / pow(1024, 5);
            units = "PB";
        } else if(num >= pow(1024, 4)){
            //num_str = String.format("%.2f", num / pow(1024, 4));
            num = num / pow(1024, 4);
            units = "TB";
        } else if(num >= pow(1024, 3)){
            //num_str = String.format("%.2f", num / pow(1024, 3));
            num = num / pow(1024, 3);
            units = "GB";
        } else if(num >= pow(1024, 2)){
            //num_str = String.format("%.2f", num / pow(1024, 2));
            num = num / pow(1024, 2);
            units = "MB";
        } else if(num >= pow(1024, 1)){
            //num_str = String.format("%.2f", num / pow(1024, 1));
            num = num / pow(1024, 1);
            units = "KB";
        } else if(num < 1024){
            //num_str = String.valueOf(num);
            if(terse){
                //return String.format("%sB", num);
                units = "B";
            } else {
                //return String.format("%s bytes", num);
                units = " bytes";
            }
        // unreachable
//        } else {
//            throw new IllegalArgumentException(String.format("unable to determine units for number '%s'", num));
        }
        // remove trailing zeros past the decimal point
        String num_str = String.format("%.2f", num).replaceFirst("(\\.\\d+)0$", "$1").replaceFirst("\\.0+$", "");
        return num_str + units;
    }
    public static final String humanUnits(double num, String units) {
        return humanUnits(num, units, false);
    }
    public static final String humanUnits(double num) {
        return humanUnits(num, null, false);
    }

    public static final String strip_scheme (String str) {
        if(str.matches("^\\w+://[^/].*")){
            return str.replaceFirst("^\\w+://", "");
        } else {
            return str.replaceFirst("^\\w+:/+", "/");
        }
    }

    public static final String stripSchemeHost(String str) {
        if(str.matches("^\\w+:///[^/].*")){
            return str.replaceFirst("^\\w+:///", "/");
        } else {
            //return str.replaceFirst("^\\w+:(?://[^/]+)?/", "/");
            return str.replaceFirst("^\\w+:(?://" + host_regex + "(?::\\d+)?)?/", "/");
        }
    }


    // ===================================================================== //
    //
    //                 C o n v e r s i o n   U t i l i t i e s
    //
    // ===================================================================== //

    public static final ArrayList<String> arrayToArraylist(String[] array) {
        return new ArrayList<String>(Arrays.asList(array));
    }

    public static final String[] arraylistToArray(ArrayList<String> arrayList) {
        String[] array = new String[arrayList.size()];
        return arrayList.toArray(array);
    }

    public static final String[] setToArray(Set<String> set){
        String[] array = set.toArray(new String[set.size()]);
        return array;
    }


    // ===================================================================== //
    //
    //                     Non-deterministic Ordering
    //
    public static final String[] uniqArray(String[] list) {
        HashSet<String> set = new HashSet<String>();
        for(String item: list) {
            set.add(item);
        }
        String[] a = {};
        return set.toArray(a);
    }

    // TODO: change this to be uniq_list instead and use List<String>
    public static final ArrayList<String> uniqArraylist(List<String> list) {
        HashSet<String> set = new HashSet<String>(list);
        ArrayList<String> a = new ArrayList<String>();
        a.addAll(set);
        return a;
    }

    // ===================================================================== //
    //
    //                     Order Preserving
    //
    public static final String[] uniqArrayOrdered(String[] list){
        Set<String> set = new LinkedHashSet<String>();
        for(String item: list) {
            set.add(item);
        }
        String[] a = {};
        return set.toArray(a);
    }

    public static final ArrayList<String> uniqArraylistOrdered(ArrayList<String> list) {
        Set<String> set = new LinkedHashSet<String>(list);
        ArrayList<String> a = new ArrayList<String>();
        a.addAll(set);
        return a;
    }


    // ===================================================================== //
    //
    //                          V a l i d a t i o n
    //
    // ===================================================================== //

    public static final Boolean isAlNum (String str) {
        if(str == null){
            return false;
        }
        return str.matches("^[A-za-z0-9]+$");
    }


    public static final Boolean isAwsAccessKey (String str) {
        if(str == null){
            return false;
        }
        return str.matches("^" + aws_access_key_regex + "$");
    }

    public static final Boolean isAwsBucket (String arg) {
        return isDnsShortName(arg);
    }


    public static final Boolean isAwsHostname (String str) {
        if(str == null){
            return false;
        }
        return str.matches("^" + aws_hostname_regex + "$");
    }

    public static final Boolean isAwsFqdn (String str) {
        if(str == null){
            return false;
        }
        return str.matches("^" + aws_fqdn_regex + "$");
    }


    public static final Boolean isAwsSecretKey (String str) {
        if(str == null){
            return false;
        }
        return str.matches("^" + aws_secret_key_regex + "$");
    }


    public static final Boolean isChars (String str, String char_range) {
        if(str == null || char_range == null) {
            return false;
        }
        if(!isRegex(String.format("[%s]", char_range))){
            throw new IllegalArgumentException("invalid regex char range passed to isChars()");
        }
        return str.matches(String.format("^[%s]+$", char_range));
    }


    public static final Boolean isCollection (String collection) {
        if(collection == null || collection.trim().isEmpty()){
            return false;
        }
        return collection.matches("^(\\w(?:[\\w.]*\\w)?)$");
    }


    public static final Boolean isDatabaseName (String database) {
        if(database == null || database.trim().isEmpty()){
            return false;
        }
        return database.matches("^\\w+$");
    }

    public static final Boolean isDatabaseColumnName (String column) {
        if(column == null || column.trim().isEmpty()){
            return false;
        }
        return column.matches("^" + column_regex + "$");
    }


    public static final Boolean isDatabaseFieldName (String field) {
        if(field == null || field.trim().isEmpty()){
            return false;
        }
        // allows field number integer or field name
        return field.matches("^(?:\\d+|[\\w()*,._-]+)$");
    }


    public static final Boolean isDatabaseTableName (String table, Boolean allow_qualified) {
        if(table == null || table.trim().isEmpty()){
            return false;
        }
        if(allow_qualified){
            return table.matches("^[A-Za-z0-9][\\w.]*[A-Za-z0-9]$");
        } else {
            return table.matches("^[A-Za-z0-9]\\w*[A-Za-z0-9]$");
        }
    }
    public static final Boolean isDatabaseTableName (String table) {
        return isDatabaseTableName(table, false);
    }


    public static final Boolean isDatabaseViewName (String view, Boolean allow_qualified) {
        return isDatabaseTableName(view, allow_qualified);
    }
    public static final Boolean isDatabaseViewName (String view) {
        return isDatabaseViewName(view, false);
    }


    public static final Boolean isDirname(String dir){
        if(dir == null || dir.trim().isEmpty()){
            return false;
        }
        return dir.matches("^" + dirname_regex + "$");
    }

    public static final Boolean isDomain (String domain) {
        if(domain == null || domain.trim().isEmpty()){
            return false;
        }
        return domain.matches("^" + domain_regex + "$");
    }


    public static final Boolean isDomainStrict (String domain) {
        if(domain == null || domain.trim().isEmpty()){
            return false;
        }
        return domain.matches("^" + domain_regex_strict + "$");
    }


    public static final Boolean isDnsShortName (String dns) {
        if(dns == null || dns.trim().length() < 3 || dns.length() > 63 ){
            return false;
        }
        return dns.matches("^" + hostname_component_regex + "$");
    }


    public static final Boolean isEmail (String email) {
        if(email == null || email.trim().isEmpty() || email.length() > 256){
            return false;
        }
        return email.matches("^" + email_regex + "$");
    }


    public static final Boolean isFilename (String filename) {
        if(filename == null || filename.trim().isEmpty()){
            return false;
        }
        return filename.matches("^" + filename_regex + "$");
    }


    public static final Boolean isFqdn (String fqdn) {
        if(fqdn == null || fqdn.trim().isEmpty() || fqdn.length() > 255){
            return false;
        }
        return fqdn.matches("^" + fqdn_regex + "$");
    }


    public static final Boolean isHex (String hex) {
        if(hex != null && hex.matches("^0x[A-Fa-f\\d]+$")){
            return true;
        } else {
            return false;
        }
    }

    // at casual glance this looks like it's duplicating isHostname but it's using a different unified regex of isHostname + isIP
    public static final Boolean isHost (String host) {
        if(host == null || host.trim().isEmpty()){
            return false;
        }
        if(host.length() > 255){
            return false;
        } else if(isIP(host)){
            return true;
        } else if(host.matches("^" + host_regex + "$")){
            return true;
        } else {
            return false;
        }
    }


    public static final Boolean isHostname (String hostname) {
        if(hostname != null && hostname.length() < 256 && hostname.matches("^" + hostname_regex + "$")){
            return true;
        } else {
            return false;
        }
    }


    public static final Boolean isInterface (String networkInterface) {
        if(networkInterface != null && networkInterface.matches("^(?:em|eth|bond|lo|docker)\\d+|lo|veth[A-Fa-f0-9]+$")){
            return true;
        } else {
            return false;
        }
    }


    // TODO: isJson
    // TODO: isXML


    public static final Boolean isKrb5Princ (String princ) {
        if(princ != null && princ.matches("^" + krb5_principal_regex + "$")){
            return true;
        } else {
            return false;
        }
    }


    public static final Boolean isLabel (String label) {
        if(label != null && label.matches("^" + label_regex + "$")){
            return true;
        } else {
            return false;
        }
    }


    public static final Boolean isLdapDn (String dn) {
        if(dn != null && dn.matches("^" + ldap_dn_regex + "$")){
            return true;
        } else {
            return false;
        }
    }

    // doubles are easier as can still call against floats if needed
    public static final Boolean isMinVersion (String version, Double min) {
        if(version == null) {
            return false;
        }
        if(min == null){
            return false;
        }
        if(!isVersionLax(version)){
            return false;
        }
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(version);
        if(matcher.find()){
            String m1 = matcher.group(1);
            // this will never happen because of the regex
//            try {
                // float >= double fails for 1.3 vs 1.3, make detected_version a double too
                double detected_version = Double.parseDouble(m1);
//            } except (NumberFormatException, e) {
//                throw IllegalArgumentException(String.format("failed to detect version from string '%s': %s", version, e));
//            }
            if(detected_version >= min){
                return true;
            }
        }
        return false;
    }


    public static final Boolean isNagiosUnit (String unit) {
        if(unit != null && valid_units.contains(unit.toLowerCase())){
            return true;
        } else {
            return false;
        }
    }


    public static final Boolean isNoSqlKey (String key) {
        if(key != null && key.matches("^[\\w_,.:+-]+$")){
            return true;
        } else {
             return false;
        }
    }


    public static final Boolean isIP (String ip) {
        if(ip == null){
            return false;
        }
        String[] octets = ip.split("\\.");
        if(octets.length != 4){
            return false;
        }
        for(String octet: octets){
            int octet_int;
            try{
                octet_int = Integer.parseInt(octet);
            } catch (NumberFormatException e) {
                return false;
            }
            if(octet_int < 0 || octet_int > 255){
                return false;
            }
        }
        if(ip.matches("^" + ip_regex + "$")){
            return true;
        }
        return false;
    }


    public static final Boolean isPort (String port) {
        if(port == null){
            return false;
        }
        int port_int;
        try{
            port_int = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            return false;
        }
        if(port.matches("^\\d+$")){
            return isPort(port_int);
        }
        return false;
    }

    public static final Boolean isPort (int port_int) {
        if(port_int < 1 || port_int > 65535){
            return false;
        }
        return true;
    }


    public static final Boolean isProcessName (String proc) {
        if(proc != null && proc.matches("^"+ process_name_regex + "$")){
            return true;
        } else {
            return false;
        }
    }


    public static final Boolean isRegex (String regex) {
        if (regex == null){
            return false;
        }
        try {
            // this seems to allow null, so catch above
            "".matches(regex);
            return true;
        } catch (PatternSyntaxException e){
            // pass
        }
        return false;
    }


    // not implementing isScientific

    // TODO: isThreshold()


    public static final Boolean isUrl (String url) {
        if(url == null || url.trim().isEmpty()){
            return false;
        }
        if(url.matches("^" + url_regex + "$")){
            return true;
        } else {
            return false;
        }
    }


    public static final Boolean isUrlPathSuffix (String url) {
        if(url != null && url.matches("^" + url_path_suffix_regex + "$")){
            return true;
        } else {
            return false;
        }
    }


    public static final Boolean isUser (String user) {
        if(user == null){
            return false;
        }
        if(user.matches("^" + user_regex + "$")){
            return true;
        }
        return false;
    }


    public static final Boolean isVersion (String version) {
        if(version != null && version.matches("^" + version_regex + "$")){
            return true;
        } else {
            return false;
        }
    }

    public static final Boolean isVersionLax (String version) {
        if(version != null && version.matches(version_regex_lax)){
            return true;
        } else {
            return false;
        }
    }


    // ===================================================================== //
    //
    //                      O S   H e l p e r s
    //
    // ===================================================================== //

    /*
    public static final void print_java_properties(){
        System.getProperties().list(System.out);
    }
    */

    public static final String getOS(){
        String os = System.getProperty("os.name");
        if(os == null){
            throw new IllegalStateException("unknown OS, retrieved null for OS");
        }
        if(os.trim().isEmpty()){
            throw new IllegalStateException("unknown OS, retrieved blank for OS");
        }
        return os;
    }

    public static final Boolean isOS (String os) {
        if(os != null && os.equals(getOS())){
            return true;
        } else {
            return false;
        }
    }

    public static final Boolean isMac(){
        return isOS("Mac OS X");
    }

    public static final Boolean isLinux(){
        return isOS("Linux");
    }

    public static final Boolean isLinuxOrMac(){
        return isLinux() || isMac();
    }
 
    public static final void linuxOnly() throws UnsupportedOSException {
        if (!isLinux()){
            throw new UnsupportedOSException("Linux");
        }
    }

    public static final void macOnly() throws UnsupportedOSException {
        if (!isMac()) {
            throw new UnsupportedOSException("Mac OS X");
        }
    }

    public static final void linuxMacOnly() throws UnsupportedOSException {
        if(!(isLinux() || isMac())) {
            throw new UnsupportedOSException("Linux or Mac OS X");
        }
    }


    // ===================================================================== //

    // minimum_value - use Math.min

    // TODO: msg_perf_thresholds()
    // TODO: msg_thresholds()

    /*
    public static final int month2int (String month) {
        if(month == null){
            throw new IllegalArgumentException("null passed to month2int");
        }
        HashMap<String, Integer> month_map = new HashMap<String, Integer>() {{
            put("jan", 1);
            put("feb", 2);
            put("mar", 3);
            put("apr", 4);
            put("may", 5);
            put("jun", 6);
            put("jul", 7);
            put("aug", 8);
            put("sep", 9);
            put("oct", 10);
            put("nov", 11);
            put("dec", 12);
        }};
        if(month_map.containsKey(month.toLowerCase())){
            return month_map.get(month.toLowerCase());
        } else {
            throw new IllegalArgumentException("invalid month passed to month2int()");
        }
        return -1; // purely to appease Java - won't reach here
    }
    */

    // TODO:
    // open_file
    // parse_file_option
    // plural
    // prompt
    // isYes
    // random_alnum
    // sec2min
    // sec2human
    // set_sudo
    // set_timeout

    public static final String plural (String arg) {
        if (arg == null) {
            return "";
        }
        try {
            double a = Double.valueOf(arg);
            if(a == 1.0){
                return "";
            } else {
                return "s";
            }
        } catch (NumberFormatException e){
            return "";
        }
    }
    public static final String plural(double arg){
        return plural(Double.toString(arg));
    }
    // not using any more as both List<Object> and Object[] makes plural(null) calls ambiguous with plural(String) :-/
//    public static final String plural(List<Object> args){
//        if(args.size() == 1){
//            return "";
//        } else {
//            return "s";
//        }
//    }
//    public static final String plural(Object[] args){
//        if(args.length == 1){
//            return "";
//        } else {
//            return "s";
//        }
//    }

    public static final String resolveIp(String host) throws UnknownHostException {
        if(host == null){
            throw new IllegalArgumentException("no host passed to resolveIp (null)");
        }
        if(host.trim().isEmpty()){
            throw new IllegalArgumentException("no host passed to resolveIp (blank)");
        }
//        try {
            InetAddress address = InetAddress.getByName(host);
            return address.getHostAddress();
//        } catch (UnknownHostException e) {
//            return null;
//        }
    }

    // only works on unix systems
    public static final Boolean userExists(String user) throws IOException, UnsupportedOSException {
        linuxMacOnly();
        if(user == null) {
            return false;
        }
        if(user.trim().isEmpty()){
            return false;
        }
        if(! isUser(user)){
            return false;
        }
        StringBuilder id = new StringBuilder();
//        try {
            Process child = Runtime.getRuntime().exec("id -u " + user);
            InputStream in = child.getInputStream();
            int c;
            while((c = in.read()) != -1){
                id.append((char)c);
            }
            in.close();
//        } catch (IOException e){
//            return false;
//        }
        String id2 = id.toString();
//        println("id2" + id2);
        if(id2 != null && ! id2.isEmpty()){
            return true;
        }
        return false;
    }

//    public static final void arg_error (String msg) {
//        println("CODE ERROR: " + msg);
//        System.exit(exit_codes.get("UNKNOWN"));
//        throw new IllegalArgumentException(msg);
//    }

//    public static final void state_error (String msg) {
//        println("CODE ERROR: " + msg);
//        System.exit(exit_codes.get("UNKNOWN"));
//        throw new IllegalStateException(msg);
//    }

    public static final void quit (String status, String message) {
//        log.error(status + ": " + message);
        if(exit_codes.containsKey(status)) {
//            println(status + ": " + message);
//            System.exit(exit_codes.get(status));
            throw new QuitException(status, message);
        } else {
            throw new IllegalArgumentException(String.format("specified an invalid exit status '%s' to quit(), message was '%s'", status, message));
        }
    }
    public static final void quit (String message){
//        log.error("CRITICAL: " + msg);
//        println("CRITICAL: " + msg);
//        System.exit(exit_codes.get("CRITICAL"));
//        quit("CRITICAL", msg);
        throw new QuitException("CRITICAL", message);
    }

    // because System.out.println is still annoying and Scala has a short version
    public static final void println (String msg) {
        //if(stdout){
            System.out.println(msg);
        //} else {
        //	System.err.println(msg);
        //}
    }
    public static final void println (double num) {
        println(String.valueOf(num));
    }
    public static final void println (long num) {
        println(String.valueOf(num));
    }
    public static final void println (Boolean b){
        println(b.toString());
    }

    /*
    private static final void printf (String msg, String... args) {
        System.out.printf(String.format("%s", msg), (Object[]) args); // cast to Object[] to avoid warning about String and Object not quite matching up
    }
    */


    public static final String repeatString(String chars, int num) {
        StringBuilder string = new StringBuilder();
        for(int i = 0; i < num; i++){
            string.append(chars);
        }
        return string.toString();
    }


    // ===================================================================== //
    //
    //                          V a l i d a t i o n
    //
    // ===================================================================== //

    // these methods are intentionally not throwing exceptions as they are designed for CLI.java usage and exit with usage()
    // for try and recover behaviour use the corresponding is* methods which return Boolean

    static final String name (String name) {
        if(name == null){
            return "";
        }
        String name2 = name.trim();
        if(! name2.isEmpty()){
            name2 = name2 + " ";
        }
        return name2;
    }

    static final String requireName(String name) {
        if(name == null || name.trim().isEmpty()){
            // TODO: improve the feedback location
            //StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace()
            throw new IllegalArgumentException("name arg not defined when calling method");
        }
        return name.trim();
    }


    public static final String validateAlnum(String alnum, String name){
        name = requireName(name);
        if(alnum == null){
            throw new IllegalArgumentException(name + "not defined (null)");
        }
        String alnum2 = alnum.trim();
        if(alnum2.isEmpty()){
            throw new IllegalArgumentException(name + "not defined (blank)");
        }
        if(! alnum2.matches("^[A-Za-z0-9]+$")){
            throw new IllegalArgumentException("invalid " + name + " defined: must be alphanumeric");
        }
        vlog_option(name, alnum2);
        return alnum2;
    }


    public static final String validateChars(String arg, String name, String chars) {
        name = requireName(name);
        if(chars == null){
            throw new IllegalArgumentException("chars field not defined (null) when calling validateChars()");
        }
        String chars2 = chars.trim();
        if(chars2.isEmpty()){
            throw new IllegalArgumentException("chars field not defined (blank) when calling validateChars()");
        }
        if(arg == null || arg.isEmpty()){
            throw new IllegalArgumentException(name + "not defined");
        }
        if(! isChars(arg, chars2)){
            throw new IllegalArgumentException("invalid " + name + " defined: must be one of the following chars - " + chars2);
        }
        vlog_option(name, arg);
        return arg;
    }


    public static final String validateAwsAccessKey(String key) {
        if(key == null){
            throw new IllegalArgumentException("aws access key not defined (null)");
        }
        String key2 = key.trim();
        if(key2.isEmpty()){
            throw new IllegalArgumentException("aws access key not defined (blank)");
        }
        if(! isAwsAccessKey(key2)){
            throw new IllegalArgumentException("invalid aws access key defined: must be 20 alphanumeric chars");
        }
        vlog_option("aws access key", repeatString("X", 18) + key2.substring(18, 20));
        return key2;
    }


    public static final String validateAwsBucket(String bucket) {
        if(bucket == null){
            throw new IllegalArgumentException("aws bucket not defined (null)");
        }
        if(bucket.trim().isEmpty()){
            throw new IllegalArgumentException("aws bucket not defined (blank)");
        }
        bucket = bucket.trim();
        if(isIP(bucket)){
            throw new IllegalArgumentException("invalid aws bucket name defined: may not be formatted as an IP address");
        }
        if(! isDnsShortName(bucket)){
            throw new IllegalArgumentException("invalid aws bucket name defined: must be alphanumeric between 3 and 63 characters long");
        }
        vlog_option("aws bucket:", bucket);
        return bucket;
    }

    public static final String validateAwsHostname(String arg) {
        if(arg == null){
            throw new IllegalArgumentException("aws hostname not defined (null)");
        }
        if(arg.trim().isEmpty()){
            throw new IllegalArgumentException("aws hostname not defined (blank)");
        }
        arg = arg.trim();
        if(isIP(arg)){
            throw new IllegalArgumentException("invalid aws hostname arg name defined: may not be formmatted as an IP address");
        }
        if(! isAwsHostname(arg)){
            throw new IllegalArgumentException("invalid aws hostname name defined: must be alphanumeric between 3 and 63 characters long");
        }
        vlog_option("aws hostname:", arg);
        return arg;
    }

    public static final String validateAwsFqdn(String arg) {
        if(arg == null){
            throw new IllegalArgumentException("aws fqdn not defined (null)");
        }
        if(arg.trim().isEmpty()){
            throw new IllegalArgumentException("aws fqdn not defined (blank)");
        }
        arg = arg.trim();
        if(isIP(arg)){
            throw new IllegalArgumentException("invalid aws fqdn arg name defined: may not be formmatted as an IP address");
        }
        if(! isAwsFqdn(arg)){
            throw new IllegalArgumentException("invalid aws fqdn name defined: must be alphanumeric between 3 and 63 characters long hostname followed by domain");
        }
        vlog_option("aws fqdn:", arg);
        return arg;
    }

    public static final String validateAwsSecretKey(String key) {
        if(key == null){
            throw new IllegalArgumentException("aws secret key not defined (null)");
        }
        if(key.trim().isEmpty()){
            throw new IllegalArgumentException("aws secret key not defined (blank)");
        }
        key = key.trim();
        if(! isAwsSecretKey(key)){
            throw new IllegalArgumentException("invalid aws secret key defined: must be 20 alphanumeric chars");
        }
        vlog_option("aws secret key", repeatString("X", 38) + key.substring(38, 40));
        return key;
    }


    public static final String validateCollection(String collection, String name) {
        name = name(name);
        if(collection == null){
            throw new IllegalArgumentException(name + "collection not defined (null)");
        }
        if(collection.trim().isEmpty()){
            throw new IllegalArgumentException(name + "collection not defined (blank)");
        }
        collection = collection.trim();
        if(! isCollection(collection)){
            throw new IllegalArgumentException("invalid " + name + "collection defined: must be alphanumeric, with optional periods in the middle");
        }
        vlog_option(name + "collection", collection);
        return collection;
    }
    public static final String validateCollection(String collection) {
        return validateCollection(collection, null);
    }


    public static final String validateDatabase(String database, String name) {
        name = name(name);
        if(database == null){
            throw new IllegalArgumentException(name + "database not defined (null)");
        }
        if(database.trim().isEmpty()){
            throw new IllegalArgumentException(name + "database not defined (blank)");
        }
        database.trim();
        if(! isDatabaseName(database)){
            throw new IllegalArgumentException("invalid " + name + "database defined: must be alphanumeric");
        }
        vlog_option(name + "database", database);
        return database;
    }
    public static final String validateDatabase(String database) {
        return validateDatabase(database, null);
    }


    public static final String validateDatabaseColumnname(String column) {
        if(column == null){
            throw new IllegalArgumentException("column not defined (null)");
        }
        if(column.trim().isEmpty()){
            throw new IllegalArgumentException("column not defined (blank)");
        }
        column = column.trim();
        if(! isDatabaseColumnName(column)){
            throw new IllegalArgumentException("invalid column defined: must be alphanumeric");
        }
        vlog_option("column", column);
        return column;
    }


    public static final String validateDatabaseFieldname(String field) {
        if(field == null){
            throw new IllegalArgumentException("field not defined (null)");
        }
        if(field.trim().isEmpty()){
            throw new IllegalArgumentException("field not defined (blank)");
        }
        field = field.trim();
        if(! isDatabaseFieldName(field)){
            throw new IllegalArgumentException("invalid field defined: must be alphanumeric");
        }
        vlog_option("field", field);
        return field;
    }


    public static final String validateDatabaseQuerySelectShow(String query, String name) {
        name = name(name);
        if(query == null){
            throw new IllegalArgumentException(name + "query not defined (null)");
        }
        if(query.trim().isEmpty()){
            throw new IllegalArgumentException(name + "query not defined (blank)");
        }
        query = query.trim();
        // XXX: fix this to be case insensitive and re-enable case insensitive unit test
        if(! query.matches("^(?i)\\s*(?:SHOW|SELECT)\\s+.+$")){
            throw new IllegalArgumentException("invalid " + name + "query defined: may only be a SELECT or SHOW statement");
        }
        if(query.matches("(?i).*\\b(?:insert|update|delete|create|drop|alter|truncate)\\b.*")){
            throw new IllegalArgumentException("invalid " + name + "query defined: DML statement or suspect chars detected in query");
        }
        vlog_option(name + "query", query);
        return query;
    }
    public static final String validateDatabaseQuerySelectShow(String query) {
        return validateDatabaseQuerySelectShow(query, null);
    }


    public static final String validateDatabaseTablename(String table, String name, Boolean allow_qualified){
        name = name(name);
        if(table == null){
            throw new IllegalArgumentException(name + "table not defined (null)");
        }
        if(table.trim().isEmpty()){
            throw new IllegalArgumentException(name + "table not defined (blank)");
        }
        table = table.trim();
        if(! isDatabaseTableName(table, allow_qualified)){
            throw new IllegalArgumentException("invalid " + name + "table defined: must be alphanumeric");
        }
        vlog_option(name + "table", table);
        return table;
    }
    public static final String validateDatabaseTablename(String table, String name) {
        return validateDatabaseTablename(table, name, false);
    }
    public static final String validateDatabaseTablename(String table, Boolean allow_qualified){
        return validateDatabaseTablename(table, null, allow_qualified);
    }
    public static final String validateDatabaseTablename(String table){
        return validateDatabaseTablename(table, null, false);
    }


    public static final String validateDatabaseViewname(String view, String name, Boolean allow_qualified){
        name = name(name);
        if(view == null){
            throw new IllegalArgumentException(name + "view not defined (null)");
        }
        if(view.trim().isEmpty()){
            throw new IllegalArgumentException(name + "view not defined (blank)");
        }
        view = view.trim();
        if(! isDatabaseViewName(view, allow_qualified)){
            throw new IllegalArgumentException("invalid " + name + "view defined: must be alphanumeric");
        }
        vlog_option(name + "view", view);
        return view;
    }
    public static final String validateDatabaseViewname(String view, String name) {
        return validateDatabaseViewname(view, name, false);
    }
    public static final String validateDatabaseViewname(String view, Boolean allow_qualified) {
        return validateDatabaseViewname(view, null, allow_qualified);
    }
    public static final String validateDatabaseViewname(String view) {
        return validateDatabaseViewname(view, null, false);
    }


    public static final String validateDomain(String domain, String name) {
        name = name(name);
        if(domain == null){
            throw new IllegalArgumentException(name + "domain not defined (null)");
        }
        if(domain.trim().isEmpty()){
            throw new IllegalArgumentException(name + "domain not defined (blank)");
        }
        domain = domain.trim();
        if(! isDomain(domain)){
            throw new IllegalArgumentException("invalid " + name + "domain name defined ('" + domain + "')");
        }
        vlog_option(name + "domain", domain);
        return domain;
    }
    public static final String validateDomain(String domain) {
        return validateDomain(domain, null);
    }

    public static final String validateDomainStrict(String domain, String name) {
        name = name(name);
        if(domain == null){
            throw new IllegalArgumentException(name + "domain not defined (null)");
        }
        if(domain.trim().isEmpty()){
            throw new IllegalArgumentException(name + "domain not defined (blank)");
        }
        domain = domain.trim();
        if(! isDomainStrict(domain)){
            throw new IllegalArgumentException("invalid " + name + "domain name defined ('" + domain + "')");
        }
        vlog_option(name + "domain", domain);
        return domain;
    }
    public static final String validateDomainStrict(String domain) {
        return validateDomainStrict(domain, null);
    }


    public static final String validateDirname(String dir, String name, Boolean novlog) {
        name = name(name);
        if(dir == null){
            throw new IllegalArgumentException(name + "directory not defined (null)");
        }
        if(dir.trim().isEmpty()){
            throw new IllegalArgumentException(name + "directory not defined (blank)");
        }
        dir = dir.trim();
        if(! isDirname(dir)){
            throw new IllegalArgumentException("invalid " + name + "directory (does not match regex criteria): '" + dir + "'");
        }
        if(! novlog){
            vlog_option(name + "directory", dir);
        }
        return dir;
    }
    public static final String validateDirname(String dir, String name) {
        return validateDirname(dir, name, false);
    }
    public static final String validateDirname(String dir) {
        return validateDirname(dir, null, false);
    }


    public static final String validateDirectory(String dir, String name, Boolean novlog){
        name = name(name);
        if(dir == null){
            throw new IllegalArgumentException(name + "directory not defined (null)");
        }
        if(dir.trim().isEmpty()){
            throw new IllegalArgumentException(name + "directory not defined (blank)");
        }
        dir = dir.trim();
        dir = validateDirname(dir, name, novlog);
        File d = new File(dir);
        if(! d.isDirectory()){
            throw new IllegalArgumentException(String.format("directory '%s' does not exist", dir));
        }
        return dir;
    }
    public static final String validateDirectory(String dir, String name) {
        return validateDirectory(dir, name, false);
    }
    public static final String validateDirectory(String dir) {
        return validateDirectory(dir, null, false);
    }
    public static final String validateDir(String dir, String name, Boolean novlog) {
        return validateDirectory(dir, name, novlog);
    }
    public static final String validateDir(String dir, String name) {
        return validateDirectory(dir, name, false);
    }
    public static final String validateDir(String dir) {
        return validateDirectory(dir, null, false);
    }


    public static final String validateEmail(String email) {
        if(email == null){
            throw new IllegalArgumentException("email not defined (null)");
        }
        if(email.trim().isEmpty()){
            throw new IllegalArgumentException("email not defined (blank)");
        }
        email = email.trim();
        if(! isEmail(email)){
            throw new IllegalArgumentException("invalid email address defined: failed regex validation");
        }
        vlog_option("email", email);
        return email;
    }


    public static final String validateFile(String filename, String name, Boolean novlog){
        name = name(name);
        validateFilename(filename, name, novlog);
        File f = new File(filename);
        if(!(f.exists() && ! f.isDirectory())){
            throw new IllegalArgumentException(name + "file not found: " + filename);
        }
        return filename;
    }
    public static final String validateFile(String filename, String name) {
        return validateFile(filename, name, false);
    }
    public static final String validateFile(String filename) {
        return validateFile(filename, null, false);
    }


    public static final String validateFilename(String filename, String name, Boolean novlog) {
        name = name(name);
        if(name.isEmpty()){
            name = "filename ";
        }
        if(filename == null){
            throw new IllegalArgumentException(name + "not defined (null)");
        }
        if(filename.trim().isEmpty()){
            throw new IllegalArgumentException(name + "not defined (blank)");
        }
        filename = filename.trim();
        if(! isFilename(filename)){
            throw new IllegalArgumentException("invalid " + name + "(does not match regex criteria): '" + filename + "'");
        }
        if(! novlog){
            vlog_option(name.trim(), filename);
        }
        return filename;
    }
    public static final String validateFilename(String filename, String name) {
        return validateFilename(filename, name, false);
    }
    public static final String validateFilename(String filename) {
        return validateFilename(filename, null, false);
    }


    public static final String validateFqdn(String fqdn, String name) {
        name = name(name);
        if(fqdn == null){
            throw new IllegalArgumentException(name + "FQDN not defined (null)");
        }
        if(fqdn.trim().isEmpty()){
            throw new IllegalArgumentException(name + "FQDN not defined (blank)");
        }
        fqdn = fqdn.trim();
        if(! isFqdn(fqdn)){
            throw new IllegalArgumentException("invalid " + name + "FQDN defined");
        }
        vlog_option(name + "fqdn", fqdn);
        return fqdn;
    }
    public static final String validateFqdn(String fqdn) {
        return validateFqdn(fqdn, null);
    }

    /*
    public static final String[] validate_host_port_user_password(String host, String port, String user, String password){
        return (validateHost(host), validate_port(port), validate_user(user), validate_password(password));
    }
    */


    public static final String validateHost(String host, String name) {
        name = name(name);
        if(host == null){
            throw new IllegalArgumentException(name + "host not defined (null)");
        }
        if(host.trim().isEmpty()){
            throw new IllegalArgumentException(name + "host not defined (blank)");
        }
        host = host.trim();
        if(! isHost(host)){
            throw new IllegalArgumentException("invalid " + name + "host defined: not a valid hostname or IP address");
        }
        vlog_option(name + "host", host);
        return host;
    }
    public static final String validateHost(String host) {
        return validateHost(host, null);
    }

    static final int parse_port (String port){
        int port_int = -1;
        try{
            port_int = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid port defined, not an integer");
        }
        if(!isPort(port)){
            throw new IllegalArgumentException("invalid port defined for hosts, must be between 1 and 65535");
        }
        return port_int;
    }

    public static final String[] validateHosts(String[] hosts, int port) {
        if(! isPort(port)){
            throw new IllegalArgumentException("invalid port defined, integer must be between 1 and 65535");
        }
        hosts = uniqArrayOrdered(hosts);
        if(hosts.length < 1){
            throw new IllegalArgumentException("hosts not defined");
        }
        String[] hosts2 = hosts;
        Pattern p = Pattern.compile(":(\\d+)$");
        for(int i=0; i < hosts2.length; i++){
            String node_port = null;
            Matcher m = p.matcher(hosts2[i]);
            if(m.find()){
                node_port = m.group(1);
                if(! isPort(node_port)){
                    throw new IllegalArgumentException("invalid port given for host " + Integer.toString(i+1));
                }
                hosts2[i] = hosts2[i].replaceAll(":\\d+$", "");
            }
            hosts2[i] = validateHost(hosts2[i]);
            //hosts2[i] = validate_resolvable(hosts2[i]);
            if(node_port == null){
                node_port = Integer.toString(port);
            }
            hosts2[i] = hosts2[i] + ":" + node_port;
            vlog_option("port", node_port);
        }
        return hosts2;
    }
    public static final String[] validateHosts(String[] hosts, String port){
        // don't uniq here it's done in called validateHosts method
        return validateHosts(hosts, parse_port(port));
    }
    public static final ArrayList<String> validateHosts(ArrayList<String> hosts, int port){
        // don't uniq here it's done in called validateHosts method
        return arrayToArraylist(validateHosts(arraylistToArray(hosts), port));
    }
    public static final ArrayList<String> validateHosts(ArrayList<String> hosts, String port){
        // don't uniq here it's done in called validateHosts method
        return validateHosts(hosts, parse_port(port));
    }
    public static final String validateHosts(String hosts, int port) {
        if(hosts == null) {
            throw new IllegalArgumentException("hosts not defined (null)");
        }
        if(hosts.trim().isEmpty()){
            throw new IllegalArgumentException("hosts not defined (blank)");
        }
        String[] hosts2 = validateHosts(hosts.split("[,\\s]+"), port);
        String final_hosts = StringUtils.join(hosts2, ",");
        // vlogged in validate_nodeport_list
        //vlog_option("node list", final_hosts);
        return final_hosts;
    }
    public static final String validateHosts(String hosts, String port) {
        return validateHosts(hosts, parse_port(port));
    }


    public static final String validateHostPort(String hostport, String name, Boolean port_required, Boolean novlog) {
        name = name(name);
        if(hostport == null){
            throw new IllegalArgumentException(name + "host:port not defined (null)");
        }
        if(hostport.trim().isEmpty()){
            throw new IllegalArgumentException(name + "host:port not defined (blank)");
        }
        hostport = hostport.trim();
        String[] host_port = hostport.split(":");
        if(host_port.length > 2){
            throw new IllegalArgumentException("invalid " + name + "host:port supplied (too many colon separated components)");
        }
        if(! isHost(host_port[0])){
            throw new IllegalArgumentException("invalid " + name + "host:port '" + hostport + "' defined: host portion '" + host_port[0] + "' is not a valid hostname or IP address");
        }
        if(host_port.length > 1){
            if (!isPort(host_port[1])) {
                throw new IllegalArgumentException(String.format("invalid port '%s' defined for " + name + "host:port: must be a positive integer", host_port[1]));
            }
        } else if (port_required) {
            throw new IllegalArgumentException("port is required");
        }
        if(!novlog){
            vlog_option(name + "hostport", hostport);
        }
        return hostport;
    }
    public static final String validateHostPort(String host, String name, Boolean port_required) {
        return validateHostPort(host, name, port_required, false);
    }
    public static final String validateHostPort(String host, String name) {
        return validateHostPort(host, name, false, false);
    }
    public static final String validateHostPort(String host){
        return validateHostPort(host, null, false, false);
    }


    public static final String validateHostname(String hostname, String name) {
        name = name(name);
        if(hostname == null){
            throw new IllegalArgumentException("hostname not defined (null)");
        }
        if(hostname.trim().isEmpty()){
            throw new IllegalArgumentException("hostname not defined (blank)");
        }
        hostname = hostname.trim();
        if(! isHostname(hostname)){
            throw new IllegalArgumentException("invalid " + name + "hostname '" + hostname + "' defined");
        }
        vlog_option(name + "hostname", hostname);
        return hostname;
    }
    public static final String validateHostname(String hostname) {
        return validateHostname(hostname, null);
    }


    public static final double validateDouble(double d, String name, double  minVal, double maxVal) {
        name = requireName(name);
        if(minVal > maxVal){
            throw new IllegalArgumentException("minVal cannot be > maxVal");
        }
        if(d < minVal){
            throw new IllegalArgumentException("invalid " + name + " defined: cannot be lower than " + minVal);
        }
        if(d > maxVal){
            throw new IllegalArgumentException("invalid " + name + " defined: cannot be greater than " + maxVal);
        }
        vlog_option(name, String.valueOf(d));
        return d;
    }
    public static final long validateLong(long l, String name, long minVal, long maxVal) {
        validateDouble(l, name, minVal, maxVal);
        return l;
    }
    public static final int validateInt(int i, String name, int minVal, int maxVal) {
        validateDouble(i, name, minVal, maxVal);
        return i;
    }
    public static final float validateFloat(float f, String name, float minVal, float maxVal) {
        validateDouble(f, name, minVal, maxVal);
        return f;
    }
    public static final double validateDouble(String d, String name, double minVal, double maxVal) {
        name = requireName(name);
        double d_double = -1;
        try {
            d_double = Double.parseDouble(d);
        } catch (NumberFormatException e){
            throw new IllegalArgumentException("invalid " + name + " defined: must be numeric (double)");
        }
        // log.info(String.format(option_format, option + ":", value)); done in validateDouble
        validateDouble(d_double, name, minVal, maxVal);
        return d_double;
    }
    public static final long validateLong(String l, String name, long minVal, long maxVal) {
        name = requireName(name);
        long l_long = -1;
        try {
            l_long = Long.parseLong(l);
        } catch (NumberFormatException e){
            throw new IllegalArgumentException("invalid " + name + " defined: must be numeric (long)");
        }
        // log.info(String.format(option_format, option + ":", value)); done in validateLong
        validateDouble(l_long, name, minVal, maxVal);
        return l_long;
    }
    public static final int validateInt(String i, String name, int minVal, int maxVal) {
        name = requireName(name);
        int i_int = -1;
        try {
            i_int = Integer.parseInt(i);
        } catch (NumberFormatException e){
            //if(debug){
            //	e.printStackTrace();
            //}
            throw new IllegalArgumentException("invalid " + name + " defined: must be numeric (int)");
        }
        // log.info(String.format(option_format, option + ":", value)); done in pass through to validateLong
        validateDouble(i_int, name, minVal, maxVal);
        return i_int;
    }
    public static final float validateFloat(String f, String name, float minVal, float maxVal) {
        name = requireName(name);
        float f_float = -1;
        try {
            f_float = Float.parseFloat(f);
        } catch (NumberFormatException e){
            throw new IllegalArgumentException("invalid " + name + " defined: must be numeric (float)");
        }
        // log.info(String.format(option_format, option + ":", value)); done in pass through to validateLong
        validateDouble(f_float, name, minVal, maxVal);
        return f_float;
    }


    public static final String validateInterface(String networkInterface) {
        if(networkInterface == null){
            throw new IllegalArgumentException("network interface not defined (null)");
        }
        if(networkInterface.trim().isEmpty()){
            throw new IllegalArgumentException("network interface not defined (blank)");
        }
        networkInterface = networkInterface.trim();
        if(! isInterface(networkInterface)){
            throw new IllegalArgumentException("invalid network interface defined: must be either eth<N>, bond<N> or lo<N>");
        }
        vlog_option("interface", networkInterface);
        return networkInterface;
    }


    public static final String validateIP(String ip, String name) {
        name = name(name);
        if(ip == null){
            throw new IllegalArgumentException(name + "IP not defined (null)");
        }
        if(ip.trim().isEmpty()){
            throw new IllegalArgumentException(name + "IP not defined (blank)");
        }
        ip = ip.trim();
        if(! isIP(ip)){
            throw new IllegalArgumentException("invalid " + name + "IP defined");
        }
        vlog_option(name + "ip", ip);
        return ip;
    }
    public static final String validateIP(String ip) {
        return validateIP(ip, null);
    }


    public static final String validateKrb5Princ(String princ, String name) {
        name = name(name);
        if(princ == null) {
            throw new IllegalArgumentException(name + "krb5 principal not defined (null)");
        }
        if(princ.trim().isEmpty()){
            throw new IllegalArgumentException(name + "krb5 principal not defined (blank)");
        }
        princ = princ.trim();
        if(! isKrb5Princ(princ)){
            throw new IllegalArgumentException("invalid " + name + "krb5 principal defined");
        }
        vlog_option(name + "krb5 principal", princ);
        return princ;
    }
    public static final String validateKrb5Princ(String princ) {
        return validateKrb5Princ(princ, null);
    }


    public static final String validateKrb5Realm(String realm, String name) {
        name = name(name);
        if(realm == null){
            throw new IllegalArgumentException(name + "realm not defined (null)");
        }
        if(realm.trim().isEmpty()){
            throw new IllegalArgumentException(name + "realm not defined (blank)");
        }
        realm = realm.trim();
        if(! isDomain(realm)){
            throw new IllegalArgumentException("invalid " + name + "realm defined");
        }
        vlog_option(name + "realm", realm);
        return realm;
    }
    public static final String validateKrb5Realm(String realm) {
        return validateKrb5Realm(realm, null);
    }


    public static final String validateLabel(String label) {
        if(label == null){
            throw new IllegalArgumentException("label not defined (null)");
        }
        if(label.trim().isEmpty()){
            throw new IllegalArgumentException("label not defined (blank)");
        }
        label = label.trim();
        if(! isLabel(label)){
            throw new IllegalArgumentException("invalid label defined: must be an alphanumeric identifier");
        }
        vlog_option("label", label);
        return label;
    }


    public static final String validateLdapDn(String dn, String name) {
        name = name(name);
        if(dn == null){
            throw new IllegalArgumentException("ldap " + name + "dn not defined (null)");
        }
        if(dn.trim().isEmpty()){
            throw new IllegalArgumentException("ldap " + name + "dn not defined (blank)");
        }
        dn = dn.trim();
        if(! isLdapDn(dn)){
            throw new IllegalArgumentException("invalid " + name + "ldap dn defined");
        }
        vlog_option("ldap " + name + "dn", dn);
        return dn;
    }
    public static final String validateLdapDn(String dn) {
        return validateLdapDn(dn, null);
    }


    public static final ArrayList<String> validateNodeList(ArrayList<String> nodes){
        ArrayList<String> final_nodes = new ArrayList<String>();
        nodes = uniqArraylistOrdered(nodes);
        if(nodes.size() < 1){
            throw new IllegalArgumentException("node(s) not defined");
        }
        for(String node: nodes){
            //node = node.trim();
            for(String node2: node.split("[,\\s]+")){
                node2 = node2.trim();
                if(! isHost(node2)){
                    throw new IllegalArgumentException("invalid node name '" + node2 + "': must be hostname/FQDN or IP address");
                }
                final_nodes.add(node2);
            }
        }
        if(final_nodes.size() < 1){
            throw new IllegalArgumentException("node(s) not defined (empty nodes given)");
        }
        vlog_option("node list", final_nodes.toString());
        return final_nodes;
    }
    public static final String[] validateNodeList(String[] nodes){
        // don't uniq here it's done in called validateNodeList method
        return arraylistToArray(validateNodeList(arrayToArraylist(nodes)));
    }
    public static final String validateNodeList(String nodelist) {
        if(nodelist == null) {
            throw new IllegalArgumentException("node(s) not defined (null)");
        }
        if(nodelist.trim().isEmpty()){
            throw new IllegalArgumentException("node(s) not defined (blank)");
        }
        String[] nodelist2 = validateNodeList(nodelist.split("[,\\s]+"));
        String final_nodes = StringUtils.join(nodelist2, ",");
        // vlogged in validateNodeList
        //vlog_option("node list", final_nodes);
        return final_nodes;
    }


    public static final ArrayList<String> validate_nodeport_list (ArrayList<String> nodes) {
        ArrayList<String> final_nodes = new ArrayList<String>();
        nodes = uniqArraylistOrdered(nodes);
        if(nodes.size() < 1){
            throw new IllegalArgumentException("node(s) not defined");
        }
        for(String node: nodes){
            //node = node.trim();
            for(String node2: node.split("[,\\s]+")){
                //node2 = node2.trim();
                final_nodes.add( validateHostPort(node2) );
            }
        }
        vlog_option("nodeport list", final_nodes.toString());
        return final_nodes;
    }
    public static final String[] validate_nodeport_list(String[] nodes){
        // don't uniq here it's done in called validate_nodeport_list method
        return arraylistToArray(validate_nodeport_list(arrayToArraylist(nodes)));
    }
    public static final String validate_nodeport_list (String nodelist) {
        if(nodelist == null) {
            throw new IllegalArgumentException("node(s) not defined (null)");
        }
        if(nodelist.trim().isEmpty()){
            throw new IllegalArgumentException("node(s) not defined (blank)");
        }
        String[] nodelist2 = validate_nodeport_list(nodelist.split("[,\\s]+"));
        String final_nodes = StringUtils.join(nodelist2, ",");
        // vlogged in validate_nodeport_list
        //vlog_option("node list", final_nodes);
        return final_nodes;
    }


    public static final String validate_nosql_key (String key, String name) {
        name = name(name);
        if(key == null){
            throw new IllegalArgumentException(name + "key not defined (null)");
        }
        if(key.trim().isEmpty()){
            throw new IllegalArgumentException(name + "key not defined (blank)");
        }
        key = key.trim();
        if(! isNoSqlKey(key)){
            throw new IllegalArgumentException("invalid " + name + "key defined: may only contain characters: alphanumeric, commas, colons, underscores, pluses and dashes");
        }
        vlog_option(name + "key", key);
        return key;
    }
    public static final String validate_nosql_key (String key) {
        return validate_nosql_key(key, null);
    }


    public static final int validate_port (int port, String name) {
        name = name(name);
        if(! isPort(port)){
            throw new IllegalArgumentException("invalid " + name + "port defined");
        }
        vlog_option(name + "port", String.valueOf(port));
        return port;
    }
    public static final String validate_port (String port, String name){
        name = name(name);
        int port_int = -1;
        try {
            port_int = Integer.parseInt(port);
        } catch (NumberFormatException e){
            throw new IllegalArgumentException("invalid " + name + "port specified: must be numeric");
        }
        return String.valueOf(validate_port(port_int, name));
    }
    public static final int validate_port (int port) {
        return validate_port(port, null);
    }
    public static final String validate_port (String port) {
        return validate_port(port, null);
    }


    public static final String validate_process_name (String process, String name) {
        name = name(name);
        if(process == null){
            throw new IllegalArgumentException(name + "process name not defined (null)");
        }
        if(process.trim().isEmpty()){
            throw new IllegalArgumentException(name + "process name not defined (blank)");
        }
        process = process.trim();
        if(! isProcessName(process)){
            throw new IllegalArgumentException("invalid " + name + "process name defined:");
        }
        return process;
    }
    public static final String validate_process_name (String process) {
        return validate_process_name(process, null);
    }


    public static final String validate_program_path (String path, String name, String regex) {
        name = requireName(name).trim();
        if(path == null){
            throw new IllegalArgumentException(name + " path not defined (null)");
        }
        if(path.trim().isEmpty()){
            throw new IllegalArgumentException(name + " path not defined (blank)");
        }
        path = path.trim();
        if(! path.matches("^[./]")){
            try {
                path = which(path);
            } catch (IOException e){
//                throw new IllegalArgumentException(name + " program not found in $PATH (" + System.getenv("PATH") + ")");
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        if(regex == null || regex.trim().isEmpty()){
            regex = name;
        }
        if(validate_regex(regex, "program path regex", true) == null){
            throw new IllegalArgumentException("invalid regex given to validate_program_path()");
        }
        validateFilename(path, null, true);
//        if(validateFilename(path, null, true) == null){
//            throw new IllegalArgumentException("invalid path given for " + name + ", failed filename regex");
//        }
        if(! path.matches("(?:^|.*/)" + regex + "$")){
           throw new IllegalArgumentException("invalid path given for " + name + ", is not a path to the " + name + " command");
        }
        File f = new File(path);
        if( ! ( f.exists() && f.isFile() ) ){
            throw new IllegalArgumentException(path + " not found");
        }
        if(!f.canExecute()){
            throw new IllegalArgumentException(path + " not executable");
        }
        vlog_option(name + " program path", path);
        return path;
    }
    public static final String validate_program_path (String path, String name) {
        return validate_program_path(path, name, null);
    }


    public static final String validate_regex (String regex, String name, Boolean posix) {
        name = name(name);
        // intentionally not trimming
        if(regex == null){
            throw new IllegalArgumentException(name + "regex not defined (null)");
        }
        if(regex.trim().isEmpty()){
            throw new IllegalArgumentException(name + "regex not defined (blank)");
        }
        if(posix){
            if(regex.matches(".*(?:\\$\\(|`).*")){
                throw new IllegalArgumentException("invalid " + name + "posix regex supplied: contains sub shell metachars ( $( / ` ) that would be dangerous to pass to shell");
            }
            // TODO: cmd("egrep '$regex' < /dev/null") and check for any output signifying error with the regex
        } else {
            try {
                "".matches(regex);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("invalid " + name + "regex defined");
            }
        }
        vlog_option(name + "regex", regex);
        return regex;
    }
    public static final String validate_regex (String regex, String name) {
        return validate_regex(regex, name, false);
    }


    public static final String validate_user (String user, String name) {
        name = name(name);
        if(user == null){
            throw new IllegalArgumentException(name + "user not defined (null)");
        }
        if(user.trim().isEmpty()){
            throw new IllegalArgumentException(name + "user not defined (blank)");
        }
        user = user.trim();
        if(! isUser(user)){
            throw new IllegalArgumentException("invalid " + name + "username defined: must be alphanumeric");
        }
        vlog_option(name + "user", user);
        return user;
    }
    public static final String validate_user (String user) {
        return validate_user(user, null);
    }


    public static final String validate_user_exists (String user, String name) throws IOException, UnsupportedOSException {
        user = validate_user(user, name);
        name = name(name);
        if(! userExists(user)){
            throw new IllegalArgumentException("invalid " + name + "user defined, not found on local system");
        }
        return user;
    }


    public static final String validate_password (String password, String name, Boolean allow_all) {
        name = name(name);
        if(password == null){
            throw new IllegalArgumentException(name + "password not defined (null)");
        }
        if(password.trim().isEmpty()){
            throw new IllegalArgumentException(name + "password not defined (blank)");
        }
        if(allow_all){
            return password;
        }
        if(password.matches(".*[\"'`].*")){
            throw new IllegalArgumentException("invalid " + name + "password defined: may not contain quotes or backticks");
        }
        if(password.matches(".*\\$\\(.*")){
            throw new IllegalArgumentException("invalid " + name + "password defined: may not contain $( as this is a subshell escape and could be dangerous to pass through to programs, especially on the command line");
        }
        vlog_option("password", "<omitted>");
        return password;
    }
    public static final String validate_password (String password, String name) {
        return validate_password(password, name, false);
    }
    public static final String validate_password (String password) {
        return validate_password(password, null, false);
    }


    public static final String validate_resolvable (String host, String name) throws UnknownHostException {
        name = name(name);
        if(host == null) {
            throw new IllegalArgumentException(name + "host not defined (null)");
        }
        if(host.trim().isEmpty()){
            throw new IllegalArgumentException(name + "host not defined (blank)");
        }
        host = host.trim();
        // throws exception now, no nulls
        String ip = resolveIp(host);
//        if(ip == null){
//            quit("CRITICAL", "failed to resolve " + name + "host '" + host + "'");
//        }
        return ip;
    }
    public static final String validate_resolvable (String host) throws UnknownHostException {
        return validate_resolvable(host, null);
    }


    public static final String validate_units (String units, String name) {
        name = name(name);
        if(units == null) {
            throw new IllegalArgumentException(name + "units not defined (null)");
        }
        if(units.trim().isEmpty()){
            throw new IllegalArgumentException(name + "units not defined (blank)");
        }
        units = units.trim();
        if(! isNagiosUnit(units)){
            throw new IllegalArgumentException("invalid " + name + "units '" + units + "' defined, must be one of: " + valid_units.toString());
        }
        vlog_option(name + "units", units);
        return units;
    }
    public static final String validate_units (String units) {
        return validate_units(units, null);
    }


    public static final String validate_url (String url, String name) {
        name = name(name);
        if(url == null) {
            throw new IllegalArgumentException(name + "url not defined (null)");
        }
        if(url.trim().isEmpty()){
            throw new IllegalArgumentException(name + "url not defined (blank)");
        }
        url = url.trim();
        if(!url.contains("://")){
            url = "http://" + url;
        }
        if(! isUrl(url)){
            throw new IllegalArgumentException("invalid " + name + "url defined: '" + url + "'");
        }
        vlog_option(name + "url", url);
        return url;
    }
    public static final String validate_url (String url) {
        return validate_url(url, null);
    }


    public static final String validate_url_path_suffix (String url, String name) {
        name = name(name);
        if(url == null) {
            throw new IllegalArgumentException(name + "url not defined (null)");
        }
        if(url.trim().isEmpty()){
           throw new IllegalArgumentException(name + "url not defined (blank)");
        }
        url = url.trim();
        if(! isUrlPathSuffix(url)){
            throw new IllegalArgumentException("invalid " + name + "url defined: '" + url + "'");
        }
        return url;
    }
    public static final String validate_url_path_suffix (String url) {
        return validate_url_path_suffix(url, null);
    }

    public static final String getVersion () {
        return utils_version;
    }

    public static final void version () {
        println("Hari Sekhon Java Utils version " + getVersion());
    }

    public static final String which (String bin) throws IOException {
        if(bin == null || bin.trim().isEmpty()){
            throw new IllegalArgumentException("no bin passed to which()");
        }
        // TODO: should probably consider switching this to os path sep instead of unix biased /
        if(bin.matches("^(?:/|\\./).*")){
            File f = new File(bin);
            if(f.isFile()){
                if(f.canExecute()){
                   return bin;
                } else {
                    throw new IOException(String.format("'%s' is not executable!", bin));
                }
            } else {
                throw new IOException(String.format("couldn't find executable '%s'", bin));
            }
        } else {
            for(String path: System.getenv("PATH").split(":")){
                String fullpath = path + "/" + bin;
                File f = new File(fullpath);
//                if(f.exists() && ! f.isDirectory()){
//                    if(! f.canExecute()){
//                        throw new IOException(String.format("'%s' is not executable!", bin);
//                    }
                if(f.isFile() && f.canExecute()){
                    return fullpath;
                }
            }
            throw new IOException(String.format("couldn't find '%s' in PATH (%s)", bin, System.getenv("PATH")));
        }
    }

    // ===================================================================== //
    //
    //                            L o g g i n g
    //
    // ===================================================================== //

    // use verbose to set log.setLogLevel one higher

//    public static final int getVerbose(){
//        return log.getLogLevel();
//    }

    public static final void setVerbose(int verbosity){
        if(verbosity > 4) {
            log.setLevel(Level.ALL);
        } else if(verbosity > 3) {
            log.setLevel(Level.TRACE);
        } else if(verbosity == 3) {
            log.setLevel(Level.INFO);
        } else if (verbosity == 2) {
            log.setLevel(Level.WARN);
        } else if (verbosity == 1) {
            log.setLevel(Level.ERROR);
        } else if (verbosity == 0) {
            log.setLevel(Level.FATAL);
        } else {
            log.setLevel(Level.INFO);
        }
    }

    public static final String get_calling_method(){
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String parent = "Could not find calling method";
        if(stackTraceElements.length > 3){
            parent = String.valueOf(stackTraceElements[3]);
        }
        return parent.replaceFirst("com.linkedin.harisekhon.Utils.", "");
    }

    public static final void vlog (String msg) {
//        log.info(String.format(get_calling_method() + " - " + msg));
        log.warn(msg);
    }

    public static final void vlog2 (String msg) {
//        log.debug(String.format(get_calling_method() + " - " + msg));
        log.info(msg);
    }

    public static final void vlog3 (String msg) {
//        log.trace(String.format(get_calling_method() + " - " + msg));
        log.debug(msg);
    }

    public static final void vlog_option (String name, String value) {
//        log.info(String.format(option_format_calling_method, get_calling_method(), name + ":", value));
        vlog2(String.format(option_format, name + ":", value));
    }
    public static final void vlog_option (String name, boolean value){
        vlog_option(name, String.valueOf(value));
    }

//    public static final void log.info(String.format(option_format, option + ":", value));_bool (String option, Boolean value) {
//        vlog_option(option, value.toString());
//    }
}
