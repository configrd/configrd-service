package io.configrd.core.git;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.configrd.core.exception.InitializationException;
import io.configrd.core.processor.ProcessorSelector;
import io.configrd.core.source.FileStreamSource;
import io.configrd.core.source.PropertyPacket;
import io.configrd.core.source.RepoDef;
import io.configrd.core.source.StreamPacket;
import io.configrd.core.source.StreamSource;
import io.configrd.core.util.StringUtils;
import io.configrd.core.util.URIBuilder;

public class GitStreamSource implements StreamSource, FileStreamSource {

  private final static Logger logger = LoggerFactory.getLogger(GitStreamSource.class);

  public static final String GIT = "git";
  private static final Timer timer = new Timer(true);
  private final GitRepoDef repoDef;
  private URIBuilder builder;

  public final GitCredentials creds;

  private final TimerTask pullTask = new TimerTask() {

    @Override
    public void run() {
      gitPull();
    }
  };

  private Git git;

  public GitStreamSource(GitRepoDef repoDef, GitCredentials creds) {

    this.repoDef = repoDef;
    this.builder = URIBuilder.create(toURI());
    this.creds = creds;

  }

  @Override
  public void close() throws IOException {
    if (git != null)
      git.close();

    pullTask.cancel();
  }

  @Override
  public RepoDef getSourceConfig() {
    return repoDef;
  }

  @Override
  public String getSourceName() {
    return GIT;
  }

  private boolean gitClone() {

    String cloneFrom = repoDef.getUri();
    URI cloneTo = URIBuilder.create().setScheme("file")
        .setPath(repoDef.getLocalClone(), repoDef.getName()).build();

    logger.info("Cloning " + cloneFrom + " into " + cloneTo);

    try {

      CloneCommand clone = Git.cloneRepository().setURI(cloneFrom).setDirectory(new File(cloneTo));

      if (StringUtils.hasText(repoDef.getBranchName())) {
        clone = clone.setBranch(repoDef.getBranchName());
      }

      setupCredentials(repoDef.toURIish().getScheme(), clone);

      git = clone.call();

    } catch (JGitInternalException e) {

      if (e.getMessage().toLowerCase().contains("already exists")) {
        logger.warn("Clone " + cloneTo + " already exists. Refreshing.");
        return gitPull();
      }

    } catch (InvalidRemoteException e2) {
      throw new IllegalArgumentException(e2);
    } catch (GitAPIException e3) {
      throw new InitializationException(e3.getMessage());


    }
    return true;

  }

  private URI toURI() {

    URIBuilder builder = URIBuilder.create()
        .setPath(repoDef.getLocalClone(), repoDef.getName(), repoDef.getRootDir()).setScheme("file")
        .setFileNameIfMissing(repoDef.getFileName());
    return builder.build();

  }

  private boolean gitPull() {

    Boolean result = Boolean.FALSE;

    if (git == null) {
      final URI cloneTo = URIBuilder.create().setScheme("file")
          .setPath(repoDef.getLocalClone(), repoDef.getName()).build();

      try {
        git = Git.open(new File(cloneTo));

        if (StringUtils.hasText(repoDef.getBranchName()))
          git.checkout().setName(repoDef.getBranchName()).call();
        
      } catch (Exception e) {
        logger.error(e.getMessage());
      }
    }


    MergeResult rr = null;
    try {

      PullCommand pc = git.pull();

      setupCredentials(repoDef.toURIish().getScheme(), pc);

      PullResult pullRes = pc.call();
      rr = pullRes.getMergeResult();

    } catch (Exception e) {
      logger.error(e.getMessage());
    }

    if (rr.getMergeStatus().equals(MergeResult.MergeStatus.MERGED)
        || rr.getMergeStatus().equals(MergeResult.MergeStatus.FAST_FORWARD)
        || rr.getMergeStatus().equals(MergeResult.MergeStatus.ALREADY_UP_TO_DATE)) {
      result = Boolean.TRUE;
      logger.debug("Pull of " + repoDef.getUri() + " completed");
    }

    return result;
  }

  @Override
  public void init() {
    gitClone();

    if (repoDef.getRefresh() > 0) {
      logger.info("Setting timed pulling at " + repoDef.getRefresh() + " seconds");
      timer.scheduleAtFixedRate(pullTask, repoDef.getRefresh() * 1000, repoDef.getRefresh() * 1000);
    }
  }

  @Override
  public URI prototypeURI(String path) {
    return builder.build(path);
  }

  private void setupCredentials(String scheme, TransportCommand<? extends GitCommand, ?> command) {

    if (scheme == null
        || (!scheme.toLowerCase().startsWith("http") && !scheme.toLowerCase().startsWith("git:"))) {

      SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
        @Override
        protected void configure(Host host, Session session) {
          session.setConfig("StrictHostKeyChecking", "no");
        }

        @Override
        protected JSch createDefaultJSch(FS fs) throws JSchException {
          JSch defaultJSch = super.createDefaultJSch(fs);
          defaultJSch.addIdentity(repoDef.getUsername(), repoDef.getPassword());
          return defaultJSch;
        }
      };

      command.setTransportConfigCallback(new TransportConfigCallback() {
        @Override
        public void configure(Transport transport) {

          SshTransport sshTransport = (SshTransport) transport;
          sshTransport.setSshSessionFactory(sshSessionFactory);
        }
      });

    } else {

      if (repoDef.getAuthMethod() != null) {
        command.setCredentialsProvider(
            new UsernamePasswordCredentialsProvider(creds.getUsername(), creds.getPassword()));
      }
    }
  }

  @Override
  public Optional<? extends PropertyPacket> stream(String path) {

    Optional<StreamPacket> packet = streamFile(path);

    try {

      if (packet.isPresent()) {
        packet.get().putAll(
            ProcessorSelector.process(packet.get().getUri().toString(), packet.get().bytes()));
      }

    } catch (IOException io) {

      logger.error(io.getMessage());
      // Nothing, simply file not there
    }

    return packet;

  }

  @Override
  public Optional<StreamPacket> streamFile(final String path) {

    StreamPacket packet = null;

    final URI uri = prototypeURI(path);

    long start = System.currentTimeMillis();

    if (repoDef.getRefresh() < 1) {
      if (!gitPull()) {
        logger.error("Pull operation failed. Possibly reading stale values");
      }
    }

    logger.debug("Requesting git path: " + uri.toString());

    try (InputStream is = new FileInputStream(new File(uri))) {

      if (is != null) {
        packet = new StreamPacket(uri, is);
      }

    } catch (IOException io) {

      logger.debug(io.getMessage());
      // Nothing, simply file not there
    }

    logger.trace("Git connector took: " + (System.currentTimeMillis() - start) + "ms to fetch "
        + path.toString());

    return Optional.ofNullable(packet);
  }

}
