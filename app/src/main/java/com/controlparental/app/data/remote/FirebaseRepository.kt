package com.controlparental.app.data.remote

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.controlparental.app.di.DevicesCollection
import com.controlparental.app.di.DeviceId
import com.controlparental.app.di.RequestsCollection
import com.controlparental.app.domain.model.AppMode
import com.controlparental.app.domain.model.ChildDevice
import com.controlparental.app.domain.model.RequestStatus
import com.controlparental.app.domain.model.TimeLimits
import com.controlparental.app.domain.model.TimeRequest
import com.controlparental.app.util.Constants
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor(
    @DevicesCollection private val devicesCollection: CollectionReference,
    @RequestsCollection private val requestsCollection: CollectionReference,
    @DeviceId private val deviceId: String
) {
    private val tag = Constants.TAG

    // ─── Device Registration ───

    suspend fun registerDevice(
        mode: AppMode,
        phoneNumber: String,
        fcmToken: String
    ) {
        val data = hashMapOf(
            Constants.FS_FIELD_DEVICE_ID to deviceId,
            Constants.FS_FIELD_MODE to mode.name,
            Constants.FS_FIELD_PHONE to phoneNumber,
            Constants.FS_FIELD_FCM_TOKEN to fcmToken,
            "lastSeen" to FieldValue.serverTimestamp(),
            "createdAt" to FieldValue.serverTimestamp()
        )
        devicesCollection.document(deviceId).set(data, com.google.firebase.firestore.SetOptions.merge()).await()
        Log.d(tag, "Device registered: $deviceId")
    }

    suspend fun updateFcmToken(token: String) {
        devicesCollection.document(deviceId).update(Constants.FS_FIELD_FCM_TOKEN, token).await()
    }

    suspend fun updateDeviceInfo(
        restrictedPackages: Set<String>? = null,
        timeLimits: TimeLimits? = null
    ) {
        val updates = mutableMapOf<String, Any>()
        restrictedPackages?.let { updates[Constants.FS_FIELD_RESTRICTED] = it.toList() }
        timeLimits?.let {
            updates[Constants.FS_FIELD_DAILY_LIMIT] = it.dailyMinutes
            updates[Constants.FS_FIELD_WEEKLY_LIMIT] = it.weeklyMinutes
        }
        updates["lastSeen"] = FieldValue.serverTimestamp()
        if (updates.isNotEmpty()) {
            devicesCollection.document(deviceId).update(updates).await()
        }
    }

    // ─── Child-Parent Linking ───

    suspend fun linkChildToParent(childDeviceId: String) {
        // En el documento del padre, agregar el childId
        devicesCollection.document(deviceId).update(
            Constants.FS_CHILD_DEVICES,
            FieldValue.arrayUnion(childDeviceId)
        ).await()
        // En el documento del hijo, establecer el parentId
        devicesCollection.document(childDeviceId).update(
            Constants.FS_FIELD_PARENT_ID, deviceId
        ).await()
    }

    suspend fun unlinkChild(childDeviceId: String) {
        devicesCollection.document(deviceId).update(
            Constants.FS_CHILD_DEVICES,
            FieldValue.arrayRemove(childDeviceId)
        ).await()
        devicesCollection.document(childDeviceId).update(
            Constants.FS_FIELD_PARENT_ID, FieldValue.delete()
        ).await()
    }

    suspend fun getChildDeviceIds(): List<String> {
        return try {
            val doc = devicesCollection.document(deviceId).get(Source.SERVER).await()
            (doc.get(Constants.FS_CHILD_DEVICES) as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getChildDevicesInfo(): List<ChildDevice> {
        val childIds = getChildDeviceIds()
        if (childIds.isEmpty()) return emptyList()
        val children = mutableListOf<ChildDevice>()
        for (id in childIds) {
            try {
                val doc = devicesCollection.document(id).get(Source.SERVER).await()
                if (doc.exists()) {
                    children.add(docToChildDevice(doc))
                }
            } catch (_: Exception) {}
        }
        return children
    }

    suspend fun getParentDeviceId(): String? {
        return try {
            val doc = devicesCollection.document(deviceId).get(Source.SERVER).await()
            doc.getString(Constants.FS_FIELD_PARENT_ID)
        } catch (_: Exception) { null }
    }

    // ─── Time Requests ───

    suspend fun createRequest(minutes: Int, packageName: String, parentDeviceId: String): String {
        val docRef = requestsCollection.document()
        val request = hashMapOf(
            Constants.FS_FIELD_CHILD_ID to deviceId,
            Constants.FS_FIELD_PARENT_ID to parentDeviceId,
            Constants.FS_FIELD_REQUESTED_MINUTES to minutes,
            Constants.FS_FIELD_STATUS to RequestStatus.PENDING.name,
            Constants.FS_FIELD_PACKAGE_NAME to packageName,
            Constants.FS_FIELD_TIMESTAMP to FieldValue.serverTimestamp(),
            Constants.FS_FIELD_CREATED_AT to System.currentTimeMillis()
        )
        docRef.set(request).await()
        return docRef.id
    }

    suspend fun updateRequestStatus(requestId: String, status: RequestStatus) {
        requestsCollection.document(requestId).update(
            mapOf(
                Constants.FS_FIELD_STATUS to status.name,
                "respondedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    fun observeRequestsForParent(): kotlinx.coroutines.flow.Flow<List<TimeRequest>> {
        return kotlinx.coroutines.flow.callbackFlow {
            val registration = requestsCollection
                .whereEqualTo(Constants.FS_FIELD_PARENT_ID, deviceId)
                .orderBy(Constants.FS_FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e(tag, "Firestore listener error", error)
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        val requests = snapshots.documents.mapNotNull { docToTimeRequest(it) }
                        trySend(requests)
                    }
                }
            awaitClose { registration.remove() }
        }
    }

    fun observeRequestsForChild(): kotlinx.coroutines.flow.Flow<List<TimeRequest>> {
        return kotlinx.coroutines.flow.callbackFlow {
            val registration = requestsCollection
                .whereEqualTo(Constants.FS_FIELD_CHILD_ID, deviceId)
                .orderBy(Constants.FS_FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e(tag, "Firestore listener error", error)
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        val requests = snapshots.documents.mapNotNull { docToTimeRequest(it) }
                        trySend(requests)
                    }
                }
            awaitClose { registration.remove() }
        }
    }

    // ─── Helpers ───

    private fun docToTimeRequest(doc: DocumentSnapshot): TimeRequest? {
        if (!doc.exists()) return null
        return try {
            TimeRequest(
                id = doc.id,
                childDeviceId = doc.getString(Constants.FS_FIELD_CHILD_ID) ?: "",
                parentDeviceId = doc.getString(Constants.FS_FIELD_PARENT_ID) ?: "",
                requestedMinutes = doc.getLong(Constants.FS_FIELD_REQUESTED_MINUTES)?.toInt() ?: 0,
                status = try {
                    RequestStatus.valueOf(doc.getString(Constants.FS_FIELD_STATUS) ?: "PENDING")
                } catch (_: Exception) { RequestStatus.PENDING },
                timestamp = doc.getLong(Constants.FS_FIELD_CREATED_AT) ?: 0L,
                packageName = doc.getString(Constants.FS_FIELD_PACKAGE_NAME) ?: "",
                reason = doc.getString(Constants.FS_FIELD_REASON) ?: "",
                respondedAt = doc.getLong("respondedAt") ?: 0L
            )
        } catch (e: Exception) {
            Log.e(tag, "Error parsing request doc", e)
            null
        }
    }

    private fun docToChildDevice(doc: DocumentSnapshot): ChildDevice {
        return ChildDevice(
            deviceId = doc.id,
            alias = doc.getString("alias") ?: "",
            phoneNumber = doc.getString(Constants.FS_FIELD_PHONE) ?: "",
            fcmToken = doc.getString(Constants.FS_FIELD_FCM_TOKEN) ?: "",
            lastSeen = doc.getLong("lastSeen") ?: 0L,
            linkedAt = doc.getLong("createdAt") ?: 0L
        )
    }
}
