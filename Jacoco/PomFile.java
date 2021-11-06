import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

public class PomFile {

    private String pom;
    private String fullPath;
    private String pomPath;
    private String artifactId;

    public PomFile(String pom) {
        this.pom = pom;
        try {
            this.fullPath = new File(pom).getParentFile().getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Rewrite contents of own pom.xml, augmented with information about dependency
    // srcs and dependency outputs
    public void rewrite() {
        File pomFile = new File(this.pom);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(false);
        dbFactory.setValidating(false);
        DocumentBuilder dBuilder;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomFile);

            // Get high-level project node to find <build> tag
            Node project = doc.getElementsByTagName("project").item(0);
            NodeList projectChildren = project.getChildNodes();

            // Check if <build> tag exists; if not have to make one
            Node build = null;
            for (int i = 0; i < projectChildren.getLength(); i++) {
                if (projectChildren.item(i).getNodeName().equals("build")) {
                    build = projectChildren.item(i);
                    break;
                }
            }
            if (build == null) {
                build = doc.createElement("build");
                project.appendChild(build);
            }

            NodeList buildChildren = build.getChildNodes();

            // Search for <plugins>
            Node plugins = null;
            for (int i = 0; i < buildChildren.getLength(); i++) {
                if (buildChildren.item(i).getNodeName().equals("plugins")) {
                    plugins = buildChildren.item(i);
                    break;
                }
            }
            // Add new <plugins> if non-existant
            if (plugins == null) {
                plugins = doc.createElement("plugins");
                build.appendChild(plugins);
            }
            addPlugin(plugins, doc);

            // Construct string representation of the file
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString();

            // Rewrite the pom file with this string
            PrintWriter fileWriter = new PrintWriter(this.pom);
            fileWriter.println(output);
            fileWriter.close();

        } catch (ParserConfigurationException | SAXException | TransformerException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {

            System.out.println("File does not exit: " + this.pom);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addPlugin(Node plugins, Document doc) {

        NodeList pluginsChildren = plugins.getChildNodes();
        for(int i = 0; i < pluginsChildren.getLength(); i++) {
            if(pluginsChildren.item(i) == null){
             continue;
            }
        
            NodeList pluginChildren = pluginsChildren.item(i).getChildNodes();
            
            for(int j = 0; j < pluginChildren.getLength(); j++) {
                if(pluginChildren.item(j).getTextContent().equals("jacoco-maven-plugin")) {
                    return;
                }
            }
        }
       
        {
            Node plugin = doc.createElement("plugin");
            {
                Node groupId = doc.createElement("groupId");
                groupId.setTextContent("org.jacoco");
                plugin.appendChild(groupId);
            }
            {
                Node artifactId = doc.createElement("artifactId");
                artifactId.setTextContent("jacoco-maven-plugin");
                plugin.appendChild(artifactId);
            }
            {
                Node version = doc.createElement("version");
                version.setTextContent("0.7.9");
                plugin.appendChild(version);
            }

            {
                Node version = doc.createElement("version");
                version.setTextContent("0.7.9");
                plugin.appendChild(version);
            }

            {
                Node executions = doc.createElement("executions");
                Node execution = doc.createElement("execution");
                Node Id = doc.createElement("Id");
                Id.setTextContent("default-prepare-agent");
                Node goals = doc.createElement("goals");
                Node goal = doc.createElement("goal");
                goal.setTextContent("prepare-agent");

                goals.appendChild(goal);
                execution.appendChild(Id);
                execution.appendChild(goals);
                executions.appendChild(execution);

                Node execution2 = doc.createElement("execution");
                Node Id2 = doc.createElement("Id");
                Id2.setTextContent("default-report");
                Node goals2 = doc.createElement("goals");
                Node goal2 = doc.createElement("goal");
                goal2.setTextContent("report");


                goals2.appendChild(goal2);
                execution2.appendChild(Id2);
                execution2.appendChild(goals2);
                executions.appendChild(execution2);

            }

            
            plugins.appendChild(plugin);
        }
           
    }

    pr ivate String getArtifactId() {
        return this.artifactId;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java PomFile <path pom.xml>");
        }

        InputStreamReader isReader = new InputStreamReader(System.in);
        BufferedReader bufReader = new BufferedReader(isReader);
        Map<String, PomFile> mapping = new HashMap<>();
        String input;
        try {
            // First create objects out of all the pom.xml files passed in
            while ((input = bufReader.readLine()) != null) {
                PomFile p = new PomFile(input);
                mapping.put(p.getArtifactId(), p);
            }

            // Go through all the objects and have them rewrite themselves using information
            // from dependencies
            for (Map.Entry<String, PomFile> entry : mapping.entrySet()) {
                PomFile p = entry.getValue();
                System.out.println(p);
                // Have the object rewrite itself (the pom)
                p.rewrite();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }
}
    
                

            

              

            