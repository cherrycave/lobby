package dev.boecker.cclobby.messaging

import dev.boecker.cclobby.CherryCaveLobby
import dev.boecker.cherrycave.common.proxy.SendRequest
import dev.kourier.amqp.channel.AMQPChannel
import dev.kourier.amqp.connection.AMQPConnection
import dev.kourier.amqp.connection.amqpConfig
import dev.kourier.amqp.connection.createAMQPConnection
import kotlinx.coroutines.launch

class MessagingSystem(val lobby: CherryCaveLobby) {

    private lateinit var connection: AMQPConnection
    private lateinit var channel: AMQPChannel

    private val sendRequestQueue = "send-requests"

    init {
        val config = amqpConfig {
            server {
                host = System.getenv("RABBITMQ_HOST") ?: "localhost"
                port = System.getenv("RABBITMQ_PORT")?.toInt() ?: 5672
                vhost = System.getenv("RABBITMQ_VHOST") ?: "minecraft"
                user = System.getenv("RABBITMQ_USER") ?: "guest"
                password = System.getenv("RABBITMQ_PASSWORD") ?: "guest"
            }
        }

        lobby.coroutineScope.launch {
            connection = createAMQPConnection(lobby.coroutineScope, config)
            channel = connection.openChannel()

            declareQueues()
        }
    }

    private suspend fun declareQueues() {
        channel.queueDeclare(
            sendRequestQueue,
            durable = true,
            exclusive = false,
            autoDelete = false,
        )
    }

    suspend fun publishSendRequest(request: SendRequest) {
        channel.basicPublish(
            lobby.json.encodeToString(request).toByteArray(),
            exchange = "",
            routingKey = sendRequestQueue,
        )
    }

}