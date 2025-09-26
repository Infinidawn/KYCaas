package bw.co.btc.kyc.verification.api;
import bw.co.btc.kyc.verification.domain.Verification;
import bw.co.btc.kyc.verification.repo.VerificationRepo;
import jakarta.validation.constraints.*; import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated; import org.springframework.web.bind.annotation.*;
import java.net.URI; import java.time.LocalDate; import java.util.Map; import java.util.UUID;
@RestController @RequestMapping("/api/v1") @Validated
public class VerificationController {
  private final VerificationRepo repo; public VerificationController(VerificationRepo repo){ this.repo = repo; }
  static class BasicVerificationRequest {
    @NotBlank public String channel; @NotBlank public String idType; @NotBlank public String idNumber;
    @NotBlank public String firstName; @NotBlank public String lastName; @NotNull public LocalDate dob;
    public String docFrontUrl; public String selfieUrl;
  }
  @PostMapping("/verify/basic")
  public ResponseEntity<?> submit(@RequestBody @Validated BasicVerificationRequest r){
    String status="REVIEW"; String reasons="";
    boolean idOk=r.idNumber!=null && r.idNumber.trim().length()>=5;
    boolean selfieOk=r.selfieUrl!=null && !r.selfieUrl.isBlank();
    boolean docOk=r.docFrontUrl!=null && !r.docFrontUrl.isBlank();
    if(idOk && (selfieOk||docOk)) status="AUTO_APPROVED";
    else if(!idOk){ status="AUTO_REJECTED"; reasons="ID_NUMBER_INVALID"; }
    else { reasons="INSUFFICIENT_EVIDENCE"; }
    Verification v=repo.save(Verification.builder().channel(r.channel).idType(r.idType).idNumber(r.idNumber)
      .firstName(r.firstName).lastName(r.lastName).dob(r.dob).docFrontUrl(r.docFrontUrl).selfieUrl(r.selfieUrl)
      .status(status).reasons(reasons).build());
    return ResponseEntity.created(URI.create("/api/v1/verifications/"+v.getId())).body(Map.of(
      "id", v.getId().toString(), "status", v.getStatus(),
      "reasons", (v.getReasons()==null||v.getReasons().isBlank())?new String[]{}:v.getReasons().split(","),
      "createdAt", v.getCreatedAt(),
      "person", Map.of("idType",v.getIdType(),"idNumber",v.getIdNumber(),"firstName",v.getFirstName(),"lastName",v.getLastName(),"dob",v.getDob()),
      "media", Map.of("docFrontUrl",v.getDocFrontUrl(),"selfieUrl",v.getSelfieUrl())
    ));
  }
  @GetMapping("/verifications/{id}")
  public ResponseEntity<?> get(@PathVariable("id") UUID id){
    return repo.findById(id).<ResponseEntity<?>>map(v->ResponseEntity.ok(Map.of(
      "id",v.getId().toString(),"status",v.getStatus(),
      "reasons",(v.getReasons()==null||v.getReasons().isBlank())?new String[]{}:v.getReasons().split(","),
      "createdAt",v.getCreatedAt(),
      "person", Map.of("idType",v.getIdType(),"idNumber",v.getIdNumber(),"firstName",v.getFirstName(),"lastName",v.getLastName(),"dob",v.getDob()),
      "media", Map.of("docFrontUrl",v.getDocFrontUrl(),"selfieUrl",v.getSelfieUrl())
    ))).orElseGet(()->ResponseEntity.notFound().build());
  }
}
