package net.syscon.elite2.swagger.codegen;

import io.swagger.codegen.CodegenProperty;
import io.swagger.codegen.CodegenResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PageCodegenResponse extends CodegenResponse {
    public final List<CodegenProperty> paginationHeaders = new ArrayList<>();
    public boolean isPageable = true;

    public PageCodegenResponse(CodegenResponse cr, List<String> paginationHeaders) {
        // Simple properties copy
        this.baseType = cr.baseType;
        this.code = cr.code;
        this.containerType = cr.containerType;
        this.hasMore = cr.hasMore;
        this.isBinary = cr.isBinary;
        this.isBoolean = cr.isBoolean;
        this.isByteArray = cr.isByteArray;
        this.isDate = cr.isDate;
        this.isDateTime = cr.isDateTime;
        this.isDefault = cr.isDefault;
        this.isDouble = cr.isDouble;
        this.isFile = cr.isFile;
        this.isFloat = cr.isFloat;
        this.isInteger = cr.isInteger;
        this.isListContainer = cr.isListContainer;
        this.isLong = cr.isLong;
        this.isMapContainer = cr.isMapContainer;
        this.isString = cr.isString;
        this.jsonSchema = cr.jsonSchema;
        this.message = cr.message;
        this.primitiveType = cr.primitiveType;
        this.schema = cr.schema;
        this.simpleType = cr.simpleType;

        this.examples = cr.examples;
        this.vendorExtensions = cr.vendorExtensions;

        // Modifications specific to PageCodegenResponse
        this.dataType = cr.dataType.replaceFirst("^List", "Page");
        transferPaginationHeaders(cr.headers, paginationHeaders);
    }

    private void transferPaginationHeaders(List<CodegenProperty> originalHeaders, List<String> paginationHeaders) {
        originalHeaders.forEach(h -> {
            if (paginationHeaders.contains(h.name)) {
                h.name = "get" + StringUtils.capitalize(h.name) + "()";
                this.paginationHeaders.add(h);
            } else {
                this.headers.add(h);
            }
        });

        this.hasHeaders = !this.headers.isEmpty();
    }
}
