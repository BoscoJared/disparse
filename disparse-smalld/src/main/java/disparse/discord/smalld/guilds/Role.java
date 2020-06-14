package disparse.discord.smalld.guilds;

import java.util.Objects;

public class Role {
  private String id;
  private String name;
  private long permissions;
  private int color;
  private boolean hoist;
  private int position;
  private boolean managed;
  private boolean mentionable;

  public Role(
      String id,
      String name,
      long permissions,
      int color,
      boolean hoist,
      int position,
      boolean managed,
      boolean mentionable) {
    this.id = id;
    this.name = name;
    this.permissions = permissions;
    this.color = color;
    this.hoist = hoist;
    this.position = position;
    this.managed = managed;
    this.mentionable = mentionable;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Role role = (Role) o;
    return id.equals(role.id) && name.equals(role.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name);
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public long getPermissions() {
    return permissions;
  }

  public int getColor() {
    return color;
  }

  public boolean isHoist() {
    return hoist;
  }

  public int getPosition() {
    return position;
  }

  public boolean isManaged() {
    return managed;
  }

  public boolean isMentionable() {
    return mentionable;
  }

  @Override
  public String toString() {
    return "Role{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", permissions="
        + permissions
        + ", color="
        + color
        + ", hoist="
        + hoist
        + ", position="
        + position
        + ", managed="
        + managed
        + ", mentionable="
        + mentionable
        + '}';
  }
}
