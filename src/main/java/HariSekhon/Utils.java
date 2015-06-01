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
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
//
//  http://www.linkedin.com/in/harisekhon
//

// Port of my personal libraries from other languages I've been using for several years

package HariSekhon;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

public class Utils {
	
	public static boolean stdout = false;
	public static final HashMap<String, Integer> exit_codes = new HashMap<String, Integer>();
	
	static {
		// java autoboxing
		exit_codes.put("OK", 		0);
		exit_codes.put("WARNING", 	1);
		exit_codes.put("CRITICAL", 	2);
		exit_codes.put("UNKNOWN", 	3);
		exit_codes.put("DEPENDENT", 4);
	}
	
	// years and years of Regex expertise and testing has gone in to this, do not edit!
	// This also gives flexibility to work around some situations where domain names may not be quite valid (eg .local/.intranet) but still keep things quite tight
	// There are certain scenarios where Google Guava and Apache Commons libraries don't help with these
	public static final String ip_prefix_regex 			= "\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}";
	// now allowing 0 or 255 as the final octet due to CIDR
	public static final String ip_regex 				= ip_prefix_regex + "(?:25[0-5]|2[0-4][0-9]|[01]?[1-9][0-9]|[01]?0[1-9]|[12]00|[0-9])\\b";
	public static final String hostname_component_regex = "\\b[A-Za-z](?:[A-Za-z0-9_\\-]{0,61}[a-zA-Z0-9])?\\b";
	// TODO: replace this with IANA TLDs as done in my Perl lib
	public static final String tld_regex				= "\\b(?:[A-Za-z]{2,4}|london|museum|travel|local|localdomain|intra)\\b";
	public static final String domain_component			= "\\b[a-zA-Z0-9](?:[a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\b";
	public static final String domain_regex				= "(?:" + domain_component + "\\.)*" + tld_regex;
	public static final String domain_regex2 			= "(?:" + domain_component + "\\.)*" + tld_regex;
	public static final String hostname_regex			= "%s(?:\\.%s)?".format(hostname_component_regex, domain_regex);
	public static final String host_regex 	  			= "\\b(?:%s|%s)\\b".format(hostname_regex, ip_regex);
	public static final String filename_regex 			= "[\\/\\w\\s_\\.:,\\*\\(\\)\\=\\%\\?\\+-]+";
	public static final String rwxt_regex 	  	 		= "[r-][w-][x-][r-][w-][x-][r-][w-][xt-]";
	public static final String fqdn_regex 	  	 		= hostname_component_regex + "\\." + domain_regex;
	public static final String email_regex 	  	 		= "\\b[A-Za-z0-9](?:[A-Za-z0-9\\._\\%\\'\\+-]{0,62}[A-Za-z0-9\\._\\%\\+-])?@" + domain_regex + "\\b";
	public static final String subnet_mask_regex 		= "\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[1-9][0-9]|[01]?0[1-9]|[12]00|[0-9])\\b";
	public static final String mac_regex				= "\\b[0-9A-F-af]{1,2}[:-](?:[0-9A-Fa-f]{1,2}[:-]){4}[0-9A-Fa-f]{1,2}\\b";
	public static final String process_name_regex		= "[\\w\\s_\\.\\/\\<\\>-]+";
	public static final String url_path_suffix_regex	= "/(?:[\\w\\.,:\\/\\%\\&\\?\\!\\=\\*\\|\\[\\]\\+-]+)?";
	public static final String url_regex				= "\\b(?i:https?://" + host_regex + "(?::\\d{1,5})?(?:" + url_path_suffix_regex + ")?)";
	public static final String user_regex				= "\\b[A-Za-z][A-Za-z0-9-]*[A-Za-z0-9]\\b";
	public static final String column_regex				= "\\b[\\w\\:]+\\b";
	public static final String ldap_dn_regex			= "\\b\\w+=[\\w\\s]+(?:,\\w+=[\\w\\s]+)*\\b";
	public static final String krb5_principal_regex		= "%s(?:/%s)?(?:@%s)?".format(user_regex, hostname_regex, domain_regex);
	public static final String threshold_range_regex	= "^(@)?(-?\\d+(?:\\.\\d+)?)(:)(-?\\d+(?:\\.\\d+)?)?$";
	public static final String threshold_simple_regex	= "^(-?\\d+(?:\\.\\d+)?)$";
	public static final String version_regex = "\\d(\\.\\d+)*";
	
	public static void code_error (String msg) {
		println("CODE ERROR: " + msg);
		System.exit(exit_codes.get("UNKNOWN"));
	}
	
	public static void quit (String status, String msg) {
		println(status + ": " + msg);
		if(exit_codes.containsKey(status)) {
			System.exit(exit_codes.get(status));
		} else {
			// TODO: provide a warning stack trace here
			code_error("specified an invalid exit status '%s' to quit()".format(status));
		}
	}
	
	public static void usage(String msg){
		println("usage: " + msg);
		System.exit(exit_codes.get("UNKNOWN"));
	}

	private static void println(String msg){
		if(stdout){
			System.out.println(msg);
		} else {
			System.err.println(msg);
		}
	}
	
	// neither Google's com.Guava google.common.net.HostSpecifier nor Apache Commons org.apache.commons.validator.routines.DomainValidator are suitable for my needs here, must port the more flexible regex methods from my Perl library
	public static String isHost(String host){
		if(host == null){
			return null;
		}
		if(host.length() > 255){
			return isIP(host);
		} else if(host.matches("^" + host_regex + "$")){
			return host;
		} else if(isIP(host) != null){
			return host;
		} else {
			return null;
		}
	}
	
	public static String isIP(String ip){
		if(ip == null){
			return null;
		}
		if(!ip.matches("^" + ip_regex + "$")){
			return null;
		}
		String[] octets = ip.split("\\.");
		if(octets.length != 4){
			return null;
		}
		for(String octet: octets){
			int octet_int;
			try{
				octet_int = Integer.parseInt(octet);
			} catch (Exception e) {
				return null;
			}
			if(octet_int < 0 || octet_int > 255){
				return null;
			}
		}
		return ip;
	}
	
	public static String isPort(String port){
		if(port == null){
			return null;
		}
		if(!port.matches("^\\d+$")){
			return null;
		}
		int port_int;
		try{
			port_int = Integer.parseInt(port);
		} catch (Exception e) {
			return null;
		}
		if(port_int >= 1 && port_int <= 65535){
			return port;
		}
		return null;
	}

    public static String validate_hostport(String hostport){
    	if(hostport == null){
    		quit("UNKNOWN", "host:port not defined");
    	}
    	//if(name != null){
    		// TODO: log here with name
    	//}
    	String[] host_port = hostport.split(":");
    	if(host_port.length > 2){
    		quit("UNKNOWN", "invalid host:port supplied");
    	}
    	if(isHost(host_port[0]) == null){
    		// this only prints the host and loses the message
    		//quit("CRITICAL", "invalid host:port '%s' defined: host portion is not a valid hostname or IP address".format(hostport));
    		quit("CRITICAL", "invalid host:port '" + hostport + "' defined: host portion '" + host_port[0] + "' is not a valid hostname or IP address");
    	}
    	if(host_port.length > 1){
    		if(isPort(host_port[1]) == null){
    			quit("CRITICAL", "invalid port '%s' defined for host:port: must be a positive integer".format(host_port[1]));
    		}
    	}
    	return hostport;
    }
    
    public static String validate_nodeport_list(String nodelist){
    	if(nodelist == null){
    		quit("UNKNOWN", "node(s) not defined");
    	}
    	ArrayList<String> nodelist2 = new ArrayList<String>();
    	for(String host: nodelist.split("\\s*,\\s*")){
    		host = validate_hostport(host);
    		nodelist2.add(host);
    	}
    	return StringUtils.join(nodelist2, ","); 
    }
    
    // TODO: replace this with Log4j now it's in Java
    public static void vlog_options(String option, String value){
    	println("%-25s %s".format("%s:".format(option), value));
    }
    
    public static void vlog_options_bool(String option, Boolean value){
    	vlog_options(option, value.toString());
    }
    
}