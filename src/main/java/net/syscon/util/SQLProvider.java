package net.syscon.util;


import net.syscon.prison.exception.PrisonApiRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class SQLProvider {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Map<String, String> statements = new HashMap<>();

    private final ApplicationContext applicationContext;
    private final String schemaType;

    @Autowired
    public SQLProvider(final ApplicationContext applicationContext, @Value("${schema.type}") final String schemaType) {
        this.applicationContext = applicationContext;
        this.schemaType = schemaType;

    }

    public void loadSql(final String className) {
        loadSqlResource("classpath:sqls/" + className + ".sql");
        final var schemas = StringUtils.split(schemaType, ",");
        if (schemas != null) {
            Arrays.asList(schemas).forEach(schema -> loadSqlResource("classpath:sqls/" + schema + "/" + className + ".sql"));
        }
    }

    private void loadSqlResource(final String resourcePath) {
        final var resource = applicationContext.getResource(resourcePath);
        if (resource.exists()) {
            try {
                log.debug("Loading resource {}", resourcePath);
                loadFromStream(resource.getInputStream());
            } catch (final IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

    private void loadFromStream(final InputStream in) {
        final var out = new CharArrayWriter();
        final var cbuf = new char[1024];
        try {
            final var reader = new BufferedReader(new InputStreamReader(in));
            var size = reader.read(cbuf);
            while (size > -1) {
                out.write(cbuf, 0, size);
                size = reader.read(cbuf);
            }
            parse(out.toCharArray());
        } catch (final Exception ex) {
            throw new PrisonApiRuntimeException(ex);
        }
    }

    private int getNext(final char[] content, final int offset, final char searchFor) {
        if (offset >= 0) {
            for (var i = offset; i < content.length; i++) {
                if (content[i] == searchFor) return i;
            }
        }
        return -1;
    }


    private String makeString(final char[] content, final int startIndex, final int endIndex) {
        final var sb = new StringBuilder();
        for (var i = startIndex; i < content.length && i < endIndex; i++) {
            sb.append(content[i]);
        }
        return sb.toString();
    }

    private void parse(final char[] content) throws ParseException {
        final Map<String, String> newStatements = new HashMap<>();
        var i = 0;
        while (i < content.length) {
            final var startIndex = getNext(content, i, '{');
            final var endIndex = getNext(content, startIndex, '}');
            if (startIndex > -1 && endIndex > -1) {
                final var key = removeSpecialChars(makeString(content, i, startIndex).trim(), ' ', '\t', '\n', '\r');
                final var value = removeSpecialChars(makeString(content, startIndex + 1, endIndex), '\r', '\n');
                newStatements.put(key, value);
                i = endIndex + 1;
            } else {
                if (startIndex < 0 && endIndex < 0) {
                    i = content.length;
                } else {
                    throw new ParseException("Missing end brace + ", startIndex);
                }
            }
        }
        statements.putAll(newStatements);
    }


    private boolean in(final char value, final char[] elements) {
        var found = false;
        for (var i = 0; !found && i < elements.length; i++) {
            found = elements[i] == value;
        }
        return found;
    }


    private String removeCharsStartingWith(final String text, final char... elementsToRemove) {
        for (var i = 0; i < text.length(); i++) {
            if (!in(text.charAt(i), elementsToRemove)) {
                return text.substring(i);
            }
        }
        return "";
    }

    private String removeCharsEndingWith(final String text, final char... elementsToRemove) {
        for (var i = text.length() - 1; i > 0; i--) {
            if (!in(text.charAt(i), elementsToRemove)) {
                return text.substring(0, i + 1);
            }
        }
        return "";
    }

    private String removeSpecialChars(final String text, final char... elementsToRemove) {
        final var result = removeCharsStartingWith(text, elementsToRemove);
        return removeCharsEndingWith(result, elementsToRemove);
    }


    public String get(final String name) {
        return statements.get(name);
    }
}

