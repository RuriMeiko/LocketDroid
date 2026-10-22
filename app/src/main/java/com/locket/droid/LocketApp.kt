package com.locket.droid

import android.app.Application
import com.locket.droid.data.ChatRepository
import com.locket.droid.data.LocketRepository

class LocketApp : Application() {
    lateinit var repository: LocketRepository
        private set
    lateinit var chatRepository: ChatRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = LocketRepository(this)
        chatRepository = ChatRepository()
    }
}
