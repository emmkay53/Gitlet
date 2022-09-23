package gitlet;

import java.io.Serializable;

public class Blob implements Serializable {

    /**
     * return the blob object.
     */
    private String name;
    /**
     * return the blob object.
     */
    private String contents;
    /**
     * return the blob object.
     */
    private String sha1;

    public Blob(String names, String content) {

        this.name = names;
        this.sha1 = Utils.sha1(content + name);
        this.contents = content;

    }

    /**
     * Return the blob name.
     */
    public String getblobName() {
        return this.name;
    }

    /**
     * Return the blob sha.
     */
    public String getblobSHA() {
        return this.sha1;
    }

    /**
     * Return the blob content.
     */
    public String getblobContents() {
        return this.contents;
    }

}
