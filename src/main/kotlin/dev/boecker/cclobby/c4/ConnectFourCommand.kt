package dev.boecker.cclobby.c4

import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

class ConnectFourCommand(val connectFour: LobbyConnectFour) : Command("connect-four", "c4") {
    init {
        addSubcommand(AcceptChallengeCommand(connectFour))
    }

    class AcceptChallengeCommand(val connectFour: LobbyConnectFour) : Command("accept") {
        init {
            var challengerUuid = ArgumentType.UUID("challenger-uuid");

            addSyntax({sender, context ->
                val uuid = context.get(challengerUuid)

                val challenge = connectFour.challenges[uuid]
                if (challenge == null) {
                    sender.sendMessage(connectFour.lobby.minimessage.deserialize("<red>You were not challenged by this person!</red>"))
                }

                val challenger = MinecraftServer.getConnectionManager().onlinePlayers.find { it.uuid == uuid }
                if (challenger == null) {
                    sender.sendMessage(connectFour.lobby.minimessage.deserialize("<red>Challenger is not online!</red>"))
                    return@addSyntax
                }

                connectFour.acceptChallenge(sender as Player, challenger)
            }, challengerUuid)
        }
    }
}