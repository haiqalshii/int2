package com.int2.candidate;

import com.int2.common.ExternalServiceException;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CvStorageService {
    private static final String OBJECT_PATH = "candidate/cv.pdf";
    private final String bucket;
    private final Storage storage;

    public CvStorageService(
            @Value("${app.gcp.candidate-bucket:}") String bucket,
            Storage storage) {
        this.bucket = bucket;
        this.storage = storage;
    }

    public String upload(byte[] pdf) {
        if (!StringUtils.hasText(bucket)) {
            throw new ExternalServiceException("Google Cloud Storage is not configured.");
        }
        try {
            BlobInfo blob = BlobInfo.newBuilder(BlobId.of(bucket, OBJECT_PATH))
                    .setContentType("application/pdf")
                    .build();
            storage.create(blob, pdf);
            return OBJECT_PATH;
        } catch (RuntimeException exception) {
            throw new ExternalServiceException("Unable to store the CV right now.", exception);
        }
    }
}
