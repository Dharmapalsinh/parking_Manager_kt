package com.dharmapal.parking_manager_kt.models


import com.google.gson.annotations.SerializedName

data class Item(
    @SerializedName("close_by")
    val closeBy: Any?,
    @SerializedName("close_reason")
    val closeReason: Any?,
    @SerializedName("closed_at")
    val closedAt: Any?,
    @SerializedName("created_at")
    val createdAt: Int?,
    @SerializedName("customer_id")
    val customerId: Any?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("entity")
    val entity: String?,
    @SerializedName("fixed_amount")
    val fixedAmount: Boolean?,
    @SerializedName("id")
    val id: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("notes")
    val notes: List<Any>?,
    @SerializedName("payment_amount")
    val paymentAmount: Int?,
    @SerializedName("payments_amount_received")
    val paymentsAmountReceived: Int?,
    @SerializedName("payments_count_received")
    val paymentsCountReceived: Int?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("tax_invoice")
    val taxInvoice: List<Any>?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("usage")
    val usage: String?
)