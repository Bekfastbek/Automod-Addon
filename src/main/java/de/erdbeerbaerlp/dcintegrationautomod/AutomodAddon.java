package de.erdbeerbaerlp.dcintegrationautomod;

import java.util.Arrays;
import java.util.List;

import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.addon.DiscordIntegrationAddon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AutomodAddon implements DiscordIntegrationAddon {
    private final List<String> adminRoleIDs = Arrays.asList(AutomodConfig.instance().automod.adminRoleIDs);
    private final String logChannelID = AutomodConfig.instance().automod.logChannelID;
    private final String minecraftChannelID = AutomodConfig.instance().automod.minecraftChannelID;
    private AutomodListener listener;

    @Override
    public void load(DiscordIntegration dc) {
        listener = new AutomodListener(dc);
        dc.getJDA().addEventListener(listener);
        DiscordIntegration.LOGGER.info("Automod Addon loaded");
    }

    @Override
    public void unload(DiscordIntegration dc) {
        dc.getJDA().removeEventListener(listener);
        DiscordIntegration.LOGGER.info("Automod Addon unloaded");
    }

    private class AutomodListener extends ListenerAdapter {
        private final DiscordIntegration dc;

        public AutomodListener(DiscordIntegration dc) {
            this.dc = dc;
        }

        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            Message message = event.getMessage();
            Member member = event.getMember();

            // Check if member is null or has no roles
            if (member == null || member.getRoles().isEmpty()) return;

            // Only process messages in the designated Minecraft channel
            if (event.getChannel().getId().equals(minecraftChannelID) && !isAdminBypass(member)) {
                for (String blacklistedWord : AutomodConfig.instance().automod.bannedWords) {
                    if (message.getContentRaw().toLowerCase().contains(blacklistedWord.toLowerCase())) {
                        // Log and prevent forwarding to Discord
                        if (logChannelID != null && !logChannelID.isEmpty()) {
                            dc.getJDA().getTextChannelById(logChannelID)
                                .sendMessage("Blocked message from **" + member.getEffectiveName() + "** containing blacklisted word: **" + blacklistedWord + "**. Content: \"" + message.getContentRaw() + "\"")
                                .queue();
                        }

                        DiscordIntegration.LOGGER.info("Blocked message from Minecraft to Discord due to blacklisted word: " + blacklistedWord);
                        
                        // Block message from Discord
                        event.getMessage().delete().queue(); // Alternatively, if deletion isn’t desired, stop processing here
                        return;
                    }
                }
            }
        }
    }

    // Check for admin bypass
    private boolean isAdminBypass(Member member) {
        for (String roleId : adminRoleIDs) {
            if (member.getRoles().stream().anyMatch(role -> role.getId().equals(roleId))) {
                return AutomodConfig.instance().automod.adminRoleAutomod;
            }
        }
        return false;
    }
}
