package org.wordpress.android.ui.posts

import com.crashlytics.android.Crashlytics
import org.wordpress.android.BuildConfig
import org.wordpress.android.fluxc.model.LocalOrRemoteId.LocalId
import org.wordpress.android.util.AppLog
import org.wordpress.android.util.AppLog.T

class CriticalPostActionTracker(
    // TODO: Find a better name than listener
    private val listener: () -> Unit,
    private val shouldCrashOnUnexpectedAction: Boolean = BuildConfig.DEBUG
) {
    enum class CriticalPostAction {
        DELETING_POST, RESTORING_POST, TRASHING_POST, MOVING_POST_TO_DRAFT
    }
    private val map = HashMap<LocalId, CriticalPostAction>()

    fun add(localPostId: LocalId, criticalPostAction: CriticalPostAction) {
        if (map.containsKey(localPostId)) {
            val currentActionName = map[localPostId]?.name
            val newActionName = criticalPostAction.name
            val errorMessage = "We should not perform more than one critical post action. Current action is " +
                    "($currentActionName), new action is ($newActionName)"
            AppLog.e(T.POSTS, errorMessage)
            if (shouldCrashOnUnexpectedAction) {
                throw IllegalStateException(errorMessage)
            } else {
                Crashlytics.log(errorMessage)
            }
        }
        map[localPostId] = criticalPostAction
        listener.invoke()
    }

    fun contains(localPostId: LocalId): Boolean = map.containsKey(localPostId)

    fun get(localPostId: LocalId): CriticalPostAction? = map[localPostId]

    fun remove(localPostId: LocalId, criticalPostAction: CriticalPostAction) {
        if (map[localPostId] == criticalPostAction) {
            map.remove(localPostId)
            listener.invoke()
        }
    }
}
