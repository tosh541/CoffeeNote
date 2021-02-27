package com.example.coffeenote

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class CoffeeNote : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var date: Date = Date()
    var title: String = ""
    var detail: String = ""
    var rich: Float = 0F
    var bitter: Float = 0F
    var sour: Float = 0F
    var total: Float = 0F
}