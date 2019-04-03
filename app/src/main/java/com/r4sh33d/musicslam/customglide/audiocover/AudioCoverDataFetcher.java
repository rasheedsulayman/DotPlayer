package com.r4sh33d.musicslam.customglide.audiocover;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class AudioCoverDataFetcher implements DataFetcher<InputStream> {
    private static final String[] FALLBACKS = {"cover.jpg", "album.jpg", "folder.jpg",
            "cover.jpeg", "album.jpeg", "folder.jpeg", "cover.png", "album.png", "folder.png"};
    private InputStream stream;
    private AudioCoverImage audioCoverImage;
    private Context context;

    public AudioCoverDataFetcher(AudioCoverImage audioCoverImage, Context context) {
        this.audioCoverImage = audioCoverImage;
        this.context = context;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        if (TextUtils.isEmpty(audioCoverImage.filePath)){
            callback.onDataReady(null);
            return;
        }
        //First try MediaMetadataRetriever
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(audioCoverImage.filePath);
            byte[] picture = retriever.getEmbeddedPicture();
            if (picture != null) {
                callback.onDataReady(new ByteArrayInputStream(picture));
            } else {
                callback.onDataReady(fallback(audioCoverImage.filePath));
            }
        } finally {
            retriever.release();
        }
    }

    private InputStream fallback(String path) {
        // No 1 fallback, Try to get AlbumArt from the Tag
        stream = getArtworkFromMp3Tag(new File(path));
        if (stream != null) {
            return stream;
        } else {
            //No 2 fallback, Try again from a cover file inside the same directory as the mp3 file
            return getArtworkFromFile(path);
        }
    }


    public static InputStream getArtworkFromMp3Tag(File musicFile) {
        try {
            MP3File f = (MP3File) AudioFileIO.read(musicFile);
            if (f.hasID3v2Tag()) {
                Artwork albumArt = f.getTag().getFirstArtwork();
                if (albumArt != null) {
                    return new ByteArrayInputStream(albumArt.getBinaryData());
                }
            }
        } catch (CannotReadException | IOException
                | TagException | ReadOnlyFileException
                | InvalidAudioFrameException ignored) {
            //Nothing for now
        }
        return null;
    }

    private InputStream getArtworkFromFile(String path) {
        try {
            File parent = new File(path).getParentFile();
            for (String fallback : FALLBACKS) {
                // TODO make it smarter by enumerating folder contents and filtering for files
                File cover = new File(parent, fallback);
                if (cover.exists()) {
                    return stream = new FileInputStream(cover);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void cleanup() {
        // already cleaned up in loadData and ByteArrayInputStream will be GC'd
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignore) {
                // can't do much about it
            }
        }
    }

    @Override
    public void cancel() {
        // cannot cancel
    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }
}
