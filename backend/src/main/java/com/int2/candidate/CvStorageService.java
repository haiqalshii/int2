package com.int2.candidate;

import com.int2.common.ExternalServiceException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class CvStorageService {
    private static final String OBJECT_PATH = "candidate/cv.pdf";
    private final String supabaseUrl;
    private final String serviceKey;
    private final String bucket;
    private final RestClient restClient;

    public CvStorageService(
            @Value("${app.supabase.url:}") String supabaseUrl,
            @Value("${app.supabase.service-key:}") String serviceKey,
            @Value("${app.supabase.candidate-bucket:candidate-files}") String bucket,
            RestClient.Builder builder) {
        this.supabaseUrl = supabaseUrl.replaceAll("/+$", "");
        this.serviceKey = serviceKey;
        this.bucket = bucket;
        this.restClient = builder.build();
    }

    public String upload(byte[] pdf) {
        if (!StringUtils.hasText(supabaseUrl) || !StringUtils.hasText(serviceKey)) {
            throw new ExternalServiceException("Supabase Storage is not configured.");
        }
        try {
            var request = restClient.post()
                    .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + OBJECT_PATH)
                    .contentType(MediaType.APPLICATION_PDF)
                    .header("apikey", serviceKey)
                    .header("x-upsert", "true");
            if (serviceKey.startsWith("eyJ")) request = request.header("Authorization", "Bearer " + serviceKey);
            request.body(pdf).retrieve().body(Map.class);
            return OBJECT_PATH;
        } catch (RestClientException exception) {
            throw new ExternalServiceException("Unable to store the CV right now.", exception);
        }
    }
}

