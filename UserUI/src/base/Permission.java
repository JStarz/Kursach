package base;

public class Permission {
    private String resource;
    private String permissions;

    public Permission(String resource, String permissions) {
        this.resource = resource;
        this.permissions = permissions;
    }

    public String getResource() {
        return resource;
    }

    public String getPermissions() {
        return permissions;
    }
}
