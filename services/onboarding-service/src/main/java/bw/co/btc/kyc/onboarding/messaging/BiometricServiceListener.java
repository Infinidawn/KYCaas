package bw.co.btc.kyc.onboarding.messaging;

import bw.co.btc.kyc.onboarding.enumeration.KycSessionState;
import bw.co.btc.kyc.onboarding.service.KycOrchestratorService;
import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener; import org.springframework.stereotype.Component;

@Slf4j @Component @RequiredArgsConstructor
public class BiometricServiceListener {
    private final KycOrchestratorService orchestrator;

    public static class BiometricCompletedEvent {
        public java.util.UUID sessionId;
        public java.util.UUID tenantId;
        public String status;
        public Double livenessScore;
        public Double faceMatchScore;
        public String message;
    }

    @RabbitListener(queues = "${app.queues.onboarding.biometricCompleted:q.onboarding.biometric.completed}")
    public void onBiometricCompleted(BiometricCompletedEvent evt) {
        log.info("[Onboarding<-Biometric] biometric.completed sessionId={} status={}", evt.sessionId, evt.status);
        orchestrator.markState(evt.sessionId, KycSessionState.BIOMETRIC_COMPLETED);
        orchestrator.stageLog(evt.sessionId, "BIOMETRIC_COMPLETED",
                "status=" + evt.status + ", live=" + evt.livenessScore + ", match=" + evt.faceMatchScore);
    }
}
