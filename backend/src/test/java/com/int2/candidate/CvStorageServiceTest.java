package com.int2.candidate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.int2.common.ExternalServiceException;
import org.junit.jupiter.api.Test;

class CvStorageServiceTest {
    @Test
    void uploadsPdfToConfiguredBucket() {
        Storage storage = mock(Storage.class);
        CvStorageService service = new CvStorageService("candidate-files", storage);
        byte[] pdf = "%PDF-test".getBytes();

        assertEquals("candidate/cv.pdf", service.upload(pdf));
        verify(storage).create(any(BlobInfo.class), eq(pdf));
    }

    @Test
    void rejectsMissingBucketConfiguration() {
        CvStorageService service = new CvStorageService("", mock(Storage.class));

        assertThrows(ExternalServiceException.class, () -> service.upload(new byte[0]));
    }
}
