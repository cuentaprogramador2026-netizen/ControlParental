import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";

admin.initializeApp();

const db = admin.firestore();

// ──────────────────────────────────────────────
// 1. Al crear una solicitud de tiempo (child → parent)
//    Envía FCM al padre para notificarle
// ──────────────────────────────────────────────
export const onTimeRequestCreated = functions.firestore
  .document("requests/{requestId}")
  .onCreate(async (snap, context) => {
    const request = snap.data();
    const parentDeviceId: string = request.parentDeviceId;
    const childDeviceId: string = request.childDeviceId;
    const requestedMinutes: number = request.requestedMinutes;
    const packageName: string = request.packageName || "";

    if (!parentDeviceId) {
      functions.logger.warn("No parentDeviceId in request", request);
      return;
    }

    try {
      // Obtener token FCM del padre
      const parentDoc = await db.collection("devices").doc(parentDeviceId).get();
      if (!parentDoc.exists) {
        functions.logger.warn(`Parent device ${parentDeviceId} not found`);
        return;
      }

      const parentFcmToken: string | undefined = parentDoc.data()?.fcmToken;
      if (!parentFcmToken) {
        functions.logger.warn(`No FCM token for parent ${parentDeviceId}`);
        return;
      }

      // Obtener alias del hijo para mostrar en la notificación
      const childDoc = await db.collection("devices").doc(childDeviceId).get();
      const childAlias: string = childDoc.data()?.alias || "Tu hijo";

      // Enviar notificación push al padre
      const message: admin.messaging.Message = {
        token: parentFcmToken,
        data: {
          type: "new_request",
          requestId: context.params.requestId,
          childDeviceId,
          childName: childAlias,
          minutes: String(requestedMinutes),
          packageName,
        },
        notification: {
          title: "Nueva solicitud de tiempo",
          body: `${childAlias} solicita ${requestedMinutes} minutos${
            packageName ? ` para ${packageName}` : ""
          }`,
        },
        android: {
          priority: "high",
          notification: {
            channelId: "parental_alerts",
            priority: "high",
            defaultSound: true,
            defaultVibrateTimings: true,
          },
        },
      };

      await admin.messaging().send(message);
      functions.logger.info("FCM sent to parent", { parentDeviceId, requestId: context.params.requestId });
    } catch (error) {
      functions.logger.error("Error sending FCM to parent", error);
    }
  });

// ──────────────────────────────────────────────
// 2. Al aprobar/rechazar una solicitud
//    Notifica al hijo del resultado
// ──────────────────────────────────────────────
export const onTimeRequestUpdated = functions.firestore
  .document("requests/{requestId}")
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();

    // Solo reaccionar si el status cambió
    if (before.status === after.status) return;

    const childDeviceId: string = after.childDeviceId;
    const status: string = after.status; // "APPROVED" | "REJECTED"
    const minutes: number = after.requestedMinutes;

    try {
      const childDoc = await db.collection("devices").doc(childDeviceId).get();
      if (!childDoc.exists) {
        functions.logger.warn(`Child device ${childDeviceId} not found`);
        return;
      }

      const childFcmToken: string | undefined = childDoc.data()?.fcmToken;
      if (!childFcmToken) {
        functions.logger.warn(`No FCM token for child ${childDeviceId}`);
        return;
      }

      if (status === "APPROVED") {
        const message: admin.messaging.Message = {
          token: childFcmToken,
          data: {
            type: "request_approved",
            requestId: context.params.requestId,
            minutes: String(minutes),
          },
          notification: {
            title: "Solicitud aprobada",
            body: `Te han concedido ${minutes} minutos adicionales`,
          },
          android: {
            priority: "high",
            notification: {
              channelId: "parental_alerts",
              priority: "high",
              defaultSound: true,
            },
          },
        };
        await admin.messaging().send(message);
      } else if (status === "REJECTED") {
        const message: admin.messaging.Message = {
          token: childFcmToken,
          data: {
            type: "request_rejected",
            requestId: context.params.requestId,
          },
          notification: {
            title: "Solicitud rechazada",
            body: "Tu solicitud de tiempo extra ha sido rechazada",
          },
          android: {
            priority: "high",
            notification: {
              channelId: "parental_alerts",
              priority: "high",
            },
          },
        };
        await admin.messaging().send(message);
      }

      functions.logger.info(`Request ${status} notification sent to child ${childDeviceId}`);
    } catch (error) {
      functions.logger.error("Error sending request update to child", error);
    }
  });

// ──────────────────────────────────────────────
// 3. Health check endpoint (opcional)
// ──────────────────────────────────────────────
export const healthCheck = functions.https.onRequest((req, res) => {
  res.status(200).json({ status: "ok", timestamp: Date.now() });
});
