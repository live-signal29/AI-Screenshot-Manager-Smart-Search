package com.example.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingManager(
    private val context: Context,
    private val preferencesRepository: UserPreferencesRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"

        // Product IDs configured in Google Play Console
        const val PRODUCT_LIFETIME = "lifetime_pro"
        const val PRODUCT_SUBS_MONTHLY = "pro_monthly"
        const val PRODUCT_SUBS_YEARLY = "pro_yearly"

        val ALL_IN_APP_PRODUCTS = listOf(PRODUCT_LIFETIME)
        val ALL_SUBSCRIPTIONS = listOf(PRODUCT_SUBS_MONTHLY, PRODUCT_SUBS_YEARLY)
    }

    private var billingClient: BillingClient? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _products = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val products: StateFlow<Map<String, ProductDetails>> = _products.asStateFlow()

    private val _purchaseMessage = MutableStateFlow<String?>(null)
    val purchaseMessage: StateFlow<String?> = _purchaseMessage.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()

        startConnection()
    }

    private fun startConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client successfully connected")
                    _isConnected.value = true
                    queryProducts()
                    restorePurchases()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                    _isConnected.value = false
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected, will retry on next operation")
                _isConnected.value = false
            }
        })
    }

    fun queryProducts() {
        val client = billingClient ?: return
        if (!_isConnected.value) {
            startConnection()
            return
        }

        // Query In-App One-Time Products
        val inAppProductList = ALL_IN_APP_PRODUCTS.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val inAppParams = QueryProductDetailsParams.newBuilder()
            .setProductList(inAppProductList)
            .build()

        client.queryProductDetailsAsync(inAppParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val currentMap = _products.value.toMutableMap()
                productDetailsList.forEach { details ->
                    currentMap[details.productId] = details
                }
                _products.value = currentMap
            }
        }

        // Query Subscriptions
        val subsProductList = ALL_SUBSCRIPTIONS.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val subsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(subsProductList)
            .build()

        client.queryProductDetailsAsync(subsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val currentMap = _products.value.toMutableMap()
                productDetailsList.forEach { details ->
                    currentMap[details.productId] = details
                }
                _products.value = currentMap
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails, offerToken: String? = null) {
        val client = billingClient ?: return
        if (!_isConnected.value) {
            _purchaseMessage.value = "Billing service is connecting. Please try again."
            startConnection()
            return
        }

        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        if (productDetails.productType == BillingClient.ProductType.SUBS && offerToken != null) {
            productDetailsParamsBuilder.setOfferToken(offerToken)
        } else if (productDetails.productType == BillingClient.ProductType.SUBS) {
            val defaultOfferToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            if (defaultOfferToken != null) {
                productDetailsParamsBuilder.setOfferToken(defaultOfferToken)
            }
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
            .build()

        val response = client.launchBillingFlow(activity, flowParams)
        if (response.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Failed to launch billing flow: ${response.debugMessage}")
            _purchaseMessage.value = "Could not start purchase: ${response.debugMessage}"
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (!purchases.isNullOrEmpty()) {
                    coroutineScope.launch {
                        for (purchase in purchases) {
                            handlePurchase(purchase)
                        }
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseMessage.value = "Purchase cancelled"
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _purchaseMessage.value = "You already own this item!"
                coroutineScope.launch {
                    preferencesRepository.setPremium(true)
                }
            }
            else -> {
                Log.e(TAG, "Purchase update failed: ${billingResult.debugMessage}")
                _purchaseMessage.value = "Purchase error: ${billingResult.debugMessage}"
            }
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Acknowledge the purchase if not already acknowledged
            if (!purchase.isAcknowledged) {
                val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient?.acknowledgePurchase(acknowledgeParams) { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase acknowledged successfully")
                    } else {
                        Log.e(TAG, "Failed to acknowledge purchase: ${result.debugMessage}")
                    }
                }
            }

            // Unlock premium
            preferencesRepository.setPremium(true)
            _purchaseMessage.value = "Premium unlocked successfully! Enjoy ad-free experience."
        }
    }

    fun restorePurchases(onComplete: ((Boolean) -> Unit)? = null) {
        val client = billingClient ?: return
        if (!_isConnected.value) {
            startConnection()
            onComplete?.invoke(false)
            return
        }

        _isProcessing.value = true

        // Check In-App Purchases
        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        client.queryPurchasesAsync(inAppParams) { result, inAppPurchases ->
            var hasActivePurchase = false
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in inAppPurchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        hasActivePurchase = true
                        coroutineScope.launch { handlePurchase(purchase) }
                    }
                }
            }

            // Check Subscriptions
            val subsParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            client.queryPurchasesAsync(subsParams) { subsResult, subsPurchases ->
                _isProcessing.value = false
                if (subsResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    for (purchase in subsPurchases) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            hasActivePurchase = true
                            coroutineScope.launch { handlePurchase(purchase) }
                        }
                    }
                }

                if (hasActivePurchase) {
                    coroutineScope.launch { preferencesRepository.setPremium(true) }
                    _purchaseMessage.value = "Purchases restored successfully!"
                    onComplete?.invoke(true)
                } else {
                    _purchaseMessage.value = "No active purchase found"
                    onComplete?.invoke(false)
                }
            }
        }
    }

    fun clearMessage() {
        _purchaseMessage.value = null
    }

    fun destroy() {
        billingClient?.endConnection()
        billingClient = null
    }
}
