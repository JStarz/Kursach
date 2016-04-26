package connection;

public interface ServerAuthResponseDelegate {
    void authFailed(String description);
    void connectionClose(String reason);
    void authSuccesful();
}
