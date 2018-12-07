package org.superbiz.moviefun.blobstore;

public class Blob {
    public final long id;
    public final String path;
    public final byte[] bytes;
    public final String contentType;

    public Blob(long id, String path, byte[] bytes, String contentType) {
        this.id = id;
        this.path = path;
        this.bytes = bytes;
        this.contentType = contentType;
    }
}