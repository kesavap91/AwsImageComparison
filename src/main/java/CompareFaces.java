import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class CompareFaces {
    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();
        InputStream inputStream = new FileInputStream("application.properties");
        prop.load(inputStream);
        String photo1 = prop.getProperty("source_image");
        String photo2 = prop.getProperty("target_image");
        ClientConfiguration clientConfig = createClientConfiguration();
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(prop.getProperty("accessKey"),
                prop.getProperty("secretKey"));

        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder
                .standard()
                .withClientConfiguration(clientConfig)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion("ap-south-1").build();

        CompareFaces compareFaces = new CompareFaces();
        ByteBuffer image1 = compareFaces.loadImage(photo1);
        ByteBuffer image2 = compareFaces.loadImage(photo2);
        if (image1 == null || image2 == null) {
            return;
        }

        CompareFacesRequest compareFacesRequest = new CompareFacesRequest()
                .withSourceImage(new Image().withBytes(image1)).withTargetImage(new Image().withBytes(image2))
                .withSimilarityThreshold(70F);

        try {

            CompareFacesResult result = rekognitionClient.compareFaces(compareFacesRequest);
            List<CompareFacesMatch> lists = result.getFaceMatches();

            System.out.println("Detected labels for " + photo1 + " and " + photo2);

            if (!lists.isEmpty()) {
                for (CompareFacesMatch label : lists) {
                    System.out.println(label.getFace() + ": Similarity is " + label.getSimilarity().toString());
                }
            } else {
                System.out.println("Faces Does not match");
            }
        } catch (AmazonRekognitionException e) {
            e.printStackTrace();
        }
        inputStream.close();
    }

    private ByteBuffer loadImage(String imgPath) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(Paths.get(imgPath));
        } catch (IOException e) {
            System.err.println("Failed to load image: " + e.getMessage());
            return null;
        }
        return ByteBuffer.wrap(bytes);
    }

    private static ClientConfiguration createClientConfiguration() {
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setConnectionTimeout(30000);
        clientConfig.setRequestTimeout(60000);
        clientConfig.setProtocol(Protocol.HTTPS);
        return clientConfig;
    }
}
