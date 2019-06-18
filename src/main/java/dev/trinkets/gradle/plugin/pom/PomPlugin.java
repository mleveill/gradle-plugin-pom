package dev.trinkets.gradle.plugin.pom;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.publish.maven.MavenPublication;

/**
 * Provides utilities for creating a POM file and including it in a Jar.
 *
 * These properties are set in the pom:
 *		groupId		-&gt; project.group
 *		artifactId	-&gt; project.name
 *		version		-&gt; project.version
 *		name		-&gt; project.name
 *		description	-&gt; project.description
 *
 * Applies the "maven-publish" plugin.
 *
 * Example use in a build.gradle file (note the plugin id differs from the package name and Maven artifactId):
 *
 *	# Without existing publication
 *	plugins { id 'dev.trinkets.pom' }
 *	tpom { pomToJar newPub('Java'), jar }
 *
 *	# With existing publication
 *	plugins { id 'dev.trinkets.pom' }
 *	publishing {
 *		publications {
 *			MyPub(MavenPublication) {
 *				from components.java
 *			}
 *		}
 *	}
 *	tpom { pomToJar pub('MyPub'), jar }
 */
public class PomPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getPlugins().apply("maven-publish");

		PomExtension ext = project.getExtensions().create(
				"tpom", PomExtension.class, project);

		project.afterEvaluate(proj -> {
			ext.getMap().forEach(mapping -> {
				MavenPublication pub = mapping.getSrc();
				String groupId = pub.getGroupId();
				String artifactId = pub.getArtifactId();

				if (groupId.isBlank()) {
					throw new IllegalArgumentException("Either give publication '"
							+ pub.getName() + "' a groupId or set a group for the project.");
				}

				if (artifactId.isBlank()) {
					throw new IllegalArgumentException("Either give publication '"
							+ pub.getName() + "' an artifactId or set a name for the project.");
				}

				if (pub.getVersion().isBlank() || pub.getVersion().equals("unspecified")) {
					throw new IllegalArgumentException("Either give publication '"
							+ pub.getName() + "' a version or set a version for the project.");
				}

				String dstFolder = "META-INF/maven/" + groupId + "/" + artifactId;

				mapping.getDst().into(dstFolder, copySpec -> {
					Task pubTask = proj.getTasks().getByName(
						"generatePomFileFor" + pub.getName() + "Publication");

					copySpec.from(pubTask).rename(".*", "pom.xml");
				});
			});
		});
	}
}
