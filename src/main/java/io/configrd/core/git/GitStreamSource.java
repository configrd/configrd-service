package io.configrd.core.git;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
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
import org.eclipse.jgit.lib.SubmoduleConfig.FetchRecurseSubmodulesMode;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jsoniter.output.JsonStream;
import io.configrd.core.exception.InitializationException;
import io.configrd.core.processor.JsonProcessor;
import io.configrd.core.processor.ProcessorSelector;
import io.configrd.core.processor.PropertiesProcessor;
import io.configrd.core.processor.YamlProcessor;
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

  public boolean put(String path, PropertyPacket packet) {

    gitConnect();

    synchronized (git) {
      if (!packet.isEmpty() && gitPull()) {
        final String fileName = write(packet);
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

  private boolean gitPush(RevCommit commit) {

    boolean success = true;

    RefSpec spec = null;
    try {

      spec = new RefSpec(commit.name() + ":" + git.getRepository().getFullBranch());

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

    //Goal is to overwrite remote with local state, it's a PUT
    PushCommand push = git.push().setForce(true).setRefSpecs(spec);

    logger.debug("Pushing " + spec);

    setupCredentials(repoDef.toURIish().getScheme(), push);

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

  private void gitConnect() {

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

  }

  private String write(PropertyPacket packet) {

    final URI uri = toURI(packet);

    String content = null;

    try {
      if (PropertiesProcessor.isPropertiesFile(uri.toString())) {
        content = PropertiesProcessor.toText(packet);
      } else if (YamlProcessor.isYamlFile(uri.toString())) {
        content = new YAMLMapper().writeValueAsString(packet);
      } else if (JsonProcessor.isJsonFile(uri.toString())) {
        content = JsonStream.serialize(packet);
      }
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }

    File myFile = new File(uri);

    try {
      FileUtils.forceMkdirParent(myFile);
    } catch (Exception e) {
      logger.error("Unable to create directories for " + uri.toString()
          + ". Are write permissions enabled?");
      throw new RuntimeException(e);
    }

    try {

      FileUtils.writeStringToFile(myFile, content, "UTF-8", false);
      logger.debug("Wrote " + myFile.getAbsolutePath());

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return myFile.getAbsolutePath();
  }

  private DirCache gitAdd(PropertyPacket packet) {

    // run the add

    URI fileName = toRelative(packet);

    try {
      logger.debug("Adding " + fileName);
      DirCache cache = git.add().addFilepattern(".").call();

      for (int i = 0; i < cache.getEntryCount(); i++) {
        logger.debug("Added " + cache.getEntry(i));
      }

      return cache;

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
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

  private URI toURI(PropertyPacket packet) {
    URIBuilder builder = URIBuilder.create()
        .setPath(git.getRepository().getDirectory().getParent(), repoDef.getRootDir(),
            packet.getUri().toString())
        .setScheme("file").setFileNameIfMissing(repoDef.getFileName());

    final URI file = builder.build();
    return file;
  }

  private URI toRelative(PropertyPacket packet) {

    URI relative = URIBuilder.create().setPath(git.getRepository().getDirectory().getParent())
        .setScheme("file").build().relativize(toURI(packet));

    return relative;
  }

  private boolean gitPull() {

    Boolean result = Boolean.FALSE;

    gitConnect();

    MergeResult rr = null;
    try {

      PullCommand pc = git.pull().setStrategy(MergeStrategy.RECURSIVE)
          .setFastForward(MergeCommand.FastForwardMode.NO_FF);

      setupCredentials(repoDef.toURIish().getScheme(), pc);

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

    if (repoDef.getAuthMethod() != null
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
