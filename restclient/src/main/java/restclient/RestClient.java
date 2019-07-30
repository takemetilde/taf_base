package restclient;

import pojo.PostsList;
import pojo.Posts;

import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * A general REST client built using the JAX-RS/Jersey library.  In order to serialize a json into Class<Payload>:
 *
 * Client client = ClientBuilder.newClient();
 * WebTarget webTarget = client.target(strURI);
 * Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
 * Payload payload = builder.get(CRUD.class);
 *
 */
public class RestClient {

    private static final String REST_URI
            = "https://jsonplaceholder.typicode.com/posts/";

    private Client client = ClientBuilder.newClient();

    /**
     * This method is sends a GET request to the REST_URI with a specific ID.
     * @param id
     * @return Posts pojo class with the appropriate bindings.
     */
    public Posts getPostsEntity(int id) {
        return client
                .target(REST_URI)
                .path(String.valueOf(id))
                .request(MediaType.APPLICATION_JSON)
                .get(Posts.class);
    }

    /**
     * This method is sends a POST request to the REST_URI.
     * @param posts
     * @return Raw JSON
     */
    public Response createJsonPosts(Posts posts) {
        return client
                .target(REST_URI)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(posts, MediaType.APPLICATION_JSON));
    }

    /**
     * This method is sends a GET request to the REST_URI with a specific ID.
     * @param id
     * @return Raw JSON
     */
    public Response getPostsJson(int id) {
        return client
                .target(REST_URI)
                .path(String.valueOf(id))
                .request(MediaType.APPLICATION_JSON)
                .get();
    }

    /**
     * This method is sends a GET request to get all in the REST_URI.
     * @return Raw JSON
     */
    public Response getAllPostsJson() {
        return client
                .target(REST_URI)
                .request(MediaType.APPLICATION_JSON)
                .get();
    }

    /**
     * This method inputs the ID of the project and binds the JSON response from the target to the Posts.class POJO
     * @return PostsList pojo class with the appropriate bindings.
     */
    public List<Posts> getPostsListJson() {
        return client
                .target(REST_URI)
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Posts>>(){});
    }


    public PostsList getPostsListEntity() {
        PostsList postsList = new PostsList(getPostsListJson());
        return postsList;
    }
}