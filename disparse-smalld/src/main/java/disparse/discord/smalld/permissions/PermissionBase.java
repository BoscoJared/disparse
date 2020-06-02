package disparse.discord.smalld.permissions;

public class PermissionBase {

  private final long value;

  public PermissionBase(long value) {
    this.value = value;
  }

  public PermissionBase plus(Permission permission) {
    return new PermissionBase(this.value | permission.getValue());
  }

  public boolean contains(Permission permission) {
    return (this.value & permission.getValue()) == permission.getValue();
  }

  public static PermissionBase of(Permission permission) {
    return new PermissionBase(permission.getValue());
  }
}
