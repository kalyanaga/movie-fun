package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

@Component
public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        saveUploadToFile(blob);
    }

    @Override
    public Optional<Blob> get(long id) throws IOException {
        Blob blob = null;
        try {
            Path coverFilePath = getExistingCoverPath(id);
            byte[] imageBytes = readAllBytes(coverFilePath);
            blob = new Blob(id, coverFilePath.toUri().getPath(), imageBytes, new Tika().detect(coverFilePath));
            return Optional.of(blob);
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void deleteAll() {
        // ...
    }

    private void saveUploadToFile(Blob blob) throws IOException {
        File targetFile = getCoverFile(blob.id);
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(blob.bytes);
        }
    }

    private File getCoverFile(@PathVariable long albumId) {
        String coverFileName = format("covers/%d", albumId);
        return new File(coverFileName);
    }

    private Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }
}