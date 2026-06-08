package com.controlparental.app.domain.usecase

import android.content.Context
import android.content.Intent
import com.controlparental.app.domain.repository.AppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CheckAndLockUseCase @Inject constructor(
    private val repository: AppRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(): Boolean {
        val foregroundApp = repository.getForegroundApp() ?: return false
        if (!repository.shouldLockApp(foregroundApp)) return false

        val intent = Intent(context, Class.forName("com.controlparental.app.ui.lock.LockOverlayActivity")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("package_name", foregroundApp)
        }
        context.startActivity(intent)
        return true
    }
}
