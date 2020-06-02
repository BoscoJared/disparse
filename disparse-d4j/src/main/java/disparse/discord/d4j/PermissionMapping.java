package disparse.discord.d4j;

import discord4j.rest.util.Permission;
import disparse.discord.AbstractPermission;
import disparse.discord.PermissionEnumConverter;

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
        return Permission.MANAGE_CHANNELS;
      case MANAGE_EMOTES:
        return Permission.MANAGE_EMOJIS;
      case MANAGE_PERMISSIONS:
      case MANAGE_ROLES:
        return Permission.MANAGE_ROLES;
      case MANAGE_SERVER:
        return Permission.MANAGE_GUILD;
      case MANAGE_WEBHOOKS:
        return Permission.MANAGE_WEBHOOKS;
      case MESSAGE_ADD_REACTION:
        return Permission.ADD_REACTIONS;
      case MESSAGE_ATTACH_FILES:
        return Permission.ATTACH_FILES;
      case MESSAGE_EMBED_LINKS:
        return Permission.EMBED_LINKS;
      case MESSAGE_EXT_EMOJI:
        return Permission.USE_EXTERNAL_EMOJIS;
      case MESSAGE_HISTORY:
        return Permission.READ_MESSAGE_HISTORY;
      case MESSAGE_MANAGE:
        return Permission.MANAGE_MESSAGES;
      case MESSAGE_MENTION_EVERYONE:
        return Permission.MENTION_EVERYONE;
      case MESSAGE_READ:
      case VIEW_CHANNEL:
        return Permission.VIEW_CHANNEL;
      case MESSAGE_TTS:
        return Permission.SEND_TTS_MESSAGES;
      case MESSAGE_WRITE:
        return Permission.SEND_MESSAGES;
      case NICKNAME_CHANGE:
        return Permission.CHANGE_NICKNAME;
      case NICKNAME_MANAGE:
        return Permission.MANAGE_NICKNAMES;
      case PRIORITY_SPEAKER:
        return Permission.PRIORITY_SPEAKER;
      case VIEW_AUDIT_LOGS:
        return Permission.VIEW_AUDIT_LOG;
      case VOICE_CONNECT:
        return Permission.CONNECT;
      case VOICE_DEAF_OTHERS:
        return Permission.DEAFEN_MEMBERS;
      case VOICE_MOVE_OTHERS:
        return Permission.MOVE_MEMBERS;
      case VOICE_MUTE_OTHERS:
        return Permission.MUTE_MEMBERS;
      case VOICE_SPEAK:
        return Permission.SPEAK;
      case VOICE_USE_VAD:
        return Permission.USE_VAD;
      default:
        return null;
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
      case MANAGE_CHANNELS:
        return AbstractPermission.MANAGE_CHANNEL;
      case MANAGE_EMOJIS:
        return AbstractPermission.MANAGE_EMOTES;
      case MANAGE_ROLES:
        return AbstractPermission.MANAGE_ROLES;
      case MANAGE_GUILD:
        return AbstractPermission.MANAGE_SERVER;
      case MANAGE_WEBHOOKS:
        return AbstractPermission.MANAGE_WEBHOOKS;
      case ADD_REACTIONS:
        return AbstractPermission.MESSAGE_ADD_REACTION;
      case ATTACH_FILES:
        return AbstractPermission.MESSAGE_ATTACH_FILES;
      case EMBED_LINKS:
        return AbstractPermission.MESSAGE_EMBED_LINKS;
      case USE_EXTERNAL_EMOJIS:
        return AbstractPermission.MESSAGE_EXT_EMOJI;
      case READ_MESSAGE_HISTORY:
        return AbstractPermission.MESSAGE_HISTORY;
      case MANAGE_MESSAGES:
        return AbstractPermission.MESSAGE_MANAGE;
      case MENTION_EVERYONE:
        return AbstractPermission.MESSAGE_MENTION_EVERYONE;
      case VIEW_CHANNEL:
        return AbstractPermission.VIEW_CHANNEL;
      case SEND_TTS_MESSAGES:
        return AbstractPermission.MESSAGE_TTS;
      case SEND_MESSAGES:
        return AbstractPermission.MESSAGE_WRITE;
      case CHANGE_NICKNAME:
        return AbstractPermission.NICKNAME_CHANGE;
      case MANAGE_NICKNAMES: NICKNAME_MANAGE:
        return AbstractPermission.NICKNAME_MANAGE;
      case PRIORITY_SPEAKER:
        return AbstractPermission.PRIORITY_SPEAKER;
      case VIEW_AUDIT_LOG:
        return AbstractPermission.VIEW_AUDIT_LOGS;
      case CONNECT:
        return AbstractPermission.VOICE_CONNECT;
      case DEAFEN_MEMBERS:
        return AbstractPermission.VOICE_DEAF_OTHERS;
      case MOVE_MEMBERS:
        return AbstractPermission.VOICE_MOVE_OTHERS;
      case MUTE_MEMBERS:
        return AbstractPermission.VOICE_MUTE_OTHERS;
      case SPEAK:
        return AbstractPermission.VOICE_SPEAK;
      case USE_VAD:
        return AbstractPermission.VOICE_USE_VAD;
      default:
        return AbstractPermission.UNKNOWN;
    }
  }
}
