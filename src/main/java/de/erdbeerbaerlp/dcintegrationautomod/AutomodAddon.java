package de.erdbeerbaerlp.dcintegrationautomod;

import java.util.Arrays;
import java.util.List;

import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.addon.DiscordIntegrationAddon;
import de.erdbeerbaerlp.dcintegration.common.api.DiscordEventHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AutomodAddon implements DiscordIntegrationAddon {
    private final List<String> adminRoleIDs = Arrays.asList(AutomodConfig.instance().automod.adminRoleIDs);
    private final String logChannelID = AutomodConfig.instance().automod.logChannelID;

    @Override
    public void load(DiscordIntegration dc) {
        dc.getJDA().addEventListener(new DiscordEventHandler() {
            public void onMessageReceived(MessageReceivedEvent event) {
                Message message = event.getMessage();
                Member member = event.getMember();

                if (member == null || member.getRoles().isEmpty()) {
                    return;
                }

                // To check admin automod bypass
                if (isAdminBypass(member)) {
                    return;
                }

                // Blacklist check
                for (String blacklistedWord : AutomodConfig.instance().automod.bannedWords) {
                    if (message.getContentRaw().toLowerCase().contains(blacklistedWord.toLowerCase())) {
                        // Message logging to channelId
                        if (logChannelID != null && !logChannelID.isEmpty()) {
                            dc.getJDA().getTextChannelById(logChannelID)
                                .sendMessage("Blocked message from **" + member.getEffectiveName() + "** containing blacklisted word: **" + blacklistedWord + "**. Content: \"" + message.getContentRaw() + "\"")
                                .queue();
                        }

                        DiscordIntegration.LOGGER.info("Blocked message from being sent to Minecraft due to blacklisted word: " + blacklistedWord);
                        return;
                    }
                }
            }
        });

        DiscordIntegration.LOGGER.info("Automod Addon loaded");
    }

    @Override
    public void reload() {
        DiscordIntegration.LOGGER.info("Automod Addon reloaded");
    }

    @Override
    public void unload(DiscordIntegration dc) {
        dc.getJDA().removeEventListener(this);
        DiscordIntegration.LOGGER.info("Automod Addon unloaded");
    }

    // Check if the member has an admin role and if bypass is enabled
    private boolean isAdminBypass(Member member) {
        for (String roleId : adminRoleIDs) {
            if (member.getRoles().stream().anyMatch(role -> role.getId().equals(roleId))) {
                return AutomodConfig.instance().automod.adminRoleAutomod;
            }
        }
        return false;
    }
}
