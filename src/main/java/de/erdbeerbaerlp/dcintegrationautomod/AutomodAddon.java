package de.erdbeerbaerlp.dcintegrationautomod;

import java.util.Arrays;
import java.util.List;

import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.addon.DiscordIntegrationAddon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AutomodAddon implements DiscordIntegrationAddon {
    private final List<String> adminRoleIDs = Arrays.asList(AutomodConfig.instance().automod.adminRoleIDs); // Add your admin role ID here
    private final String logChannelID = AutomodConfig.instance().automod.logChannelID;
    private final String minecraftChannelID = AutomodConfig.instance().automod.minecraftChannelID;
    private AutomodListener listener;

    // Addon Loading
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
            System.out.println("Admin Role IDs: " + adminRoleIDs);
        }

        // Main Logic
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (!AutomodConfig.instance().automod.enabled) {
                return;
            }

            Message message = event.getMessage();
            Member member = event.getMember();

            // Skip if member is null
            if (member == null) return;

            // Check if member has admin role - bypass automod if they do
            boolean isAdmin = member.getRoles().stream().anyMatch(role -> adminRoleIDs.contains(role.getId().trim()));
            if (isAdmin) {
                return;
            }

            String content = message.getContentRaw();
            String channelId = event.getChannel().getId();

            // Check if the message is in the Minecraft channel before applying blacklist checks
            if (channelId.equals(minecraftChannelID)) {
                // Check for blacklisted words
                for (String blacklistedWord : AutomodConfig.instance().automod.bannedWords) {
                    if (content.toLowerCase().contains(blacklistedWord.toLowerCase())) {
                        // Delete message
                        message.delete().queue();

                        // Log the blocked message
                        if (logChannelID != null && !logChannelID.isEmpty()) {
                            dc.getJDA().getTextChannelById(logChannelID)
                                    .sendMessage("Blocked message from **" + member.getEffectiveName() +
                                            "** containing blacklisted word: **" + blacklistedWord +
                                            "**. Content: \"" + message.getContentRaw() + "\"")
                                    .queue();
                        }
                        DiscordIntegration.LOGGER.info("Blocked message from Minecraft to Discord due to blacklisted word: " + blacklistedWord);
                        return;
                    }
                }
            }
        }
    }
}