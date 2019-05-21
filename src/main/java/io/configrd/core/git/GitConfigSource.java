package io.configrd.core.git;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
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
import io.configrd.core.source.DefaultConfigSource;
import io.configrd.core.source.FileConfigSource;
import io.configrd.core.source.FileStreamSource;
import io.configrd.core.source.PropertyPacket;
import io.configrd.core.source.StreamPacket;
import io.configrd.core.source.StreamSource;
import io.configrd.core.source.WritableConfigSource;
import io.configrd.core.util.StringUtils;
import io.configrd.core.util.URIBuilder;

public class GitConfigSource extends DefaultConfigSource<GitStreamSource>
    implements FileConfigSource, WritableConfigSource, Closeable {

  private final static Logger logger = LoggerFactory.getLogger(GitConfigSource.class);

  private static final Timer timer = new Timer(true);

  public final GitCredentials creds;

  private final TimerTask pullTask = new TimerTask() {

    @Override
    public void run() {
      gitPull();
    }
  };

  private Git git;

  public void init() {
    gitClone();

    if (getStreamSource().getSourceConfig().getRefresh() > 0) {
      logger.info("Setting timed pulling at " + getStreamSource().getSourceConfig().getRefresh()
          + " seconds");
      timer.scheduleAtFixedRate(pullTask, getStreamSource().getSourceConfig().getRefresh() * 1000,
          getStreamSource().getSourceConfig().getRefresh() * 1000);
    }
  }

  private void setupCredentials(String scheme, TransportCommand<? extends GitCommand, ?> command) {

    if (getStreamSource().getSourceConfig().getAuthMethod() != null
        && (scheme == null || (!scheme.toLowerCase().startsWith("http")
            && !scheme.toLowerCase().startsWith("git:")))) {

      SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
        @Override
        protected void configure(Host host, Session session) {
          session.setConfig("StrictHostKeyChecking", "no");
        }

        @Override
        protected JSch createDefaultJSch(FS fs) throws JSchException {
          JSch defaultJSch = super.createDefaultJSch(fs);
          defaultJSch.addIdentity(getStreamSource().getSourceConfig().getUsername(),
              getStreamSource().getSourceConfig().getPassword());
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

      if (getStreamSource().getSourceConfig().getAuthMethod() != null) {
        command.setCredentialsProvider(
            new UsernamePasswordCredentialsProvider(creds.getUsername(), creds.getPassword()));
      }
    }
  }

  @Override
  public void close() throws IOException {
    if (git != null)
      git.close();

    pullTask.cancel();
  }

  private boolean gitPush(RevCommit commit) {

    boolean success = true;

    RefSpec spec = null;
    try {

      spec = new RefSpec(commit.name() + ":" + git.getRepository().getFullBranch());

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

    // Goal is to overwrite remote with local state, it's a PUT
    PushCommand push = git.push().setForce(true).setRefSpecs(spec);

    logger.debug("Pushing " + spec);

    setupCredentials(getStreamSource().getSourceConfig().toURIish().getScheme(), push);

    try {

      Iterable<PushResult> result = push.call();

      Set<String> messages = new HashSet<>();

      for (PushResult r : result) {

        success = success && r.getRemoteUpdates().stream()
            .allMatch(s -> (RemoteRefUpdate.Status.OK.equals(s.getStatus())
                || RemoteRefUpdate.Status.UP_TO_DATE.equals(s.getStatus())));

        if (!success) {
          messages.addAll(r.getRemoteUpdates().stream()
              .filter(u -> !StringUtils.hasText(u.getMessage())).map(u -> {
                return u.getStatus() + " - " + u.getMessage();
              }).collect(Collectors.toSet()));
        }
      }

      if (!messages.isEmpty()) {
        logger.error("Push of ref spec " + spec + " failed.");
        for (String s : messages) {
          logger.error(s);
        }
      } else {
        success = true;
        logger.info("Push " + spec + " succeeded.");
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return success;
  }

  private boolean gitPull() {

    Boolean result = Boolean.FALSE;

    gitConnect();

    MergeResult rr = null;
    try {

      PullCommand pc = git.pull().setStrategy(MergeStrategy.RECURSIVE)
          .setFastForward(MergeCommand.FastForwardMode.NO_FF);

      setupCredentials(getStreamSource().getSourceConfig().toURIish().getScheme(), pc);

      PullResult pullRes = pc.call();
      rr = pullRes.getMergeResult();

      if (rr != null && rr.getMergeStatus().isSuccessful()) {
        result = Boolean.TRUE;
        logger.debug(rr.toString());
      } else if (rr != null) {
        logger.error("Merge on pull failed with " + rr.toString());
      }


    } catch (Exception e) {
      logger.error(e.getMessage());

      if (e.getMessage().toLowerCase().contains(("ref may not exist"))) {
        result = true;
      }
    }

    return result;
  }

  private boolean gitClone() {

    String cloneFrom = getStreamSource().getSourceConfig().getUri();

    URI cloneTo = getStreamSource().toClone();

    logger.info("Cloning " + cloneFrom + " into " + cloneTo);

    try {

      CloneCommand clone = Git.cloneRepository().setURI(cloneFrom).setDirectory(new File(cloneTo));

      if (StringUtils.hasText(getStreamSource().getSourceConfig().getBranchName())) {
        clone = clone.setBranch(getStreamSource().getSourceConfig().getBranchName());
      }

      setupCredentials(getStreamSource().getSourceConfig().toURIish().getScheme(), clone);

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

  private void gitConnect() {

    if (git == null) {
      final URI cloneTo = URIBuilder.create().setScheme("file")
          .setPath(getStreamSource().getSourceConfig().getLocalClone(),
              getStreamSource().getSourceConfig().getName())
          .build();

      try {
        git = Git.open(new File(cloneTo));

        if (StringUtils.hasText(getStreamSource().getSourceConfig().getBranchName()))
          git.checkout().setName(getStreamSource().getSourceConfig().getBranchName()).call();

      } catch (Exception e) {
        logger.error(e.getMessage());
      }
    }

  }

  public GitConfigSource(GitStreamSource source, Map<String, Object> values, GitCredentials creds) {
    super(source, values);
    this.creds = creds;
  }

  @Override
  public Map<String, Object> getRaw(String path) {

    Optional<? extends PropertyPacket> stream = streamSource.stream(path);

    if (!stream.isPresent())
      return new HashMap<>();

    return stream.get();
  }

  @Override
  public Map<String, Object> get(String path, Set<String> names) {

    if (getStreamSource().getSourceConfig().getRefresh() < 1) {
      if (!gitPull()) {
        logger.error("Pull operation failed. Possibly reading stale values");
      }
    }

    return super.get(path, names);
  }

  @Override
  public boolean isCompatible(StreamSource source) {
    return (source instanceof GitStreamSource);
  }

  @Override
  public Optional<StreamPacket> getFile(String path) {
    return ((FileStreamSource) streamSource).streamFile(path);
  }

  private RevCommit gitCommit(final String message) {

    RevCommit revCommit = null;
    try {

      CommitCommand commit = git.commit().setMessage(message);
      revCommit = commit.call();

      logger.info("Committed " + revCommit.getName() + ", " + revCommit.getFullMessage());

    } catch (Exception e) {
      logger.error(e.getMessage());
      throw new RuntimeException(e);
    }

    return revCommit;
  }

  private DirCache gitAdd(PropertyPacket packet) {

    // run the add
    URI fileName = getStreamSource().toRelative(packet);

    try {
      DirCache cache = git.add().addFilepattern(fileName.toString()).call();

      for (int i = 0; i < cache.getEntryCount(); i++) {
        logger.debug("Added " + cache.getEntry(i));
      }

      return cache;

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public boolean put(String path, Map<String, Object> props) {

    PropertyPacket packet = null;
    if (!(props instanceof PropertyPacket)) {

      packet = new PropertyPacket(URI.create(path));
      packet.putAll(props);

    } else {

      packet = (PropertyPacket) props;

    }

    gitConnect();

    synchronized (git) {

      if (!packet.isEmpty() && gitPull()) {

        boolean success = getStreamSource().put(path, packet);

        DirCache c = gitAdd(packet);
        RevCommit commit = gitCommit("Updated " + packet.size() + " entries.");
        return gitPush(commit);

      } else {
        logger.warn("Packet to write to " + packet.getUri() + " is empty. Nothing changed.");
      }
    }

    return false;
  }

  @Override
  public boolean patch(String path, String etag, Map<String, Object> props) {
    // TODO Auto-generated method stub
    return false;
  }

}
