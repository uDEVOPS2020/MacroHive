package uProxy.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = { "uProxy" })
public class UProxyApplication {

	public static String proxyID = "";

	public static String proxedIP = "n.d.";
	public static String server = "host.docker.internal";
	public static int port = 20000;

	public static String collectorServer = "host.docker.internal";
	public static int collectorPort = 11112;

	public static void main(String[] args) {



		if (args.length < 3) {
			System.out.println("[ERROR] Application needs at least 3 input parameters (proxyID, serverToProxy, portToProxy, <optional>proxedIP) ");
		} else {
			
			System.out.println("ARG-0 (proxyID) = " + args[0]);
			System.out.println("ARG-1 (serverToProxy) = " + args[1]);
			System.out.println("ARG-2 (portToProxy) = " + args[2]);
			
			if(args.length == 4) {
				System.out.println("ARG-3 (proxedIP) = " + args[3]);
				proxedIP = args[3];
			}
			
			proxyID = args[0];
			server = args[1];
			try {
				port = Integer.parseInt(args[2]);

				SpringApplication.run(UProxyApplication.class, args);
			} catch (NumberFormatException e) {
				System.out.println("[ERROR] Port number must be an integer");
			}

		}
	}

}
