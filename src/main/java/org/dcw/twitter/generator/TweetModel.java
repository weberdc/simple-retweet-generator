package org.dcw.twitter.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;

public class TweetModel {
    private JsonNode root;

    public TweetModel(JsonNode root) {
        this.root = root;
    }

    public JsonNode getRoot() {
        return root;
    }

    JsonNode get(final String path) {
        return getNested(root, path);
    }

    JsonNode getNested(final JsonNode obj, final String path) {
        if (path.contains(".")) {
            final String head = path.substring(0, path.indexOf('.'));
            final String tail = path.substring(path.indexOf('.') + 1);
            if (head.startsWith("[")) {
                final int index = Integer.parseInt(head.substring(1, head.length() - 1));
                if (obj.has(index)) {
                    return getNested(obj.get(index), tail);
                } else {
                    System.err.println("Could not find index: " + index);
                    return JsonNodeFactory.instance.nullNode(); // error!
                }
            }
            if (obj.has(head)) {
                return getNested(obj.get(head), tail);
            } else {
                System.err.println("Could not find sub-path: " + tail);
                return JsonNodeFactory.instance.nullNode(); // error!
            }
        } else {
            return obj.has(path) ? obj.get(path) : JsonNodeFactory.instance.nullNode();
        }
    }

    public void set(String path, Object value) {
        setNested(root, path, value);
    }

    void setNested(final JsonNode node, final String path, final Object value) {
        if (path.contains(".")) {
            final String head = path.substring(0, path.indexOf('.'));
            final String tail = path.substring(path.indexOf('.') + 1);
            if (head.startsWith("[")) { // deal with arrays of structures
                final int index = Integer.parseInt(head.substring(1, head.length() - 1));
                if (node.has(index)) {
                    setNested(node.get(index), tail, value);
                } else {
                    System.err.println("Could not find index: " + index);
                }
            } else if (node.has(head)) {
                if (tail.startsWith("[")) {
                    setNested(node.get(head), tail, value);
                } else {
                    setNested(node.get(head), tail, value);
                }
            } else {
                System.err.println("Could not find sub-path: " + tail);
            }
        } else {
            final JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
            ObjectNode obj = null;
            if (path.startsWith("[")) { // deal with arrays of values
                final int index = Integer.parseInt(path.substring(1, path.length() - 1));
                if (node.has(index)) {
                    obj = (ObjectNode) node.get(index); // set node to the indexed element
                } else {
                    System.err.println("Could not find index: " + index);
                }
            } else {
                obj = (ObjectNode) node;
            }

            if (value == null) {
                obj.set(path, jsonNodeFactory.nullNode());
            } else if (value instanceof JsonNode) {
                obj.set(path, (JsonNode) value);
            } else if (value instanceof Boolean) {
                obj.set(path, jsonNodeFactory.booleanNode((Boolean) value));
            } else if (value instanceof BigDecimal) {
                obj.set(path, jsonNodeFactory.numberNode((BigDecimal) value));
            } else if (value instanceof String) {
                obj.set(path, jsonNodeFactory.textNode(value.toString()));
            } else if (value instanceof double[]) { //value.getClass().isArray()) {
                final ArrayNode arrayNode = jsonNodeFactory.arrayNode();
                double[] array = (double[]) value;
                for (double d : array) {
                    arrayNode.add(d);
                }
                obj.set(path, arrayNode);
            } else if (value instanceof int[]) {
                final ArrayNode arrayNode = jsonNodeFactory.arrayNode();
                int[] array = (int[]) value;
                for (int i : array) {
                    arrayNode.add(i);
                }
                obj.set(path, arrayNode);
            }
        }
    }

    public boolean has(final String path) {
        return has(root, path);
    }

    public boolean has(final JsonNode obj, final String path) {
        if (path.contains(".")) {
            final String head = path.substring(0, path.indexOf('.'));
            final String tail = path.substring(path.indexOf('.') + 1);
            return obj.has(head) && has(obj.get(head), tail);
        }
        return obj.has(path);
    }
}

