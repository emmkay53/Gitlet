package gitlet;


import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

public class Commit implements Serializable {

    /**
     * Return the timestamp of a commit.
     */
    private String message;
    /**
     * Return the timestamp of a commit.
     */
    private String timestamp;
    /**
     * Return the timestamp of a commit.
     */
    private HashMap<String, String> _tree;
    /**
     * Return the timestamp of a commit.
     */
    private TreeMap<String, String> _commitmap;
    /**
     * Return the timestamp of a commit.
     */
    private String parent;
    /**
     * Return the timestamp of a commit.
     */
    private String sha;
    /**
     * Return the timestamp of a commit.
     */
    private String branch;

    public Commit() {
        this.message = "initial commit";
        this.parent = "";
        this.timestamp = "Wed Dec 31 16:00:00 1969 -0800";
        this._commitmap = new TreeMap<String, String>();
        this.sha = Utils.sha1(this.timestamp, this.parent,
                this.message, Utils.serialize(this._commitmap));
        this.branch = "master";

    }

    public Commit(String messages, String parents, String branches) {
        Date date = new Date();
        this.message = messages;
        this.parent = parents;
        this.timestamp = new SimpleDateFormat(
                "EEE MMM dd HH:mm:ss yyyy Z").format(new Date());
        if ((Utils.readObject(new File(".gitlet/commits/"
                + parent), Commit.class)._commitmap) == null) {
            _commitmap = new TreeMap<String, String>();
        } else {
            this._commitmap = copyparentmap(Utils.readObject(new File(
                    ".gitlet/commits/" + parent), Commit.class)._commitmap);
        }

        this.sha = Utils.sha1(this.timestamp, this.parent,
                this.message, Utils.serialize(this._commitmap));
        this.branch = branches;

    }


    /**
     * Return the SHA1 of a commit node.
     */
    public String getSHA() {
        return this.sha;
    }

    /**
     * Return the commit message of a commit.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Return the timestamp of a commit.
     */
    public String getTimestamp() {
        return this.timestamp;
    }

    public TreeMap<String, String> getcommitmap() {
        return _commitmap;
    }

    public String getParent() {
        return this.parent;
    }

    public String getBranch() {
        return this.branch;
    }

    public TreeMap<String, String> copyparentmap(
            TreeMap<String, String> parentmap) {
        TreeMap<String, String> map = new TreeMap<>();

        if (parentmap == null) {
            return map;
        } else {
            Set<String> set1 = parentmap.keySet();
            for (String key : set1) {
                String value = parentmap.get(key);
                map.put(key, value);
            }
            return map;
        }
    }


}
