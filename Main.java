package gitlet;

import java.io.File;
import java.io.IOException;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Emma Kang
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */

    static final File CWD = new File(".");
    /**
     * Gitlet directory.
     */
    static final File GITLET_FOLDER = new File(".gitlet");
    /**
     * staging area directory.
     */
    static final File STAGING_FOLDER = Utils.join(GITLET_FOLDER, "staging");

    /**
     * folder with master commits.
     */
    static final File COMMITS_FOLDER = Utils.join(GITLET_FOLDER, "commits");
    /**
     * blobs.
     */
    static final File BLOBS_FOLDER = Utils.join(GITLET_FOLDER, "blobs");
    /**
     * branches.
     */
    static final File BRANCHES = Utils.join(GITLET_FOLDER, "branches");
    /**
     * branches.
     */
    static final File REMOVE_FOLDER = Utils.join(GITLET_FOLDER, "remove");

    /**
     * repo object.
     */
    private static Repo repobject;

    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        repobject = new Repo();
        if (args[0].equals("init")) {
            repobject.init();
            return;
        }
        if (!GITLET_FOLDER.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        } else if (args[0].equals("add")) {
            repobject.add(args[1]);
            return;
        } else if (args[0].equals("commit")) {
            if (args.length == 1) {
                System.out.println("Please enter a commit message.");
                return;
            }
            repobject.commit(args[1]);
            return;
        } else if (args[0].equals("checkout")) {
            repobject.checkout(args);
            return;
        } else if (args[0].equals("log")) {
            repobject.log();
            return;
        } else if (args[0].equals("rm")) {
            repobject.rm(args[1]);
            return;
        } else if (args[0].equals("global-log")) {
            repobject.globallog();
            return;
        } else if (args[0].equals("find")) {
            repobject.find(args[1]);
            return;
        } else if (args[0].equals("status")) {
            repobject.status();
            return;
        } else if (args[0].equals("branch")) {
            repobject.branch(args[1]);
            return;
        } else if (args[0].equals("rm-branch")) {
            repobject.rmbranch(args[1]);
            return;
        } else if (args[0].equals("reset")) {
            repobject.reset(args[1]);
            return;
        } else if (args[0].equals("merge")) {
            repobject.merge(args[1]);
            return;
        } else if (args[0].equals("diff")) {
            repobject.diff(args);
            return;
        }

        System.out.println("No command with that name exists.");
    }

    public static void setupPersistence() {

        GITLET_FOLDER.mkdir();
        STAGING_FOLDER.mkdir();
        COMMITS_FOLDER.mkdir();
        BRANCHES.mkdir();
        BLOBS_FOLDER.mkdir();
        REMOVE_FOLDER.mkdir();

    }

}
