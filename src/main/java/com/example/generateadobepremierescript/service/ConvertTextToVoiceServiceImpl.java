package com.example.generateadobepremierescript.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
@Service
public class ConvertTextToVoiceServiceImpl {
    public void convertTextToVoice(String text) {
        // Set the path to your JSON credentials file
        String credentialsPath = "rising-study-393313-f46ae6effe46.json";

        // Set the output path for the generated audio file
        String outputPath = "C:\\Users\\gohar\\Videos\\Captures\\file.mp3";


        try {
            // Load the credentials
            GoogleCredentials credentials = GoogleCredentials.fromStream(Files.newInputStream(Paths.get(credentialsPath)));
            FixedCredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);

            // Create a TextToSpeechClient using the credentials
            try (TextToSpeechClient client = TextToSpeechClient.create(TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider(credentialsProvider)
                    .build())) {

                // Create a SynthesisInput object from the text
                SynthesisInput input = SynthesisInput.newBuilder()
                        .setText(text)
                        .build();

                // Select the voice model
                VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                        .setLanguageCode("en-US") // Set the desired language code (e.g., "en-US" for English)
                        .build();

                // Select the audio encoding parameters
                AudioConfig audioConfig = AudioConfig.newBuilder()
                        .setAudioEncoding(AudioEncoding.MP3) // Set the desired audio format (e.g., MP3)
                        .build();

                // Call the API to synthesize speech
                SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audioConfig);

                // Retrieve the audio content from the response
                ByteString audioContents = response.getAudioContent();

                // Save the audio file to disk
                try (FileOutputStream out = new FileOutputStream(outputPath)) {
                    out.write(audioContents.toByteArray());
                    System.out.println("Audio file saved: " + outputPath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
