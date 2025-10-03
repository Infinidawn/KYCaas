package bw.co.btc.kyc.document.api;
import bw.co.btc.kyc.document.dto.DocumentIntakeRequest;
import bw.co.btc.kyc.document.dto.DocumentIntakeResponse;
import bw.co.btc.kyc.document.service.DocumentIntakeService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController
@RequestMapping(value="/public/v1/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
public class DocumentController {

  private final DocumentIntakeService service;

  public DocumentController(DocumentIntakeService service) { this.service = service; }

  /**
   * Submit document metadata and object keys for OCR.
   * X-Tenant-Id header optional in dev; in prod should be injected by gateway.
   */
  @PostMapping("/{sessionId}/documents")
  public DocumentIntakeResponse processDocuments(@PathVariable UUID sessionId,
                                                 @RequestHeader("X-Tenant-Id") UUID tenantId,
                                                 @Valid @RequestBody DocumentIntakeRequest body,
                                                 @RequestHeader(value = "X-Correlation-Id", required = false) String corrId) {
    UUID corr = service.processDocuments(sessionId, tenantId, body, corrId);
    return new DocumentIntakeResponse(true, corr);
  }
}
