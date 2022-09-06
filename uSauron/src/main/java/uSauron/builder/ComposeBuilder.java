package uSauron.builder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class ComposeBuilder {

	@SuppressWarnings("unchecked")
	public static String proxyCompose(String composeFile) {

		Integer portCount = 20000;
		Yaml yaml = new Yaml();
		InputStream inputStream = new ByteArrayInputStream(composeFile.getBytes());
		Map<String, Object> obj = yaml.load(inputStream);

		Map<String, Object> services = (Map<String, Object>) obj.get("services");
		Map<String, Object> proxiedServices = new HashMap<>();
		proxiedServices.putAll(services);

		for (Map.Entry<String, Object> singleService : services.entrySet()) {

			ArrayList newPorts = new ArrayList();

			Map<String, Object> tempContentService = (Map<String, Object>) singleService.getValue();

			if (tempContentService.get("ports") != null && tempContentService.get("command") == null) {

				newPorts.add(portCount.toString() + ":" + portCount.toString());

				// modifico nome servizio
				proxiedServices.put(singleService.getKey() + "-proxied",
						proxiedServices.remove(singleService.getKey()));

				Map<String, Object> contentService = (Map<String, Object>) proxiedServices
						.get(singleService.getKey() + "-proxied");


				contentService.put("command", "[\"java\", \"-Dserver.port=" + portCount
						+ "\", \"-Xmx200m\",  \"-jar\", \"/app/" + singleService.getKey() + "-1.0.jar\"]");

				ArrayList tempPorts = new ArrayList((ArrayList) contentService.get("ports"));
																								
				contentService.put("ports", newPorts);

				// creo proxy
				Map<String, Object> contentProxyService = new HashMap<>();

				contentProxyService.put("container_name", singleService.getKey());
				contentProxyService.put("image", "uproxy:latest");
				contentProxyService.put("ports", tempPorts);
				String[] parts = tempPorts.toString().split(":");

				if (tempContentService.get("networks") != null) {
					ArrayList tempNetwork = new ArrayList((ArrayList) tempContentService.get("networks"));
																											
					contentProxyService.put("networks", tempNetwork);
				}
				
				contentProxyService.put("command", "[\"java\",\"-Dserver.port="+ parts[0].replace("[", "") +"\",\"-jar\",\"/uproxy.jar\", \""+singleService.getKey()+"\", \"host.docker.internal\", \""+ portCount +"\"]");
				proxiedServices.put(singleService.getKey(), contentProxyService);
				portCount++;
			} else {
				if (tempContentService.get("ports") == null) {
					if (tempContentService.get("command") != null) {
						System.out.println("[WARNING] Service with command and no ports: \"" + singleService.getKey() + "\"");
					} else {
						System.out.println("[WARNING] Service with no ports: \"" + singleService.getKey() + "\"");
					}
				} else {
					System.out.println(
							"[WARNING] Service with command: \"" + singleService.getKey() + "\"");
					tempContentService.put("command", tempContentService.get("command").toString());
				}
			}
		}

		obj.put("services", proxiedServices);

		DumperOptions options = new DumperOptions();
		options.setIndent(2);
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

		Yaml yamlOut = new Yaml(options);
		StringWriter writer = new StringWriter();
		yamlOut.dump(obj, writer);

		String finalOutput = writer.toString();
		
		finalOutput = finalOutput.replace("'[", "[");
		finalOutput = finalOutput.replace("]'", "]");
		
//		finalOutput = finalOutput.replaceAll("[&,*]id[0-9]+", "");
//		finalOutput = finalOutput.replaceAll("!!", "#");

		return finalOutput;
	}

}
