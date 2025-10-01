package bw.co.btc.kyc.biometrics.api;

import bw.co.btc.kyc.biometrics.service.BiometricIntakeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import bw.co.btc.kyc.biometrics.dto.BiometricIntakeRequest;
import bw.co.btc.kyc.biometrics.dto.BiometricIntakeResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/public/v1/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
public class BiometricsController {

  private final BiometricIntakeService service;

  public BiometricsController(BiometricIntakeService service) {
    this.service = service;
  }

  @PostMapping("/{sessionId}/biometrics")
  public BiometricIntakeResponse submitBiometric(
          @PathVariable UUID sessionId,
          @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
          @Valid @RequestBody BiometricIntakeRequest req) {

    UUID corr = service.process(sessionId,
            tenantId == null || tenantId.isBlank() ? "demo-tenant" : tenantId,
            req);

    return new BiometricIntakeResponse(true, corr);
  }
}
