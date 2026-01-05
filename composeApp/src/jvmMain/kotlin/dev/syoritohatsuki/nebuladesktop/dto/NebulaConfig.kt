package dev.syoritohatsuki.nebuladesktop.dto

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * # Prepared for future use
 * - Limited settings mode
 * - Additional linter validator
 * - On save validation
 * */

@Serializable
data class NebulaConfig(
    val pki: Pki,
    @SerialName("static_host_map") val staticHostMap: Map<String, List<String>>,
    @SerialName("static_map") val staticMap: StaticMap,
    val lighthouse: Lighthouse,
    val listen: Listen,
    val punchy: Punchy,
    val cipher: Chiper,
    @SerialName("preferred_ranges") val preferredRanges: List<String>,
    val relay: Relay,
    val tun: Tun,
    val sshd: Sshd,
    val logging: Logging,
    val firewall: Firewall,
    val routines: Int = 1,
    val stats: Stats,
    val handshakes: Handshakes,
) {
    @Serializable
    data class Pki(
        @Required val ca: String,
        @Required val cert: String,
        @Required val key: String,
        val blocklist: List<String>?,
        @SerialName("disconnect_invalid") val disconnectInvalid: Boolean = false,
        @SerialName("initiating_version") val initiatingVersion: String?,
    )

    @Serializable
    data class StaticMap(
        val network: Network = Network.IP4,
        val cadence: String = "30s",
        @SerialName("lookup_timeout") val lookupTimeout: String = "250ms",
    ) {
        @Serializable
        enum class Network {
            IP4, IP6, IP
        }
    }

    @Serializable
    data class Lighthouse(
        @SerialName("am_lighthouse") val amLighthouse: Boolean = false,
        @SerialName("serve_dns") val serveDns: Boolean = false,
        val dns: DNS,
        val interval: Int = 10,
        val hosts: List<String>,
        @SerialName("remote_allow_list") val remoteAllowList: Map<String, Boolean>,
        @SerialName("local_allow_list") val localAllowList: Map<String, LocalAllowRule>,
        @SerialName("advertise_addrs") val advertiseAddrs: List<String>,
        @SerialName("calculated_remotes") val calculatedRemotes: Map<String, List<CalculatedRemoteEntry>>
    ) {
        @Serializable
        data class DNS(
            val host: String = "0.0.0.0",
            val port: Int = 53,
        )

        @Serializable
        sealed class LocalAllowRule {
            /** ```yaml
             * lighthouse:
             *   local_allow_list:
             *     # Example to block tun0 and all docker interfaces.
             *     interfaces:
             *       tun0: false
             *       'docker.*': false
             *     # Example to only advertise this subnet to the lighthouse.
             *     '10.0.0.0/8': true
             * ```
             * */
            data class Interfaces(val rules: Map<String, Boolean>) : LocalAllowRule()
            data class Subnet(val allowed: Boolean) : LocalAllowRule()
        }

        @Serializable
        data class CalculatedRemoteEntry(
            val mask: String, val port: Int
        )
    }

    @Serializable
    data class Listen(
        val host: String = "0.0.0.0",
        val port: Int,
        val batch: Int = 64,
        @SerialName("read_buffer") val readBuffer: Int,
        @SerialName("write_buffer") val writeBuffer: Int,
        @SerialName("send_recv_error") val sendRecvError: SendRecvError = SendRecvError.ALWAYS,
        @SerialName("so_mark") val soMark: Int,
    ) {
        @Serializable
        enum class SendRecvError {
            ALWAYS, NEVER, PRIVATE
        }
    }

    @Serializable
    data class Punchy(
        val punch: Boolean = false,
        val delay: Int = 15,
        val respond: Boolean = false,
        @SerialName("respond_delay") val respondDelay: String = "5s"
    )

    @Serializable
    enum class Chiper {
        AES, CHACHAPOLY
    }

    @Serializable
    data class Relay(
        val relays: List<String>,
        @SerialName("am_relay") val amRelay: Boolean = false,
        @SerialName("use_relay") val useRelay: Boolean = true,
    )

    @Serializable
    data class Tun(
        val disabled: Boolean = false,
        val dev: String,
        @SerialName("drop_local_broadcast") val dropLocalBroadcast: Boolean = false,
        @SerialName("drop_multicast") val dropMulticast: Boolean = false,
        @SerialName("tx_queue") val txQueue: Int = 500,
        val mtu: Int = 1300,
        val routes: List<RouteEntry>,
        @SerialName("unsafe_routes") val unsafeRoutes: List<UnsafeRoute>,
        @SerialName("use_system_route_table") val useSystemRouteTable: Boolean = false
    ) {
        @Serializable
        data class RouteEntry(
            val mtu: Int, val route: String
        )

        @Serializable
        data class UnsafeRoute(
            val route: String, val via: List<Gateway>, val mtu: Int, val metric: Int, val install: Boolean
        ) {
            @Serializable
            data class Gateway(
                val gateway: String, val weight: Int
            )
        }
    }

    @Serializable
    data class Sshd(
        val enabled: Boolean = false,
        val listen: String,
        @SerialName("host_key") val hostKey: String,
        @SerialName("authorized_users") val authorizedUsers: List<AuthorizedUserEntry>,
        @SerialName("trusted_cas") val trustedCas: List<String>,
    ) {
        @Serializable
        data class AuthorizedUserEntry(
            val name: String,
            val keys: List<String>,
        )
    }

    @Serializable
    data class Logging(
        val level: Level = Level.INFO,
        val format: Format = Format.TEXT,
        @SerialName("disable_timestamp") val disableTimestamp: Boolean = false,
        @SerialName("timestamp_format") val timestampFormat: String
    ) {
        @Serializable
        enum class Level {
            PANIC, FATAL, ERROR, WARNING, INFO, DEBUG
        }

        @Serializable
        enum class Format {
            JSON, TEXT
        }
    }

    @Serializable
    data class Firewall(
        @SerialName("outbound_action") val outboundAction: Action,
        @SerialName("inbound_action") val inboundAction: Action,
        @SerialName("default_local_cidr_any") val defaultLocalCidrAny: Boolean = false,
        val conntrack: Conntrack,
        val outbound: List<FirewallRule>,
        val inbound: List<FirewallRule>
    ) {
        @Serializable
        enum class Action {
            DROP, REJECT
        }

        @Serializable
        data class Conntrack(
            @SerialName("tcp_timeout") val tcpTimeout: String = "12m",
            @SerialName("udp_timeout") val udpTimeout: String = "3m",
            @SerialName("default_timeout") val defaultTimeout: String = "10m"
        )

        @Serializable
        data class FirewallRule(
            val port: String = "any",
            val proto: Proto = Proto.ANY,
            @SerialName("ca_name") val caName: String,
            @SerialName("ca_sha") val caSha: String,
            val host: String,
            val group: String,
            val groups: List<String>,
            val cidr: String,
            @SerialName("local_cidr") val localCidr: String
        )

        @Serializable
        enum class Proto {
            ANY, TCP, UDP, ICMP
        }
    }

    @Serializable
    data class Stats(
        val type: Type,
        @Required val interval: String = "60s",
        @SerialName("message_metrics") val messageMetrics: Boolean = false,
        @SerialName("lighthouse_metrics") val lighthouseMetrics: Boolean = false,
        val prefix: String = "nebula",
        val protocol: Protocol = Protocol.TCP,
        @Required val host: String,
        @Required val listen: String,
        @Required val path: String = "/metrics",
        val namespace: String,
        val subsystem: String
    ) {
        enum class Type {
            GRAPHITE, PROMETHEUS
        }

        enum class Protocol {
            TCP, UDP
        }

    }

    @Serializable
    data class Handshakes(
        @SerialName("try_interval") val tryInterval: String = "100ms",
        val retries: Int = 10,
        @SerialName("trigger_buffer") val triggerBuffer: Int = 64,
    )
}