/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.publish.maven.internal.artifact;

import org.gradle.api.Action;
import org.gradle.api.Buildable;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.DefaultDomainObjectSet;
import org.gradle.api.internal.file.AbstractFileCollection;
import org.gradle.api.internal.notations.api.NotationParser;
import org.gradle.api.internal.tasks.AbstractTaskDependency;
import org.gradle.api.internal.tasks.TaskDependencyInternal;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.publish.maven.MavenArtifact;
import org.gradle.api.publish.maven.MavenArtifactSet;
import org.gradle.api.tasks.TaskDependency;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

public class DefaultMavenArtifactSet extends DefaultDomainObjectSet<MavenArtifact> implements MavenArtifactSet {
    private final TaskDependencyInternal builtBy = new ArtifactsTaskDependency();
    private final ArtifactsFileCollection files = new ArtifactsFileCollection();
    private final NotationParser<MavenArtifact> mavenArtifactParser;

    public DefaultMavenArtifactSet(NotationParser<MavenArtifact> mavenArtifactParser) {
        super(MavenArtifact.class);
        this.mavenArtifactParser = mavenArtifactParser;
    }

    public MavenArtifact addArtifact(Object source) {
        MavenArtifact artifact = mavenArtifactParser.parseNotation(source);
        add(artifact);
        return artifact;
    }

    public MavenArtifact addArtifact(Object source, Action<MavenArtifact> config) {
        MavenArtifact artifact = addArtifact(source);
        config.execute(artifact);
        return artifact;
    }

    public FileCollection getFiles() {
        return files;
    }

    private class ArtifactsFileCollection extends AbstractFileCollection {

        public String getDisplayName() {
            return "maven artifacts";
        }

        @Override
        public TaskDependency getBuildDependencies() {
            return builtBy;
        }

        public Set<File> getFiles() {
            Set<File> files = new LinkedHashSet<File>();
            for (MavenArtifact artifact : DefaultMavenArtifactSet.this) {
                files.add(artifact.getFile());
            }
            return files;
        }
    }

    private class ArtifactsTaskDependency extends AbstractTaskDependency {
        public void resolve(TaskDependencyResolveContext context) {
            for (MavenArtifact mavenArtifact : DefaultMavenArtifactSet.this) {
                if (mavenArtifact instanceof Buildable) {
                    context.add(mavenArtifact);
                }
            }
        }
    }
}
