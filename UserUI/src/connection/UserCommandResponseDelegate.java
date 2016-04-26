package connection;

import java.util.Map;

public interface UserCommandResponseDelegate {
    void readResource(String value, String error);
    void writeResource(boolean success, String error);
    void getPermissions(Map<String, String> perms, String error);
}
