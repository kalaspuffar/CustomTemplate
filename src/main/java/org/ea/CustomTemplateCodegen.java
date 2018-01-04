package org.ea;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenParameter;
import io.swagger.codegen.CodegenResponse;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.codegen.SupportingFile;
import io.swagger.models.HttpMethod;
import io.swagger.models.Info;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.util.Yaml;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomTemplateCodegen extends DefaultCodegen implements CodegenConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomTemplateCodegen.class);
    protected String implFolder = "service";
    public static final String GOOGLE_CLOUD_FUNCTIONS = "googleCloudFunctions";
    public static final String EXPORTED_NAME = "exportedName";
    protected String apiVersion = "1.0.0";
    protected int serverPort = 8080;
    protected String projectName = "swagger-server";
    protected boolean googleCloudFunctions;
    protected String exportedName;

    public NodeJSServerCodegen() {
        this.outputFolder = "generated-code/nodejs";
        this.modelTemplateFiles.clear();
        this.apiTemplateFiles.put("controller.mustache", ".js");
        this.embeddedTemplateDir = this.templateDir = "nodejs";
        this.setReservedWordsLowerCase(Arrays.asList("break", "case", "class", "catch", "const", "continue", "debugger", "default", "delete", "do", "else", "export", "extends", "finally", "for", "function", "if", "import", "in", "instanceof", "let", "new", "return", "super", "switch", "this", "throw", "try", "typeof", "var", "void", "while", "with", "yield"));
        this.additionalProperties.put("apiVersion", this.apiVersion);
        this.additionalProperties.put("serverPort", this.serverPort);
        this.additionalProperties.put("implFolder", this.implFolder);
        this.supportingFiles.add(new SupportingFile("writer.mustache", "utils".replace(".", "/"), "writer.js"));
        this.cliOptions.add(CliOption.newBoolean("googleCloudFunctions", "When specified, it will generate the code which runs within Google Cloud Functions instead of standalone Node.JS server. See https://cloud.google.com/functions/docs/quickstart for the details of how to deploy the generated code."));
        this.cliOptions.add(new CliOption("exportedName", "When the generated code will be deployed to Google Cloud Functions, this option can be used to update the name of the exported function. By default, it refers to the basePath. This does not affect normal standalone nodejs server code."));
    }

    public String apiPackage() {
        return "controllers";
    }

    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    public String getName() {
        return "nodejs-server";
    }

    public String getHelp() {
        return "Generates a nodejs server library using the swagger-tools project.  By default, it will also generate service classes--which you can disable with the `-Dnoservice` environment variable.";
    }

    public String toApiName(String name) {
        return name.length() == 0 ? "DefaultController" : this.initialCaps(name);
    }

    public String toApiFilename(String name) {
        return this.toApiName(name);
    }

    public String apiFilename(String templateName, String tag) {
        String result = super.apiFilename(templateName, tag);
        if (templateName.equals("service.mustache")) {
            String stringToMatch = File.separator + "controllers" + File.separator;
            String replacement = File.separator + this.implFolder + File.separator;
            result = result.replaceAll(Pattern.quote(stringToMatch), replacement);
        }

        return result;
    }

    private String implFileFolder(String output) {
        return this.outputFolder + "/" + output + "/" + this.apiPackage().replace('.', '/');
    }

    public String escapeReservedWord(String name) {
        return this.reservedWordsMappings().containsKey(name) ? (String)this.reservedWordsMappings().get(name) : "_" + name;
    }

    public String apiFileFolder() {
        return this.outputFolder + File.separator + this.apiPackage().replace('.', File.separatorChar);
    }

    public String getExportedName() {
        return this.exportedName;
    }

    public void setExportedName(String name) {
        this.exportedName = name;
    }

    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        Map<String, Object> objectMap = (Map)objs.get("operations");
        List<CodegenOperation> operations = (List)objectMap.get("operation");
        Iterator var4 = operations.iterator();

        label58:
        while(true) {
            CodegenOperation operation;
            Iterator it;
            do {
                do {
                    if (!var4.hasNext()) {
                        return objs;
                    }

                    operation = (CodegenOperation)var4.next();
                    operation.httpMethod = operation.httpMethod.toLowerCase();
                    List<CodegenParameter> params = operation.allParams;
                    if (params != null && params.size() == 0) {
                        operation.allParams = null;
                    }

                    List<CodegenResponse> responses = operation.responses;
                    if (responses != null) {
                        it = responses.iterator();

                        while(it.hasNext()) {
                            CodegenResponse resp = (CodegenResponse)it.next();
                            if ("0".equals(resp.code)) {
                                resp.code = "default";
                            }
                        }
                    }
                } while(operation.examples == null);
            } while(operation.examples.isEmpty());

            it = operation.examples.iterator();

            while(true) {
                String contentType;
                do {
                    if (!it.hasNext()) {
                        continue label58;
                    }

                    Map<String, String> example = (Map)it.next();
                    contentType = (String)example.get("contentType");
                } while(contentType != null && contentType.startsWith("application/json"));

                it.remove();
            }
        }
    }

    private static List<Map<String, Object>> getOperations(Map<String, Object> objs) {
        List<Map<String, Object>> result = new ArrayList();
        Map<String, Object> apiInfo = (Map)objs.get("apiInfo");
        List<Map<String, Object>> apis = (List)apiInfo.get("apis");
        Iterator var4 = apis.iterator();

        while(var4.hasNext()) {
            Map<String, Object> api = (Map)var4.next();
            result.add((Map)api.get("operations"));
        }

        return result;
    }

    private static List<Map<String, Object>> sortOperationsByPath(List<CodegenOperation> ops) {
        Multimap<String, CodegenOperation> opsByPath = ArrayListMultimap.create();
        Iterator var2 = ops.iterator();

        while(var2.hasNext()) {
            CodegenOperation op = (CodegenOperation)var2.next();
            opsByPath.put(op.path, op);
        }

        List<Map<String, Object>> opsByPathList = new ArrayList();
        Iterator var8 = opsByPath.asMap().entrySet().iterator();

        while(var8.hasNext()) {
            Entry<String, Collection<CodegenOperation>> entry = (Entry)var8.next();
            Map<String, Object> opsByPathEntry = new HashMap();
            opsByPathList.add(opsByPathEntry);
            opsByPathEntry.put("path", entry.getKey());
            opsByPathEntry.put("operation", entry.getValue());
            List<CodegenOperation> operationsForThisPath = Lists.newArrayList((Iterable)entry.getValue());
            ((CodegenOperation)operationsForThisPath.get(operationsForThisPath.size() - 1)).hasMore = false;
            if (opsByPathList.size() < opsByPath.asMap().size()) {
                opsByPathEntry.put("hasMore", "true");
            }
        }

        return opsByPathList;
    }

    public void processOpts() {
        super.processOpts();

        this.supportingFiles.add(new SupportingFile("swagger.mustache", "api", "swagger.yaml"));
        this.writeOptional(this.outputFolder, new SupportingFile("index.mustache", "", "index.js"));
        this.writeOptional(this.outputFolder, new SupportingFile("package.mustache", "", "package.json"));
        this.writeOptional(this.outputFolder, new SupportingFile("README.mustache", "", "README.md"));
    }

    public void preprocessSwagger(Swagger swagger) {
        String host = swagger.getHost();
        String port = "8080";
        if (host != null) {
            String[] parts = host.split(":");
            if (parts.length > 1) {
                port = parts[1];
            }
        }

        this.additionalProperties.put("serverPort", port);
        if (swagger.getInfo() != null) {
            Info info = swagger.getInfo();
            if (info.getTitle() != null) {
                this.projectName = info.getTitle().replaceAll("[^a-zA-Z0-9]", "-").replaceAll("^[-]*", "").replaceAll("[-]*$", "").replaceAll("[-]{2,}", "-").toLowerCase();
                this.additionalProperties.put("projectName", this.projectName);
            }
        }

        Map<String, Path> paths = swagger.getPaths();
        if (paths != null) {
            Iterator var5 = paths.keySet().iterator();

            while(true) {
                String pathname;
                Map operationMap;
                do {
                    if (!var5.hasNext()) {
                        return;
                    }

                    pathname = (String)var5.next();
                    Path path = (Path)paths.get(pathname);
                    operationMap = path.getOperationMap();
                } while(operationMap == null);

                Iterator var9 = operationMap.keySet().iterator();

                while(var9.hasNext()) {
                    HttpMethod method = (HttpMethod)var9.next();
                    Operation operation = (Operation)operationMap.get(method);
                    String tag = "default";
                    if (operation.getTags() != null && operation.getTags().size() > 0) {
                        tag = this.toApiName((String)operation.getTags().get(0));
                    }

                    if (operation.getOperationId() == null) {
                        operation.setOperationId(this.getOrGenerateOperationId(operation, pathname, method.toString()));
                    }

                    if (operation.getVendorExtensions().get("x-swagger-router-controller") == null) {
                        operation.getVendorExtensions().put("x-swagger-router-controller", this.sanitizeTag(tag));
                    }
                }
            }
        }
    }

    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        Swagger swagger = (Swagger)objs.get("swagger");
        if (swagger != null) {
            try {
                SimpleModule module = new SimpleModule();
                module.addSerializer(Double.class, new JsonSerializer<Double>() {
                    public void serialize(Double val, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
                        jgen.writeNumber(new BigDecimal(val));
                    }
                });
                objs.put("swagger-yaml", Yaml.mapper().registerModule(module).writeValueAsString(swagger));
            } catch (JsonProcessingException var7) {
                LOGGER.error(var7.getMessage(), var7);
            }
        }

        Iterator var8 = getOperations(objs).iterator();

        while(var8.hasNext()) {
            Map<String, Object> operations = (Map)var8.next();
            List<CodegenOperation> ops = (List)operations.get("operation");
            List<Map<String, Object>> opsByPathList = sortOperationsByPath(ops);
            operations.put("operationsByPath", opsByPathList);
        }

        return super.postProcessSupportingFileData(objs);
    }

    public String removeNonNameElementToCamelCase(String name) {
        return this.removeNonNameElementToCamelCase(name, "[-:;#]");
    }

    public String escapeUnsafeCharacters(String input) {
        return input.replace("*/", "*_/").replace("/*", "/_*");
    }

    public String escapeQuotationMark(String input) {
        return input.replace("\"", "");
    }
}
