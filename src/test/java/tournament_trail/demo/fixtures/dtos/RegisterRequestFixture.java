package tournament_trail.demo.fixtures.dtos;

import tournament_trail.demo.web.dtos.RegisterRequest;

public class RegisterRequestFixture {
    public static final String TEST_INVALID_USERNAME= "TEST";
    public static final String TEST_INVALID_PASSWORD= "TEST";
    public static final String TEST_INVALID_EMAIL= "TEST";

    public static final String TEST_USERNAME= "TestUsername1.";
    public static final String TEST_PASSWORD= "TestPassword1.";
    public static final String TEST_EMAIL= "example@example.com";

    public static RegisterRequest createInvalid(){
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(TEST_INVALID_USERNAME);
        registerRequest.setPassword(TEST_INVALID_PASSWORD);
        registerRequest.setEmail(TEST_INVALID_EMAIL);

        return registerRequest;
    }

    public static RegisterRequest create(){
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(TEST_USERNAME);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setEmail(TEST_EMAIL);

        return registerRequest;
    }
}