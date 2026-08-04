package io.codegauge.parser;

import io.codegauge.core.Dependency;
import io.codegauge.core.DependencyResult;
import io.codegauge.core.Plugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link PomParser} backed by the JDK's built-in DOM XML parser — no
 * third-party dependency required.
 *
 * <p><strong>Known limitation:</strong> {@code ${property}} placeholders are
 * resolved only against this POM's own {@code <properties>} section.
 * Properties inherited from a parent POM or a BOM's
 * {@code <dependencyManagement>} are not resolved; such versions are
 * reported as the raw, unresolved {@code ${...}} string.
 */
public final class MavenPomParser implements PomParser {

    private static final Pattern PROPERTY_PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");

    @Override
    public DependencyResult parse(Path pomFile) throws IOException {
        Document document = parseXml(pomFile);
        Element root = document.getDocumentElement();

        Map<String, String> properties = readProperties(root);
        List<Dependency> dependencies = readDependencies(root, properties);
        List<Plugin> plugins = readPlugins(root, properties);
        List<String> duplicates = findDuplicateCoordinates(dependencies);

        return new DependencyResult(true, dependencies, plugins, duplicates);
    }

    private Document parseXml(Path pomFile) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // XXE hardening: a scanned repository's pom.xml is not a fully
            // trusted input, so external entities and DOCTYPE declarations
            // are disabled outright.
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(pomFile.toFile());
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse POM: " + pomFile, e);
        }
    }

    private Map<String, String> readProperties(Element root) {
        Map<String, String> properties = new HashMap<>();
        Element propertiesElement = firstChildElement(root, "properties");
        if (propertiesElement != null) {
            for (Element child : childElements(propertiesElement)) {
                properties.put(child.getTagName(), child.getTextContent().trim());
            }
        }
        return properties;
    }

    private List<Dependency> readDependencies(Element root, Map<String, String> properties) {
        List<Dependency> result = new ArrayList<>();
        Element dependenciesElement = firstChildElement(root, "dependencies");
        if (dependenciesElement == null) {
            return result;
        }
        for (Element dep : childElements(dependenciesElement, "dependency")) {
            String groupId = textOf(dep, "groupId", "");
            String artifactId = textOf(dep, "artifactId", "");
            String version = resolve(textOf(dep, "version", ""), properties);
            String scope = textOf(dep, "scope", "compile");
            if (!groupId.isEmpty() && !artifactId.isEmpty()) {
                result.add(new Dependency(groupId, artifactId, version, scope));
            }
        }
        return result;
    }

    private List<Plugin> readPlugins(Element root, Map<String, String> properties) {
        List<Plugin> result = new ArrayList<>();
        Element buildElement = firstChildElement(root, "build");
        if (buildElement == null) {
            return result;
        }
        Element pluginsElement = firstChildElement(buildElement, "plugins");
        if (pluginsElement == null) {
            return result;
        }
        for (Element plugin : childElements(pluginsElement, "plugin")) {
            String groupId = textOf(plugin, "groupId", "org.apache.maven.plugins");
            String artifactId = textOf(plugin, "artifactId", "");
            String version = resolve(textOf(plugin, "version", ""), properties);
            if (!artifactId.isEmpty()) {
                result.add(new Plugin(groupId, artifactId, version));
            }
        }
        return result;
    }

    private List<String> findDuplicateCoordinates(List<Dependency> dependencies) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Dependency dependency : dependencies) {
            counts.merge(dependency.coordinate(), 1, Integer::sum);
        }
        List<String> duplicates = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > 1) {
                duplicates.add(entry.getKey());
            }
        }
        return duplicates;
    }

    private String resolve(String rawValue, Map<String, String> properties) {
        if (rawValue.isEmpty()) {
            return rawValue;
        }
        Matcher matcher = PROPERTY_PLACEHOLDER.matcher(rawValue);
        if (matcher.matches()) {
            String propertyName = matcher.group(1);
            return properties.getOrDefault(propertyName, rawValue);
        }
        return rawValue;
    }

    private static Element firstChildElement(Element parent, String tagName) {
        for (Element child : childElements(parent)) {
            if (child.getTagName().equals(tagName)) {
                return child;
            }
        }
        return null;
    }

    private static List<Element> childElements(Element parent) {
        List<Element> result = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                result.add((Element) node);
            }
        }
        return result;
    }

    private static List<Element> childElements(Element parent, String tagName) {
        List<Element> result = new ArrayList<>();
        for (Element child : childElements(parent)) {
            if (child.getTagName().equals(tagName)) {
                result.add(child);
            }
        }
        return result;
    }

    private static String textOf(Element parent, String tagName, String defaultValue) {
        Element element = firstChildElement(parent, tagName);
        return element == null ? defaultValue : element.getTextContent().trim();
    }
}