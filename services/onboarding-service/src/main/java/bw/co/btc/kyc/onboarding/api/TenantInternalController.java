package bw.co.btc.kyc.onboarding.api;

import bw.co.btc.kyc.onboarding.entity.Tenant;
import bw.co.btc.kyc.onboarding.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal/tenants")
@RequiredArgsConstructor
public class TenantInternalController {

    private final TenantService tenantService;


}
