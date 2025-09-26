package bw.co.btc.kyc.document.api;
import bw.co.btc.kyc.document.dto.DocumentIntakeRequest;
import bw.co.btc.kyc.document.service.DocumentIntakeService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.Map; import java.util.UUID;

@RestController
@RequestMapping(value="/public/v1/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
public class DocumentController {

  private final DocumentIntakeService service;
  public DocumentController(DocumentIntakeService service) { this.service = service; }

  @PostMapping("/{id}/documents")
  public Map<String,Object> intake(@PathVariable("id") UUID sessionId,
                                   @RequestHeader(value="X-Tenant-Id", required=false) String tenantId,
                                   @Valid @RequestBody DocumentIntakeRequest body) {
    service.handle(sessionId, (tenantId==null?"demo-tenant":tenantId), body);
    return Map.of("accepted", true, "correlationId", UUID.randomUUID().toString());
  }
}
