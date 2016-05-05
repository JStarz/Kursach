package connection;

import java.util.Map;

public interface UserCommandResponseDelegate {
    void resourcePath(String resource, String path, String name);
    void readResource(String value, String error);
    void writeResource(boolean success, String error);
    void getPermissions(Map<String, String> perms, String error);
}
