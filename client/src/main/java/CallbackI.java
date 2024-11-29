public class CallbackI implements Demo.Callback {
    public void reportResponse(String response, com.zeroc.Ice.Current current) {
        System.out.println(response);
    }
}


