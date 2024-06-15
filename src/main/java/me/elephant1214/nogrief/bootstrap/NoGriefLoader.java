package me.elephant1214.nogrief.bootstrap;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

@SuppressWarnings("UnstableApiUsage")
public class NoGriefLoader implements PluginLoader {
    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build());
        resolver.addRepository(new RemoteRepository.Builder("jitpack", "default", "https://www.jitpack.io").build());
        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.24"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("cloud.commandframework:cloud-paper:1.8.4"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("cloud.commandframework:cloud-annotations:1.8.4"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("cloud.commandframework:cloud-minecraft-extras:1.8.4"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.github.Elephant1214:CCFUtils:0a75e37d55"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.github.Elephant1214:InventoryMenus:fd2b7367c9"), null));

        classpathBuilder.addLibrary(resolver);
    }
}
