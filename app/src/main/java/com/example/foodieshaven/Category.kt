package com.example.foodieshaven

import android.os.Parcel
import android.os.Parcelable

data class Category(
    val id: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val isFeatured: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(imageUrl)
        parcel.writeByte(if (isActive) 1 else 0)
        parcel.writeByte(if (isFeatured) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Category> {
        override fun createFromParcel(parcel: Parcel): Category = Category(parcel)

        override fun newArray(size: Int): Array<Category?> = arrayOfNulls(size)
    }
}
