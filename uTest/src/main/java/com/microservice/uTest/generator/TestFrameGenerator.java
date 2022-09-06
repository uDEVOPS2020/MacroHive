/*
tf == testframes (un testframe identifica una chiamata con determinate classi di input per ogni input)
si differenziano dai testcase perchè questi ultimi sono i test veri e propri che vanno a porre dei valori per quelle classi di input
esempio: input(user, psw) [testframe: input(stringa alfanumerica < 5char, stringa alfanumerica < 10char)] [testcase: input(luca, luca123)]

*/
package com.microservice.uTest.generator;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import com.microservice.uTest.dataStructure.InputClass;
import com.microservice.uTest.dataStructure.TestFrame;

import io.swagger.parser.SwaggerParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.models.*;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.AbstractNumericProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BaseIntegerProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.models.refs.RefFormat;

public class TestFrameGenerator {
	private static boolean DOCKER_INTERNAL_MODE = true;

	private static boolean DEBUG_MODE = false;
	private static boolean QUICK_MODE = false;
	private static boolean QUICK_NV_MODE = false;
	private static boolean PAIRWISE_MODE = false;
	private static boolean VALID_MODE = false;
	private static boolean VALID_MODE_P = false;
	private static boolean INVALID_MODE = false;
	private static boolean PAIRWISE_NV_MODE = false;
	private static boolean VALID_INVALID_MODE = false;
	private static boolean QUICK_BOUNDED_MODE = false;
	private static boolean QUICK_BOUNDED_NV_MODE = false;

	private static final int N_IC_INTEGER = 7;
	private static final int N_IC_STRING = 3;
	private static final int N_IC_LANGUAGE = 3;
	// private static final int N_IC_SYMBOL = 3;
	private static final int N_IC_BOOLEAN = 2;
	private static final int N_IC_DEFAULT = 0;
	private static final int N_IC_QUICK = 2;
	private static final int N_IC_VALID = 1;

	private static final String hostPassed = "192.168.1.132";
	private static Integer quickmodeBound;
	private static Integer boundPerMethodCount;

	public ArrayList<TestFrame> testFrames;

	private int count;
	private int offset;
	private int countTfFile;
	private String host;
	private static boolean skipMethod = false;
	private static boolean methodHasNotRequiredBodyPar;

	private int jsonID;
	private int pathID;
	private int pathMethodID;

	// Statistics
	private int methodCount;
	private int pathCount;

	public TestFrameGenerator() throws IOException {
		super();
		testFrames = new ArrayList<TestFrame>();
		count = 0;
		countTfFile = 0;
		host = "";
		boundPerMethodCount = 0;
		pathID = 0;
		pathMethodID = 0;
		methodCount = 0;
		pathCount = 0;
		
		DOCKER_INTERNAL_MODE = true;

		DEBUG_MODE = false;
		QUICK_MODE = false;
		QUICK_NV_MODE = false;
		PAIRWISE_MODE = false;
		VALID_MODE = false;
		VALID_MODE_P = false;
		INVALID_MODE = false;
		PAIRWISE_NV_MODE = false;
		VALID_INVALID_MODE = false;
		QUICK_BOUNDED_MODE = false;
		QUICK_BOUNDED_NV_MODE = false;
		skipMethod = false;
	}

	public TestFrameGenerator(boolean _debug) throws IOException {
		super();
		testFrames = new ArrayList<TestFrame>();
		count = 0;
		countTfFile = 0;
		host = "";
		boundPerMethodCount = 0;
		pathID = 0;
		pathMethodID = 0;
		methodCount = 0;
		pathCount = 0;
		
		DOCKER_INTERNAL_MODE = true;

		DEBUG_MODE = _debug;
		QUICK_MODE = false;
		QUICK_NV_MODE = false;
		PAIRWISE_MODE = false;
		VALID_MODE = false;
		VALID_MODE_P = false;
		INVALID_MODE = false;
		PAIRWISE_NV_MODE = false;
		VALID_INVALID_MODE = false;
		QUICK_BOUNDED_MODE = false;
		QUICK_BOUNDED_NV_MODE = false;
		skipMethod = false;
	}

	@SuppressWarnings("unchecked")
	public int JsonURIParser(String path, int jsonID, PrintWriter logWriter) throws IOException {

		if (QUICK_BOUNDED_NV_MODE) {
			QUICK_BOUNDED_MODE = true;
			QUICK_NV_MODE = true;
		}

		if (VALID_MODE_P || PAIRWISE_NV_MODE)
			PAIRWISE_MODE = true;

		if (PAIRWISE_MODE || VALID_INVALID_MODE || QUICK_BOUNDED_MODE || QUICK_NV_MODE)
			QUICK_MODE = true;

		this.jsonID = jsonID;

		System.out.println();
		logWriter.println();
		System.out.println("[INFO] [Generator] Json path: " + path);
		logWriter.println("[INFO] [Generator] Json path: " + path);

		count = 0;
		countTfFile = 0;
		offset = testFrames.size();
		String basePath = null;

		Swagger swagger = new SwaggerParser().read(path);

		if (swagger == null) {
			System.out.println("[ERROR] [Generator] Swagger parse failed, OpenAPI v3 not supported");
			logWriter.println("[ERROR] [Generator] Swagger parse failed, OpenAPI v3 not supported");
		}
		
		host = swagger.getHost();
		basePath = swagger.getBasePath();
		
		if (DOCKER_INTERNAL_MODE) {
			String[] parts = host.split(":");
			host = "host.docker.internal:" + parts[1];
		}
		

		if (!host.contains(":")) {
			System.out.println("[" + ansi().fgBright(YELLOW).a("WARNING").reset() + "] [Generator] Host found: \""
					+ host + "\", Host written: \"" + hostPassed + "/" + host + "\"");
			logWriter.println("[WARNING] [Generator] Host found: \"" + host + "\", Host written: \"" + hostPassed + "/"
					+ host + "\"");
			host = hostPassed + "/" + host;
		}

		if (basePath != null && !basePath.equals("/"))
			host += basePath;

		if (DEBUG_MODE) {
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Generator] Description: "
					+ swagger.getInfo().getDescription());
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Generator] Host: " + host);

		}

		for (Map.Entry<String, Path> entry : swagger.getPaths().entrySet()) {

			pathCount++;
			pathID++;
			String uri = entry.getKey();

			if (DEBUG_MODE) {
				System.out.println();
				System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
						+ "] --------------------------------------------------------");
				System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] Path: " + entry.getKey());
			}

			for (Map.Entry<HttpMethod, Operation> op : entry.getValue().getOperationMap().entrySet()) {

				String method = op.getKey().toString();
				
				pathMethodID++;
				methodHasNotRequiredBodyPar = false;
				methodCount++;

				if (DEBUG_MODE) {
					System.out.println();
					System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] Method: " + op.getKey());
					System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "]	Parameters #"
							+ op.getValue().getParameters().size() + " : ");
				}

				if (op.getValue().getParameters().size() != 0) {

					ArrayList<String> types = new ArrayList<String>();
					ArrayList<String> formats = new ArrayList<String>();
					ArrayList<String> names = new ArrayList<String>();
					ArrayList<String> modes = new ArrayList<String>();
					ArrayList<String> mins = new ArrayList<String>();
					ArrayList<String> maxs = new ArrayList<String>();
					ArrayList<String> defaults = new ArrayList<String>();
					ArrayList<Boolean> required = new ArrayList<Boolean>();
					ArrayList<String> referenceNames = new ArrayList<String>();
					ArrayList<String> arrayNames = new ArrayList<String>();
					ArrayList<InputClass> icTemp = new ArrayList<InputClass>();
					ArrayList<InputClass> icHeader = new ArrayList<InputClass>();

					boolean skipForParamType = false;

					for (Parameter p : op.getValue().getParameters()) {
						if (!(p instanceof BodyParameter) && !(p instanceof QueryParameter)
								&& !(p instanceof PathParameter) && !(p instanceof HeaderParameter)) {
							skipForParamType = true;

							System.out.println("[" + ansi().fgBright(YELLOW).a("WARNING").reset() + "] Method \""
									+ op.getKey() + "\" skipped for not supported parameter ("
									+ p.getClass().getSimpleName() + ")");
						}
					}

					if (!skipForParamType) {

						for (Parameter p : op.getValue().getParameters()) {

							String paramType = p.getClass().getSimpleName();

							if (p instanceof BodyParameter) {

								if (DEBUG_MODE)
									System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "]		"
											+ p.getName() + " : " + paramType);

								if (method.equals("DELETE") || method.equals("delete")) {
									System.out.println("[" + ansi().fgBright(YELLOW).a("WARNING").reset()
											+ "] [Generator] Found body parameter in DELETE method, will probably fail this test.. ("
											+ uri + ")");
									logWriter.println(
											"[WARNING] [Generator] Found body parameter in DELETE method, will probably fail this test.. ("
													+ uri + ")");
								}

								if (method.equals("GET") || method.equals("get")) {
									System.out.println("[" + ansi().fgBright(YELLOW).a("WARNING").reset()
											+ "] [Generator] Found body parameter in GET method, not implemented, skip parsing and send without payload.. ("
											+ uri + ")");
									logWriter.println(
											"[WARNING] [Generator] Found body parameter in GET method, not implemented, skip parsing and send without payload.. ("
													+ uri + ")");
								} else {

									parseBody(swagger, (BodyParameter) p, types, names, modes, formats, mins, maxs,
											defaults, required, logWriter);

								}

							} else if (p instanceof QueryParameter) {
								if (DEBUG_MODE) {
									System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "]		"
											+ p.getName() + ": " + paramType + " Type:"
											+ ((AbstractSerializableParameter<QueryParameter>) p).getType() + " ("
											+ ((AbstractSerializableParameter<QueryParameter>) p).getFormat() + ")"
											+ " Default:"
											+ ((AbstractSerializableParameter<QueryParameter>) p).getDefault());
								}

								if (((AbstractSerializableParameter<QueryParameter>) p).getType().equals("array")) {
									Property items = ((AbstractSerializableParameter<QueryParameter>) p).getItems();

									types.add(items.getType());
									formats.add(items.getFormat());
									if (types.get(types.size() - 1).equals("string")) {
										mins.add(String.valueOf(((AbstractSerializableParameter<QueryParameter>) items)
												.getMinLength()));
									} else {
										mins.add(String.valueOf(
												((AbstractSerializableParameter<QueryParameter>) items).getMinimum()));
										maxs.add(String.valueOf(
												((AbstractSerializableParameter<QueryParameter>) items).getMaximum()));
									}
									defaults.add(String.valueOf(
											((AbstractSerializableParameter<QueryParameter>) items).getDefault()));
									modes.add("QueryParameter_a");
								} else {
									types.add(((AbstractSerializableParameter<QueryParameter>) p).getType());
									if (types.get(types.size() - 1).equals("string")) {
										mins.add(String.valueOf(
												((AbstractSerializableParameter<QueryParameter>) p).getMinLength()));
										maxs.add(String.valueOf(
												((AbstractSerializableParameter<QueryParameter>) p).getMaxLength()));
									} else {
										mins.add(String.valueOf(
												((AbstractSerializableParameter<QueryParameter>) p).getMinimum()));
										maxs.add(String.valueOf(
												((AbstractSerializableParameter<QueryParameter>) p).getMaximum()));
									}
									defaults.add(String
											.valueOf(((AbstractSerializableParameter<QueryParameter>) p).getDefault()));
									modes.add("QueryParameter");
									formats.add(((AbstractSerializableParameter<QueryParameter>) p).getFormat());
								}
								names.add(p.getName());

							} else if (p instanceof PathParameter) {
								if (DEBUG_MODE)
									System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "]		"
											+ p.getName() + ": " + paramType + " Type:"
											+ ((AbstractSerializableParameter<PathParameter>) p).getType() + " ("
											+ ((AbstractSerializableParameter<PathParameter>) p).getFormat() + ")"
											+ " Default:"
											+ ((AbstractSerializableParameter<PathParameter>) p).getDefault());

								types.add(((AbstractSerializableParameter<PathParameter>) p).getType());
								names.add(p.getName());

								if (types.get(types.size() - 1).equals("string")) {
									mins.add(String.valueOf(
											((AbstractSerializableParameter<PathParameter>) p).getMinLength()));
									maxs.add(String.valueOf(
											((AbstractSerializableParameter<PathParameter>) p).getMaxLength()));
								} else {
									mins.add(String
											.valueOf(((AbstractSerializableParameter<PathParameter>) p).getMinimum()));
									maxs.add(String
											.valueOf(((AbstractSerializableParameter<PathParameter>) p).getMaximum()));
								}

								defaults.add(String
										.valueOf(((AbstractSerializableParameter<PathParameter>) p).getDefault()));
								modes.add("PathParameter");

								formats.add(((AbstractSerializableParameter<PathParameter>) p).getFormat());

							} else if (p instanceof HeaderParameter) {
								if (DEBUG_MODE)
									System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "]		"
											+ p.getName() + ": " + paramType + " Type:"
											+ ((AbstractSerializableParameter<HeaderParameter>) p).getType() + " ("
											+ ((AbstractSerializableParameter<HeaderParameter>) p).getFormat() + ")"
											+ " Default:"
											+ ((AbstractSerializableParameter<HeaderParameter>) p).getDefault());

								icHeader.add(new InputClass(p.getName(),
										((AbstractSerializableParameter<HeaderParameter>) p).getType(),
										String.valueOf(((AbstractSerializableParameter<PathParameter>) p).getDefault()),
										true, ((AbstractSerializableParameter<HeaderParameter>) p).getRequired()));

							} else {
								System.out.println("[" + ansi().fgBright(RED).a("ERROR").reset()
										+ "] [Generator] Found parameter not supported.. (Supported: BodyParameter, QueryParameter, PathParameter)");
								logWriter.println(
										"[ERROR] [Generator] Found parameter not supported.. (Supported: BodyParameter, QueryParameter, PathParameter)");
							}
						}

						if (!skipMethod) {
							ArrayList<Integer> responses = new ArrayList<Integer>();
							if (op.getValue().getResponses().size() != 0) {

								if (DEBUG_MODE)
									System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] Responses #"
											+ op.getValue().getResponses().size() + " : ");

								for (Map.Entry<String, Response> resp : op.getValue().getResponses().entrySet()) {
									if (DEBUG_MODE)
										System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] 	"
												+ resp.getKey() + ": " + resp.getValue().getDescription());
									responses.add(Integer.parseInt(resp.getKey()));
								}
							}

							int notValidCount = 2;

							if (PAIRWISE_MODE) {
								int countBodyPar = 0;
								for (int i = 0; i < modes.size(); i++)
									if (modes.get(i).equals("BodyParameter"))
										countBodyPar++;

								if (countBodyPar == 1) {
									if (DEBUG_MODE)
										System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
												+ "] [Generator] Pairwise_mode: found only 1 BodyParameter, setting notValidCount to 1..");
									notValidCount = 1;
								}
							}

							if ((method.equals("GET") || method.equals("get")) && types.size() == 0) {
								add0ParameterTestFrame(uri, method, op.getValue());
							} else {
								if (QUICK_BOUNDED_MODE)
									boundPerMethodCount = 0;

								exploreInputTree(types, formats, mins, maxs, defaults, required, names, modes,
										referenceNames, arrayNames, responses, icTemp, uri, method, notValidCount, 0,
										true, icHeader, logWriter);

								if (methodHasNotRequiredBodyPar)
									updateMethodTestFrameWithNotRequired();

								if (QUICK_BOUNDED_MODE && (boundPerMethodCount == quickmodeBound)) {
									System.out.println("[INFO] [Generator] Bound of " + quickmodeBound
											+ " test per method reached.. (" + uri + " " + method + ")");
									logWriter.println("[INFO] [Generator] Bound of " + quickmodeBound
											+ " test per method reached.. (" + uri + " " + method + ")");
								}
							}
						}
						skipMethod = false;
					}					
				} else {
					add0ParameterTestFrame(uri, method, op.getValue());
				}
				
				
			}
		}

		return 0;
	}

	// recursive function
	private void exploreInputTree(ArrayList<String> types, ArrayList<String> formats, ArrayList<String> mins,
			ArrayList<String> maxs, ArrayList<String> defaults, ArrayList<Boolean> required, ArrayList<String> names,
			ArrayList<String> modes, ArrayList<String> referenceNames, ArrayList<String> arrayNames,
			ArrayList<Integer> responses, ArrayList<InputClass> ic, String uri, String method, int notValidCount,
			int paramIndex, boolean validBranch, ArrayList<InputClass> icHeader, PrintWriter logWriter) {

		ArrayList<InputClass> icIteration = new ArrayList<InputClass>();
		ArrayList<Boolean> requiredTemp = new ArrayList<Boolean>(required);
		int n = 0;
		
		if (types.size() > 0) {

			ArrayList<String> modesTemp = new ArrayList<String>(modes);

			if (types.get(0).equals("ref") || types.get(0).equals("array")) {

				if (types.get(0).equals("ref")) {
					referenceNames.add(names.get(0));
					modesTemp.remove("BodyParameter_ref");
				} else {
					arrayNames.add(names.get(0));
					modesTemp.remove("BodyParameter_array");
				}
								
				types.remove(0);
				formats.remove(0);
				mins.remove(0);
				maxs.remove(0);
				defaults.remove(0);
				names.remove(0);

				exploreInputTree(types, formats, mins, maxs, defaults, required, names, modesTemp, referenceNames,
						arrayNames, responses, ic, uri, method, notValidCount, paramIndex, validBranch, icHeader,
						logWriter);

				return;
			}

			modesTemp.removeIf(mode -> mode.equals("BodyParameter_ref"));
			modesTemp.removeIf(mode -> mode.equals("BodyParameter_array"));
						
			if (modesTemp.get(paramIndex) == "BodyParameter") {
				n = inputClassEnum(names.get(0), types.get(0), formats.get(0), mins.get(0), maxs.get(0),
						defaults.get(0), requiredTemp.get(0), icIteration, logWriter);
				requiredTemp.remove(0);
			} else {
				n = inputClassEnum(names.get(0), types.get(0), formats.get(0), mins.get(0), maxs.get(0),
						defaults.get(0), true, icIteration, logWriter);
			}

			types.remove(0);
			formats.remove(0);
			mins.remove(0);
			maxs.remove(0);
			defaults.remove(0);
			names.remove(0);
		}
				
		paramIndex++;

		if (icIteration.size() > 0) {
			if (types.size() > 0) {
				for (int i = 0; i < n; i++) {

					boolean Skip = false;

					ArrayList<InputClass> icTemp = new ArrayList<InputClass>();
					ArrayList<String> typesTemp = new ArrayList<String>();
					ArrayList<String> formatsTemp = new ArrayList<String>();
					ArrayList<String> minsTemp = new ArrayList<String>();
					ArrayList<String> maxsTemp = new ArrayList<String>();
					ArrayList<String> defaultsTemp = new ArrayList<String>();
					ArrayList<String> namesTemp = new ArrayList<String>();

					for (int j = 0; j < ic.size(); j++) {
						icTemp.add(ic.get(j));
					}
					icTemp.add(icIteration.get(i));

					for (int j = 0; j < types.size(); j++) {
						typesTemp.add(types.get(j));
						formatsTemp.add(formats.get(j));
						minsTemp.add(mins.get(j));
						maxsTemp.add(maxs.get(j));
						defaultsTemp.add(defaults.get(j));
						namesTemp.add(names.get(j));
					}

					if (PAIRWISE_MODE && (modes.get(paramIndex - 1).contains("BodyParameter")
							|| modes.get(paramIndex - 1).contains("QueryParameter"))) {
						if (icIteration.get(i).valid) {
							if (types.size() < notValidCount && !validBranch) {
								Skip = true;
							}
						} else {
							validBranch = false;
							notValidCount--;
							if (notValidCount < 0 || types.size() < notValidCount) {
								Skip = true;
							}
						}

					} else if (VALID_MODE) {
						if (!icIteration.get(i).valid) {
							Skip = true;
						}

					} else if (VALID_INVALID_MODE) {
						if (paramIndex == 1 && !icIteration.get(i).valid)
							validBranch = false;

						if ((icIteration.get(i).valid && !validBranch) || (!icIteration.get(i).valid && validBranch))
							Skip = true;
					} else if (QUICK_NV_MODE) {
						if (paramIndex == 1 && !icIteration.get(i).valid)
							validBranch = false;

						if (validBranch && !icIteration.get(i).valid)
							validBranch = false;
					}
					
					if (!Skip)
						exploreInputTree(typesTemp, formatsTemp, minsTemp, maxsTemp, defaultsTemp, requiredTemp,
								namesTemp, modes, referenceNames, arrayNames, responses, icTemp, uri, method,
								notValidCount, paramIndex, validBranch, icHeader, logWriter);
				}

			} else {

				for (int i = 0; i < n; i++) {
					boolean Skip = false;

					ArrayList<InputClass> icTemp = new ArrayList<InputClass>();
					for (int j = 0; j < ic.size(); j++) {
						icTemp.add(ic.get(j));
					}
					icTemp.add(icIteration.get(i));

					if ((PAIRWISE_NV_MODE || QUICK_NV_MODE) && validBranch /*&& paramIndex != 1*/) {
						if(icIteration.get(i).valid) {
							Skip = true;
						}
					}

					if (PAIRWISE_MODE && (modes.get(paramIndex - 1).contains("BodyParameter")
							|| modes.get(paramIndex - 1).contains("QueryParameter"))) {
						if (icIteration.get(i).valid && notValidCount > 0 && !validBranch) {
							Skip = true;
						} else if (((!icIteration.get(i).valid && --notValidCount < 0)
								|| (!icIteration.get(i).valid && validBranch)) && paramIndex != 1) {
							Skip = true;
						}
					} else if (VALID_INVALID_MODE) {
						if ((icIteration.get(i).valid && !validBranch) || (!icIteration.get(i).valid && validBranch))
							Skip = true;
					}

					if (!Skip) {
						if (QUICK_BOUNDED_MODE) {
							if (boundPerMethodCount < quickmodeBound) {
								boundPerMethodCount++;
								addTestFrames(icTemp, modes, referenceNames, arrayNames, responses, uri, method,
										icHeader, logWriter);
							}
						} else {
							addTestFrames(icTemp, modes, referenceNames, arrayNames, responses, uri, method, icHeader,
									logWriter);
						}
					}
				}
			}
		} else {
			System.out.println("[" + ansi().fgBright(RED).a("ERROR").reset()
					+ "] [Generator] Error in test exploration, this method should not be considered.. (" + uri + " "
					+ method + ")");
			logWriter.println("[ERROR] [Generator] Error in test exploration, this method should not be considered.. ("
					+ uri + " " + method + ")");
		}
	}

	private void addTestFrames(ArrayList<InputClass> icTemp, ArrayList<String> modes, ArrayList<String> referenceNames,
			ArrayList<String> arrayNames, ArrayList<Integer> responses, String uri, String method,
			ArrayList<InputClass> icHeader, PrintWriter logWriter) {

		boolean pathP = false;
		boolean queryP = false;
		boolean bodyP = false;
		boolean refP = false;
		boolean arrayP_lv0 = false;
		boolean arrayP_lv1 = false;
		String qry = new String();
		String payload = new String();
		int countRef = 0;
		int countArray = 0;

		for (int j = 0; j < icTemp.size(); j++) {

			if (modes.get(j).equals("PathParameter")) {// && !pathP) {
				if (refP) {
					payload += "}";
					refP = false;
				}

				if (arrayP_lv0) {
					payload += "]";
					arrayP_lv0 = false;
				} else if (arrayP_lv1) {
					payload += "}]";
					arrayP_lv1 = false;
				}

				pathP = true;

			} else if (modes.get(j).equals("QueryParameter")) {
				if (refP) {
					payload += "}";
					refP = false;
				}
				if (arrayP_lv0) {
					payload += "]";
					arrayP_lv0 = false;
				} else if (arrayP_lv1) {
					payload += "}]";
					arrayP_lv1 = false;
				}
				if (!queryP) {
					qry = "?" + icTemp.get(j).name + "={" + icTemp.get(j).name + "}";
				} else {
					qry += "&" + icTemp.get(j).name + "={" + icTemp.get(j).name + "}";
				}
				queryP = true;

			} else if (modes.get(j).equals("QueryParameter_a")) {
				if (refP) {
					payload += "}";
					refP = false;
				}
				if (arrayP_lv0) {
					payload += "]";
					arrayP_lv0 = false;
				} else if (arrayP_lv1) {
					payload += "}]";
					arrayP_lv1 = false;
				}
				if (!queryP) {
					qry = "?" + icTemp.get(j).name + "=[{" + icTemp.get(j).name + "}]";
				} else {
					qry += "&" + icTemp.get(j).name + "=[{" + icTemp.get(j).name + "}]";
				}
				queryP = true;

			} else if (modes.get(j).equals("BodyParameter")) {
				if (refP) {
					payload += "}";
					refP = false;
				}
				if (arrayP_lv0) {
					payload += "]";
					arrayP_lv0 = false;
				} else if (arrayP_lv1) {
					payload += "}]";
					arrayP_lv1 = false;
				}

				if (!bodyP) {
					payload = "{\"" + icTemp.get(j).name + "\" : \"{" + icTemp.get(j).name + "}\"";
				} else {
					payload += ", \"" + icTemp.get(j).name + "\" : \"{" + icTemp.get(j).name + "}\"";
				}
				bodyP = true;

			} else if (modes.get(j).equals("BodyParameter_r1")) {
				if (arrayP_lv0) {
					payload += "]";
					arrayP_lv0 = false;
				} else if (arrayP_lv1) {
					payload += "}]";
					arrayP_lv1 = false;
				}
				if (!refP) {

					if (!bodyP) {
						payload = "{\"" + referenceNames.get(countRef) + "\" : {";
					} else {
						payload += ", \"" + referenceNames.get(countRef) + "\" : {";
					}
					countRef++;
					bodyP = true;

					payload += "\"" + icTemp.get(j).name + "\" : \"{" + icTemp.get(j).name + "}\"";

				} else {
					payload += ", \"" + icTemp.get(j).name + "\" : \"{" + icTemp.get(j).name + "}\"";
				}
				refP = true;

			} else if (modes.get(j).contains("BodyParameter_a")) {
				if (refP) {
					payload += "}";
					refP = false;
				}

				if (!arrayP_lv0 && !arrayP_lv1) {

					if (!bodyP) {
						if (modes.get(j).equals("BodyParameter_a0")) {
							payload = "{\"" + arrayNames.get(countArray) + "\" : [";
							arrayP_lv0 = true;
						} else {
							payload = "{\"" + arrayNames.get(countArray) + "\" : [{";
							arrayP_lv1 = true;
						}
					} else {
						if (modes.get(j).equals("BodyParameter_a0")) {
							payload += ", \"" + arrayNames.get(countArray) + "\" : [";
							arrayP_lv0 = true;
						} else {
							payload += ", \"" + arrayNames.get(countArray) + "\" : [{";
							arrayP_lv1 = true;
						}
					}
					countArray++;
					bodyP = true;

					if (modes.get(j).equals("BodyParameter_a1")) {
						payload += "\"" + icTemp.get(j).name + "\" : \"{" + icTemp.get(j).name + "}\"";
					} else {
						payload += "\"{" + icTemp.get(j).name + "}\"";
					}
				} else {
					if (modes.get(j).equals("BodyParameter_a1")) {

						payload += ", \"" + icTemp.get(j).name + "\" : \"{" + icTemp.get(j).name + "}\"";

					} else {

						payload += ", \"{" + icTemp.get(j).name + "}\"";

					}
				}

			} else {
				System.out.println("[" + ansi().fgBright(RED).a("ERROR").reset()
						+ "] [Generator] Found mode not implemented.. (" + modes.get(j) + ")");
				logWriter.println("[ERROR] [Generator] Found mode not implemented.. (" + modes.get(j) + ")");
			}
		}

		if (refP) {
			payload += "}";
			refP = false;
		}
		if (arrayP_lv0) {
			payload += "]";
			arrayP_lv0 = false;
		} else if (arrayP_lv1) {
			payload += "}]";
			arrayP_lv1 = false;
		}

		if (pathP && !queryP && !bodyP) {
			testFrames.add(new TestFrame(jsonID, pathID, pathMethodID, "http://" + host + uri, String.valueOf(offset + count), method,
					"null", DEBUG_MODE));
		} else if (queryP && !bodyP) {
			testFrames.add(new TestFrame(jsonID, pathID, pathMethodID, "http://" + host + uri + qry, String.valueOf(offset + count),
					method, "null", DEBUG_MODE));
		} else if ((!pathP && !queryP && bodyP) || (pathP && !queryP && bodyP)) {
			payload += "}";
			testFrames.add(new TestFrame(jsonID, pathID, pathMethodID, "http://" + host + uri, String.valueOf(offset + count), method,
					"null", DEBUG_MODE));
			testFrames.get(offset + count).setPayload(payload);
		} else if (!pathP && queryP && bodyP) {
			payload += "}";
			testFrames.add(new TestFrame(jsonID, pathID, pathMethodID, "http://" + host + uri + qry, String.valueOf(offset + count),
					method, "null", DEBUG_MODE));
			testFrames.get(offset + count).setPayload(payload);
		} else {
			System.out.println("[" + ansi().fgBright(RED).a("ERROR").reset()
					+ "] [Generator] Undefined, impossible to add Test.. (uri: " + uri + ")");
			logWriter.println("[ERROR] [Generator] Undefined, impossible to add Test.. (uri: " + uri + ")");
			return;
		}

		for (int j = 0; j < icTemp.size(); j++) {
			testFrames.get(offset + count).ic.add(icTemp.get(j));
		}

		for (int j = 0; j < icHeader.size(); j++) {
			testFrames.get(offset + count).icHeader.add(icHeader.get(j));
		}

		testFrames.get(offset + count).expectedResponses = responses;

		count++;
		countTfFile++;

	}

	private void add0ParameterTestFrame(String uri, String method, Operation op) {
		testFrames.add(new TestFrame(jsonID, pathID, pathMethodID, "http://" + host + uri, String.valueOf(offset + count), method,
				"null", DEBUG_MODE));

		if (op.getResponses().size() != 0) {

			if (DEBUG_MODE)
				System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] Responses #"
						+ op.getResponses().size() + " : ");

			for (Map.Entry<String, Response> resp : op.getResponses().entrySet()) {
				if (DEBUG_MODE)
					System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] 	" + resp.getKey() + ": "
							+ resp.getValue().getDescription());
				testFrames.get(offset + count).expectedResponses.add(Integer.parseInt(resp.getKey()));
			}
			count++;
			countTfFile++;
		}
	}

	private static void parseBody(Swagger swagger, BodyParameter p, ArrayList<String> types, ArrayList<String> names,
			ArrayList<String> modes, ArrayList<String> formats, ArrayList<String> mins, ArrayList<String> maxs,
			ArrayList<String> defaults, ArrayList<Boolean> required, PrintWriter logWriter) {
		if (DEBUG_MODE) {
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Generator] BODY: ");
		}
		try {
			String ref = p.getSchema().getReference();
			if (ref == null) {

				parseBodyNoRef(swagger, (BodyParameter) p, types, names, modes, formats, mins, maxs, defaults, required,
						logWriter);
			} else {

				RefProperty rp = new RefProperty(ref);

				boolean isRef = parseReference(swagger, rp, types, names, modes, formats, mins, maxs, defaults,
						required, 0, false, false, logWriter);

				if (!isRef) {
					System.out.println("[" + ansi().fgBright(RED).a("ERROR").reset()
							+ "] [Generator] Error while parsing body reference..");
					System.out.println("[ERROR] [Generator] Error while parsing body reference..");
					return;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[" + ansi().fgBright(RED).a("ERROR").reset()
					+ "] [Generator] Error catched while parsing body.. (Parameter: " + p.getName() + ")");
			logWriter.println("[" + ansi().fgBright(RED).a("ERROR").reset()
					+ "] [Generator] Error catched while parsing body.. (Parameter: " + p.getName() + ")");
		}
	}

	private static boolean parseReference(Swagger swagger, RefProperty rp, ArrayList<String> types,
			ArrayList<String> names, ArrayList<String> modes, ArrayList<String> formats, ArrayList<String> mins,
			ArrayList<String> maxs, ArrayList<String> defaults, ArrayList<Boolean> required, int iteration,
			boolean refRec, boolean arrayRec, PrintWriter logWriter) {

		if ((rp.getRefFormat().equals(RefFormat.INTERNAL) && swagger.getDefinitions().containsKey(rp.getSimpleRef()))
				&& iteration < 3) {
			Model m = swagger.getDefinitions().get(rp.getSimpleRef());

			if (m.getProperties() != null) {
				for (Map.Entry<String, Property> propertyEntry : m.getProperties().entrySet()) {

					if (propertyEntry.getValue() instanceof RefProperty) {

						if (iteration + 1 < 3) {
							if (DEBUG_MODE)
								System.out.println(
										"[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Generator]		"
												+ propertyEntry.getKey() + " : " + propertyEntry.getValue().getType());

							modes.add("BodyParameter_ref");
							names.add(propertyEntry.getKey());
							types.add(propertyEntry.getValue().getType());

							mins.add(getMin(propertyEntry.getValue()));
							maxs.add(getMax(propertyEntry.getValue()));
							defaults.add(getDefault(propertyEntry.getValue()));

							formats.add("");

							parseReference(swagger, (RefProperty) propertyEntry.getValue(), types, names, modes,
									formats, mins, maxs, defaults, required, ++iteration, true, false, logWriter);
							iteration--;
						}

					} else if (propertyEntry.getValue() instanceof ArrayProperty) {
						
						if (iteration + 1 < 3) {
							Property items = ((ArrayProperty) propertyEntry.getValue()).getItems();

							if (items instanceof RefProperty) {
								if (iteration + 1 < 3) {
									if (DEBUG_MODE)
										System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
												+ "] [Generator]		" + propertyEntry.getKey() + " : "
												+ propertyEntry.getValue().getType());
									modes.add("BodyParameter_array");
									names.add(propertyEntry.getKey());
									types.add(propertyEntry.getValue().getType());

									formats.add("");
									mins.add("");
									maxs.add("");
									defaults.add("");

									parseReference(swagger, (RefProperty) items, types, names, modes, formats, mins,
											maxs, defaults, required, ++iteration, false, true, logWriter);
									iteration--;
								}

							} else {
								modes.add("BodyParameter_array");
								names.add(propertyEntry.getKey());
								types.add(propertyEntry.getValue().getType());
								formats.add("");
								mins.add("");
								maxs.add("");
								defaults.add("");

								types.add(items.getType());

								mins.add(getMin(items));
								maxs.add(getMax(items));
								defaults.add(getDefault(items));

								names.add(items.getType());
								formats.add(items.getFormat());
								modes.add("BodyParameter_a1");

								if (DEBUG_MODE) {
									System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
											+ "] [Generator]		" + propertyEntry.getKey() + " : "
											+ propertyEntry.getValue().getType());
									System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
											+ "] [Generator]			" + propertyEntry.getKey() + " : "
											+ propertyEntry.getValue().getType() + " Of " + items.getType() + " ("
											+ items.getFormat() + ")");
								}

							}
						}
					} else {

						if (propertyEntry.getValue().getType().equals("object")) {

							if (DEBUG_MODE) {
								System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
										+ "] [Generator]		No properties for " + propertyEntry.getKey()
										+ ", trying with additionalProp..");
								System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
										+ "] [Generator]		" + propertyEntry.getKey() + " : "
										+ ((MapProperty) propertyEntry.getValue()).getAdditionalProperties().getType()
										+ "("
										+ ((MapProperty) propertyEntry.getValue()).getAdditionalProperties().getFormat()
										+ ")");
							}

							names.add(propertyEntry.getKey());
							types.add(((MapProperty) propertyEntry.getValue()).getAdditionalProperties().getType());

							mins.add(getMin(((MapProperty) propertyEntry.getValue()).getAdditionalProperties()));
							maxs.add(getMax(((MapProperty) propertyEntry.getValue()).getAdditionalProperties()));
							defaults.add(
									getDefault(((MapProperty) propertyEntry.getValue()).getAdditionalProperties()));

							formats.add(((MapProperty) propertyEntry.getValue()).getAdditionalProperties().getFormat());
						} else {

							if (DEBUG_MODE && iteration == 0) {
								System.out.println(
										"[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Generator]		"
												+ propertyEntry.getKey() + " : " + propertyEntry.getValue().getType()
												+ "(" + propertyEntry.getValue().getFormat() + ")");
							} else if (DEBUG_MODE && iteration == 1) {
								System.out.println(
										"[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Generator]			"
												+ propertyEntry.getKey() + " : " + propertyEntry.getValue().getType()
												+ "(" + propertyEntry.getValue().getFormat() + ")");
							}

							types.add(propertyEntry.getValue().getType());
							formats.add(propertyEntry.getValue().getFormat());

							mins.add(getMin(propertyEntry.getValue()));
							maxs.add(getMax(propertyEntry.getValue()));
							defaults.add(getDefault(propertyEntry.getValue()));

							names.add(propertyEntry.getKey());
						}

						if (iteration == 0) {
							modes.add("BodyParameter");

							if (String.valueOf(propertyEntry.getValue().getRequired()).equals("true")) {
								required.add(true);

								if (DEBUG_MODE)
									System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
											+ "] [Generator]   Required!");
							} else {
								required.add(false);
								methodHasNotRequiredBodyPar = true;
								if (DEBUG_MODE)
									System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
											+ "] [Generator]   Not Required!");
							}
						} else if (iteration == 1) {
							if (refRec) {
								modes.add("BodyParameter_r1");
							} else if (arrayRec) {
								modes.add("BodyParameter_a1");
							}
						} else {
							for(int i = 0 ; i < 2 ; i++) {
								types.remove(types.size()-1);
								names.remove(names.size()-1);
								
								formats.remove(formats.size()-1);
								mins.remove(mins.size()-1);
								maxs.remove(maxs.size()-1);
								defaults.remove(defaults.size()-1);
							}
							
							modes.remove(modes.size()-1);
							
							
							System.out.println("[" + ansi().fgBright(YELLOW).a("WARNING").reset()
									+ "] [Generator] Found more than 2 recursive reference, not implemented, skip param.. (ref: "
									+ rp.getSimpleRef() + ")");
							logWriter.println(
									"[WARNING] [Generator] Found more than 2 recursive reference, not implemented, skip param.. (ref: "
											+ rp.getSimpleRef() + ")");
							return true;
						}

					}
				}
				return true;
			}
			System.out.println(
					"[" + ansi().fgBright(YELLOW).a("WARNING").reset() + "] [Generator]	No properties found");
			logWriter.println("[WARNING] [Generator] No properties found");
			return true;

		} else if (iteration > 2) {
			skipMethod = true;
			types.clear();
			names.clear();
			modes.clear();
			formats.clear();
			
			System.out.println("[" + ansi().fgBright(RED).a("ERROR").reset()
					+ "] [Generator] Found more than 3 recursive iteration, skip method.. (ref: " + rp.getSimpleRef()
					+ ")");
			logWriter.println("[ERROR] [Generator] Found more than 3 recursive iteration, skip method.. (ref: "
					+ rp.getSimpleRef() + ")");

			return false;
		} else {
			System.out.println("[" + ansi().fgBright(YELLOW).a("WARNING").reset()
					+ "] [Generator] Maybe reference definition missing.. (ref: " + rp.getSimpleRef() + ")");
			logWriter.println("[" + ansi().fgBright(YELLOW).a("WARNING").reset()
					+ "] [Generator] Maybe reference definition missing.. (ref: " + rp.getSimpleRef() + ")");
			return false;
		}
	}

	private static void parseBodyNoRef(Swagger swagger, BodyParameter p, ArrayList<String> types,
			ArrayList<String> names, ArrayList<String> modes, ArrayList<String> formats, ArrayList<String> mins,
			ArrayList<String> maxs, ArrayList<String> defaults, ArrayList<Boolean> required, PrintWriter logWriter) {

		if (DEBUG_MODE)
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
					+ "]		No reference found, trying schema parsing..");

		Model model = ((BodyParameter) p).getSchema();
		boolean arrayFound = false;

		if (model instanceof ArrayModel) {
			arrayFound = true;
			ArrayModel arrayModel = (ArrayModel) model;

			Property items = arrayModel.getItems();

			modes.add("BodyParameter_array");
			names.add(p.getName());
			types.add(arrayModel.getType());
			formats.add("");
			mins.add("");
			maxs.add("");
			defaults.add("");

			if (items instanceof RefProperty) {
				RefProperty arrayModelRefProp = (RefProperty) arrayModel.getItems();
				parseReference(swagger, arrayModelRefProp, types, names, modes, formats, mins, maxs, defaults, required,
						1, false, true, logWriter);

			} else {

				if (DEBUG_MODE)
					System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Generator]   " + p.getName()
							+ " : " + arrayModel.getType() + " Of " + items.getType() + " (" + items.getFormat() + ")");

				names.add(items.getType());
				types.add(items.getType());
				formats.add(items.getFormat());

				mins.add(getMin(items));
				maxs.add(getMax(items));
				defaults.add(getDefault(items));

				modes.add("BodyParameter_a0");
			}

		} else if (model.getProperties() != null) {
			for (Map.Entry<String, Property> propertyEntry : model.getProperties().entrySet()) {
				if (DEBUG_MODE)
					System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Generator]   "
							+ propertyEntry.getKey() + " : " + propertyEntry.getValue().getType() + " ("
							+ propertyEntry.getValue().getFormat() + ")");
				types.add(propertyEntry.getValue().getType());
				formats.add(propertyEntry.getValue().getFormat());

				mins.add(getMin(propertyEntry.getValue()));
				maxs.add(getMax(propertyEntry.getValue()));
				defaults.add(getDefault(propertyEntry.getValue()));

				names.add(propertyEntry.getKey());
				modes.add("BodyParameter");

				if (String.valueOf(propertyEntry.getValue().getRequired()).equals("true")) {
					required.add(true);

					if (DEBUG_MODE)
						System.out
								.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Generator]   Required!");
				} else {
					required.add(false);
					methodHasNotRequiredBodyPar = true;
					if (DEBUG_MODE)
						System.out.println(
								"[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Generator]   Not Required!");
				}
			}
		} else if (!arrayFound) {

			ModelImpl modelImpl = (ModelImpl) model;
			if (modelImpl.getAdditionalProperties() != null) {

				if (DEBUG_MODE) {
					System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
							+ "] [Generator]		No properties, trying with additionalProp..");
					System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Generator]   " + p.getName()
							+ " : " + modelImpl.getAdditionalProperties().getType() + " ("
							+ modelImpl.getAdditionalProperties().getFormat() + ")");
				}
				names.add(p.getName());
				types.add(modelImpl.getAdditionalProperties().getType());

				mins.add(getMin(modelImpl.getAdditionalProperties()));
				maxs.add(getMax(modelImpl.getAdditionalProperties()));
				defaults.add(getDefault(modelImpl.getAdditionalProperties()));

				modes.add("BodyParameter");
				formats.add(modelImpl.getAdditionalProperties().getFormat());

				if (String.valueOf(String.valueOf(modelImpl.getAdditionalProperties().getRequired())).equals("true")) {
					required.add(true);

					if (DEBUG_MODE)
						System.out
								.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Generator]   Required!");
				} else {
					required.add(false);
					methodHasNotRequiredBodyPar = true;
					if (DEBUG_MODE)
						System.out.println(
								"[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Generator]   Not Required!");
				}
			}
		} else {
			System.out.println(
					"[" + ansi().fgBright(RED).a("ERROR").reset() + "] [Generator] Error while parsing body schema..");
			logWriter.println("[ERROR] [Generator] Error while parsing body schema..");
		}
	}

	private int inputClassEnum(String name, String type, String format, String min, String max, String defaultValue,
			boolean required, ArrayList<InputClass> ic, PrintWriter logWriter) {

		switch (type) {

		case "number":

			if (INVALID_MODE) {

				ic.add(new InputClass(name, "symbol", null, null, "null", false, required));
				return N_IC_VALID;
			}

			if (format != null) {
				if (format.equals("double")) {
					ic.add(new InputClass(name, "double", min, max, defaultValue, true, required));
				} else {
					ic.add(new InputClass(name, "float", min, max, defaultValue, true, required));
				}
			} else {

				ic.add(new InputClass(name, "float", min, max, defaultValue, true, required));
			}

			if (VALID_MODE) {
				return N_IC_VALID;
			} else if (VALID_MODE_P) {
				ic.add(new InputClass(name, "double", false, required));
				return N_IC_VALID + 1;
			}

			ic.add(new InputClass(name, "symbol", null, null, "null", false, required));

			return N_IC_QUICK;

		case "boolean":

			if (INVALID_MODE) {

				ic.add(new InputClass(name, "symbol", null, null, "null", false, required));
				return N_IC_VALID;
			}

			ic.add(new InputClass(name, "b_true", true, required));

			if (VALID_MODE) {
				return N_IC_VALID;
			} else if (VALID_MODE_P) {
				ic.add(new InputClass(name, "b_false", false, required));
				return N_IC_VALID + 1;
			}

			ic.add(new InputClass(name, "symbol", null, null, "null", false, required));

			if (!QUICK_MODE) {
				ic.add(new InputClass(name, "b_false", null, null, required));
				return N_IC_BOOLEAN + 1;
			}

			return N_IC_BOOLEAN;

		case "integer":

			if (INVALID_MODE) {

				ic.add(new InputClass(name, "symbol", null, null, "null", false, required));
				return N_IC_VALID;
			}

			if (format != null) {
				if (format.equals("int64")) {
					ic.add(new InputClass(name, "int64", min, max, defaultValue, true, required));
				} else {
					ic.add(new InputClass(name, "int32", min, max, defaultValue, true, required));
				}
			} else {
				ic.add(new InputClass(name, "range", min, max, defaultValue, true, required));
			}

			if (VALID_MODE) {
				return N_IC_VALID;
			} else if (VALID_MODE_P) {
				ic.add(new InputClass(name, "range", "1", "100", null, false, required));
				return N_IC_VALID + 1;
			}

			ic.add(new InputClass(name, "symbol", null, null, "null", false, required));

			if (!QUICK_MODE) {
				ic.add(new InputClass(name, "empty", null, null, required));
				ic.add(new InputClass(name, "greater", "2147483647", null, required));
				ic.add(new InputClass(name, "lower", "-2147483648", null, required));
				ic.add(new InputClass(name, "range", "1", "100", required));
				ic.add(new InputClass(name, "range", "-100", "-1", required));
				ic.add(new InputClass(name, "symbol", "0", null, required));
				return N_IC_INTEGER + 1;
			}
			return N_IC_QUICK;

		case "string":

			if (INVALID_MODE) {
				ic.add(new InputClass(name, "empty", min, max, defaultValue, false, required));
				return N_IC_VALID;
			}

			boolean date = false;
			if (format != null) {
				if (format.equals("date")) {
					ic.add(new InputClass(name, "date", true, required));
					date = true;
				} else if (format.equals("date-time")) {
					ic.add(new InputClass(name, "date-time", true, required));
					date = true;
				} else {
					ic.add(new InputClass(name, "s_range", "1", "20", defaultValue, true, required));
				}
			} else {
				ic.add(new InputClass(name, "s_range", "1", "20", defaultValue, true, required));
			}

			if (VALID_MODE) {
				return N_IC_VALID;
			} else if (VALID_MODE_P && !date) {
				ic.add(new InputClass(name, "s_range", "1", "50", null, false, required));
				return N_IC_VALID + 1;
			} else if (VALID_MODE_P) {
				if (format.equals("date")) {
					ic.add(new InputClass(name, "date", false, required));
				} else if (format.equals("date-time")) {
					ic.add(new InputClass(name, "date-time", false, required));
				}
				return N_IC_VALID + 1;
			}

			ic.add(new InputClass(name, "empty", false, required));

			if (!QUICK_MODE) {
				ic.add(new InputClass(name, "symbol", null, null, "null", false, required));
				return N_IC_STRING;
			}
			return N_IC_QUICK;

		case "language":

			if (INVALID_MODE) {

				ic.add(new InputClass(name, "symbol", null, null, "null", false, required));
				return N_IC_VALID;
			}

			ic.add(new InputClass(name, "lang", true, required));

			if (VALID_MODE) {
				return N_IC_VALID;
			}

			ic.add(new InputClass(name, "symbol", null, null, "null", false, required));

			if (!QUICK_MODE) {
				ic.add(new InputClass(name, "s_range", "1", "100", required));
				return N_IC_LANGUAGE;
			}
			return N_IC_QUICK;

		case "s_symbol":
			System.out.println("[INFO] [Generator] Type \"" + type + "\" found");
			logWriter.println("[INFO] [Generator] Type \"" + type + "\" found");

			return N_IC_DEFAULT;

		default:
			System.out.println("[" + ansi().fgBright(RED).a("ERROR").reset() + "] [Generator] Type \"" + type
					+ "\" not implemented, returning 0 IC..");
			logWriter.println("[ERROR] [Generator] Type \"" + type + "\" not implemented, returning 0 IC..");
			return N_IC_DEFAULT;
		}
	}

	private static String getDefault(Property property) {
		if (property instanceof BooleanProperty) {
			BooleanProperty booleanProperty = (BooleanProperty) property;
			return String.valueOf(booleanProperty.getDefault());
		} else if (property instanceof StringProperty) {
			StringProperty stringProperty = (StringProperty) property;
			return String.valueOf(stringProperty.getDefault());
		} else if (property instanceof DoubleProperty) {
			DoubleProperty doubleProperty = (DoubleProperty) property;
			return String.valueOf(doubleProperty.getDefault());
		} else if (property instanceof FloatProperty) {
			FloatProperty floatProperty = (FloatProperty) property;
			return String.valueOf(floatProperty.getDefault());
		} else if (property instanceof IntegerProperty) {
			IntegerProperty integerProperty = (IntegerProperty) property;
			return String.valueOf(integerProperty.getDefault());
		} else if (property instanceof LongProperty) {
			LongProperty longProperty = (LongProperty) property;
			return String.valueOf(longProperty.getDefault());
		}

		return "";
	}

	public static String getMin(Property property) {
		if (property instanceof BaseIntegerProperty) {
			BaseIntegerProperty integerProperty = (BaseIntegerProperty) property;
			return String.valueOf(integerProperty.getMinimum() != null ? integerProperty.getMinimum() : null);
		} else if (property instanceof AbstractNumericProperty) {
			AbstractNumericProperty numericProperty = (AbstractNumericProperty) property;
			return String.valueOf(numericProperty.getMinimum());
		} else if (property instanceof StringProperty) {
			StringProperty stringProperty = (StringProperty) property;
			return String.valueOf(stringProperty.getMinLength());
		}

		return "";
	}

	public static String getMax(Property property) {
		if (property instanceof BaseIntegerProperty) {
			BaseIntegerProperty integerProperty = (BaseIntegerProperty) property;
			return String.valueOf(integerProperty.getMaximum() != null ? integerProperty.getMaximum() : null);
		} else if (property instanceof AbstractNumericProperty) {
			AbstractNumericProperty numericProperty = (AbstractNumericProperty) property;
			return String.valueOf(numericProperty.getMaximum());
		} else if (property instanceof StringProperty) {
			StringProperty stringProperty = (StringProperty) property;
			return String.valueOf(stringProperty.getMaxLength());
		}

		return "";
	}

	public void updateMethodTestFrameWithNotRequired() {
		Random random = new Random();
		int countTest = 0;
		int latestPathID = testFrames.get(testFrames.size() - 1).getPathID();
		int selectedTest;

		for (int i = testFrames.size() - 1; i >= 0; i--) {
			if (testFrames.get(i).getPathID() == latestPathID) {
				countTest++;
			} else {
				break;
			}
		}

		if (countTest > 1) {
			selectedTest = random.nextInt(countTest - 1);

			testFrames.get(testFrames.size() - selectedTest - 1).removeOneNotRequiredParam();
		}
	}

	public void printCount() {
		if (countTfFile - 1 < 10) {
			System.out.print("\b" + countTfFile);
		} else if (countTfFile - 1 < 100) {
			System.out.print("\b\b" + countTfFile);
		} else if (countTfFile - 1 < 1000) {
			System.out.print("\b\b\b" + countTfFile);
		} else if (countTfFile - 1 < 10000) {
			System.out.print("\b\b\b\b" + countTfFile);
		} else if (countTfFile - 1 < 100000) {
			System.out.print("\b\b\b\b\b" + countTfFile);
		} else if (countTfFile - 1 < 1000000) {
			System.out.print("\b\b\b\b\b\b" + countTfFile);
		} else if (countTfFile - 1 < 10000000) {
			System.out.print("\b\b\b\b\b\b\b" + countTfFile);
		} else if (countTfFile - 1 < 100000000) {
			System.out.print("\b\b\b\b\b\b\b\b" + countTfFile);
		} else if (countTfFile - 1 < 1000000000) {
			System.out.print("\b\b\b\b\b\b\b\b\b" + countTfFile);
		}
	}

	public void stampaTestFrames() {
		System.out.println();
		System.out.println("\n***********************************************************");
		System.out.println("********************** " + testFrames.size() + " Test Frame **********************\n");

		for (int i = 0; i < testFrames.size(); i++) {
			System.out.printf("- " + testFrames.get(i).getName() + " | Method:" + testFrames.get(i).getReqType()
					+ " | #Ic: " + testFrames.get(i).ic.size() + " | Body: " + testFrames.get(i).getPayload()
					+ " | Expected responses: ");
			testFrames.get(i).printExpectedResponses();
			if (PAIRWISE_MODE || VALID_MODE || QUICK_MODE || VALID_MODE_P || INVALID_MODE) {
				System.out.print(" | ");
				testFrames.get(i).printValidCombination();
			}
			System.out.print(" | Priority: " + testFrames.get(i).getPriority());
			System.out.println();
		}
	}

	public void stampaTestFrames(PrintWriter testFile) {
		testFile.write("********************** " + testFrames.size() + " Test Frame **********************\n");

		for (int i = 0; i < testFrames.size(); i++) {
			testFile.write("- " + testFrames.get(i).getName() + " | Method:" + testFrames.get(i).getReqType()
					+ " | #Ic: " + testFrames.get(i).ic.size() + " | Body: " + testFrames.get(i).getPayload()
					+ " | Expected responses: ");
			testFrames.get(i).printExpectedResponses(testFile);
			if (PAIRWISE_MODE || VALID_MODE || QUICK_MODE || VALID_MODE_P || INVALID_MODE) {
				testFile.write(" | ");
				testFrames.get(i).printValidCombination(testFile);
			}
			testFile.write(" | Priority: " + testFrames.get(i).getPriority());
			testFile.write("\n");
		}

	}

	public void printGenerationStatistics() {
		System.out.println();
		System.out.println("[INFO] [Generator] ***************** Generation Statistics");
		System.out.println("[INFO] [Generator] Total paths count: " + pathCount);
		System.out.println("[INFO] [Generator] Total methods count: " + methodCount);
		System.out.println("[INFO] [Generator] Total tests generated: " + testFrames.size());
	}

	public void printGenerationStatistics(PrintWriter statWriter) {
		statWriter.println();
		statWriter.println("***************** Generation Statistics");
		statWriter.println("Total paths count: " + pathCount);
		statWriter.println("Total methods count: " + methodCount);
		statWriter.println("Total tests generated: " + testFrames.size());

		statWriter.flush();
	}

	public void setQuickMode(boolean quickMode) {
		QUICK_MODE = quickMode;
	}

	public void setPairWiseMode(boolean pairWiseMode) {
		PAIRWISE_MODE = pairWiseMode;
	}

	public void setValidMode(boolean validMode) {
		VALID_MODE = validMode;
	}

	public void setValidModeP(boolean validModeP) {
		VALID_MODE_P = validModeP;
	}

	public void setInvalidModeP(boolean invalidMode) {
		INVALID_MODE = invalidMode;
	}

	public void setPairWiseNVMode(boolean pairWiseNVMode) {
		PAIRWISE_NV_MODE = pairWiseNVMode;
	}

	public void setQuickNVMode(boolean quickNVMode) {
		QUICK_NV_MODE = quickNVMode;
	}

	public void setQuickBoundedNVMode(boolean quickBoundedNVMode) {
		QUICK_BOUNDED_NV_MODE = quickBoundedNVMode;
	}

	public void setValidInvalidMode(boolean validInvalidMode) {
		VALID_INVALID_MODE = validInvalidMode;
	}

	public void setQuickBoundedMode(boolean quickBoundedMode) {
		QUICK_BOUNDED_MODE = quickBoundedMode;
	}

	public void setQuickModeBound(int _bound) {
		quickmodeBound = _bound;
	}

}
