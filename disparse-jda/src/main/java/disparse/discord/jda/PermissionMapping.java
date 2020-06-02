package disparse.discord.jda;

import disparse.discord.AbstractPermission;
import disparse.discord.PermissionEnumConverter;
import net.dv8tion.jda.api.Permission;

public class PermissionMapping implements PermissionEnumConverter<Permission> {

  @Override
  public Permission into(AbstractPermission permission) {
    switch (permission) {
      case ADMINISTRATOR:
        return Permission.ADMINISTRATOR;
      case BAN_MEMBERS:
        return Permission.BAN_MEMBERS;
      case CREATE_INSTANT_INVITE:
        return Permission.CREATE_INSTANT_INVITE;
      case KICK_MEMBERS:
        return Permission.KICK_MEMBERS;
      case MANAGE_CHANNEL:
        return Permission.MANAGE_CHANNEL;
      case MANAGE_EMOTES:
        return Permission.MANAGE_EMOTES;
      case MANAGE_PERMISSIONS:
        return Permission.MANAGE_PERMISSIONS;
      case MANAGE_ROLES:
        return Permission.MANAGE_ROLES;
      case MANAGE_SERVER:
        return Permission.MANAGE_SERVER;
      case MANAGE_WEBHOOKS:
        return Permission.MANAGE_WEBHOOKS;
      case MESSAGE_ADD_REACTION:
        return Permission.MESSAGE_ADD_REACTION;
      case MESSAGE_ATTACH_FILES:
        return Permission.MESSAGE_ATTACH_FILES;
      case MESSAGE_EMBED_LINKS:
        return Permission.MESSAGE_EMBED_LINKS;
      case MESSAGE_EXT_EMOJI:
        return Permission.MESSAGE_EXT_EMOJI;
      case MESSAGE_HISTORY:
        return Permission.MESSAGE_HISTORY;
      case MESSAGE_MANAGE:
        return Permission.MESSAGE_MANAGE;
      case MESSAGE_MENTION_EVERYONE:
        return Permission.MESSAGE_MENTION_EVERYONE;
      case MESSAGE_READ:
        return Permission.MESSAGE_READ;
      case MESSAGE_TTS:
        return Permission.MESSAGE_TTS;
      case MESSAGE_WRITE:
        return Permission.MESSAGE_WRITE;
      case NICKNAME_CHANGE:
        return Permission.NICKNAME_CHANGE;
      case NICKNAME_MANAGE:
        return Permission.NICKNAME_MANAGE;
      case PRIORITY_SPEAKER:
        return Permission.PRIORITY_SPEAKER;
      case VIEW_AUDIT_LOGS:
        return Permission.VIEW_AUDIT_LOGS;
      case VIEW_CHANNEL:
        return Permission.VIEW_CHANNEL;
      case VOICE_CONNECT:
        return Permission.VOICE_CONNECT;
      case VOICE_DEAF_OTHERS:
        return Permission.VOICE_DEAF_OTHERS;
      case VOICE_MOVE_OTHERS:
        return Permission.VOICE_MOVE_OTHERS;
      case VOICE_MUTE_OTHERS:
        return Permission.VOICE_MUTE_OTHERS;
      case VOICE_SPEAK:
        return Permission.VOICE_SPEAK;
      case VOICE_USE_VAD:
        return Permission.VOICE_USE_VAD;
      default:
        return Permission.UNKNOWN;
    }
  }

  @Override
  public AbstractPermission from(Permission permission) {
    switch (permission) {
      case ADMINISTRATOR:
        return AbstractPermission.ADMINISTRATOR;
      case BAN_MEMBERS:
        return AbstractPermission.BAN_MEMBERS;
      case CREATE_INSTANT_INVITE:
        return AbstractPermission.CREATE_INSTANT_INVITE;
      case KICK_MEMBERS:
        return AbstractPermission.KICK_MEMBERS;
      case MANAGE_CHANNEL:
        return AbstractPermission.MANAGE_CHANNEL;
      case MANAGE_EMOTES:
        return AbstractPermission.MANAGE_EMOTES;
      case MANAGE_PERMISSIONS:
        return AbstractPermission.MANAGE_PERMISSIONS;
      case MANAGE_ROLES:
        return AbstractPermission.MANAGE_ROLES;
      case MANAGE_SERVER:
        return AbstractPermission.MANAGE_SERVER;
      case MANAGE_WEBHOOKS:
        return AbstractPermission.MANAGE_WEBHOOKS;
      case MESSAGE_ADD_REACTION:
        return AbstractPermission.MESSAGE_ADD_REACTION;
      case MESSAGE_ATTACH_FILES:
        return AbstractPermission.MESSAGE_ATTACH_FILES;
      case MESSAGE_EMBED_LINKS:
        return AbstractPermission.MESSAGE_EMBED_LINKS;
      case MESSAGE_EXT_EMOJI:
        return AbstractPermission.MESSAGE_EXT_EMOJI;
      case MESSAGE_HISTORY:
        return AbstractPermission.MESSAGE_HISTORY;
      case MESSAGE_MANAGE:
        return AbstractPermission.MESSAGE_MANAGE;
      case MESSAGE_MENTION_EVERYONE:
        return AbstractPermission.MESSAGE_MENTION_EVERYONE;
      case MESSAGE_READ:
        return AbstractPermission.MESSAGE_READ;
      case MESSAGE_TTS:
        return AbstractPermission.MESSAGE_TTS;
      case MESSAGE_WRITE:
        return AbstractPermission.MESSAGE_WRITE;
      case NICKNAME_CHANGE:
        return AbstractPermission.NICKNAME_CHANGE;
      case NICKNAME_MANAGE:
        return AbstractPermission.NICKNAME_MANAGE;
      case PRIORITY_SPEAKER:
        return AbstractPermission.PRIORITY_SPEAKER;
      case VIEW_AUDIT_LOGS:
        return AbstractPermission.VIEW_AUDIT_LOGS;
      case VIEW_CHANNEL:
        return AbstractPermission.VIEW_CHANNEL;
      case VOICE_CONNECT:
        return AbstractPermission.VOICE_CONNECT;
      case VOICE_DEAF_OTHERS:
        return AbstractPermission.VOICE_DEAF_OTHERS;
      case VOICE_MOVE_OTHERS:
        return AbstractPermission.VOICE_MOVE_OTHERS;
      case VOICE_MUTE_OTHERS:
        return AbstractPermission.VOICE_MUTE_OTHERS;
      case VOICE_SPEAK:
        return AbstractPermission.VOICE_SPEAK;
      case VOICE_USE_VAD:
        return AbstractPermission.VOICE_USE_VAD;
      default:
        return AbstractPermission.UNKNOWN;
    }
  }
}
