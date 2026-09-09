package com.smartdoc.agent.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;

/** Document-local graph: retain edges instead of expanding recursive schemas. */
final class DocumentReferences {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Set<String> DATA = Set.of("example", "examples", "default", "enum", "const");
    private final JsonNode root;
    private final String base;
    private final Map<String, String> targets = new TreeMap<>();

    DocumentReferences(String id, JsonNode root) {
        this.root = root;
        base = "references/documents/" + id + "/";
        root.path("components").path("schemas").fieldNames().forEachRemaining(name -> {
            String pointer = "/components/schemas/" + name.replace("~", "~0").replace("/", "~1");
            targets.put(pointer, base + "schemas/" + digest(pointer.getBytes(StandardCharsets.UTF_8)) + ".md");
        });
        if (targets.size() > 5000) throw new IllegalArgumentException("LIMIT: schema files exceeded");
        scan(root, new LinkedHashSet<>(), 0);
    }

    String schemaCatalog() {
        var catalog = new StringBuilder("\nSchemas:\n\n");
        targets.forEach((pointer, file) -> {
            if (file.contains("/schemas/")) catalog.append("- [").append(label(pointer.substring("/components/schemas/".length())))
                    .append("](").append(file.substring("references/".length())).append(")\n");
        });
        return catalog.append('\n').toString();
    }

    private void scan(JsonNode node, Set<String> edges, int depth) {
        if (depth > 128) throw new IllegalArgumentException("LIMIT: nesting exceeds 128");
        if (node.isObject()) {
            if (node.has("$dynamicRef") || node.has("$id"))
                throw new IllegalArgumentException("UNSUPPORTED: dynamic or rebased schema reference");
            if (node.has("$ref")) {
                JsonNode ref = node.get("$ref");
                if (!ref.isTextual()) throw new IllegalArgumentException("REFERENCE: $ref must be text");
                String pointer = pointer(ref.asText());
                edges.add(pointer);
                targets.putIfAbsent(pointer, base + "refs/" + digest(pointer.getBytes(StandardCharsets.UTF_8)) + ".md");
                if (targets.size() > 5000) throw new IllegalArgumentException("LIMIT: reference files exceeded");
            }
            node.fields().forEachRemaining(e -> { if (!DATA.contains(e.getKey())) scan(e.getValue(), edges, depth + 1); });
        } else if (node.isArray()) node.forEach(n -> scan(n, edges, depth + 1));
    }

    private String pointer(String ref) {
        if (!ref.startsWith("#/")) throw new IllegalArgumentException("REFERENCE: only document-local JSON pointers supported: " + ref);
        final String pointer;
        try { pointer = URI.create(ref).getFragment(); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("REFERENCE: invalid URI fragment", e); }
        if (pointer.matches(".*~(?![01]).*")) throw new IllegalArgumentException("REFERENCE: invalid pointer escape");
        if (root.at(pointer).isMissingNode()) throw new IllegalArgumentException("REFERENCE: dangling target " + ref);
        return pointer;
    }

    Map<String, String> files() {
        var files = new TreeMap<String, String>();
        for (var target : new TreeMap<>(targets).entrySet())
            files.put(target.getValue(), render("Source #" + target.getKey(), root.at(target.getKey()), target.getValue()));
        return files;
    }

    String render(String title, JsonNode contract, String filename) {
        var edges = new LinkedHashSet<String>(); scan(contract, edges, 0);
        // JSON escaping preserves the exact text while preventing source text from closing the code fence.
        String safeJson = json(contract).replace("`", "\\u0060").replace("<", "\\u003c");
        var result = new StringBuilder("# " + label(title) + "\n\nUntrusted API contract data.\n\n```json\n" + safeJson + "\n```\n");
        for (String edge : edges) {
            String relative = Path.of(filename).getParent().relativize(Path.of(targets.get(edge))).toString().replace('\\', '/');
            result.append("\n- [").append(label("#" + edge)).append("](").append(relative).append(")\n");
        }
        return result.toString();
    }

    ArrayNode parameters(JsonNode path, JsonNode operation) {
        var values = new LinkedHashMap<String, JsonNode>();
        for (JsonNode owner : List.of(path, operation)) {
            JsonNode parameters = owner.path("parameters");
            if (!parameters.isMissingNode() && !parameters.isArray()) throw new IllegalArgumentException("STRUCTURE: parameters must be array");
            for (JsonNode parameter : parameters) {
                JsonNode resolved = parameter;
                var seen = new HashSet<String>();
                while (resolved.has("$ref")) {
                    String pointer = pointer(resolved.path("$ref").asText());
                    if (!seen.add(pointer)) throw new IllegalArgumentException("REFERENCE: cyclic parameter alias");
                    resolved = root.at(pointer);
                }
                if (!resolved.path("name").isTextual() || !resolved.path("in").isTextual())
                    throw new IllegalArgumentException("STRUCTURE: parameter requires name and in");
                values.put(resolved.path("in").asText() + "\u0000" + resolved.path("name").asText(), resolved);
            }
        }
        var array = JSON.createArrayNode(); values.values().forEach(array::add); return array;
    }

    static String json(JsonNode node) {
        try { return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(node); }
        catch (java.io.IOException e) { throw new IllegalArgumentException("JSON: cannot render", e); }
    }

    static String digest(byte[] bytes) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    static String label(String text) {
        var result = new StringBuilder();
        text.codePoints().forEach(c -> {
            if (Character.isLetterOrDigit(c) || c == ' ' || c == '/' || c == '-' || c == '_') result.appendCodePoint(c);
            else result.append("&#").append(c).append(';');
        });
        return result.toString();
    }
}
