package disparse.discord;

public interface PermissionEnumConverter<R extends Enum<R>> {
  R into(AbstractPermission permission);

  AbstractPermission from(R permission);
}
