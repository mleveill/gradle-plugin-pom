package dev.trinkets.gradle.plugin.pom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import static org.gradle.testkit.runner.TaskOutcome.*;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * TODO: test existing pub.
 */
public class PomPluginTest {

    @Rule
	public final TemporaryFolder testProjectDir = new TemporaryFolder();
    @Rule
	public final TemporaryFolder resultsDir = new TemporaryFolder();
    private File settingsFile;
    private File buildFile;
	private File gradleProps;

    @Before
    public void setup() throws IOException {
        settingsFile = testProjectDir.newFile("settings.gradle");
        buildFile = testProjectDir.newFile("build.gradle");
		gradleProps = testProjectDir.newFile("gradle.properties");
    }

    @Test
    public void jar() throws IOException, SAXException, XPathException, ParserConfigurationException {
		final String group = "com.example";
		final String artifact = "pom-test-jar";
		final String version = "0.0.1";

		writeFile(gradleProps, "version=" + version);

        writeFile(settingsFile, "pluginManagement {\n"
								+ "    repositories {\n"
								+ "        flatDir dirs: '" + new File("build/libs").getAbsolutePath().replace("\\", "/") + "' // dev.trinkets.pom plugin\n"
								+ "    }\n"
								+ "}\n"
								+ "rootProject.name = '" + artifact + "'\n");

		// TODO: needs the special auxillary gradle plugin pom for this to work:

        String buildFileContent = "plugins {\n"
								+ "    id 'java-library'\n"
								+ "    id 'dev.trinkets.pom' version '" + System.getProperty("projectVersion") + "'\n"
								+ "}\n"
								+ "group = '" + group + "'\n"
								+ "tpom { pomToJar newPub('Java'), jar }\n";

        writeFile(buildFile, buildFileContent);
		//System.err.println("Build file: " + buildFile.getAbsolutePath());

        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir.getRoot())
            .withArguments("jar", "--stacktrace")
			//.withPluginClasspath()
            .build();

		assertEquals(SUCCESS, result.task(":jar").getOutcome());

		Path pom = resultsDir.getRoot().toPath().resolve("pom.xml");
		extractFileFromZip(
				testProjectDir.getRoot().toPath().resolve("build/libs/pom-test-jar-0.0.1.jar"),
				"META-INF/maven/" + group + "/" + artifact + "/pom.xml", pom);
		checkPom(pom, group, artifact, version);
	}

	private void checkPom(Path pom,
			String expectedGroup, String expectedArtifact, String expectedVersion)
			throws IOException, SAXException, XPathException, ParserConfigurationException {

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression getGroup = xPath.compile("/project/groupId");
		XPathExpression getArtifact = xPath.compile("/project/artifactId");
		XPathExpression getVersion = xPath.compile("/project/version");

		try (FileInputStream fileIS = new FileInputStream(pom.toFile())) {
			Document doc = builder.parse(fileIS);
			String group = getGroup.evaluateExpression(doc, Node.class).getTextContent();
			String artifact = getArtifact.evaluateExpression(doc, Node.class).getTextContent();
			String version = getVersion.evaluateExpression(doc, Node.class).getTextContent();
			assertEquals("Pom group", expectedGroup, group);
			assertEquals("Pom artifact", expectedArtifact, artifact);
			assertEquals("Pom version", expectedVersion, version);
		}
	}

	public void extractFileFromZip(Path zip, String fileName, Path dst) throws IOException {
		try (FileSystem fileSystem = FileSystems.newFileSystem(zip, null)) {
			Path fileToExtract = fileSystem.getPath(fileName);
			Files.copy(fileToExtract, dst);
		}
	}

    private void writeFile(File destination, String content) throws IOException {
        try (FileWriter out = new FileWriter(destination);
				BufferedWriter output = new BufferedWriter(out)) {
            output.write(content);
        }
    }
}
