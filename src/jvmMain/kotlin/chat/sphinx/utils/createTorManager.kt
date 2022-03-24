package chat.sphinx.utils

import chat.sphinx.concepts.authentication.data.AuthenticationStorage
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.logger.SphinxLogger
import chat.sphinx.logger.d
import chat.sphinx.utils.build_config.BuildConfigDebug
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.JvmPreferencesSettings
import com.russhwolf.settings.Settings
import io.matthewnelson.build_config.BuildConfigVersionCode
import io.matthewnelson.kmp.tor.KmpTorLoaderJvm
import io.matthewnelson.kmp.tor.PlatformInstaller
import io.matthewnelson.kmp.tor.TorConfigProviderJvm
import io.matthewnelson.kmp.tor.common.address.Port
import io.matthewnelson.kmp.tor.common.address.PortProxy
import io.matthewnelson.kmp.tor.controller.common.config.TorConfig
import io.matthewnelson.kmp.tor.controller.common.config.TorConfig.Setting.*
import io.matthewnelson.kmp.tor.controller.common.config.TorConfig.Option.*
import io.matthewnelson.kmp.tor.controller.common.file.Path
import io.matthewnelson.kmp.tor.manager.TorManager
import kotlinx.coroutines.CoroutineScope
import java.util.prefs.Preferences

actual fun createTorManager(
    applicationScope: CoroutineScope,
    authenticationStorage: AuthenticationStorage,
    buildConfigDebug: BuildConfigDebug,
    buildConfigVersionCode: BuildConfigVersionCode,
    dispatchers: CoroutineDispatchers,
    LOG: SphinxLogger
): TorManager {
    val osName = System.getProperty("os.name")

    val platformInstaller = when {
        osName.contains("Windows") -> {
            PlatformInstaller.mingwX64(PlatformInstaller.InstallOption.CleanInstallIfMissing)
        }
        osName.contains("Mac") || osName.contains("Darwin") -> {
            PlatformInstaller.macosX64(PlatformInstaller.InstallOption.CleanInstallIfMissing)
        }
        osName.contains("Linux") -> {
            PlatformInstaller.linuxX64(PlatformInstaller.InstallOption.CleanInstallIfMissing)
        }
        else -> {
            throw RuntimeException("Could not identify OS from 'os.name=$osName'")
        }
    }

    LOG.d("createTorManager", "Setting up KmpTor for os: ${platformInstaller.os}, arch: ${platformInstaller.arch}")

    // Note that for this example the temp directory is utilized. Keep in mind
    // that all processes and users have access to the temporary directory and
    // its use should be avoided in production.
    val tmpDir: String = System.getProperty("java.io.tmpdir")
        ?: throw RuntimeException("Could not identify OS's temporary directory")

    val configProvider = object: TorConfigProviderJvm() {
        override val workDir: Path = Path(tmpDir).builder {
            addSegment("kmptor-javafx-sample")
            addSegment("work")
        }
        override val cacheDir: Path = Path(tmpDir).builder {
            addSegment("kmptor-javafx-sample")
            addSegment("cache")
        }

        override fun provide(): TorConfig {
            return TorConfig.Builder {
                // Set multiple ports for all of the things
                val dns = Ports.Dns()
                put(dns.set(AorDorPort.Value(PortProxy(9252))))
                put(dns.set(AorDorPort.Value(PortProxy(9253))))

                val socks = Ports.Socks()
                put(socks.set(AorDorPort.Value(PortProxy(9254))))
                put(socks.set(AorDorPort.Value(PortProxy(9255))))

                val http = Ports.HttpTunnel()
                put(http.set(AorDorPort.Value(PortProxy(9258))))
                put(http.set(AorDorPort.Value(PortProxy(9259))))

                val trans = Ports.Trans()
                put(trans.set(AorDorPort.Value(PortProxy(9262))))
                put(trans.set(AorDorPort.Value(PortProxy(9263))))

                // If a port (9263) is already taken (by ^^^^ trans port above)
                // this will take its place and "overwrite" the trans port entry
                // because port 9263 is taken.
                put(socks.set(AorDorPort.Value(PortProxy(9263))))

                // Set Flags
                socks.setFlags(setOf(
                    Ports.Socks.Flag.OnionTrafficOnly
                )).setIsolationFlags(setOf(
                    Ports.IsolationFlag.IsolateClientAddr
                )).set(AorDorPort.Value(PortProxy(9264)))
                put(socks)

                // reset our socks object to defaults
                socks.setDefault()

                // Not necessary, as if ControlPort is missing it will be
                // automatically added for you; but for demonstration purposes...
                put(Ports.Control().set(AorDorPort.Auto))

                // Tor defaults this setting to false which would mean if
                // Tor goes dormant (default is after 24h), the next time it
                // is started it will still be in the dormant state and will
                // not bootstrap until being set to "active". This ensures that
                // if it is a fresh start, dormancy will be cancelled automatically.
                put(DormantCanceledByStartup().set(TorF.True))

                // If planning to use v3 Client Authentication in a persistent
                // manner (where private keys are saved to disk via the "Persist"
                // flag), this is needed to be set.
                put(ClientOnionAuthDir().set(FileSystemDir(
                    workDir.builder { addSegment(ClientOnionAuthDir.DEFAULT_NAME) }
                )))

                // Add Hidden services
                put(HiddenService()
                    .setPorts(ports = setOf(
                        HiddenService.Ports(virtualPort = Port(1025), targetPort = Port(1027)),
                        HiddenService.Ports(virtualPort = Port(1026), targetPort = Port(1027))
                    ))
                    .setMaxStreams(maxStreams = HiddenService.MaxStreams(value = 2))
                    .setMaxStreamsCloseCircuit(value = TorF.True)
                    .set(FileSystemDir(
                        workDir.builder {
                            addSegment(HiddenService.DEFAULT_PARENT_DIR_NAME)
                            addSegment("test_service")
                        }
                    ))
                )

                put(HiddenService()
                    .setPorts(ports = setOf(
                        HiddenService.Ports(virtualPort = Port(1028), targetPort = Port(1030)),
                        HiddenService.Ports(virtualPort = Port(1029), targetPort = Port(1030))
                    ))
                    .set(FileSystemDir(
                        workDir.builder {
                            addSegment(HiddenService.DEFAULT_PARENT_DIR_NAME)
                            addSegment("test_service_2")
                        }
                    ))
                )
            }.build()
        }
    }

    val jvmLoader = KmpTorLoaderJvm(installer = platformInstaller, provider = configProvider)

    return TorManager.newInstance(loader = jvmLoader, networkObserver = null, requiredEvents = null)
}

@OptIn(ExperimentalSettingsImplementation::class)
actual fun createPlatformSettings(): Settings {
    val preferences = Preferences.userRoot()
    return JvmPreferencesSettings(preferences)
}