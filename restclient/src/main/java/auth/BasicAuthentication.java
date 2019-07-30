package auth;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

public class BasicAuthentication extends Authentication{

    private static String username = "username";
    private static String password = "password";

    public static HttpAuthenticationFeature basicAuth() {
        HttpAuthenticationFeature httpAuthenticationFeature = HttpAuthenticationFeature.basicBuilder()
                .nonPreemptive().credentials(username, password).build();

        return httpAuthenticationFeature;

    }

}
