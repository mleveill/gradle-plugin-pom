package dev.trinkets.gradle.plugin.pom;

import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.jvm.tasks.Jar;

/**
 * Specifies that a pom file gets included in a Jar file.
 *
 * @see PomPlugin
 */
public class JarPom {

	private final MavenPublication src;
	private final Jar dst;

	public JarPom(MavenPublication src, Jar dst) {
		this.src = src;
		this.dst = dst;
	}

	public MavenPublication getSrc() {
		return src;
	}

	public Jar getDst() {
		return dst;
	}
}
