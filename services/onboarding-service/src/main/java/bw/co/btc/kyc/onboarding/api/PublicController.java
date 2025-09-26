package bw.co.btc.kyc.onboarding.api;
import bw.co.btc.kyc.onboarding.domain.KycSession;
import bw.co.btc.kyc.onboarding.repo.KycSessionRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
@RestController @RequestMapping("/public/v1")
public class PublicController {
  private final KycSessionRepo repo;
  public PublicController(KycSessionRepo repo){ this.repo = repo; }
  static record StartSessionRequest(String channel, String phone){}


  @PostMapping("/sessions")
  public ResponseEntity<?> start(@RequestBody StartSessionRequest r){
    UUID id = UUID.randomUUID();
    KycSession s = KycSession.builder()
      .id(id).tenant_id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
      .channel(r.channel()).phone(r.phone())
      .state("PENDING").created_at(Instant.now()).build();
    repo.save(s);
    return ResponseEntity.created(URI.create("/public/v1/sessions/"+id)).body(Map.of("sessionId", id.toString()));
  }


  @GetMapping("/sessions/{id}/decision")
  public ResponseEntity<?> decision(@PathVariable("id") UUID id){
    return repo.findById(id).map(s -> ResponseEntity.ok(Map.of(
      "sessionId", s.getId().toString(),
      "status", s.getState(),
      "reasons", new String[]{})))
    .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
