/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.syscon.elite2.swagger.codegen.language;

import io.swagger.codegen.*;
import io.swagger.codegen.languages.JavaClientCodegen;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import net.syscon.elite2.swagger.codegen.ConfigurableCodegenConfig;
import net.syscon.elite2.swagger.codegen.Inflector;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

/**
 * https://github.com/swagger-api/swagger-codegen/blob/master/modules/swagger-codegen/src/main/java/com/wordnik/swagger/codegen/languages/JaxRSServerCodegen.java.
 *
 * @author jbellmann
 */
public class JaxRsInterfaces extends JavaClientCodegen implements CodegenConfig, ConfigurableCodegenConfig {
    // Additional CLI options
    public static final String SUPPORT_PACKAGE = "supportPackage";

    // Vendor extension constants.
    public static final String VENDOR_EXTENSION_JAVA_OPERATION_NAME = "x-annotation-javaOperationName";
    private static final String X_ANNOTATION_JAVA_TYPE = "x-annotation-javaType";
    private static final String X_ANNOTATION_TYPE_HEADERS = "x-annotation-headersWithType_";

    protected String supportPackage = "";

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public String getName() {
        return "jaxrsinterfaces";
    }


    @Override
    public void preprocessSwagger(Swagger swagger) {
        vendorExtensions.put("basePath", swagger.getBasePath());
        super.preprocessSwagger(swagger);
    }

    @Override
    public String getHelp() {
        return "Generates JAXRS-Interfaces.";
    }

    public JaxRsInterfaces() {
        super();
        embeddedTemplateDir = templateDir = "JaxRsInterfaces";
        modelTemplateFiles.put("model.mustache", ".java");
        apiTemplateFiles.put("api.mustache", ".java");

        cliOptions.add(CliOption.newString(SUPPORT_PACKAGE, "Package for supporting files (defaults to API package if not specified)."));
    }

    @Override
    public List<SupportingFile> supportingFiles() {
        supportingFiles.clear();

        supportingFiles.add(new SupportingFile("order.mustache",
                (supportPackage()).replace(".", File.separator), "Order.java"));

        supportingFiles.add(new SupportingFile("custodyStatusCode.mustache",
                (supportPackage()).replace(".", File.separator), "CustodyStatusCode.java"));

        supportingFiles.add(new SupportingFile("ResponseDelegate.mustache",
                (supportPackage()).replace(".", File.separator), "ResponseDelegate.java"));

        supportingFiles.add(new SupportingFile("OperationResponse.mustache",
                (supportPackage()).replace(".", File.separator), "OperationResponse.java"));

        languageSpecificPrimitives = new HashSet<>(
                Arrays.asList("String", "boolean", "Boolean", "Double", "Integer", "Long", "Float"));

        return supportingFiles;
    }

    @Override
    public String apiFileFolder() {
        return outputFolder + "/" + apiPackage().replace('.', File.separatorChar);
    }

    @Override
    public String modelFileFolder() {
        return outputFolder + "/" + modelPackage().replace('.', File.separatorChar);
    }

    @Override
    public String modelPackage() {
        if (this.modelPackage == null || this.modelPackage.trim().isEmpty()) {
            throw new RuntimeException("'modelPackage' should not be null or empty");
        }

        return this.modelPackage;
    }

    @Override
    public String apiPackage() {

        if (this.apiPackage == null || this.apiPackage.trim().isEmpty()) {
            throw new RuntimeException("'apiPackage' should not be null or empty");
        }

        return this.apiPackage;
    }

    @Override
    public String getTypeDeclaration(final Property p) {
        if (p instanceof ArrayProperty) {
            ArrayProperty ap = (ArrayProperty) p;
            Property inner = ap.getItems();
            return getSwaggerType(p) + "<" + getTypeDeclaration(inner) + ">";
        } else if (p instanceof MapProperty) {
            MapProperty mp = (MapProperty) p;
            Property inner = mp.getAdditionalProperties();

            return getTypeDeclaration(inner);
        }

        return super.getTypeDeclaration(p);
    }

    @Override
    public void addOperationToGroup(final String tag, final String resourcePath, final Operation operation,
            final CodegenOperation co, final Map<String, List<CodegenOperation>> operations) {
        String basePath = resourcePath;
        if (basePath.startsWith("/")) {
            basePath = basePath.substring(1);
        }

        int pos = basePath.indexOf("/");
        if (pos > 0) {
            basePath = basePath.substring(0, pos);
        }

        if (Objects.equals(basePath, "")) {
            basePath = "default";
        } else {
            if (co.path.startsWith("/" + basePath)) {
                co.path = co.path.substring(("/" + basePath).length());
            }

            co.subresourceOperation = !co.path.isEmpty();
        }

        List<CodegenOperation> opList = operations.computeIfAbsent(basePath, k -> new ArrayList<>());

        opList.add(co);
        co.baseName = basePath;
    }

    @Override
    public void processOpts() {
        super.processOpts();

        // support package
        if (additionalProperties.containsKey(SUPPORT_PACKAGE)) {
            this.setSupportPackage((String) additionalProperties.get(SUPPORT_PACKAGE));
        }

        // some additional Jackson imports
        importMapping.put("JsonAnyGetter", "com.fasterxml.jackson.annotation.JsonAnyGetter");
        importMapping.put("JsonAnySetter", "com.fasterxml.jackson.annotation.JsonAnySetter");
        importMapping.put("JsonIgnore", "com.fasterxml.jackson.annotation.JsonIgnore");
        importMapping.put("JsonInclude", "com.fasterxml.jackson.annotation.JsonInclude");

        // we do not want to have (or need)
        importMapping.remove("SerializedName");

        // Prevents recursive addition of 'JsonCreator' import during post-processing
        importMapping.remove("com.fasterxml.jackson.annotation.JsonProperty");
    }

    @Override
    public CodegenModel fromModel(String name, Model model, Map<String, Model> allDefinitions) {
        CodegenModel codegenModel = super.fromModel(name, model, allDefinitions);

        // we need these imports for every model class
        codegenModel.imports.add("JsonAnyGetter");
        codegenModel.imports.add("JsonAnySetter");
        codegenModel.imports.add("JsonInclude");
        codegenModel.imports.add("JsonIgnore");

        return codegenModel;
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);

        // We don't need these imports in every model
        model.imports.remove("SerializedName");
        model.imports.remove("JsonValue");

        // Convert to use java type where specified
        applyJavaTypesToModel(property);

        // Tidy up enum property name (i.e. remove 'Enum' suffix)
        if (property.isEnum) {
            property.enumName = property.nameInCamelCase;
            property.datatypeWithEnum = property.enumName;
        }

        // Handle defaultValue = "null" - if property default value is "null" string,
        // actually set to 'null' so that template can conditionally render default
        // value.
        if ("null".equals(property.defaultValue)) {
            property.defaultValue = null;
        }

        // If property is a List but is not required, ensure default value is null.
        // Current behaviour is to set default value to an empty list.
        // (This is a 'fix' that is actually proposed for future release of swagger-codegen).
        if (property.isListContainer && !property.required) {
            property.defaultValue = null;
        }
    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, Map<String, Model> definitions, Swagger swagger) {
        CodegenOperation op = super.fromOperation(path, httpMethod, operation, definitions, swagger);

        // First, apply any vendor extensions defined in Swagger spec for operation.
        applyOperationVendorExtensions(path, operation, swagger);

        // Adjust operation id (and dependent properties), if necessary.
        applyOperationId(op, operation);

        applyJavaTypesToParams(op);
        applyJavaTypesToResponseHeaders(op);

        // Move 'notes' property to 'summary' property (no RAML 1.0 attribute maps to OAS 2.0 operation summary).
        op.summary = op.notes;
        op.notes = null;

        return op;
    }

    private void applyJavaTypesToModel(CodegenProperty property) {
        final String javaType = (String)property.vendorExtensions.get(X_ANNOTATION_JAVA_TYPE);
        if (javaType != null) {
            property.datatype = javaType;
            property.baseType = property.datatype;
            property.datatypeWithEnum = property.datatype;
        }
    }

    private void applyJavaTypesToResponseHeaders(CodegenOperation op) {
        op.responses.forEach(response -> {
            response.vendorExtensions.forEach((key, headerNamesList) -> {
                if (key.startsWith(X_ANNOTATION_TYPE_HEADERS)) {
                    final String baseType = StringUtils.substringAfter(key, X_ANNOTATION_TYPE_HEADERS);
                    List<String> headerNames = Arrays.asList(StringUtils.split((String)headerNamesList, ","));
                    op.responses.forEach(r -> r.headers.forEach(h -> {
                        if (headerNames.contains(h.baseName)) {
                            h.baseType = baseType;
                        }
                    }));
                }
            });
        });
    }

    private void applyJavaTypesToParams(CodegenOperation op) {
        op.allParams.forEach(param -> {
            final String javaType = (String)param.vendorExtensions.get(X_ANNOTATION_JAVA_TYPE);
            if (javaType != null) {
                param.dataType = javaType;
                param.baseType = param.dataType;
                param.datatypeWithEnum = param.dataType;
                param.isPrimitiveType = false;
            }
        });
    }

    // Extracts any vendor extensions present in Swagger specification (derived from custom annotations in RAML 1.0 spec).
    private void applyOperationVendorExtensions(String path, Operation operation, Swagger swagger) {
        if (swagger == null) {
            return;
        }

        Map<String, Path> paths = swagger.getPaths();
        Path operationPath = paths.get(path);

        if (operationPath != null) {
            operation.getVendorExtensions().putAll(operationPath.getVendorExtensions());
        }
    }

    private void applyOperationId(CodegenOperation codegenOperation, Operation operation) {
        // First check if the 'javaOperationName' annotation has been applied as vendor extension.
        String javaOperationName = (String) operation.getVendorExtensions().get(VENDOR_EXTENSION_JAVA_OPERATION_NAME);

        // If a 'javaOperationName' has been provided, use it, otherwise use a sanitized version of current operation id.
        String revisedOperationId =
                StringUtils.defaultIfBlank(javaOperationName, sanitizeOperationId(codegenOperation.operationId));

        codegenOperation.nickname = revisedOperationId;
        codegenOperation.operationIdCamelCase = StringUtils.capitalize(revisedOperationId);
    }

    private String sanitizeOperationId(String operationId) {
        String sanitizedOperationId;

        if (StringUtils.startsWithIgnoreCase(operationId, "get")) {
            return "get" + operationId.substring(3);
        } else if (StringUtils.startsWithIgnoreCase(operationId, "post")) {
            return "post" + operationId.substring(4);
        }
        else {
            sanitizedOperationId = operationId;
        }

        return sanitizedOperationId;
    }

    @Override
    public Map<String, Object> postProcessOperations(final Map<String, Object> objs) {
        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");

        if (operations != null) {
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");

            for (CodegenOperation operation : ops) {
                if (operation.returnType == null) {
                    operation.returnType = "void";
                } else if (operation.returnType.startsWith("List")) {
                    String rt = operation.returnType;
                    int end = rt.lastIndexOf(">");

                    if (end > 0) {
                        operation.returnType = rt.substring("List<".length(), end);
                        operation.returnContainer = "List";
                    }
                } else if (operation.returnType.startsWith("Map")) {
                    String rt = operation.returnType;
                    int end = rt.lastIndexOf(">");

                    if (end > 0) {
                        operation.returnType = rt.substring("Map<".length(), end);
                        operation.returnContainer = "Map";
                    }
                } else if (operation.returnType.startsWith("Set")) {
                    String rt = operation.returnType;
                    int end = rt.lastIndexOf(">");

                    if (end > 0) {
                        operation.returnType = rt.substring("Set<".length(), end);
                        operation.returnContainer = "Set";
                    }
                }
            }
        }

        return objs;
    }

    @Override
    public void setApiPackage(final String apiPackage) {
        this.apiPackage = apiPackage;
    }

    @Override
    public void setModelPackage(final String modelPackage) {
        this.modelPackage = modelPackage;
    }

    @Override
    public boolean isBuilderSupported() {
        return true;
    }

    @Override
    public void enableBuilderSupport() {
        modelTemplateFiles.put("modelBuilder.mustache", "Builder.java");
    }

    @Override
    public boolean is303Supported() {
        return false;
    }

    @Override
    public void enable303() {
        // modelTemplateFiles.remove("model.mustache");
        // modelTemplateFiles.put("model303.mustache", ".java");
    }

    @Override
    public String toApiName(String name) {
        String apiName;

        if (StringUtils.isBlank(name)) {
            apiName = "DefaultResource";
        } else {
            Inflector inflector = Inflector.getInstance();

            apiName = sanitizeName(name);
            apiName = inflector.singularize(apiName);
            apiName = inflector.upperCamelCase(apiName);

            apiName += "Resource";
        }

        return apiName;
    }

    @Override
    public void skipApiGeneration() {
        // TODO Auto-generated method stub

    }

    @Override
    public void skipModelGeneration() {
        // TODO Auto-generated method stub

    }

    public String supportPackage() {
        return StringUtils.defaultIfBlank(supportPackage, apiPackage());
    }

    public void setSupportPackage(String supportPackage) {
        this.supportPackage = supportPackage;
    }
}
