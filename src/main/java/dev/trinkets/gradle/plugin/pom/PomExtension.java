package dev.trinkets.gradle.plugin.pom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.gradle.api.Project;
import org.gradle.api.publish.Publication;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.jvm.tasks.Jar;

/**
 * Provides utilities for creating a POM file and including it in a Jar.
 *
 * @see PomPlugin
 */
public class PomExtension {

	/** Specifies which pom files gets included in which Jar files. */
	private final ArrayList<JarPom> map = new ArrayList<>();
	/** Gradle project. */
	private final Project project;

	/**
	 * @param project Gradle project
	 */
	public PomExtension(Project project) {
		this.project = project;
	}

	/**
	 * @param src The publication of the POM to copy.
	 * @param dst The Jar that will contain the copied POM file.
	 */
	public void pomToJar(MavenPublication src, Jar dst) {
		map.add(new JarPom(src, dst));
	}

	/**
	 * @return Specifies which pom files gets included in which Jar files.
	 */
	public List<JarPom> getMap() {
		return Collections.unmodifiableList(map);
	}

	/**
	 * Finds an existing publication.
	 *
	 * @param name Unique name of a new publication.
	 * @return The new instance.
	 */
	public Publication pub(String name) {
		PublishingExtension publishing =
				project.getExtensions().getByType(PublishingExtension.class);

		return publishing.getPublications().getByName(name);
	}

	/**
	 * Creates a new publication.
	 *
	 * @param name Unique name of a new publication.
	 * @return The new instance.
	 */
	public MavenPublication newPub(String name) {
		PublishingExtension publishing =
				project.getExtensions().getByType(PublishingExtension.class);

		return publishing.getPublications().create(name, MavenPublication.class, pub -> {
			pub.from(project.getComponents().getByName("java"));
		});
	}
}
