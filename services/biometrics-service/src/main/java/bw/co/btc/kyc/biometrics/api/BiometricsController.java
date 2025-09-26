package bw.co.btc.kyc.biometrics.api;
import bw.co.btc.kyc.biometrics.domain.BiometricResult;
import bw.co.btc.kyc.biometrics.repo.BiometricRepo;
import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
import java.util.Map; import java.util.UUID; import java.time.Instant;
@RestController @RequestMapping("/public/v1")
public class BiometricsController {
  private final BiometricRepo repo; public BiometricsController(BiometricRepo repo){ this.repo=repo; }
  static record SelfieReq(String selfieImageUrl,String livenessVideoUrl){}
  @PostMapping("/sessions/{id}/selfie")
  public ResponseEntity<?> intake(@PathVariable("id") UUID sessionId, @RequestBody SelfieReq r){
    BiometricResult br = BiometricResult.builder()
            .id(UUID.randomUUID())
            .tenantId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
            .sessionId(sessionId)
            .selfieImageUrl(r.selfieImageUrl())
            .createdAt(Instant.now())
            .build();
    repo.save(br);
    return ResponseEntity.accepted().body(Map.of("accepted", true, "correlationId", br.getId().toString()));
  }
}
