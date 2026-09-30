package com.diegovillena.apponirica.ui

import android.net.Uri
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import com.diegovillena.apponirica.transcription.MotorTranscripcion
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.diegovillena.apponirica.AppOniricaApplication
import com.diegovillena.apponirica.ui.ajustes.AjustesScreen
import com.diegovillena.apponirica.ui.captura.CapturaScreen
import com.diegovillena.apponirica.ui.captura.CapturaViewModel
import com.diegovillena.apponirica.ui.detalle.DetalleScreen
import com.diegovillena.apponirica.ui.detalle.DetalleViewModel
import com.diegovillena.apponirica.ui.diario.DiarioScreen
import com.diegovillena.apponirica.ui.diario.DiarioViewModel
import com.diegovillena.apponirica.ui.diario.FiltroPalabra
import com.diegovillena.apponirica.ui.estadisticas.EstadisticasScreen
import com.diegovillena.apponirica.ui.estadisticas.EstadisticasViewModel

private const val RutaDiario = "diario?stem={stem}&display={display}"
private const val RutaEstadisticas = "estadisticas"
private const val RutaAjustes = "ajustes"
private const val RutaCaptura = "captura"
private const val RutaDetalle = "detalle/{id}"

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val contenedor = (LocalContext.current.applicationContext as AppOniricaApplication).container
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val pestanaActiva = rutaActual == RutaDiario || rutaActual == RutaEstadisticas

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (pestanaActiva) {
                ExtendedFloatingActionButton(
                    text = { Text("Grabar sueño") },
                    icon = { Icon(Icons.Filled.Mic, contentDescription = null) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = { navController.navigate(RutaCaptura) },
                    modifier = Modifier.testTag("fab-grabar"),
                )
            }
        },
        bottomBar = {
            if (pestanaActiva) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Pestaña(navController, "Diario", Icons.Filled.Bedtime, RutaDiario, "diario", rutaActual, "tab-diario")
                    Pestaña(navController, "Estadísticas", Icons.Filled.BarChart, RutaEstadisticas, "estadisticas", rutaActual, "tab-estadisticas")
                    Pestaña(navController, "Ajustes", Icons.Filled.Settings, RutaAjustes, "ajustes", rutaActual, "tab-ajustes")
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = RutaDiario,
            modifier = Modifier
                .padding(padding)
                .semantics { testTagsAsResourceId = true },
            enterTransition = { fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 16 } },
            exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = { fadeIn(tween(220)) },
            popExitTransition = { fadeOut(tween(160)) + slideOutVertically(tween(220)) { it / 16 } },
        ) {
            composable(
                RutaDiario,
                arguments = listOf(
                    navArgument("stem") { type = NavType.StringType; nullable = true },
                    navArgument("display") { type = NavType.StringType; nullable = true },
                ),
            ) { entrada ->
                val stem = entrada.arguments?.getString("stem")
                val display = entrada.arguments?.getString("display")
                val vm: DiarioViewModel = viewModel(initializer = { DiarioViewModel(contenedor.repositorio) })
                DiarioScreen(
                    vm = vm,
                    filtroInicial = if (stem != null && display != null) FiltroPalabra(stem, display) else null,
                    onAbrirSueno = { id -> navController.navigate("detalle/$id") },
                )
            }
            composable(RutaEstadisticas) {
                val vm: EstadisticasViewModel = viewModel(initializer = { EstadisticasViewModel(contenedor.repositorio) })
                EstadisticasScreen(
                    vm = vm,
                    onFiltrarPalabra = { stem, display ->
                        navController.navigate("diario?stem=${Uri.encode(stem)}&display=${Uri.encode(display)}")
                    },
                )
            }
            composable(RutaAjustes) {
                val motor by contenedor.motor.collectAsState(initial = MotorTranscripcion.OFFLINE)
                AjustesScreen(
                    motor = motor,
                    onElegirMotor = { nuevo -> contenedor.fijarMotor(nuevo) },
                )
            }
            composable(RutaCaptura) {
                val vm: CapturaViewModel = viewModel(initializer = {
                    CapturaViewModel(contenedor.repositorio, contenedor.transcriptorPara())
                })
                CapturaScreen(vm = vm, onVolver = { navController.popBackStack() })
            }
            composable(
                RutaDetalle,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entrada ->
                val id = entrada.arguments?.getLong("id") ?: return@composable
                val vm: DetalleViewModel = viewModel(key = "detalle-$id") {
                    DetalleViewModel(contenedor.repositorio, id)
                }
                DetalleScreen(vm = vm, onVolver = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun RowScope.Pestaña(
    navController: androidx.navigation.NavController,
    etiqueta: String,
    icono: ImageVector,
    ruta: String,
    destinoAlPulsar: String,
    rutaActual: String?,
    tag: String,
) {
    NavigationBarItem(
        selected = rutaActual == ruta,
        onClick = {
            if (rutaActual != ruta) navController.navigate(destinoAlPulsar) {
                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                launchSingleTop = true
            }
        },
        icon = { Icon(icono, contentDescription = etiqueta) },
        label = { Text(etiqueta) },
        modifier = Modifier.testTag(tag),
    )
}