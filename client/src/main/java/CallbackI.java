public class CallbackI implements VotingSystem.Callback {
    public void reportResponse(String response, com.zeroc.Ice.Current current) {
        System.out.println(response);
    }
}


