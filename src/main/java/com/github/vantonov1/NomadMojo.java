package com.github.vantonov1;


import com.github.vantonov1.maven.metadata.Metadata;
import com.github.vantonov1.maven.metadata.Snapshot;
import com.hashicorp.nomad.apimodel.Job;
import com.hashicorp.nomad.apimodel.Task;
import com.hashicorp.nomad.apimodel.TaskArtifact;
import com.hashicorp.nomad.apimodel.TaskGroup;
import com.hashicorp.nomad.javasdk.NomadApiClient;
import com.hashicorp.nomad.javasdk.NomadApiConfiguration;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY)
public class NomadMojo extends AbstractMojo {
    @Parameter(property = "deploy.addr")
    private URL addr;

    @Parameter(property = "deploy.options")
    private List<String> options;

    @Parameter(property = "deploy.datacenters", defaultValue = "dc1")
    private List<String> datacenters;

    @Parameter(name = "downloadUrl", defaultValue = "${project.distributionManagement.downloadUrl}", readonly = true)
    private String downloadUrl;

    @Parameter(name = "groupId", defaultValue = "${project.groupId}", readonly = true)
    private String groupId;

    @Parameter(name = "artifactId", defaultValue = "${project.artifactId}", readonly = true)
    private String artifactId;

    @Parameter(name = "version", defaultValue = "${project.version}", readonly = true)
    private String version;

    @Parameter(name = "classifier", defaultValue = "${project.classifier}", readonly = true)
    private String classifier;

    public void execute() throws MojoExecutionException {
        try {
            final NomadApiConfiguration.Builder builder = new NomadApiConfiguration.Builder().setFromEnvironmentVariables(System.getenv());
            if(addr != null && !addr.toExternalForm().isEmpty()) {
                builder.setAddress(addr.toExternalForm());
            }
            final String jarUrl = getJarNameFromMetadata();
            final String jar = jarUrl.substring(jarUrl.lastIndexOf('/') + 1);
            new NomadApiClient(builder.build()).getJobsApi().register(createJob(jarUrl, jar));
        } catch (Exception e) {
            throw new MojoExecutionException("error while deploying to nomad", e);
        }
    }

    private Job createJob(String jarUrl, String jarName) {
        final Job job = new Job();
        job.setId(artifactId);
        job.setDatacenters(datacenters);
        final TaskGroup group = new TaskGroup();
        group.setName(artifactId);
        final Task task = new Task();
        task.setName(artifactId);
        task.setDriver("java");
        task.setConfig(Map.of("jvm_options", options, "jar_path", "local/" + jarName));
        final TaskArtifact artifact = new TaskArtifact();
        artifact.setGetterSource(jarUrl);
        task.setArtifacts(List.of(artifact));
        group.addTasks(task);
        job.setTaskGroups(List.of(group));
        return job;
    }

    private String getJarNameFromMetadata() throws JAXBException, MalformedURLException {
        final String root = downloadUrl + (downloadUrl.endsWith("/") ? "" : '/') + groupId.replace('.', '/') + '/' + artifactId + '/' + version;
        return root + '/' + getJarName(root);
    }

    private String getJarName(String root) throws JAXBException, MalformedURLException {
        return artifactId + '-' + getBuildStamp(root) + (classifier != null ? '-' + classifier : "") + ".jar";
    }

    private String getBuildStamp(String root) throws JAXBException, MalformedURLException {
        if (version.contains("SNAPSHOT")) {
            final Metadata metadata = (Metadata) JAXBContext.newInstance(Metadata.class).createUnmarshaller().unmarshal(new URL(root + "/maven-metadata.xml"));
            assert artifactId.equals(metadata.getArtifactId());
            final Snapshot snapshot = metadata.getVersioning().getSnapshot();
            return version.replace("SNAPSHOT", snapshot.getTimestamp()) + '-' + snapshot.getBuildNumber();
        } else {
            return version;
        }
    }
}
