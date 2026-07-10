package com.ecopantry.app.ui.navigation

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.ecopantry.app.R
import com.ecopantry.app.ui.screen.*
import kotlinx.serialization.Serializable

// ── Route definitions ──────────────────────────────────────────────────────
@Serializable data object SplashRoute          : NavKey
@Serializable data object LoginRoute           : NavKey
@Serializable data object RegistrationRoute    : NavKey
@Serializable data object HomeRoute            : NavKey
@Serializable data object InventoryRoute       : NavKey
@Serializable data object BrowseDonationsRoute : NavKey
@Serializable data object TrackerRoute         : NavKey
@Serializable data object ImpactRoute          : NavKey
@Serializable data object ProfileRoute         : NavKey
@Serializable data object NotificationRoute    : NavKey

@Serializable data class AddEditFoodRoute(val itemId: String? = null) : NavKey
@Serializable data class FoodDetailRoute(val itemId: String)          : NavKey
@Serializable data class AddDonationRoute(val itemId: String)         : NavKey
@Serializable data class DonationDetailRoute(val donationId: String)  : NavKey

// ── Nav item descriptor ────────────────────────────────────────────────────
private data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: NavKey
)

private val navItems = listOf(
    NavItem("Home",      Icons.Default.Home,             HomeRoute),
    NavItem("Inventory", Icons.Default.Inventory2,       InventoryRoute),
    NavItem("Donate",    Icons.Default.VolunteerActivism, BrowseDonationsRoute),
    NavItem("Tracker",   Icons.Default.BarChart,          TrackerRoute),
    NavItem("Profile",   Icons.Default.Person,            ProfileRoute)
)

// The five main-tab routes — used to decide whether to show the nav shell
private val mainRouteClasses = setOf(
    HomeRoute::class,
    InventoryRoute::class,
    BrowseDonationsRoute::class,
    TrackerRoute::class,
    ProfileRoute::class
)

@Composable
fun AppNavigation() {
    val backStack    = rememberNavBackStack(SplashRoute)
    val currentRoute = backStack.lastOrNull()
    val isMainRoute  = currentRoute?.let { it::class in mainRouteClasses } == true

    val config            = LocalConfiguration.current
    val isTabletLandscape = config.screenWidthDp > 600 &&
            config.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isMainRoute) {
        if (isTabletLandscape) {
            // ── Tablet landscape: NavigationRail on the left ───────────────
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier       = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        Spacer(Modifier.height(8.dp))
                        Image(
                            painter = painterResource(id = R.drawable.ecopantry_logo),
                            contentDescription = "EcoPantry",
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .size(32.dp)
                        )
                    }
                ) {
                    Spacer(Modifier.weight(1f))
                    navItems.forEach { item ->
                        val selected = currentRoute?.let { it::class == item.route::class } == true
                        NavigationRailItem(
                            selected = selected,
                            onClick  = {
                                if (!selected) {
                                    backStack.removeAll { it::class in mainRouteClasses }
                                    backStack.add(item.route)
                                }
                            },
                            icon  = { Icon(item.icon, item.label) },
                            label = { Text(item.label) }
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    MainNavDisplay(backStack)
                }
            }
        } else {
            // ── Phone / tablet portrait: NavigationBar at the bottom ───────
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        navItems.forEach { item ->
                            val selected = currentRoute?.let { it::class == item.route::class } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick  = {
                                    if (!selected) {
                                        backStack.removeAll { it::class in mainRouteClasses }
                                        backStack.add(item.route)
                                    }
                                },
                                icon  = { Icon(item.icon, item.label) },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    MainNavDisplay(backStack)
                }
            }
        }
    } else {
        // ── Auth, splash, and detail screens — no persistent nav shell ─────
        MainNavDisplay(backStack)
    }
}

// ── Single NavDisplay reused by all layout variants ────────────────────────
@Composable
private fun MainNavDisplay(backStack: MutableList<NavKey>) {
    NavDisplay(
        backStack       = backStack,
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        onBack          = { backStack.removeLastOrNull() },
        entryProvider   = entryProvider {

            entry<SplashRoute> {
                SplashScreen(
                    navigateToLogin = { backStack.clear(); backStack.add(LoginRoute) },
                    navigateToHome  = { backStack.clear(); backStack.add(HomeRoute) }
                )
            }

            entry<LoginRoute> {
                LoginScreen(
                    navigateToRegister = { backStack.add(RegistrationRoute) },
                    navigateToHome     = { backStack.clear(); backStack.add(HomeRoute) }
                )
            }

            entry<RegistrationRoute> {
                RegistrationScreen(
                    navigateBack   = { backStack.removeLastOrNull() },
                    navigateToHome = { backStack.clear(); backStack.add(HomeRoute) }
                )
            }

            entry<HomeRoute> {
                HomeScreen(
                    navigateToInventory        = {
                        backStack.removeAll { it::class in mainRouteClasses }
                        backStack.add(InventoryRoute)
                    },
                    navigateToBrowseDonations  = {
                        backStack.removeAll { it::class in mainRouteClasses }
                        backStack.add(BrowseDonationsRoute)
                    },
                    navigateToTracker          = {
                        backStack.removeAll { it::class in mainRouteClasses }
                        backStack.add(TrackerRoute)
                    },
                    navigateToProfile          = {
                        backStack.removeAll { it::class in mainRouteClasses }
                        backStack.add(ProfileRoute)
                    },
                    navigateToAddFood          = { backStack.add(AddEditFoodRoute()) },
                    navigateToNotifications    = { backStack.add(NotificationRoute) },
                    navigateToFoodDetail       = { id -> backStack.add(FoodDetailRoute(id)) }
                )
            }

            entry<InventoryRoute> {
                InventoryScreen(
                    navigateToAddFood         = { backStack.add(AddEditFoodRoute()) },
                    navigateToFoodDetail      = { id -> backStack.add(FoodDetailRoute(id)) },
                    navigateToDonationDetail  = { id -> backStack.add(DonationDetailRoute(id)) }
                )
            }

            entry<BrowseDonationsRoute> {
                BrowseDonationScreen(
                    navigateToDonationDetail = { id -> backStack.add(DonationDetailRoute(id)) }
                )
            }

            entry<TrackerRoute> {
                Column(modifier = Modifier.fillMaxSize()) {
                    var showImpact by remember { mutableStateOf(false) }
                    TabRow(selectedTabIndex = if (showImpact) 1 else 0) {
                        Tab(selected = !showImpact, onClick = { showImpact = false }, text = { Text("Weekly") })
                        Tab(selected = showImpact, onClick = { showImpact = true }, text = { Text("My Impact") })
                    }
                    if (showImpact) ImpactScreen() else WeeklyTrackerScreen()
                }
            }

            entry<ProfileRoute> {
                ProfileScreen(
                    navigateToLogin = { backStack.clear(); backStack.add(LoginRoute) }
                )
            }

            entry<NotificationRoute> {
                NotificationScreen(navigateBack = { backStack.removeLastOrNull() })
            }

            entry<AddEditFoodRoute> { key ->
                AddEditFoodScreen(
                    itemId       = key.itemId,
                    navigateBack = { backStack.removeLastOrNull() }
                )
            }

            entry<FoodDetailRoute> { key ->
                FoodDetailScreen(
                    itemId                 = key.itemId,
                    navigateBack           = { backStack.removeLastOrNull() },
                    navigateToEdit         = { id -> backStack.add(AddEditFoodRoute(id)) },
                    navigateToAddDonation  = { id -> backStack.add(AddDonationRoute(id)) }
                )
            }

            entry<AddDonationRoute> { key ->
                AddDonationScreen(
                    itemId               = key.itemId,
                    navigateBack         = { backStack.removeLastOrNull() },
                    navigateToInventory  = {
                        backStack.removeAll { it::class in mainRouteClasses || it::class == FoodDetailRoute::class || it::class == AddDonationRoute::class }
                        backStack.add(InventoryRoute)
                    }
                )
            }

            entry<DonationDetailRoute> { key ->
                DonationDetailScreen(
                    donationId   = key.donationId,
                    navigateBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
