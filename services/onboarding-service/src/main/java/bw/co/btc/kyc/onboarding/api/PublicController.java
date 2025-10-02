package bw.co.btc.kyc.onboarding.api;

import bw.co.btc.kyc.onboarding.dto.*;
import bw.co.btc.kyc.onboarding.entity.KycSession;
import bw.co.btc.kyc.onboarding.entity.KycStageLog;
import bw.co.btc.kyc.onboarding.enumeration.KycSessionState;
import bw.co.btc.kyc.onboarding.repo.KycDecisionRepository;
import bw.co.btc.kyc.onboarding.repo.KycSessionRepo;
import bw.co.btc.kyc.onboarding.repo.KycStageLogRepository;
import bw.co.btc.kyc.onboarding.security.TenantContext;
import bw.co.btc.kyc.onboarding.storage.PresignService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@RequestMapping(path = "/public/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class PublicController {

  private final KycSessionRepo sessions;
  private final KycDecisionRepository decisions;
  private final PresignService presign;
  private final KycStageLogRepository stageLogs;

  public PublicController(KycSessionRepo sessions,
                          KycDecisionRepository decisions,
                          PresignService presign,
                          KycStageLogRepository stageLogs
  ) {
    this.sessions = sessions;
    this.decisions = decisions;
    this.presign = presign;
    this.stageLogs = stageLogs;
  }

  // -------------------- 1) Create Session --------------------
  @PostMapping("/sessions")
  public ResponseEntity<StartSessionResponse> start(@Valid @RequestBody StartSessionRequest r) {
    UUID id = UUID.randomUUID();

    UUID tenantId = TenantContext.getTenantId();
    if (tenantId == null) {
      throw new IllegalStateException("No tenant resolved from API key");
    }

    var s = KycSession.builder()
            .id(id)
            .tenantId(tenantId)
            .channel(r.channel().name())
            .phone(r.phone())
            .state(KycSessionState.PENDING.name())
            .createdAt(Instant.now())
            .build();
    sessions.save(s);

    stageLogs.save(new KycStageLog(
            UUID.randomUUID(),
            s.getId(),
            "SESSION_STARTED",
            "channel=" + s.getChannel(),
            Instant.now()
    ));

    return ResponseEntity
            .created(URI.create("/public/v1/sessions/" + id))
            .body(new StartSessionResponse(id, s.getCreatedAt()));
  }

  // -------------------- 2) Issue Upload URLs --------------------
  @PostMapping("/sessions/{sessionId}/upload-urls")
  public UploadUrlsResponse createUploadUrls(@PathVariable UUID sessionId) throws Exception {

    UUID tenantId = TenantContext.getTenantId();
    if (tenantId == null) {
      throw new IllegalStateException("No tenant resolved from API key");
    }

    // Use tenant UUID as folder prefix
    final String prefix = "%s/sessions/%s/".formatted(tenantId, sessionId);

    Map<String, UploadUrlsResponse.UploadItem> items = new HashMap<>();
    items.put("documentFront", presign.mkUploadDescriptor(prefix + "documents/front.jpg"));
    items.put("documentBack",  presign.mkUploadDescriptor(prefix + "documents/back.jpg"));
    items.put("selfie",        presign.mkUploadDescriptor(prefix + "selfie/selfie.jpg"));
    items.put("livenessVideo", presign.mkUploadDescriptor(prefix + "liveness/video.mp4"));

    sessions.findById(sessionId).ifPresent(s -> {
      s.setState(KycSessionState.MEDIA_ISSUED.name());
      sessions.save(s);

      stageLogs.save(new KycStageLog(
              UUID.randomUUID(), s.getId(), "MEDIA_ISSUED",
              "issued=" + String.join(",", items.keySet()),
              Instant.now()
      ));
    });

    return new UploadUrlsResponse(sessionId, Instant.now(), items);
  }

  // -------------------- 3) Read Decision/Status --------------------
  @GetMapping("/sessions/{sessionId}/decision")
  public ResponseEntity<DecisionResponse> getDecision(@PathVariable UUID sessionId) {
    return decisions.findBySessionId(sessionId)
            .map(d -> ResponseEntity.ok(
                    new DecisionResponse(
                            d.getStatus().name(),
                            (d.getReasons() == null || d.getReasons().isBlank())
                                    ? Collections.emptyList()
                                    : Arrays.stream(d.getReasons().split(","))
                                    .map(String::trim).collect(Collectors.toList()),
                            d.getCreatedAt()
                    )
            ))
            .orElseGet(() -> ResponseEntity.ok(
                    new DecisionResponse("PENDING", Collections.emptyList(), null)
            ));
  }


  // ---------- NEW: session stage timeline ----------
  @GetMapping("/sessions/{sessionId}/stages")
  public Map<String, Object> getStages(@PathVariable UUID sessionId,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "50") int size) {
    // simple pagination; if you prefer non-paged, load all and return list
    var pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 200)));
    var all = stageLogs.findBySessionIdOrderByCreatedAtAsc(sessionId); // simple: no paging
    // If you want true paging, add a Page method in repository.

    var items = all.stream()
            .map(l -> new StageLogDto(l.getId(), l.getSessionId(), l.getEvent(), l.getDetails(), l.getCreatedAt()))
            .toList();

    return Map.of(
            "sessionId", sessionId,
            "count", items.size(),
            "items", items
    );
  }
}
