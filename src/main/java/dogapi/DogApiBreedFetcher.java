package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Fetch the list of sub breeds for the given breed from the dog.ceo API.
     * @param breed the breed to fetch sub breeds for
     * @return list of sub breeds for the given breed
     * @throws BreedNotFoundException if the breed does not exist (or if the API call fails for any reason)
     */
    @Override
    public List<String> getSubBreeds(String breed) {
        String url = "https://dog.ceo/api/breed/" + breed + "/list";

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String body = Objects.requireNonNull(response.body(), "Empty response body").string();
                JSONObject json = new JSONObject(body);
                String status = json.optString("status", "");

                if ("success".equals(status)) {
                    JSONArray arr = json.getJSONArray("message");
                    List<String> subBreeds = new ArrayList<>(arr.length());
                    for (int i = 0; i < arr.length(); i++) {
                        subBreeds.add(arr.getString(i));
                    }
                    return subBreeds;
                }

                String msg = json.optString("message", "Dog API error");
                throw new BreedNotFoundException(msg);
            }
        } catch (IOException e) {
            throw new BreedNotFoundException("Failed to fetch sub-breeds for " + breed + ": " + e.getMessage());
        }
    }
}