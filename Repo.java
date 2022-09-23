package gitlet;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Arrays;

import static gitlet.Main.*;

public class Repo implements Serializable {

    /**
     * Gitlet directory.
     */
    private File hEADfile = Utils.join(CWD, ".gitlet/head");
    /**
     * Gitlet directory.
     */
    private File currentbranch = Utils.join(CWD, ".gitlet/currentbranch");


    public Repo() {

    }

    public void init() throws IOException {


        if (GITLET_FOLDER.exists()) {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
        } else {
            setupPersistence();

            Commit initCommit = new Commit();
            String commmitID = Utils.sha1(Utils.serialize(initCommit));


            hEADfile = new File(".gitlet/head");
            hEADfile.createNewFile();
            Utils.writeContents(hEADfile, commmitID);

            File initialcommitfile = Utils.join(COMMITS_FOLDER, commmitID);
            Utils.writeObject(initialcommitfile, initCommit);
            File cBranch = Utils.join(CWD, ".gitlet/currentbranch");
            Utils.writeContents(cBranch, "master");

            File newbranches = Utils.join(BRANCHES, "master");
            Utils.writeContents(newbranches, commmitID);

        }

    }

    public void add(String file) throws IOException {

        File inputfile = new File(file);
        if (!inputfile.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        File removedirectory = new File(".gitlet/remove");
        String[] rdirectory = removedirectory.list();
        if (rdirectory != null) {
            for (String removefiles : rdirectory) {
                if (removefiles.equals(file)) {
                    File deletef = new File(".gitlet/remove/" + file);
                    deletef.delete();
                    return;
                }
            }
        }

        String headcommit = Utils.readContentsAsString(hEADfile);
        TreeMap<String, String> copymap = Utils.readObject(new File(
                ".gitlet/commits/" + headcommit), Commit.class).getcommitmap();

        if (copymap.get(file) != null) {
            String checkoutSHA = copymap.get(file);
            File checkoutfile = Utils.join(BLOBS_FOLDER, checkoutSHA);
            Blob checkoutblob = Utils.readObject(checkoutfile, Blob.class);
            if ((checkoutblob.getblobContents()).equals(
                    Utils.readContentsAsString(inputfile))) {
                return;
            }
        }

        String content = Utils.readContentsAsString(inputfile);
        Blob addedBlob = new Blob(file, content);
        if (inputfile.exists()) {
            File addedfile = Utils.join(
                    STAGING_FOLDER, addedBlob.getblobName());
            Utils.writeObject(addedfile, addedBlob);
        }
    }

    public void commit(String message) {

        if (message.length() == 0) {
            System.out.println("Please enter a commit message.");
            return;
        }
        String parentcommit = Utils.readContentsAsString(Utils.join(BRANCHES,
                Utils.readContentsAsString(currentbranch)));
        Commit currentcommit = new Commit(message, parentcommit,
                Utils.readContentsAsString(currentbranch));

        File stagingdirectory = new File(".gitlet/staging");
        File[] directory = stagingdirectory.listFiles();
        File removedirectory = new File(".gitlet/remove");
        File[] rdirectory = removedirectory.listFiles();

        if ((directory.length == 0) && (rdirectory.length == 0)) {
            System.out.println("No changes added to the commit.");
            return;
        }
        if (directory != null) {
            for (File stagingfiles : directory) {
                Blob stagedfile = Utils.readObject(stagingfiles, Blob.class);
                currentcommit.getcommitmap().put(
                        stagedfile.getblobName(), stagedfile.getblobSHA());
                File addedfile = Utils.join(
                        BLOBS_FOLDER, stagedfile.getblobSHA());
                Utils.writeObject(addedfile, stagedfile);
            }
            for (File staging : directory) {
                staging.delete();
            }
        }
        String[] removes = REMOVE_FOLDER.list();
        for (String filen : removes) {
            currentcommit.getcommitmap().put(filen, null);
        }
        if (rdirectory != null) {
            for (File stagingfiles : rdirectory) {
                stagingfiles.delete();
            }
        }

        File addedcommitfile = Utils.join(
                COMMITS_FOLDER, currentcommit.getSHA());
        Utils.writeObject(addedcommitfile, currentcommit);

        Utils.writeContents(hEADfile, currentcommit.getSHA());
        File branchesdirectory = new File(".gitlet/branches");
        String[] bdirectory = branchesdirectory.list();
        if (bdirectory != null) {
            for (String branch : bdirectory) {
                if (branch.equals(Utils.readContentsAsString(currentbranch))) {
                    Utils.writeContents(new File(".gitlet/branches/"
                            + branch), currentcommit.getSHA());
                }
            }
        }
    }

    public void checkout(String... args) throws IOException {
        if (args[1].equals("--")) {
            String headcommit = Utils.readContentsAsString(hEADfile);
            TreeMap<String, String> copymap = Utils.readObject(new File(
                    ".gitlet/commits/"
                            + headcommit), Commit.class).getcommitmap();
            if (copymap.containsKey(args[2])) {
                String checkoutSHA = copymap.get(args[2]);
                File checkoutfile = Utils.join(BLOBS_FOLDER, checkoutSHA);
                Blob checkoutblob = Utils.readObject(checkoutfile, Blob.class);
                File overwrittenfile = new File(CWD,
                        checkoutblob.getblobName());
                overwrittenfile.createNewFile();
                Utils.writeContents(overwrittenfile,
                        checkoutblob.getblobContents());
            } else {
                System.out.println("File does not exist in that commit.");
                return;
            }
        } else if (args.length == 2) {
            checkout2(args[1]);
        } else if (args[2].equals("--")) {
            String commitID = args[1];
            File commitdirectory = new File(".gitlet/commits");
            String[] cdirectory = commitdirectory.list();
            if (cdirectory != null) {
                for (String idcommit : cdirectory) {
                    if (idcommit.startsWith(commitID)) {
                        File commitfile = Utils.join(COMMITS_FOLDER, idcommit);
                        Commit checkoutcommit
                                = Utils.readObject(commitfile, Commit.class);
                        TreeMap<String, String> copymap
                                = checkoutcommit.getcommitmap();
                        String filename = args[3];
                        if (copymap.containsKey(filename)) {
                            String checkoutSHA = copymap.get(filename);
                            File checkoutfile = Utils.join(
                                    BLOBS_FOLDER, checkoutSHA);
                            Blob checkoutblob = Utils.readObject(
                                    checkoutfile, Blob.class);
                            File overwrittenfile = new File(CWD,
                                    checkoutblob.getblobName());
                            overwrittenfile.createNewFile();
                            Utils.writeContents(overwrittenfile,
                                    checkoutblob.getblobContents());
                            return;
                        } else {
                            System.out.println("File does "
                                    + "not exist in that commit.");
                            return;
                        }
                    }
                }
                System.out.println("No commit with that id exists.");
                return;
            }
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    public void checkout2(String branchname) throws IOException {
        if (Utils.readContentsAsString(currentbranch).equals(branchname)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        if (!(new File(".gitlet/branches/" + branchname).exists())) {
            System.out.println("No such branch exists.");
            return;
        }
        Utils.writeContents(currentbranch, branchname);
        String shacommit = Utils.readContentsAsString(
                Utils.join(BRANCHES, branchname));
        Commit comit = Utils.readObject(new File(".gitlet/commits/"
                + shacommit), Commit.class);
        TreeMap<String, String> commitsmap = comit.getcommitmap();
        String headcommit = Utils.readContentsAsString(hEADfile);
        TreeMap<String, String> copymap = Utils.readObject(new File(
                ".gitlet/commits/" + headcommit), Commit.class).getcommitmap();
        Set<String> set1 = commitsmap.keySet();
        for (String key : set1) {
            File orig = Utils.join(CWD, key);
            if (orig.exists()) {
                if (copymap.get(key) == null) {
                    String contsa = Utils.readContentsAsString(orig);
                    File checkoutf = Utils.join(BLOBS_FOLDER,
                            commitsmap.get(key));
                    Blob checkoutblob = Utils.readObject(checkoutf, Blob.class);
                    if (!contsa.equals(checkoutblob.getblobContents())) {
                        System.out.print("There is an untracked file"
                                + " in the way; delete "
                                + "it, or add and commit it first.");
                        return;
                    }
                }
            }
        }
        Set<String> set = commitsmap.keySet();
        for (String key : set) {
            if (commitsmap.get(key) != null) {
                String checkoutSHA = commitsmap.get(key);
                File checkoutfile = Utils.join(BLOBS_FOLDER, checkoutSHA);
                Blob checkoutblob = Utils.readObject(checkoutfile, Blob.class);
                File overwrittenfile = new File(CWD,
                        checkoutblob.getblobName());
                overwrittenfile.createNewFile();
                Utils.writeContents(overwrittenfile,
                        checkoutblob.getblobContents());
            }
        }
        for (String a : Utils.plainFilenamesIn(CWD)) {
            if (copymap.get(a) != null) {
                if (commitsmap.get(a) == null) {
                    File f = Utils.join(CWD, a);
                    f.delete();
                }
            }
        }
        Utils.writeContents(hEADfile, Utils.readContents(Utils.join(
                BRANCHES, (Utils.readContentsAsString(currentbranch)))));
    }

    public void log() {

        String headcommit = Utils.readContentsAsString(hEADfile);

        Commit headcopy = Utils.readObject(new File(
                ".gitlet/commits/" + headcommit), Commit.class);

        while (!headcopy.equals(null)) {
            System.out.println("===");
            System.out.println("commit " + headcopy.getSHA());
            System.out.println("Date: " + headcopy.getTimestamp());
            System.out.println(headcopy.getMessage());
            System.out.println(" ");

            if (headcopy.getParent().equals("")) {
                break;
            }
            Commit parent = Utils.readObject(new File(
                    ".gitlet/commits/" + headcopy.getParent()), Commit.class);
            headcopy = parent;
        }
    }

    public void rm(String filename) {

        String headcommit = Utils.readContentsAsString(hEADfile);
        TreeMap<String, String> copymap = Utils.readObject(new File(
                ".gitlet/commits/" + headcommit), Commit.class).getcommitmap();

        if (!(Utils.join(STAGING_FOLDER, filename).exists())
                && !(copymap.containsKey(filename))) {
            System.out.println("No reason to remove the file.");
            return;
        }

        if (Utils.join(STAGING_FOLDER, filename).exists()) {
            File inputfile = Utils.join(STAGING_FOLDER, filename);
            inputfile.delete();
        }


        if (copymap.containsKey(filename)) {
            String removeSHA = copymap.get(filename);
            File removefile = Utils.join(BLOBS_FOLDER, removeSHA);
            Blob checkoutblob = Utils.readObject(removefile, Blob.class);

            File addedfile = Utils.join(
                    REMOVE_FOLDER, filename);
            Utils.writeObject(addedfile, checkoutblob.getblobContents());

            File overwrittenfile = new File(CWD, filename);
            if (overwrittenfile.exists()) {
                overwrittenfile.delete();
            }
        }
        String sha = Utils.readContentsAsString(Utils.join(
                BRANCHES, Utils.readContentsAsString(currentbranch)));
        Commit currcomm = Utils.readObject(Utils.join(
                COMMITS_FOLDER, sha), Commit.class);
        currcomm.getcommitmap().put(filename, null);
        copymap.put(filename, null);

    }

    public void globallog() {
        File commitdirectory = new File(".gitlet/commits");
        File[] cdirectory = commitdirectory.listFiles();
        if (cdirectory != null) {
            for (File stagingfiles : cdirectory) {
                Commit headcopy = Utils.readObject(stagingfiles, Commit.class);

                System.out.println("===");
                System.out.println("commit " + headcopy.getSHA());
                System.out.println("Date: " + headcopy.getTimestamp());
                System.out.println(headcopy.getMessage());
                System.out.println(" ");

            }

        }
    }


    public void find(String message) {
        File commitdirectory = new File(".gitlet/commits");
        File[] cdirectory = commitdirectory.listFiles();
        int messagecounter = 0;
        if (cdirectory != null) {
            for (File stagingfiles : cdirectory) {
                Commit headcopy = Utils.readObject(stagingfiles, Commit.class);
                if (headcopy.getMessage().equals(message)) {
                    messagecounter += 1;
                    System.out.println(headcopy.getSHA());
                }
            }
        }

        if (messagecounter == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void status() {

        String curbranch = Utils.readContentsAsString(currentbranch);

        File branchesdirectory = new File(".gitlet/branches");
        String[] bdirectory = branchesdirectory.list();
        System.out.println("=== Branches ===");
        TreeMap<String, String> branchesmap = new TreeMap<>();

        if (bdirectory != null) {
            for (String branches : bdirectory) {
                branchesmap.put(branches, branches);
            }
        }
        Set<String> set1 = branchesmap.keySet();
        for (String key : set1) {
            if (key.equals(curbranch)) {
                System.out.println("*" + key);
            } else {
                System.out.println(key);
            }
        }
        System.out.println(" ");

        File stagingdirectory = new File(".gitlet/staging");
        String[] sdirectory = stagingdirectory.list();
        System.out.println("=== Staged Files ===");
        if (sdirectory != null) {
            for (String stagedfiles : sdirectory) {
                System.out.println(stagedfiles);
            }
        }
        System.out.println(" ");

        System.out.println("=== Removed Files ===");
        File removedirectory = new File(".gitlet/remove");
        String[] rdirectory = removedirectory.list();
        if (rdirectory != null) {
            for (String remove : rdirectory) {
                System.out.println(remove);
            }
        }
        System.out.println(" ");

        System.out.println("=== Modifications Not Staged For Commit ===");
        modificationotstageforcommit();
        System.out.println(" ");

        System.out.println("=== Untracked Files ===");
        untrackedfiles();
        System.out.println(" ");
    }

    public void branch(String branchname) {

        File branchesdirectory = new File(".gitlet/branches");
        String[] bdirectory = branchesdirectory.list();
        if (bdirectory != null) {
            for (String branches : bdirectory) {
                if (branches.equals(branchname)) {
                    System.out.println("A branch with "
                            + "that name already exists.");
                    return;
                }
            }
        }

        File newbranch = Utils.join(BRANCHES, branchname);
        String contents = Utils.readContentsAsString(hEADfile);
        Utils.writeContents(newbranch, contents);
    }

    public void rmbranch(String branchname) {

        if (Utils.readContentsAsString(currentbranch).equals(branchname)) {
            System.out.println("Cannot remove the current branch.");
            return;
        } else {

            File branchesdirectory = new File(".gitlet/branches");
            String[] bdirectory = branchesdirectory.list();
            if (bdirectory != null) {
                for (String branch : bdirectory) {
                    if (branch.equals(branchname)) {
                        File deletef = new File(
                                ".gitlet/branches/" + branchname);
                        deletef.delete();
                        return;
                    }
                }
                System.out.println("A branch with that name does not exist.");
                return;
            }
        }
    }

    public void reset(String commitid) throws IOException {

        File commitfile = Utils.join(COMMITS_FOLDER, commitid);
        if (!commitfile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Commit headcommit = Utils.readObject(Utils.join(COMMITS_FOLDER,
                Utils.readContentsAsString(hEADfile)), Commit.class);
        Commit resetfile = Utils.readObject(commitfile, Commit.class);
        for (String filename : Utils.plainFilenamesIn(CWD)) {
            if (headcommit.getcommitmap().get(filename) == null) {
                if (resetfile.getcommitmap().get(filename) != null) {
                    System.out.println("There is an untracked file "
                            + "in the way; delete it, "
                            + "or add and commit it first.");
                    return;
                }

            }
        }

        Utils.writeContents(currentbranch, resetfile.getBranch());
        Utils.writeContents(Utils.join(BRANCHES,
                resetfile.getBranch()), commitid);

        Set<String> set = resetfile.getcommitmap().keySet();
        for (String key : set) {
            if (resetfile.getcommitmap().get(key) != null) {
                String checkoutSHA = resetfile.getcommitmap().get(key);
                File checkoutfile = Utils.join(BLOBS_FOLDER, checkoutSHA);
                Blob checkoutblob = Utils.readObject(checkoutfile, Blob.class);
                File overwrittenfile = new File(CWD,
                        checkoutblob.getblobName());
                overwrittenfile.createNewFile();
                Utils.writeContents(overwrittenfile,
                        checkoutblob.getblobContents());
            }
        }

        for (String a : Utils.plainFilenamesIn(CWD)) {
            if (headcommit.getcommitmap().get(a) != null) {
                if (resetfile.getcommitmap().get(a) == null) {
                    File f = Utils.join(CWD, a);
                    f.delete();
                }
            }
        }

        Utils.writeContents(hEADfile, commitid);
        File stagingdirectory = new File(".gitlet/staging");
        File[] sdirectory = stagingdirectory.listFiles();
        if (sdirectory != null) {
            for (File stagingfiles : sdirectory) {
                stagingfiles.delete();
            }
        }
    }

    public void merge(String branchname) throws IOException {
        File branch = Utils.join(BRANCHES, branchname);
        if (!(branch.exists())) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        String currbranch = Utils.readContentsAsString(currentbranch);
        if (currbranch.equals(branchname)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        File stagingdirectory = new File(".gitlet/staging");
        File[] sdirectory = stagingdirectory.listFiles();
        int messagecounter = 0;
        for (File stagingfiles : sdirectory) {
            messagecounter += 1;
        }
        File removedirectory = new File(".gitlet/remove");
        File[] rdirectory = removedirectory.listFiles();
        for (File removefiles : rdirectory) {
            messagecounter += 1;
        }
        if (messagecounter != 0) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        continuecheck(branchname);
    }

    public void continuecheck(String branchname) throws IOException {

        Commit headcommits = Utils.readObject(Utils.join(
                COMMITS_FOLDER, Utils.readContentsAsString(
                        hEADfile)), Commit.class);
        String branchsha = Utils.readContentsAsString(
                Utils.join(BRANCHES, branchname));
        Commit givenbranch = Utils.readObject(
                Utils.join(COMMITS_FOLDER, branchsha), Commit.class);
        while (!headcommits.equals(null)) {
            if (headcommits.getSHA().equals(branchsha)) {
                System.out.println("Given branch is an "
                        + "ancestor of the current branch.");
                return;
            }
            if (headcommits.getParent().equals("")) {
                break;
            }
            Commit parent = Utils.readObject(Utils.join(COMMITS_FOLDER,
                    headcommits.getParent()), Commit.class);
            headcommits = parent;
        }
         headcommits = Utils.readObject(Utils.join(
                COMMITS_FOLDER, Utils.readContentsAsString(
                        hEADfile)), Commit.class);
        for (String files : Utils.plainFilenamesIn(CWD)) {
            if (Utils.readContentsAsString(Utils.join(CWD, files)) != null) {
                if (headcommits.getcommitmap().get(files) == null) {
                    System.out.println("There is an untracked file in"
                            + " the way; delete it, or add "
                            + "and commit it first.");
                    return;
                }
            }
        }
        while (!givenbranch.equals(null)) {
            if (givenbranch.getSHA().equals(
                    Utils.readContentsAsString(hEADfile))) {
                Utils.writeContents(hEADfile, givenbranch);
                Utils.writeContents(currentbranch, branchname);
                System.out.println("Current branch fast-forwarded.");
                return;
            }
            if (givenbranch.getParent().equals("")) {
                break;
            }
            Commit parent = Utils.readObject(Utils.join(COMMITS_FOLDER,
                    givenbranch.getParent()), Commit.class);
            givenbranch = parent;
        }
        helper(branchname);
    }

    public void helper(String branchname) throws IOException {

        Boolean conflict = false;

        String headsha1 = Utils.readContentsAsString(hEADfile);
        Commit headcommits = Utils.readObject(Utils.join(COMMITS_FOLDER, headsha1), Commit.class);
        String branchsha = Utils.readContentsAsString(Utils.join(BRANCHES, branchname));
        Commit givenbranch = Utils.readObject(Utils.join(COMMITS_FOLDER, branchsha), Commit.class);

        Commit splitpoint = null;

        ArrayList<String> ancesotorofhead = new ArrayList<>();
        while (!headcommits.equals(null)) {
            ancesotorofhead.add(headcommits.getSHA());
            if (headcommits.getParent().equals("")) {
                break;
            }
            Commit parent = Utils.readObject(new File(".gitlet/commits/" + headcommits.getParent()), Commit.class);
            headcommits = parent;
        }

        while (!givenbranch.equals(null)) {
            if (ancesotorofhead.contains(givenbranch.getSHA())) {
                splitpoint = givenbranch;
                break;
            }
            if (givenbranch.getParent().equals("")) {
                break;
            }
            Commit parent = Utils.readObject(Utils.join(COMMITS_FOLDER, givenbranch.getParent()), Commit.class);
            givenbranch = parent;
        }

        String headsha = Utils.readContentsAsString(hEADfile);
        Commit headcommit = Utils.readObject(Utils.join(COMMITS_FOLDER, headsha), Commit.class);

        TreeMap<String, String> splitmap = splitpoint.getcommitmap();

        //get the commitmap of that pointer
        TreeMap<String, String> copymap = headcommit.getcommitmap();

        Set<String> current = copymap.keySet();
        for (String key : current) {
            if (copymap.get(key) != null) {
                if (givenbranch.getcommitmap().get(key) == null) {
                    String doja;
                }
            }
        }

        Set<String> given = givenbranch.getcommitmap().keySet();
        for (String key : given) {
            if (givenbranch.getcommitmap().get(key) != null) {
                if (copymap.get(key) == null) {
                    checkout("checkout ", givenbranch.getcommitmap().get(key), " -- ", "key");
                    File overwrittenfile = new File(STAGING_FOLDER, key);
                    overwrittenfile.createNewFile();
                    Utils.writeContents(overwrittenfile,
                            Utils.readObject(Utils.join(BLOBS_FOLDER, givenbranch.getcommitmap().get(key)), Blob.class).getblobContents());

//                    CHECKOUT THE GIVEN VERSIONS
                }
            }
        }

        Set<String> set = splitmap.keySet();
        for (String key : set) {
            String checkoutSHA = splitmap.get(key);
            File checkoutfile = Utils.join(BLOBS_FOLDER, checkoutSHA);
            Blob checkoutblob = Utils.readObject(checkoutfile, Blob.class);

            if ((givenbranch.getcommitmap().get(key) == null) && (copymap.get(key)) != null) {
                rm(key);
            } else if ((givenbranch.getcommitmap().get(key) != null) && (copymap.get(key)) == null) {
                String hi;
            }

            if ((givenbranch.getcommitmap().get(key) != null) && (copymap.get(key)) != null) {

                Blob branchblob = Utils.readObject(Utils.join(BLOBS_FOLDER, givenbranch.getcommitmap().get(key)), Blob.class);
                Blob headblob = Utils.readObject(Utils.join(BLOBS_FOLDER, headcommit.getcommitmap().get(key)), Blob.class);

                if (!(checkoutblob.getblobContents().equals(branchblob.getblobContents()))
                        && (checkoutblob.getblobContents().equals(headblob.getblobContents()))) {
                    File overwrittenfile = new File(STAGING_FOLDER, checkoutblob.getblobName());
                    overwrittenfile.createNewFile();
                    Utils.writeContents(overwrittenfile, branchblob.getblobContents());
                } else if (checkoutblob.getblobContents().equals(branchblob.getblobContents())
                        && !(checkoutblob.getblobContents().equals(headblob.getblobContents()))) {
                    String doja;

                } else if ((branchblob.getblobContents().equals(headblob.getblobContents()))) {
                    String doja;

                } else if (!(headblob.getblobContents().equals(branchblob.getblobContents()))) {
                    conflict = true;
                    helpconcat(headblob, branchblob, key);

                }
            }

        }

        commit("Merged" + branchname + "into" + Utils.readContentsAsString(currentbranch) + ". ");
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
        Utils.writeContents(hEADfile, Utils.readContents(Utils.join(BRANCHES, (Utils.readContentsAsString(currentbranch)))));

    }

    public void helpconcat(Blob head, Blob given, String name) throws IOException {

        String newcontent = "<<<<<<< HEAD\n";
        newcontent += head.getblobContents();
        newcontent += "=======\n";
        newcontent += given.getblobContents();
        newcontent += ">>>>>>>";

        File overwrittenfile = new File(STAGING_FOLDER, name);
        overwrittenfile.createNewFile();
        Utils.writeContents(overwrittenfile, newcontent);

    }


    public void modificationotstageforcommit() {

        for (String stages : Utils.plainFilenamesIn(STAGING_FOLDER)) {
            if (!(Utils.join(CWD, stages).exists())) {
                System.out.println(stages + " (deleted)");
            } else if (!(Utils.readObject(Utils.join(STAGING_FOLDER,
                    stages), Blob.class).getblobContents().equals(
                    Utils.readContentsAsString(Utils.join(CWD, stages))))) {
                System.out.println(stages + " (modified)");
            }
        }

        String headcommit = Utils.readContentsAsString(hEADfile);
        TreeMap<String, String> copymap = Utils.readObject(Utils.join(
                COMMITS_FOLDER, headcommit), Commit.class).getcommitmap();

        Set<String> set1 = copymap.keySet();
        for (String key : set1) {
            File orig = Utils.join(CWD, key);
            String checkoutSHA = copymap.get(key);
            if ((checkoutSHA != null) && !(orig.exists())
                    && !(Utils.join(REMOVE_FOLDER, key).exists())) {
                System.out.println(orig + " (deleted)");
            } else if (orig.exists() && !(Utils.readObject(Utils.join(
                            BLOBS_FOLDER, checkoutSHA),
                    Blob.class).getblobContents().equals(
                    Utils.readContentsAsString(orig))
                    && !(Utils.join(STAGING_FOLDER, key).exists()))) {
                System.out.println(orig + " (modified)");
            }
        }

    }

    public void untrackedfiles() {
        String headcommit = Utils.readContentsAsString(hEADfile);
        TreeMap<String, String> copymap = Utils.readObject(new File(
                ".gitlet/commits/" + headcommit), Commit.class).getcommitmap();

        for (String filename : Utils.plainFilenamesIn(CWD)) {
            if (Utils.readContentsAsString(Utils.join(CWD, filename)) != null) {
                if ((copymap.get(filename) == null)
                        && !(Utils.join(STAGING_FOLDER, filename).exists())) {
                    System.out.println(filename);
                }
            }
        }
    }

    public void diff(String... files) {
        String version1;
        Commit commitV2 = new Commit();
        List<String> fileListV2;
        List<String> remainingFiles = new ArrayList<>();
        ArrayList<String> contentsOfV1 = new ArrayList<>();
        ArrayList<String> contentsOfV2 = new ArrayList<>();
        if (files.length == 3) {
            version1 = Utils.readContentsAsString(
                    Utils.join(BRANCHES, files[1]));
            String version2 = Utils.readContentsAsString(
                    Utils.join(BRANCHES, files[2]));
            commitV2 = Utils.readObject(Utils.join(
                    COMMITS_FOLDER, version2), Commit.class);
            Set<String> fileSetV2 = commitV2.getcommitmap().keySet();
            fileListV2 = new ArrayList<>(fileSetV2);
            remainingFiles = new ArrayList<>(fileListV2);
        } else if (files.length == 2) {
            String branchName = files[1];
            version1 = Utils.readContentsAsString(
                    Utils.join(BRANCHES, branchName));
            fileListV2 = getCWDFiles();
        } else {
            version1 = Utils.readContentsAsString(hEADfile);
            fileListV2 = getCWDFiles();
        }
        helps(version1, remainingFiles, fileListV2, commitV2, files);
        Commit commitV1 = Utils.readObject(Utils.join(
                COMMITS_FOLDER, version1), Commit.class);
        if (files.length == 3) {
            diffAddFiles(contentsOfV1, contentsOfV2, remainingFiles,
                    commitV1, commitV2);
        }
    }

    public void helps(String version1, List<String> remainingFiles,
                      List<String> fileListV2, Commit commitV2,
                      String... files) {

        Commit commitV1 = Utils.readObject(Utils.join(
                COMMITS_FOLDER, version1), Commit.class);
        Set<String> fileSetV1 = commitV1.getcommitmap().keySet();
        for (String key : fileSetV1) {
            ArrayList<String> contentsOfV1 = new ArrayList<>();
            ArrayList<String> contentsOfV2 = new ArrayList<>();

            if (commitV1.getcommitmap().get(key) != null) {
                String fileSHA = commitV1.getcommitmap().get(key);
                String[] bloblines = getBlobLines(fileSHA);
                for (String text : bloblines) {
                    contentsOfV1.add(text);
                }
                if (files.length < 3) {
                    if (fileListV2.contains(key)) {
                        String cwdContents = Utils.readContentsAsString(
                                Utils.join(CWD, key));
                        String[] cwdLines = cwdContents.split("\\r?\\n");
                        for (String text : cwdLines) {
                            contentsOfV2.add(text);
                        }
                    }
                } else {
                    String fileSHAv2 = commitV2.getcommitmap().get(key);
                    if (fileSHAv2 != null) {
                        String[] bloblinesv2 = getBlobLines(fileSHAv2);
                        for (String text : bloblinesv2) {
                            contentsOfV2.add(text);
                        }
                    }
                }
                Diff newDiff = new Diff();
                newDiff.setSequences(contentsOfV1, contentsOfV2);
                if (!newDiff.sequencesEqual()) {
                    if (contentsOfV2.size() == 0) {
                        System.out.println("diff --git a/"
                                + key + " /dev/null");
                        System.out.println("--- a/" + key);
                        System.out.println("+++ /dev/null");
                    } else {
                        System.out.println("diff --git a/" + key + " b/" + key);
                        System.out.println("--- a/" + key);
                        System.out.println("+++ b/" + key);
                    }
                    int[] diffs = newDiff.diffs();
                    printDiff(diffs, contentsOfV1, contentsOfV2);
                }
            }
            if (files.length == 3) {
                remainingFiles.remove(key);
            }
        }
    }

    public void printDiff(int[] diffs, ArrayList<String> contentsOfV1,
                          ArrayList<String> contentsOfV2) {
        int i = 0;
        while (i < diffs.length) {
            int[] remove = {diffs[i], diffs[i + 1]};
            int[] add = {diffs[i + 2], diffs[i + 3]};
            if (remove[1] > 0) {
                remove[0] = remove[0] + 1;
            }
            if (add[1] > 0) {
                add[0] = add[0] + 1;
            }
            String removeString;
            String addString;
            if (remove[1] == 1) {
                removeString = remove[0] + "";
            } else {
                removeString = remove[0] + "," + remove[1];
            }
            if (add[1] == 1) {
                addString = add[0] + "";
            } else {
                addString = add[0] + "," + add[1];
            }
            System.out.println("@@ -" + removeString
                    + " +" + addString + " @@");
            int j = 0;
            while (j < remove[1]) {
                System.out.println("-" + contentsOfV1.get(remove[0] - 1 + j));
                j++;
            }
            int k = 0;
            while (k < add[1]) {
                System.out.println("+" + contentsOfV2.get(add[0] - 1 + k));
                k++;
            }
            i += 4;
        }
    }

    public List<String> getCWDFiles() {
        String[] v2Directory = CWD.list();
        return Arrays.asList(v2Directory);
    }

    public String[] getBlobLines(String fileSHA) {
        File blobv2 = Utils.join(BLOBS_FOLDER, fileSHA);
        Blob blobbyv2 = Utils.readObject(blobv2, Blob.class);
        String contentsv2 = blobbyv2.getblobContents();
        return contentsv2.split("\\r?\\n");
    }

    public void diffAddFiles(ArrayList<String> contentsOfV1, ArrayList<String>
            contentsOfV2, List<String> remainingFiles,
                             Commit commitV1, Commit commitV2) {
        contentsOfV2.clear();
        contentsOfV1.clear();
        for (String key : remainingFiles) {
            System.out.println("diff --git /dev/null b/" + key);
            System.out.println("--- /dev/null");
            System.out.println("+++ b/" + key);
            if (commitV1.getcommitmap().get(key) != null) {
                String fileSHA = commitV1.getcommitmap().get(key);
                String[] bloblines = getBlobLines(fileSHA);
                for (String text : bloblines) {
                    contentsOfV1.add(text);
                }
            }
            String fileSHAv2 = commitV2.getcommitmap().get(key);
            if (fileSHAv2 != null) {
                String[] bloblinesv2 = getBlobLines(fileSHAv2);
                for (String text : bloblinesv2) {
                    contentsOfV2.add(text);
                }
            }
            Diff newDiff = new Diff();
            newDiff.setSequences(contentsOfV1, contentsOfV2);
            int[] diffs = newDiff.diffs();

            printDiff(diffs, contentsOfV1, contentsOfV2);
        }
    }


}
