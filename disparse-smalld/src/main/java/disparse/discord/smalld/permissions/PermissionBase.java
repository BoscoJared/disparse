package disparse.discord.smalld.permissions;

public class PermissionBase {

  private final long value;

  public PermissionBase(long value) {
    this.value = value;
  }

  public PermissionBase plus(Permission permission) {
    return new PermissionBase(this.value | permission.getValue());
  }

  public PermissionBase plus(PermissionBase permission) {
    return new PermissionBase(this.value | permission.value);
  }

  public boolean contains(Permission permission) {
    if (this.value == Permission.ADMINISTRATOR.getValue()) return true;

    long ans = (this.value & permission.getValue());
    return (this.value & permission.getValue()) == permission.getValue();
  }

  public long getValue() {
    return value;
  }

  public static PermissionBase of(Permission permission) {
    return new PermissionBase(permission.getValue());
  }
}
