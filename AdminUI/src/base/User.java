package base;

public class User {
    private String name;
    private String resource;
    private String permissions;

    public User(String name, String resource, String permissions) {
        this.name = name;
        this.resource = resource;
        this.permissions = permissions;
    }

    public String getName() {
        return name;
    }

    public String getResource() {
        return resource;
    }

    public String getPermissions() {
        return permissions;
    }
}
