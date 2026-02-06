package com.stgsporting.piehmecup.services;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;
import software.amazon.awssdk.services.s3.S3Client;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    @Value("${aws.key}")
    private String accessKey;
    @Value("${aws.secret}")
    private String secretKey;

    @Value("${aws.s3.bucket.region}")
    private String region;
    @Value("${aws.s3.bucket.name}")
    private String bucketName;
    @Value("${aws.s3.directory}")
    private String directory;

    @Value("${aws.cloudfront.domain}")
    private String domain;
    @Value("${aws.cloudfront.key.id}")
    private String keyId;
    @Value("${aws.cloudfront.key.path}")
    private String keyPath;

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public String generateSignedUrl(String key) {
        if (key == null || key.isEmpty()) {
            log.debug("generateSignedUrl called with empty key; returning null");
            return null;
        }

        // Optional existence check to surface missing objects early
        try {
            if (!doesObjectExist(key)) {
                log.warn("S3 object does not exist for key='{}' (bucket='{}'). Signed URL will still be generated but CloudFront will 403.", key, bucketName);
            }
        } catch (Exception ex) {
            log.warn("Could not verify existence of key='{}': {}", key, ex.getMessage());
        }

        CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();
        Instant expirationTime = Instant.now().plus(5, ChronoUnit.HOURS);
        log.debug("Generating CloudFront signed URL for domain='{}', key='{}', expires='{}'", domain, key, expirationTime);

        CannedSignerRequest request;
        try {
            Path privateKeyPath = resolvePrivateKeyPath();
            log.debug("Using private key path: {} (exists={})", privateKeyPath, Files.exists(privateKeyPath));
            request = CannedSignerRequest.builder()
                    .resourceUrl("https://" + domain + "/" + key)
                    .privateKey(privateKeyPath)
                    .keyPairId(keyId)
                    .expirationDate(expirationTime)
                    .build();
        } catch (Exception e) {
            log.error("Failed to build CannedSignerRequest for key='{}': {}", key, e.getMessage(), e);
            return null;
        }

        SignedUrl signedUrl;
        try {
            signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(request);
        } catch (Exception e) {
            log.error("Failed to sign CloudFront URL for key='{}': {}", key, e.getMessage(), e);
            return null;
        }

        log.debug("Signed URL generated successfully for key='{}'", key);
        return signedUrl.url();
    }

    /**
     * Resolve the CloudFront private key path robustly:
     * - If keyPath exists relative to the current working directory, use it.
     * - Otherwise try to load it from the application classpath (e.g., bundled in resources)
     *   by copying it to a temporary file and returning that path.
     */
    private Path resolvePrivateKeyPath() throws IOException {
        Path cwdPath = Paths.get(System.getProperty("user.dir"), keyPath);
        if (Files.exists(cwdPath)) {
            log.trace("Private key found in working directory: {}", cwdPath);
            return cwdPath;
        }

        // Try absolute path as provided
        Path absPath = Paths.get(keyPath);
        if (Files.exists(absPath)) {
            log.trace("Private key found at absolute path: {}", absPath);
            return absPath;
        }

        // Fallback: load from classpath and write to a temp file
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(keyPath)) {
            if (is == null) {
                throw new FileNotFoundException("CloudFront private key not found at '" + keyPath + "' or in classpath.");
            }
            Path temp = Files.createTempFile("cloudfront-private-key", ".pem");
            Files.copy(is, temp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            temp.toFile().deleteOnExit();
            log.trace("Private key loaded from classpath into temp file: {}", temp);
            return temp;
        }
    }

    /**
     * HEAD the object to verify existence. Returns true if object metadata is retrievable.
     */
    public boolean doesObjectExist(String key) {
        try {
            getS3Client().headObject(r -> r.bucket(bucketName).key(key));
            return true;
        } catch (Exception e) {
            log.debug("headObject failed for key='{}': {}", key, e.getMessage());
            return false;
        }
    }

    private S3Client s3Client;

    private S3Client getS3Client() {
        if (this.s3Client != null) {
            return this.s3Client;
        }

        AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return this.s3Client = S3Client
                .builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    public String uploadFile(MultipartFile file, String saveTo) {
        if (file == null || file.isEmpty()) {
            log.debug("uploadFile called with empty file; returning null");
            return null;
        }

        String key = getKey(saveTo, file.getOriginalFilename());
        log.debug("Uploading file originalName='{}' as key='{}' to bucket='{}'", file.getOriginalFilename(), key, bucketName);

        try(InputStream inputStream = file.getInputStream()) {
            RequestBody requestBody = RequestBody.fromInputStream(inputStream, file.getSize());

            getS3Client().putObject(
                    request -> request.bucket(bucketName).key(key).ifNoneMatch("*"),
                    requestBody
            );
        }catch (IOException e) {
            log.error("Failed to upload file '{}' to S3: {}", key, e.getMessage(), e);
            return null;
        }

        return key;
    }

    public void deleteFile(String key) {
        log.debug("Deleting S3 object key='{}' from bucket='{}'", key, bucketName);
        getS3Client().deleteObject(request -> request.bucket(bucketName).key(key));
    }

    private String getKey(String parentDirectory, String filename) {
        String ext = FilenameUtils.getExtension(filename);
        String generated = directory + parentDirectory + "/" + generateUniqueString() + "." + ext;
        log.trace("Generated S3 key='{}' (ext='{}')", generated, ext);
        return generated;
    }

    private String generateUniqueString() {
        long millis = System.currentTimeMillis();
        String rndchars = RandomStringUtils.random(16, 32, 126, true, true, null, new Random());
        String unique = rndchars + "_" + millis;
        log.trace("Generated unique string='{}'", unique);
        return unique;
    }
}
