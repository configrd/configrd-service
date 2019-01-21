package io.configrd.core.git;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.configrd.core.exception.InitializationException;
import io.configrd.core.processor.ProcessorSelector;
import io.configrd.core.source.PropertyPacket;
import io.configrd.core.source.RepoDef;
import io.configrd.core.source.StreamPacket;
import io.configrd.core.source.StreamSource;
import io.configrd.core.util.StringUtils;
import io.configrd.core.util.URIBuilder;

public class GitStreamSource implements StreamSource {

  private final static Logger logger = LoggerFactory.getLogger(GitStreamSource.class);

  private final GitRepoDef repoDef;
  private URIBuilder builder;
  public static final String GIT = "git";
  public final GitCredentials creds;

  private Git git;

  public GitStreamSource(GitRepoDef repoDef, GitCredentials creds) {

    this.repoDef = repoDef;
    this.builder = URIBuilder.create(repoDef.toURI());
    this.creds = creds;

  }

  @Override
  public void close() throws IOException {
    if (git != null)
      git.close();
  }

  @Override
  public Optional<PropertyPacket> stream(final String path) {

    StreamPacket packet = null;

    long start = System.currentTimeMillis();

    URI request = prototypeURI(path);

    logger.debug("Requesting git path: " + request.toString());

    try (InputStream is = new FileInputStream(new File(request))) {

      if (is != null) {

        packet = new StreamPacket(request, is);
        packet.putAll(ProcessorSelector.process(request.toString(), packet.bytes()));

      }

    } catch (IOException io) {

      logger.debug(io.getMessage());
      // Nothing, simply file not there

    }

    logger.trace("Git connector took: " + (System.currentTimeMillis() - start) + "ms to fetch "
        + request.toString());

    return Optional.ofNullable(packet);
  }

  @Override
  public String getSourceName() {
    return GIT;
  }

  @Override
  public RepoDef getSourceConfig() {
    return repoDef;
  }

  @Override
  public URI prototypeURI(String path) {
    return builder.build(path);
  }

  @Override
  public void init() {
    try {

      CloneCommand clone = Git.cloneRepository().setURI(repoDef.getUri()).setRemote("origin")
          .setDirectory(new File(repoDef.toURI()));

      URIish uri = null;

      try {
        uri = new URIish(repoDef.getUri());
      } catch (Exception e) {
        throw new IllegalArgumentException(e);
      }

      if (StringUtils.hasText(repoDef.getBranchName())) {
        clone = clone.setBranch(repoDef.getBranchName());
      }

      if (uri.getScheme() == null || (!uri.getScheme().toLowerCase().startsWith("http")
          && !uri.getScheme().toLowerCase().startsWith("git:"))) {

        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
          @Override
          protected void configure(Host host, Session session) {

          }

          @Override
          protected JSch createDefaultJSch(FS fs) throws JSchException {
            JSch defaultJSch = super.createDefaultJSch(fs);
            defaultJSch.addIdentity(repoDef.getUsername(), repoDef.getPassword());
            return defaultJSch;
          }
        };

        clone.setTransportConfigCallback(new TransportConfigCallback() {
          @Override
          public void configure(Transport transport) {

            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactory);
          }
        });

      } else {

        if (repoDef.getAuthMethod() != null) {
          clone = clone.setCredentialsProvider(
              new UsernamePasswordCredentialsProvider(creds.getUsername(), creds.getPassword()));
        }
      }

      git = clone.call();

    } catch (InvalidRemoteException e2) {
      throw new IllegalArgumentException(e2);
    } catch (GitAPIException e3) {
      throw new InitializationException(e3.getMessage());
    }
  }



}
