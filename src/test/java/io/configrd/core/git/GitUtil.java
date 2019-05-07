package io.configrd.core.git;

import java.io.File;
import java.net.URI;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;

public class GitUtil {

  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(GitUtil.class);

  public static Git initBare() throws Exception {

    TemporaryFolder folder = new TemporaryFolder();
    folder.create();
    return Git.init().setBare(true).setDirectory(folder.getRoot()).call();

  }

  public static boolean pull(Git git) throws Exception {

    PullResult result = git.pull().setStrategy(MergeStrategy.RECURSIVE)
        .setFastForward(MergeCommand.FastForwardMode.NO_FF).call();

    if (result != null && result.isSuccessful()) {
      logger.debug("Pull result " + result.getMergeResult().toString());
      return true;
    }else {
      logger.error("Pull failed.");
      return false;
    }
  }

  public static File modifyFile(Git git, File file) throws Exception {

    pull(git);

    URI uri = URI.create(git.getRepository().getDirectory().getParent())
        .relativize(URI.create(file.getAbsolutePath()));

    FileUtils.writeStringToFile(file, loremIpsum, "UTF-8", false);
    DirCache cache = git.add().addFilepattern(".").call();

    for (int i = 0; i < cache.getEntryCount(); i++) {
      logger.debug("Added " + cache.getEntry(i));
    }

    RevCommit commit = git.commit().setMessage("Modifying file " + uri).call();

    Iterable<PushResult> results = git.push().setForce(true).call();

    for (PushResult r : results) {
      
      logger.debug(r.getMessages());

      if (r.getRemoteUpdates().stream()
          .allMatch(s -> (RemoteRefUpdate.Status.OK.equals(s.getStatus())
              || RemoteRefUpdate.Status.UP_TO_DATE.equals(s.getStatus())))) {

        logger.info("Push succeeded.");

      }
    }

    return file;

  }

  public static File addRandomFile(Git git) throws Exception {

    git.pull().call();
    File temp = File.createTempFile(UUID.randomUUID().toString(), "txt",
        git.getRepository().getDirectory().getParentFile());
    FileUtils.writeStringToFile(temp, loremIpsum, "UTF-8");
    git.add().setUpdate(true).addFilepattern(".").call();
    Iterable<PushResult> results = git.push().call();

    for (PushResult r : results) {
      logger.info(r.getMessages());
    }

    return temp;

  }

  public static Git clone(File uri) throws Exception {

    TemporaryFolder folder = new TemporaryFolder();
    folder.create();

    Git git =
        Git.cloneRepository().setURI(uri.getAbsolutePath()).setDirectory(folder.getRoot()).call();

    return git;

  }

  public void fill(Git git) throws Exception {

    FileUtils.copyDirectory(FileUtils.toFile(getClass().getResource("/")),
        git.getRepository().getDirectory());

    git.add().setUpdate(true).addFilepattern(".").call();
    git.commit().setMessage("test code commit").call();

    Iterable<PushResult> results = git.push().call();

    for (PushResult r : results) {
      logger.info(r.getMessages());
    }
  }

  public static void cleanup(Git git) {
    if (git != null) {
      FileUtils.deleteQuietly(git.getRepository().getDirectory().getParentFile());
    }
  }

  private static final String loremIpsum =
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Id volutpat lacus laoreet non. Neque ornare aenean euismod elementum. Consectetur adipiscing elit pellentesque habitant morbi tristique. Penatibus et magnis dis parturient montes nascetur ridiculus mus. Gravida rutrum quisque non tellus orci ac auctor augue. Augue mauris augue neque gravida. Vel eros donec ac odio tempor orci dapibus. Feugiat vivamus at augue eget arcu dictum varius duis at. Leo urna molestie at elementum eu facilisis sed odio. In eu mi bibendum neque egestas congue quisque egestas diam. Pretium viverra suspendisse potenti nullam. Cras sed felis eget velit aliquet sagittis id consectetur. Euismod in pellentesque massa placerat duis. Ornare lectus sit amet est placerat in. Feugiat sed lectus vestibulum mattis ullamcorper velit.\n"
          + "\n"
          + "Mi tempus imperdiet nulla malesuada pellentesque elit eget gravida cum. Elementum integer enim neque volutpat. Semper viverra nam libero justo laoreet sit. Sed libero enim sed faucibus turpis in eu mi bibendum. Commodo quis imperdiet massa tincidunt. Convallis posuere morbi leo urna. Morbi tristique senectus et netus et malesuada fames ac. Nibh nisl condimentum id venenatis a condimentum vitae sapien pellentesque. Eget gravida cum sociis natoque penatibus et. Aliquam purus sit amet luctus venenatis. Quam viverra orci sagittis eu volutpat odio facilisis mauris. Aliquam sem fringilla ut morbi tincidunt. Enim neque volutpat ac tincidunt vitae semper. Mauris vitae ultricies leo integer malesuada nunc.\n"
          + "\n"
          + "In iaculis nunc sed augue lacus. Ut pharetra sit amet aliquam id. Morbi blandit cursus risus at ultrices. Nulla pellentesque dignissim enim sit amet venenatis urna cursus eget. Cursus in hac habitasse platea dictumst quisque sagittis purus. Adipiscing tristique risus nec feugiat. Vivamus at augue eget arcu dictum varius duis. Dignissim suspendisse in est ante. Sagittis id consectetur purus ut faucibus pulvinar. Facilisis magna etiam tempor orci eu lobortis elementum nibh tellus. Cum sociis natoque penatibus et magnis dis parturient montes nascetur. Interdum consectetur libero id faucibus nisl tincidunt. Et egestas quis ipsum suspendisse ultrices.\n"
          + "\n"
          + "Ipsum consequat nisl vel pretium lectus quam id. Odio aenean sed adipiscing diam donec adipiscing tristique. Sed ullamcorper morbi tincidunt ornare massa eget. Nisl nunc mi ipsum faucibus vitae aliquet nec ullamcorper sit. Cras sed felis eget velit. Lobortis mattis aliquam faucibus purus in massa. Diam maecenas sed enim ut. Mattis aliquam faucibus purus in. In hac habitasse platea dictumst vestibulum rhoncus est. Dui sapien eget mi proin sed libero enim sed. Accumsan in nisl nisi scelerisque. Arcu vitae elementum curabitur vitae nunc sed velit. Velit scelerisque in dictum non consectetur a erat nam at. Mauris ultrices eros in cursus turpis massa tincidunt dui ut. Fusce id velit ut tortor pretium viverra suspendisse potenti. Ornare arcu dui vivamus arcu felis bibendum. Suscipit tellus mauris a diam maecenas.\n"
          + "\n"
          + "Ac auctor augue mauris augue neque gravida. Massa eget egestas purus viverra. Et ligula ullamcorper malesuada proin libero. Pellentesque diam volutpat commodo sed egestas egestas. Iaculis eu non diam phasellus vestibulum lorem. Elementum eu facilisis sed odio. Vestibulum rhoncus est pellentesque elit. Sollicitudin aliquam ultrices sagittis orci a. Id consectetur purus ut faucibus pulvinar. Magna eget est lorem ipsum dolor sit amet consectetur. Sagittis aliquam malesuada bibendum arcu vitae elementum curabitur. Diam quis enim lobortis scelerisque fermentum dui. Ut placerat orci nulla pellentesque dignissim. Lobortis elementum nibh tellus molestie nunc.";
}
